package com.mhtour.audio

import android.Manifest
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.content.SharedPreferences
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import io.livekit.android.ConnectOptions
import io.livekit.android.LiveKit
import io.livekit.android.room.Room
import io.livekit.android.token.TokenRequestOptions
import io.livekit.android.token.TokenSource
import io.livekit.android.token.cached
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult

class MainActivity : ComponentActivity() {
    // LiveKit Cloud Development Token Server untuk tahap pengembangan.
    // API secret tidak pernah disimpan di aplikasi.
    private val liveKitTokenSource by lazy {
        TokenSource.fromDevelopmentTokenServer("mhtour-19kg8e").cached()
    }

    private var room: Room? = null
    private var currentCode: String? = null
    private var micEnabled = false

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        val micGranted = grants[Manifest.permission.RECORD_AUDIO] == true
        val cameraGranted = grants[Manifest.permission.CAMERA] == true
        if (cameraGranted) {
            startQrScanner()
        }
        if (!micGranted && grants.containsKey(Manifest.permission.RECORD_AUDIO)) {
            Toast.makeText(this, "Izin mikrofon diperlukan untuk Guide berbicara.", Toast.LENGTH_LONG).show()
        }
    }

    private val green = Color.rgb(7, 78, 59)
    private val emerald = Color.rgb(11, 122, 91)
    private val deep = Color.rgb(3, 45, 34)
    private val gold = Color.rgb(198, 145, 36)
    private val cream = Color.rgb(248, 246, 238)
    private val ink = Color.rgb(30, 41, 59)
    private val muted = Color.rgb(100, 116, 139)
    private val white = Color.WHITE

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LiveKit.init(applicationContext)
        home()
    }

    private fun hasInternet(): Boolean {
        val cm = getSystemService(ConnectivityManager::class.java) ?: return false
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun requireInternet(): Boolean {
        if (hasInternet()) return true
        Toast.makeText(this, "Tidak ada koneksi internet. Aktifkan Wi-Fi atau data seluler.", Toast.LENGTH_LONG).show()
        return false
    }

    private fun requestMicIfNeeded() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(arrayOf(Manifest.permission.RECORD_AUDIO))
        }
    }

    private fun requestCameraIfNeeded() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA))
        } else {
            startQrScanner()
        }
    }

    private fun bg(color: Int, radius: Float = 18f, stroke: Int? = null, strokeWidth: Int = 1): GradientDrawable =
        GradientDrawable().apply {
            setColor(color); cornerRadius = dp(radius.toInt()).toFloat()
            stroke?.let { setStroke(dp(strokeWidth), it) }
        }

    private fun gradientBg(top: Int, bottom: Int, radius: Float = 24f) = GradientDrawable(
        GradientDrawable.Orientation.TL_BR, intArrayOf(top, bottom)
    ).apply { cornerRadius = dp(radius.toInt()).toFloat() }

    private fun tv(text: String, size: Float, color: Int = ink, bold: Boolean = false): TextView = TextView(this).apply {
        this.text = text; textSize = size; setTextColor(color)
        if (bold) setTypeface(Typeface.DEFAULT, Typeface.BOLD)
        includeFontPadding = true
    }

    private fun primaryButton(text: String, color: Int = emerald): Button = Button(this).apply {
        this.text = text; setTextColor(white); textSize = 15f; isAllCaps = false
        typeface = Typeface.DEFAULT_BOLD; minHeight = dp(52); background = bg(color, 16f)
        stateListAnimator = null; elevation = dp(2).toFloat()
    }

    private fun outlineButton(text: String): Button = Button(this).apply {
        this.text = text; setTextColor(green); textSize = 15f; isAllCaps = false
        typeface = Typeface.DEFAULT_BOLD; minHeight = dp(50); background = bg(white, 16f, Color.rgb(203,213,225), 1)
        stateListAnimator = null
    }

    private fun edit(hint: String, value: String): EditText = EditText(this).apply {
        this.hint = hint; setText(value); setSingleLine(true); textSize = 15f
        setTextColor(ink); setHintTextColor(muted); background = bg(white, 14f, Color.rgb(226,232,240), 1)
        setPadding(dp(16), 0, dp(16), 0)
    }

    private fun page(title: String, subtitle: String): LinearLayout {
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(cream) }
        root.addView(hero(title, subtitle))
        return root
    }

    private fun hero(title: String, subtitle: String): View {
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; setPadding(dp(22), dp(20), dp(22), dp(24))
            background = gradientBg(deep, green, 0f)
        }
        val brand = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL }
        val mark = TextView(this).apply {
            text = "MH"; textSize = 18f; gravity = Gravity.CENTER; setTextColor(green); typeface = Typeface.DEFAULT_BOLD
            background = bg(gold, 14f); layoutParams = LinearLayout.LayoutParams(dp(46), dp(46))
        }
        brand.addView(mark)
        val brandText = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(12),0,0,0) }
        brandText.addView(tv("MH TOUR", 20f, white, true)); brandText.addView(tv("UMRAH AUDIO COMPANION", 10f, Color.rgb(226,232,240), true))
        brand.addView(brandText)
        box.addView(brand)
        val t = tv(title, 25f, white, true); t.setPadding(0, dp(20), 0, dp(2)); box.addView(t)
        box.addView(tv(subtitle, 13f, Color.rgb(226,232,240)))
        return box
    }

    private fun scroll(content: View): ScrollView = ScrollView(this).apply {
        isFillViewport = true; addView(content)
    }

    private fun contentColumn(): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL; setPadding(dp(18), dp(18), dp(18), dp(26))
    }

    private fun card(): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL; setPadding(dp(18), dp(17), dp(18), dp(17)); background = bg(white, 18f, Color.rgb(231,229,220), 1)
        elevation = dp(1).toFloat()
    }

    private fun addGap(parent: LinearLayout, h: Int = 12) = parent.addView(Space(this), LinearLayout.LayoutParams(1, dp(h)))
    private fun addFull(parent: LinearLayout, view: View, h: Int) = parent.addView(view, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(h)))

    private fun logoView(size: Int): ImageView = ImageView(this).apply {
        setImageResource(com.mhtour.audio.R.drawable.mh_tour_logo)
        scaleType = ImageView.ScaleType.FIT_CENTER
        contentDescription = "Logo MH Tour"
        layoutParams = LinearLayout.LayoutParams(dp(size), dp(size))
    }

    private fun micIcon(size: Int = 26, tint: Int = Color.WHITE): TextView = TextView(this).apply {
        text = "🎙"
        textSize = (size * 0.72f)
        gravity = Gravity.CENTER
        setTextColor(tint)
        includeFontPadding = false
    }

    private fun addPulse(view: View) {
        view.animate().cancel()
        view.scaleX = 1f
        view.scaleY = 1f
        view.alpha = 1f
        view.animate().scaleX(1.10f).scaleY(1.10f).alpha(0.78f).setDuration(650).withEndAction {
            view.animate().scaleX(1f).scaleY(1f).alpha(1f).setDuration(650).withEndAction {
                if (!isFinishing) addPulse(view)
            }.start()
        }.start()
    }

    private fun home() {
        room?.disconnect(); room = null; currentCode = null; micEnabled = false
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.rgb(245, 250, 248))
        }

        val top = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(22), dp(18), dp(22), dp(2))
        }
        val country = tv("ID · Indonesia", 18f, Color.rgb(25, 32, 31), true)
        country.gravity = Gravity.CENTER_VERTICAL
        top.addView(country, LinearLayout.LayoutParams(0, dp(44), 1f))
        val globe = tv("🌐", 28f, Color.DKGRAY); globe.gravity = Gravity.CENTER
        top.addView(globe, LinearLayout.LayoutParams(dp(48), dp(44)))
        root.addView(top)

        val scrollContent = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(26), dp(12), dp(26), dp(26))
        }

        scrollContent.addView(logoView(230), LinearLayout.LayoutParams(dp(230), dp(230)).apply {
            gravity = Gravity.CENTER_HORIZONTAL
        })
        val managed = tv("Dikelola Oleh:", 18f, Color.rgb(78, 84, 82), true)
        managed.gravity = Gravity.CENTER
        scrollContent.addView(managed, LinearLayout.LayoutParams(-1, dp(30)))
        val org = tv("Ponpes Miftahul Huda Cigondewah", 19f, Color.rgb(70, 77, 75), true)
        org.gravity = Gravity.CENTER
        scrollContent.addView(org, LinearLayout.LayoutParams(-1, dp(54)))
        addGap(scrollContent, 18)

        val prompt = tv("Pilih pengalaman sesuai peran Anda.", 19f, Color.rgb(34, 42, 40), false)
        prompt.gravity = Gravity.CENTER
        scrollContent.addView(prompt, LinearLayout.LayoutParams(-1, dp(58)))
        addGap(scrollContent, 8)

        val info = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(20), dp(16), dp(20), dp(16))
            background = bg(Color.rgb(244, 249, 247), 18f, Color.rgb(224, 233, 229), 1)
            elevation = dp(2).toFloat()
        }
        val infoText = tv("Peserta masuk dengan kode Journey.\nAkun SafarHub dapat mengelola Workspace dan memandu Journey ketika ditugaskan.", 15f, Color.rgb(62, 70, 67), false)
        infoText.gravity = Gravity.CENTER
        info.addView(infoText, LinearLayout.LayoutParams(-1, dp(142)))
        scrollContent.addView(info, LinearLayout.LayoutParams(-1, dp(158)))
        addGap(scrollContent, 26)

        val jamaahBtn = primaryButton("🎧  Dengarkan sebagai Jamaah", Color.rgb(0, 126, 115)).apply {
            textSize = 17f; minHeight = dp(62); background = bg(Color.rgb(0, 126, 115), 32f)
        }
        scrollContent.addView(jamaahBtn, LinearLayout.LayoutParams(-1, dp(62)))
        addGap(scrollContent, 16)
        val guideBtn = outlineButton("🎙  Lanjut sebagai Guide").apply {
            textSize = 17f; minHeight = dp(62); background = bg(Color.TRANSPARENT, 32f, Color.rgb(92, 108, 104), 2)
        }
        scrollContent.addView(guideBtn, LinearLayout.LayoutParams(-1, dp(62)))
        addGap(scrollContent, 34)

        val privacy = TextView(this).apply {
            text = "ⓘ  Kebijakan Privasi"
            textSize = 17f
            setTextColor(Color.rgb(0, 111, 101))
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setPadding(0, dp(8), 0, dp(8))
        }
        scrollContent.addView(privacy, LinearLayout.LayoutParams(-1, dp(54)))
        privacy.setOnClickListener {
            Toast.makeText(this, "MH Tour hanya menggunakan mikrofon saat Guide memulai sesi bicara.", Toast.LENGTH_LONG).show()
        }

        root.addView(ScrollView(this).apply { isFillViewport = true; addView(scrollContent) }, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)
        jamaahBtn.isEnabled = true
        jamaahBtn.isClickable = true
        guideBtn.isEnabled = true
        guideBtn.isClickable = true
        jamaahBtn.setOnClickListener { if (requireInternet()) jamaahScreen() }
        guideBtn.setOnClickListener { if (requireInternet()) guideScreen() }
    }

    private fun makeQrBitmap(payload: String, size: Int = 720): Bitmap {
        val matrix: BitMatrix = MultiFormatWriter().encode(payload, BarcodeFormat.QR_CODE, size, size)
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        for (x in 0 until size) for (y in 0 until size) {
            bitmap.setPixel(x, y, if (matrix[x, y]) Color.BLACK else Color.WHITE)
        }
        return bitmap
    }

    private fun shareText(text: String) {
        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(android.content.Intent.EXTRA_TEXT, text)
        }
        startActivity(android.content.Intent.createChooser(intent, "Bagikan Rombongan MH Tour"))
    }

    private fun guideScreen() {
        val root = page("Ruang Audio Guide", "Buat ruang rombongan, bagikan QR, lalu bicara secara real-time.")
        val body = contentColumn()

        val identity = card(); identity.addView(tv("PROFIL GUIDE", 12f, gold, true)); addGap(identity, 5)
        val name = edit("Nama Guide / Muthawwif", "Guide")
        identity.addView(name, LinearLayout.LayoutParams(-1, dp(52))); body.addView(identity)
        addGap(body)

        val session = card(); session.addView(tv("SESI ROMBONGAN", 12f, gold, true)); addGap(session, 4)
        val code = tv("—", 34f, green, true); code.gravity = Gravity.CENTER; session.addView(code)
        val hint = tv("Kode akan muncul setelah sesi dibuat", 12f, muted); hint.gravity = Gravity.CENTER; session.addView(hint)
        addGap(session, 10)
        val qrTitle = tv("QR CODE ROMBONGAN", 11f, gold, true); qrTitle.gravity = Gravity.CENTER; session.addView(qrTitle)
        addGap(session, 6)
        val qr = ImageView(this).apply {
            setBackgroundColor(Color.WHITE); setPadding(dp(12), dp(12), dp(12), dp(12)); scaleType = ImageView.ScaleType.FIT_CENTER; visibility = View.GONE
        }
        session.addView(qr, LinearLayout.LayoutParams(-1, dp(250)))
        val qrHint = tv("Jemaah dapat memindai QR ini untuk bergabung ke rombongan.", 12f, muted); qrHint.gravity = Gravity.CENTER; qrHint.visibility = View.GONE; session.addView(qrHint)
        addGap(session, 10)
        val create = primaryButton("Buat Sesi Rombongan", gold); session.addView(create)
        val shareQr = outlineButton("Bagikan Kode & QR Rombongan"); shareQr.isEnabled = false; session.addView(shareQr)
        body.addView(session)
        addGap(body)

        val live = card(); live.addView(tv("KONTROL AUDIO", 12f, gold, true)); addGap(live, 5)
        val micPanel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER
            setPadding(dp(8), dp(10), dp(8), dp(10))
        }
        val micCircle = TextView(this).apply {
            text = "🎙"; textSize = 42f; gravity = Gravity.CENTER; setTextColor(white); background = bg(Color.rgb(0, 126, 115), 60f)
        }
        micPanel.addView(micCircle, LinearLayout.LayoutParams(dp(104), dp(104)).apply { gravity = Gravity.CENTER_HORIZONTAL })
        val micStatus = tv("MIKROFON SIAP", 14f, muted, true); micStatus.gravity = Gravity.CENTER
        micPanel.addView(micStatus, LinearLayout.LayoutParams(-1, dp(36)))
        live.addView(micPanel)
        addGap(live, 5)
        val state = tv("Belum ada sesi aktif", 16f, muted, true); state.gravity = Gravity.CENTER; live.addView(state)
        addGap(live, 10)
        val talk = primaryButton("🎙  Mulai Bicara", emerald); talk.isEnabled = false; live.addView(talk)
        addGap(live, 8)
        val mute = outlineButton("Mute Mikrofon"); mute.isEnabled = false; live.addView(mute)
        addGap(live, 8)
        val back = outlineButton("Kembali ke Beranda"); live.addView(back); body.addView(live)

        root.addView(scroll(body), LinearLayout.LayoutParams(-1, 0, 1f)); setContentView(root)

        create.setOnClickListener {
            if (!requireInternet()) return@setOnClickListener
            create.isEnabled = false; state.text = "Membuat sesi..."
            lifecycleScope.launch {
                try {
                    currentCode = generateSessionCode(); code.text = currentCode
                    qr.setImageBitmap(makeQrBitmap("MHTOUR|JOIN|$currentCode")); qr.visibility = View.VISIBLE; qrHint.visibility = View.VISIBLE
                    shareQr.isEnabled = true
                    talk.isEnabled = true
                    mute.isEnabled = false
                    state.text = "Sesi siap — bagikan kode atau QR ke jemaah. Tekan Mulai Bicara untuk mengaktifkan mikrofon."
                    micStatus.text = "MIKROFON SIAP"
                } catch (e: Exception) { state.text = "Gagal membuat sesi: ${e.message}" }
                finally { create.isEnabled = true }
            }
        }

        shareQr.setOnClickListener { currentCode?.let { c -> shareText("MH Tour — Rombongan: $c\n\nScan QR untuk bergabung. Kode rombongan: $c") } }

        talk.setOnClickListener {
            if (currentCode.isNullOrBlank()) {
                state.text = "Buat sesi rombongan terlebih dahulu."
                Toast.makeText(this, "Tekan “Buat Sesi Rombongan” terlebih dahulu.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!requireInternet()) return@setOnClickListener
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                requestMicIfNeeded()
                state.text = "Izinkan mikrofon, lalu tekan Mulai Bicara lagi."
                return@setOnClickListener
            }
            talk.isEnabled = false; state.text = "Menghubungkan ke server online..."
            lifecycleScope.launch {
                try {
                    val token = requestLiveKitToken(currentCode!!, name.text.toString().trim().ifBlank { "Guide" })
                    room?.disconnect(); room = connectLiveKit(token)
                    val ok = room!!.localParticipant.setMicrophoneEnabled(true)
                    if (!ok) error("Mikrofon gagal dipublish")
                    micEnabled = true; mute.isEnabled = true; talk.text = "🎙  Sedang Bicara"; micStatus.text = "●  ANDA SEDANG BERBICARA"; state.text = "TERHUBUNG • Suara Anda LIVE ke jemaah"; addPulse(micCircle)
                } catch (e: Exception) { state.text = "Gagal terhubung: ${e.message}"; micStatus.text = "MIKROFON GAGAL TERHUBUNG"; room?.disconnect(); room = null }
                finally { talk.isEnabled = true }
            }
        }
        mute.setOnClickListener {
            lifecycleScope.launch {
                try {
                    micEnabled = !micEnabled; room?.localParticipant?.setMicrophoneEnabled(micEnabled)
                    mute.text = if (micEnabled) "Mute Mikrofon" else "Unmute Mikrofon"
                    talk.text = if (micEnabled) "🎙  Sedang Bicara" else "○  Suara Dimute"
                    micStatus.text = if (micEnabled) "●  ANDA SEDANG BERBICARA" else "MIKROFON DIMUTE"
                    state.text = if (micEnabled) "Mikrofon LIVE • Jemaah dapat mendengar" else "Mikrofon dimute • Sesi tetap tersambung"
                    if (micEnabled) addPulse(micCircle) else { micCircle.animate().cancel(); micCircle.scaleX = 1f; micCircle.scaleY = 1f; micCircle.alpha = 1f }
                } catch (e: Exception) { state.text = "Gagal mengubah mikrofon: ${e.message}" }
            }
        }
        back.setOnClickListener { home() }
    }

    private fun jamaahScreen(prefilledCode: String = "") {
        val root = page("Masuk ke Rombongan", "Masukkan kode yang diberikan Guide untuk mulai menerima audio.")
        val body = contentColumn()
        val form = card(); form.addView(tv("DATA JEMAAH", 12f, gold, true)); addGap(form, 5)
        val name = edit("Nama Jemaah", "Jemaah"); form.addView(name, LinearLayout.LayoutParams(-1, dp(52))); addGap(form, 10)
        val code = edit("Kode sesi  •  contoh UM123456", prefilledCode); code.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS; form.addView(code, LinearLayout.LayoutParams(-1, dp(52))); addGap(form, 10)
        val scan = outlineButton("▣  Scan QR Rombongan"); form.addView(scan); addGap(form, 12)
        val join = primaryButton("Gabung & Dengarkan", green); form.addView(join); body.addView(form)
        addGap(body)

        val listening = card(); listening.addView(tv("STATUS AUDIO", 12f, gold, true)); addGap(listening, 4)
        val state = tv("Belum terhubung", 17f, muted, true); state.gravity = Gravity.CENTER; listening.addView(state)
        addGap(listening, 10)
        val indicator = tv("◉  MENUNGGU SUARA GUIDE", 13f, green, true); indicator.gravity = Gravity.CENTER; listening.addView(indicator)
        addGap(listening, 12)
        listening.addView(tv("Pastikan volume media HP aktif. Audio Guide akan terdengar otomatis setelah Guide mulai berbicara.", 12f, muted))
        addGap(listening, 12)
        val back = outlineButton("Kembali ke Beranda"); listening.addView(back); body.addView(listening)

        root.addView(scroll(body), LinearLayout.LayoutParams(-1, 0, 1f)); setContentView(root)
        join.isEnabled = true
        join.isClickable = true
        scan.isEnabled = true
        scan.isClickable = true
        scan.setOnClickListener {
            requestCameraIfNeeded()
        }
        join.setOnClickListener {
            if (!requireInternet()) return@setOnClickListener
            val c = code.text.toString().trim().uppercase(); if (c.isBlank()) { code.error = "Masukkan kode sesi"; return@setOnClickListener }
            join.isEnabled = false; state.text = "Menghubungkan..."; indicator.text = "◉  MENGHUBUNGKAN KE SERVER ONLINE"
            lifecycleScope.launch {
                try { val token = requestLiveKitToken(c, name.text.toString().trim().ifBlank { "Jemaah" }); room?.disconnect(); room = connectLiveKit(token); currentCode = c; state.text = "TERHUBUNG • Menunggu Guide"; indicator.text = "●  SIAP MENDENGARKAN" }
                catch (e: Exception) { state.text = "Gagal bergabung: ${e.message}"; indicator.text = "◉  KONEKSI GAGAL"; room?.disconnect(); room = null }
                finally { join.isEnabled = true }
            }
        }
        back.setOnClickListener { home() }
    }

    private fun startQrScanner() {
        IntentIntegrator(this).apply {
            setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
            setPrompt("Arahkan kamera ke QR Code rombongan MH Tour")
            setBeepEnabled(true)
            setOrientationLocked(false)
            initiateScan()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result: IntentResult? = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents != null) {
                val payload = result.contents.trim()
                val parts = payload.split("|")
                val code = when {
                    parts.size >= 4 && parts[0] == "MHTOUR" && parts[1] == "JOIN" -> parts[3].trim().uppercase()
                    parts.size >= 3 && parts[0] == "MHTOUR" && parts[1] == "JOIN" -> parts[2].trim().uppercase()
                    else -> payload.removePrefix("MHTOUR|JOIN|").trim().uppercase()
                }
                if (code.matches(Regex("UM[A-Z0-9]{4,20}"))) {
                    jamaahScreenWithCode(code)
                } else {
                    Toast.makeText(this, "QR bukan QR rombongan MH Tour yang valid", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(this, "Pemindaian dibatalkan", Toast.LENGTH_SHORT).show()
            }
            return
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun jamaahScreenWithCode(codeValue: String) {
        jamaahScreen(codeValue)
    }

    private fun generateSessionCode(): String {
        val digits = (100000..999999).random()
        return "UM$digits"
    }

    private suspend fun requestLiveKitToken(code: String, name: String): JSONObject = withContext(Dispatchers.IO) {
        val roomName = "mh-tour-$code"
        val identity = "${name.replace("\\s+".toRegex(), "-")}-${java.util.UUID.randomUUID()}"
        val result = liveKitTokenSource.fetch(
            TokenRequestOptions(
                roomName = roomName,
                participantName = name,
                participantIdentity = identity
            )
        )
        val credentials = result.getOrThrow()
        JSONObject().put("serverUrl", credentials.serverUrl).put("participantToken", credentials.participantToken)
    }

    private suspend fun connectLiveKit(response: JSONObject): Room {
        val serverUrl = response.optString("serverUrl").trim()
        val participantToken = response.optString("participantToken").trim()
        require(serverUrl.startsWith("ws://") || serverUrl.startsWith("wss://")) {
            "Alamat LiveKit tidak valid"
        }
        require(participantToken.isNotBlank()) { "Token LiveKit kosong" }

        val newRoom = LiveKit.create(applicationContext)
        newRoom.connect(
            url = serverUrl,
            token = participantToken,
            options = ConnectOptions(
                autoSubscribe = true,
                audio = false,
                video = false
            )
        )
        return newRoom
    }

    override fun onDestroy() { room?.disconnect(); room = null; super.onDestroy() }
}
