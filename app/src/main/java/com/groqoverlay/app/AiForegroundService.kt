package com.groqoverlay.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.util.TypedValue
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.groqoverlay.app.data.AppDatabase
import com.groqoverlay.app.data.GroqClient
import com.groqoverlay.app.data.Message
import com.groqoverlay.app.data.MessageDao
import com.groqoverlay.app.data.PrefsKeys
import com.groqoverlay.app.data.SecurePreferences
import com.groqoverlay.app.ui.MessageAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import okhttp3.Call
import java.io.IOException
import java.util.concurrent.TimeUnit

class AiForegroundService : Service() {
    companion object {
        const val ACTION_OPEN_OVERLAY = "com.groqoverlay.app.OPEN_OVERLAY"
        private const val CHANNEL_ID = "ai_overlay_service"
        private const val NOTIFICATION_ID = 1001
        private const val PREFS_OVERLAY = "overlay_state"
        private const val KEY_W = "width"; private const val KEY_H = "height"
        private const val KEY_X = "x"; private const val KEY_Y = "y"
    }

    private lateinit var windowManager: WindowManager
    private lateinit var inputMethodManager: InputMethodManager
    private lateinit var dao: MessageDao
    private val mainHandler = Handler(Looper.getMainLooper())
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var overlayView: View? = null
    private var layoutParams: WindowManager.LayoutParams? = null
    private var adapter: MessageAdapter? = null
    private var currentCall: Call? = null

    override fun onBind(intent: Intent?): IBinder? = null
    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        dao = AppDatabase.getDatabase(this).messageDao()
        createNotificationChannel()
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, buildNotification())
        if (intent?.action == ACTION_OPEN_OVERLAY) showOverlayIfAllowed()
        return START_STICKY
    }
    override fun onDestroy() {
        layoutParams?.let { saveOverlayState(it) }
        removeOverlay()
        scope.cancel()
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(CHANNEL_ID, "Groq AI overlay service", NotificationManager.IMPORTANCE_LOW)
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }
    private fun buildNotification(): Notification {
        val openOverlayIntent = Intent(this, AiForegroundService::class.java).apply { action = ACTION_OPEN_OVERLAY }
        val openPendingIntent = PendingIntent.getService(this, 2, openOverlayIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val appIntent = Intent(this, MainActivity::class.java)
        val appPendingIntent = PendingIntent.getActivity(this, 1, appIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        return NotificationCompat.Builder(this, CHANNEL_ID).setContentTitle("Groq AI активен").setContentText("Нажмите, чтобы открыть AI")
            .setSmallIcon(R.drawable.ic_notification).setOngoing(true).setOnlyAlertOnce(true).setSound(null)
            .setCategory(NotificationCompat.CATEGORY_SERVICE).setContentIntent(appPendingIntent)
            .addAction(R.drawable.ic_notification, "Открыть AI", openPendingIntent).build()
    }
    private fun showOverlayIfAllowed() {
        if (overlayView != null) return
        if (!Settings.canDrawOverlays(this)) { Toast.makeText(this, "Нужно разрешить отображение поверх окон", Toast.LENGTH_LONG).show(); return }
        mainHandler.post { createOverlay() }
    }
    private fun releaseFocus() {
        val view = overlayView ?: return; val params = layoutParams ?: return
        view.findViewById<EditText>(R.id.etMessage).clearFocus()
        val nf = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        if ((params.flags and nf) == 0) { params.flags = params.flags or nf; try { windowManager.updateViewLayout(view, params) } catch (_: Exception) {} }
    }
    private fun takeFocus() {
        val view = overlayView ?: return; val params = layoutParams ?: return
        val nf = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        if ((params.flags and nf) != 0) { params.flags = params.flags and nf.inv(); try { windowManager.updateViewLayout(view, params) } catch (_: Exception) {} }
    }
    private fun copyTextToClipboard(text: String) {
        if (text.isBlank()) return
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("Groq AI", text))
        Toast.makeText(this, "Скопировано", Toast.LENGTH_SHORT).show()
    }
    private fun copyAll() { copyTextToClipboard(adapter?.allText() ?: "") }
    private fun shareDialog() {
        val text = adapter?.allText() ?: return
        if (text.isBlank()) return
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Groq AI диалог")
            putExtra(Intent.EXTRA_TEXT, text)
}
        startActivity(Intent.createChooser(intent, "Поделиться диалогом").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
    private fun setStreamingUI(streaming: Boolean) {
        val view = overlayView ?: return
        view.findViewById<View>(R.id.btnSend).visibility = if (streaming) View.GONE else View.VISIBLE
        view.findViewById<View>(R.id.btnStop).visibility = if (streaming) View.VISIBLE else View.GONE
    }
    private fun scrollToBottom() {
        val rv = overlayView?.findViewById<RecyclerView>(R.id.rvMessages) ?: return
        rv.post { if ((adapter?.itemCount ?: 0) > 0) rv.scrollToPosition(adapter!!.itemCount - 1) }
    }

    @Suppress("ClickableViewAccessibility")
    private fun createOverlay() {
        if (overlayView != null) return
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val opacity = prefs.getInt(PrefsKeys.OVERLAY_OPACITY, 95)
        val fontSize = prefs.getInt(PrefsKeys.OVERLAY_FONT_SIZE, 14)
        val cornerRadius = prefs.getInt(PrefsKeys.OVERLAY_CORNER_RADIUS, 20)
        val blurBg = prefs.getBoolean(PrefsKeys.OVERLAY_BLUR_BG, false)
    val blurSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && windowManager.isCrossWindowBlurEnabled
    val blurActive = blurBg && blurSupported
        val view = LayoutInflater.from(this).inflate(R.layout.overlay_window, null)
        val bg = view.findViewById<View>(R.id.overlayRoot).background as? GradientDrawable
        if (bg != null) { val alphaPct = if (blurActive) opacity * 50 / 100 else opacity; bg.setColor((alphaPct * 255 / 100) shl 24); bg.cornerRadius = dpToPx(cornerRadius).toFloat() }
    // Стеклянный блик (работает всегда)
    if (blurBg) {
        val glass = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(0x33FFFFFF, 0x00FFFFFF)
        )
        glass.cornerRadius = dpToPx(cornerRadius).toFloat()
        val base = view.findViewById<View>(R.id.overlayRoot).background
        view.findViewById<View>(R.id.overlayRoot).background =
            android.graphics.drawable.LayerDrawable(arrayOf(base, glass))
    }
    if (blurBg) {
        val sheen = GradientDrawable()
        sheen.gradientType = GradientDrawable.RADIAL_GRADIENT
        sheen.gradientRadius = resources.displayMetrics.widthPixels * 0.7f
        sheen.colors = intArrayOf(0x26FFFFFF, 0x00FFFFFF)
        sheen.cornerRadius = dpToPx(cornerRadius).toFloat()
        view.background = android.graphics.drawable.LayerDrawable(arrayOf(view.background, sheen))
    }

        adapter = MessageAdapter(
            onCopy = { copyTextToClipboard(it) },
            onDelete = { msg ->
                scope.launch {
                    dao.deleteById(msg.id)
                    adapter?.removeMessage(msg)
}
}
        )
        adapter!!.fontSize = fontSize.toFloat()
        val rv = view.findViewById<RecyclerView>(R.id.rvMessages)
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter
        scope.launch {
            adapter?.submitList(dao.getAllMessages())
            scrollToBottom()
}

        val dm = resources.displayMetrics
        val op = getSharedPreferences(PREFS_OVERLAY, MODE_PRIVATE)
        val minW = (280 * dm.density).toInt(); val minH = (360 * dm.density).toInt()
        val maxW = dm.widthPixels; val maxH = dm.heightPixels
        val defW = (dm.widthPixels * 0.92f).toInt().coerceAtMost(1100)
        val defH = (dm.heightPixels * 0.75f).toInt().coerceAtMost(1600)
        val w = op.getInt(KEY_W, defW).coerceIn(minW, maxW); val h = op.getInt(KEY_H, defH).coerceIn(minH, maxH)
        val defX = (dm.widthPixels - w) / 2; val defY = (dm.heightPixels - h) / 2
        val x = op.getInt(KEY_X, defX).coerceIn(0, maxOf(0, dm.widthPixels - w))
        val y = op.getInt(KEY_Y, defY).coerceIn(0, maxOf(0, dm.heightPixels - h))
        val params = WindowManager.LayoutParams(w, h, WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED, PixelFormat.TRANSLUCENT)
        params.gravity = Gravity.TOP or Gravity.START; params.x = x; params.y = y
        params.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
    if (blurBg && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && windowManager.isCrossWindowBlurEnabled) params.setBlurBehindRadius(dpToPx(30))

        val title = view.findViewById<View>(R.id.title)
        val close = view.findViewById<View>(R.id.btnClose)
        val clear = view.findViewById<View>(R.id.btnClear)
        val copy = view.findViewById<View>(R.id.btnCopy)
        val share = view.findViewById<View>(R.id.btnShare)
        val stop = view.findViewById<View>(R.id.btnStop)
        val resizeHandle = view.findViewById<View>(R.id.resizeHandle)
        val etMessage = view.findViewById<EditText>(R.id.etMessage)
        val btnSend = view.findViewById<View>(R.id.btnSend)

        view.setOnTouchListener { _, event -> if (event.actionMasked == MotionEvent.ACTION_OUTSIDE) { releaseFocus(); true } else false }
        etMessage.setOnTouchListener { v, event ->
            if (event.actionMasked == MotionEvent.ACTION_DOWN) { takeFocus(); v.post { v.requestFocus(); inputMethodManager.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT) } }; false
        }
        copy.setOnClickListener { copyAll() }
        share.setOnClickListener { shareDialog() }
        stop.setOnClickListener { currentCall?.cancel() }
        clear.setOnClickListener {
            scope.launch {
                dao.deleteAll()
                adapter?.clearAll()
                Toast.makeText(this@AiForegroundService, "История очищена", Toast.LENGTH_SHORT).show()
}
}
        btnSend.setOnClickListener {
        val text = etMessage.text.toString().trim()
        if (text.isNotEmpty()) { etMessage.text.clear(); sendMessage(text) }
}
        etMessage.setOnEditorActionListener { _, actionId, event ->
            val sendByAction = actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEND
            val sendByEnter = event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN
            if (sendByAction || sendByEnter) {
        val text = etMessage.text.toString().trim()
        if (text.isNotEmpty()) { etMessage.text.clear(); sendMessage(text) }
                true
            } else false
        }

        var initialX = 0; var initialY = 0; var initialTouchX = 0f; var initialTouchY = 0f
        title.setOnTouchListener { _, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> { initialX = params.x; initialY = params.y; initialTouchX = event.rawX; initialTouchY = event.rawY; true }
                MotionEvent.ACTION_MOVE -> {
                    params.x = (initialX + (event.rawX - initialTouchX).toInt()).coerceIn(0, maxOf(0, dm.widthPixels - params.width))
                    params.y = (initialY + (event.rawY - initialTouchY).toInt()).coerceIn(0, maxOf(0, dm.heightPixels - params.height))
                    windowManager.updateViewLayout(view, params); true
                }
                else -> false
            }
}
        var startW = 0; var startH = 0; var startTouchX = 0f; var startTouchY = 0f
        resizeHandle.setOnTouchListener { _, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> { startW = params.width; startH = params.height; startTouchX = event.rawX; startTouchY = event.rawY; true }
                MotionEvent.ACTION_MOVE -> {
        val newW = (startW + (event.rawX - startTouchX).toInt()).coerceIn(minW, maxW)
        val newH = (startH + (event.rawY - startTouchY).toInt()).coerceIn(minH, maxH)
                    params.width = newW; params.height = newH
                    params.x = params.x.coerceIn(0, maxOf(0, dm.widthPixels - newW))
                    params.y = params.y.coerceIn(0, maxOf(0, dm.heightPixels - newH))
                    windowManager.updateViewLayout(view, params); true
                }
                else -> false
            }
}
        close.setOnClickListener { saveOverlayState(params); removeOverlay() }
        overlayView = view; layoutParams = params; windowManager.addView(view, params)
    }

    private fun sendMessage(text: String) {
        val key = SecurePreferences(this).getGroqKey()
        if (key.isBlank()) { Toast.makeText(this, "⚠️ Сначала сохраните ключ в настройках", Toast.LENGTH_LONG).show(); return }
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val model = prefs.getString(PrefsKeys.GROQ_MODEL, PrefsKeys.DEFAULT_MODEL) ?: PrefsKeys.DEFAULT_MODEL
        val systemPrompt = prefs.getString(PrefsKeys.SYSTEM_PROMPT, "") ?: ""
        scope.launch {
        val id = dao.insert(Message(role = "user", content = text))
            adapter?.addMessage(Message(id = id, role = "user", content = text))
            scrollToBottom()
            startStreaming(key, model, systemPrompt)
}
    }

    private fun startStreaming(key: String, model: String, systemPrompt: String) {
        scope.launch {
        val history = dao.getAllMessages()
        val call = GroqClient.buildStreamCall(key, model, systemPrompt, history)
            currentCall = call
            adapter?.isStreaming = true
            setStreamingUI(true)
        var addedPlaceholder = false
            try {
        val full = GroqClient.readStream(call) { token ->
                    mainHandler.post {
        if (!addedPlaceholder) {
                            adapter?.addMessage(Message(role = "assistant", content = ""))
                            addedPlaceholder = true
                        }
                        adapter?.appendToLast(token)
                        scrollToBottom()
}
}
                if (full.isNotBlank()) {
        val id = dao.insert(Message(role = "assistant", content = full))
                    adapter?.setLastId(id)
}
} catch (e: IOException) {
    finalizePartial(addedPlaceholder)
    if (call.isCanceled()) {
        Toast.makeText(this@AiForegroundService, "⏹ Генерация остановлена", Toast.LENGTH_SHORT).show()
    } else {
        Toast.makeText(this@AiForegroundService, "❌ ${e.message}", Toast.LENGTH_LONG).show()
    }
} finally {
                currentCall = null
                adapter?.isStreaming = false
                setStreamingUI(false)
                adapter?.notifyDataSetChanged()
                scrollToBottom()
}
}
    }

    private suspend fun finalizePartial(added: Boolean) {
    val partial = if (added) (adapter?.lastContent() ?: "") else ""
    if (partial.isNotBlank()) {
        val id = dao.insert(Message(role = "assistant", content = partial))
        adapter?.setLastId(id)
    } else {
        adapter?.removeLastIfEmpty()
    }
}
private fun dpToPx(dp: Int): Int = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), resources.displayMetrics).toInt()

    private fun saveOverlayState(params: WindowManager.LayoutParams) {
        getSharedPreferences(PREFS_OVERLAY, MODE_PRIVATE).edit().putInt(KEY_W, params.width).putInt(KEY_H, params.height).putInt(KEY_X, params.x).putInt(KEY_Y, params.y).apply()
    }

    private fun removeOverlay() {
        overlayView?.let { try { windowManager.removeView(it) } catch (_: Exception) {} }
        overlayView = null; layoutParams = null; adapter = null
    }
}
