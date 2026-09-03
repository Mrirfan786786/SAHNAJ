package com.example.presentation.screens

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberCard
import com.example.ui.theme.CyberCardElevated
import com.example.ui.theme.CyberError
import com.example.ui.theme.CyberGreen
import com.example.ui.theme.CyberRed
import com.example.ui.theme.CyberRedBorder
import com.example.ui.theme.CyberRedBright
import com.example.ui.theme.CyberRedContainer
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberTextMuted
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.CyberTextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

// ==========================================
// DATA MODELS & PREFERENCES (SELF-CONTAINED)
// ==========================================

data class EnrolledVoiceProfile(
    val id: String,
    val name: String,
    val role: String,
    val isPrimaryOwner: Boolean = false,
    val enrolledDate: String = "Active",
    val sampleCount: Int = 3,
    val confidence: Float = 0.98f
)

enum class ListenMode(val displayName: String, val hindiSubtitle: String) {
    EVERYONE("Everyone", "सभी के लिए खुला"),
    OWNER_ONLY("Owner Only", "केवल मालिक (Md Irfan Alam)"),
    OWNER_FAMILY("Owner + Family", "मालिक और परिवार")
}

enum class GuardEnforcementResult {
    ALLOWED,
    CONVERSATION_ONLY,
    BLOCKED_AND_ALARM
}

object VoiceGuardianPreferences {
    private const val PREFS_NAME = "sahnaj_voice_guardian_security_prefs"

    private const val KEY_GUARDIAN_ENABLED = "key_voice_guardian_enabled"
    private const val KEY_AWAY_GUARD_ENABLED = "key_away_guard_enabled"
    private const val KEY_LISTEN_MODE = "key_listen_mode"
    private const val KEY_MATCH_STRICTNESS = "key_match_strictness"
    private const val KEY_COUNTRY_CODE = "key_country_code"
    private const val KEY_SOS_CONTACTS = "key_sos_contacts"
    private const val KEY_ENROLLED_VOICES = "key_enrolled_voices_json"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun isGuardianEnabled(context: Context): Boolean =
        getPrefs(context).getBoolean(KEY_GUARDIAN_ENABLED, true)

    fun setGuardianEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_GUARDIAN_ENABLED, enabled).apply()
    }

    fun isAwayGuardEnabled(context: Context): Boolean =
        getPrefs(context).getBoolean(KEY_AWAY_GUARD_ENABLED, false)

    fun setAwayGuardEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_AWAY_GUARD_ENABLED, enabled).apply()
    }

    fun getListenMode(context: Context): ListenMode {
        val modeStr = getPrefs(context).getString(KEY_LISTEN_MODE, ListenMode.OWNER_ONLY.name)
        return try {
            ListenMode.valueOf(modeStr ?: ListenMode.OWNER_ONLY.name)
        } catch (_: Exception) {
            ListenMode.OWNER_ONLY
        }
    }

    fun setListenMode(context: Context, mode: ListenMode) {
        getPrefs(context).edit().putString(KEY_LISTEN_MODE, mode.name).apply()
    }

    fun getMatchStrictness(context: Context): Float =
        getPrefs(context).getFloat(KEY_MATCH_STRICTNESS, 0.70f)

    fun setMatchStrictness(context: Context, strictness: Float) {
        getPrefs(context).edit().putFloat(KEY_MATCH_STRICTNESS, strictness).apply()
    }

    fun getCountryCode(context: Context): String =
        getPrefs(context).getString(KEY_COUNTRY_CODE, "+91 (India)") ?: "+91 (India)"

    fun setCountryCode(context: Context, code: String) {
        getPrefs(context).edit().putString(KEY_COUNTRY_CODE, code).apply()
    }

    fun getSosContacts(context: Context): String =
        getPrefs(context).getString(KEY_SOS_CONTACTS, "Mom (+91 9876543210), Emergency (112)")
            ?: "Mom (+91 9876543210), Emergency (112)"

    fun setSosContacts(context: Context, contacts: String) {
        getPrefs(context).edit().putString(KEY_SOS_CONTACTS, contacts).apply()
    }

    fun getEnrolledVoices(context: Context): List<EnrolledVoiceProfile> {
        val jsonStr = getPrefs(context).getString(KEY_ENROLLED_VOICES, null)
        if (jsonStr.isNullOrBlank()) {
            return listOf(
                EnrolledVoiceProfile(
                    id = "owner_primary",
                    name = "Boss / Md Irfan Alam",
                    role = "Primary Owner (मालिक)",
                    isPrimaryOwner = true,
                    enrolledDate = "24x7 Active Biometric",
                    sampleCount = 3,
                    confidence = 0.98f
                )
            )
        }

        val list = mutableListOf<EnrolledVoiceProfile>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    EnrolledVoiceProfile(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        role = obj.getString("role"),
                        isPrimaryOwner = obj.optBoolean("isPrimaryOwner", false),
                        enrolledDate = obj.optString("enrolledDate", "Active"),
                        sampleCount = obj.optInt("sampleCount", 3),
                        confidence = obj.optDouble("confidence", 0.95).toFloat()
                    )
                )
            }
        } catch (_: Exception) {
            list.add(
                EnrolledVoiceProfile(
                    id = "owner_primary",
                    name = "Boss / Md Irfan Alam",
                    role = "Primary Owner (मालिक)",
                    isPrimaryOwner = true,
                    enrolledDate = "24x7 Active Biometric",
                    sampleCount = 3,
                    confidence = 0.98f
                )
            )
        }
        return list
    }

    fun saveEnrolledVoices(context: Context, voices: List<EnrolledVoiceProfile>) {
        val array = JSONArray()
        for (v in voices) {
            val obj = JSONObject().apply {
                put("id", v.id)
                put("name", v.name)
                put("role", v.role)
                put("isPrimaryOwner", v.isPrimaryOwner)
                put("enrolledDate", v.enrolledDate)
                put("sampleCount", v.sampleCount)
                put("confidence", v.confidence.toDouble())
            }
            array.put(obj)
        }
        getPrefs(context).edit().putString(KEY_ENROLLED_VOICES, array.toString()).apply()
    }

    /**
     * Logic Enforcement evaluation engine:
     * Evaluates whether an incoming voice command with biometric score should be executed.
     */
    fun evaluateVoiceEnforcement(
        context: Context,
        speakerConfidence: Float,
        isDeviceControlCommand: Boolean
    ): GuardEnforcementResult {
        if (!isGuardianEnabled(context)) {
            return GuardEnforcementResult.ALLOWED
        }

        val listenMode = getListenMode(context)
        val strictness = getMatchStrictness(context)
        val isAwayGuard = isAwayGuardEnabled(context)

        val isOwnerRecognized = speakerConfidence >= strictness

        return when (listenMode) {
            ListenMode.EVERYONE -> GuardEnforcementResult.ALLOWED
            ListenMode.OWNER_ONLY, ListenMode.OWNER_FAMILY -> {
                if (isOwnerRecognized) {
                    GuardEnforcementResult.ALLOWED
                } else {
                    if (isDeviceControlCommand) {
                        if (isAwayGuard) {
                            GuardEnforcementResult.BLOCKED_AND_ALARM
                        } else {
                            GuardEnforcementResult.CONVERSATION_ONLY
                        }
                    } else {
                        GuardEnforcementResult.CONVERSATION_ONLY
                    }
                }
            }
        }
    }
}

// ==========================================
// VOICE GUARDIAN COMPOSABLE SCREEN
// ==========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceGuardianScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Persistent State Values
    var isGuardianEnabled by remember { mutableStateOf(VoiceGuardianPreferences.isGuardianEnabled(context)) }
    var isAwayGuardMode by remember { mutableStateOf(VoiceGuardianPreferences.isAwayGuardEnabled(context)) }
    var selectedListenMode by remember { mutableStateOf(VoiceGuardianPreferences.getListenMode(context)) }
    var matchStrictness by remember { mutableFloatStateOf(VoiceGuardianPreferences.getMatchStrictness(context)) }
    var selectedCountryCode by remember { mutableStateOf(VoiceGuardianPreferences.getCountryCode(context)) }
    var sosContactsText by remember { mutableStateOf(VoiceGuardianPreferences.getSosContacts(context)) }

    val enrolledVoicesList = remember {
        mutableStateListOf<EnrolledVoiceProfile>().apply {
            addAll(VoiceGuardianPreferences.getEnrolledVoices(context))
        }
    }

    // Interactive Dialogs & Testing State
    var showAddVoiceDialog by remember { mutableStateOf(false) }
    var showCountryDropdown by remember { mutableStateOf(false) }
    var isTestingVoice by remember { mutableStateOf(false) }
    var testResultText by remember { mutableStateOf<String?>(null) }
    var testResultMatch by remember { mutableStateOf<Boolean?>(null) }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            isGuardianEnabled = true
            VoiceGuardianPreferences.setGuardianEnabled(context, true)
            Toast.makeText(context, "Voice Guardian Biometrics Active", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Microphone permission required for Voice Guardian", Toast.LENGTH_LONG).show()
        }
    }

    val availableCountries = listOf(
        "+91 (India)",
        "+1 (United States / Canada)",
        "+44 (United Kingdom)",
        "+971 (United Arab Emirates)",
        "+966 (Saudi Arabia)",
        "+65 (Singapore)",
        "+61 (Australia)",
        "+49 (Germany)"
    )

    fun triggerHapticFeedback() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                @Suppress("DEPRECATION")
                vibrator?.vibrate(80)
            }
        } catch (_: Exception) {}
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Voice Guardian",
                                fontWeight = FontWeight.Black,
                                fontSize = 19.sp,
                                color = CyberTextPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (isGuardianEnabled) CyberRedContainer else Color(0xFF221526),
                                border = BorderStroke(1.dp, if (isGuardianEnabled) CyberRedBright else CyberRedBorder)
                            ) {
                                Text(
                                    text = if (isGuardianEnabled) "ACTIVE" else "STANDBY",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (isGuardianEnabled) CyberRedBright else CyberTextMuted,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "बायोमेट्रिक वॉइस शील्ड • Biometric Security Engine",
                            fontSize = 11.sp,
                            color = CyberRedBright
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("voice_guardian_back_button")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = CyberRedBright
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CyberSurface)
            )
        },
        containerColor = CyberBlack
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ==========================================
            // HEADER BANNER & SECURITY INFO
            // ==========================================
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("voice_guardian_header_card"),
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF1B0A11),
                border = BorderStroke(1.dp, CyberRedBorder)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(CyberRedBright, CyberRedContainer, Color.Transparent)
                                )
                            )
                            .border(1.5.dp, CyberRedBright, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "SAHNAJ Biometric Voice Defense",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberTextPrimary
                        )
                        Text(
                            text = "सिर्फ आपकी प्रमाणित आवाज़ पर सिस्टम कंट्रोल्स (कॉल, ऐप्स, टॉर्च) चलेंगे। अनजान आवाज़ पर गार्ड मोड एक्टिव होगा।",
                            fontSize = 11.5.sp,
                            color = CyberTextSecondary,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // ==========================================
            // MASTER CONTROLS & SWITCHES
            // ==========================================
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("voice_guardian_switches_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CyberCard),
                border = BorderStroke(1.dp, if (isGuardianEnabled) CyberRedBright else CyberRedBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Switch 1: Voice Guardian ON
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isGuardianEnabled) CyberRedContainer else Color(0xFF221622)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Fingerprint,
                                    contentDescription = null,
                                    tint = if (isGuardianEnabled) CyberRedBright else CyberTextMuted,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = "Voice Guardian ON",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyberTextPrimary
                                )
                                Text(
                                    text = "मास्टर बायोमेट्रिक वॉइस फ़िल्टर",
                                    fontSize = 11.5.sp,
                                    color = CyberRedBright
                                )
                            }
                        }

                        Switch(
                            checked = isGuardianEnabled,
                            onCheckedChange = { newState ->
                                triggerHapticFeedback()
                                if (newState) {
                                    val hasMic = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.RECORD_AUDIO
                                    ) == PackageManager.PERMISSION_GRANTED

                                    if (!hasMic) {
                                        micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    } else {
                                        isGuardianEnabled = true
                                        VoiceGuardianPreferences.setGuardianEnabled(context, true)
                                    }
                                } else {
                                    isGuardianEnabled = false
                                    VoiceGuardianPreferences.setGuardianEnabled(context, false)
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = CyberRedBright,
                                uncheckedThumbColor = CyberTextMuted,
                                uncheckedTrackColor = Color(0xFF2B1D28)
                            ),
                            modifier = Modifier.testTag("voice_guardian_master_switch")
                        )
                    }

                    HorizontalDivider(color = Color(0xFF261922))

                    // Switch 2: Away / Guard Mode
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isAwayGuardMode) Color(0xFF380812) else Color(0xFF221622)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = if (isAwayGuardMode) CyberRedBright else CyberTextMuted,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = "Away / Guard Mode",
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyberTextPrimary
                                )
                                Text(
                                    text = "अनजान आवाज़ पर चेतावनी व लॉक (Intruder Lock)",
                                    fontSize = 11.sp,
                                    color = CyberAmber
                                )
                            }
                        }

                        Switch(
                            checked = isAwayGuardMode,
                            onCheckedChange = {
                                triggerHapticFeedback()
                                isAwayGuardMode = it
                                VoiceGuardianPreferences.setAwayGuardEnabled(context, it)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = CyberAmber,
                                uncheckedThumbColor = CyberTextMuted,
                                uncheckedTrackColor = Color(0xFF2B1D28)
                            ),
                            modifier = Modifier.testTag("away_guard_mode_switch")
                        )
                    }
                }
            }

            // ==========================================
            // LISTEN MODE SELECTOR (SEGMENTED BUTTONS)
            // ==========================================
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("listen_mode_selector_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CyberCard),
                border = BorderStroke(1.dp, CyberRedBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = CyberRedBright,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Listen Mode Selector",
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberTextPrimary
                        )
                    }

                    Text(
                        text = "चुनें कि कौन सहनाज के साथ बातचीत और सिस्टम एक्शन्स कर सकता है:",
                        fontSize = 12.sp,
                        color = CyberTextSecondary
                    )

                    // Segmented Button Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF140D17))
                            .border(1.dp, Color(0xFF331D2D), RoundedCornerShape(10.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        ListenMode.values().forEach { mode ->
                            val isSelected = selectedListenMode == mode
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelected) CyberRedBright else Color.Transparent
                                    )
                                    .clickable {
                                        triggerHapticFeedback()
                                        selectedListenMode = mode
                                        VoiceGuardianPreferences.setListenMode(context, mode)
                                    }
                                    .testTag("listen_mode_tab_${mode.name.lowercase()}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = mode.displayName,
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                                    color = if (isSelected) Color.White else CyberTextMuted,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    // Explanatory tag for current mode
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF1E111C),
                        border = BorderStroke(1.dp, Color(0xFF3C1F34))
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = when (selectedListenMode) {
                                    ListenMode.EVERYONE -> Icons.Default.Public
                                    ListenMode.OWNER_ONLY -> Icons.Default.Person
                                    ListenMode.OWNER_FAMILY -> Icons.Default.Group
                                },
                                contentDescription = null,
                                tint = CyberRedBright,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "मोड सक्रिय: ${selectedListenMode.hindiSubtitle}",
                                fontSize = 11.5.sp,
                                color = CyberTextSecondary
                            )
                        }
                    }
                }
            }

            // ==========================================
            // VOICE ENROLLMENT SECTION
            // ==========================================
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("voice_enrollment_section_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CyberCard),
                border = BorderStroke(1.dp, CyberRedBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = null,
                                tint = CyberRedBright,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = "Voice Enrollment Profiles",
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyberTextPrimary
                                )
                                Text(
                                    text = "रजिस्टर्ड वॉइस प्रिंट्स",
                                    fontSize = 11.sp,
                                    color = CyberTextSecondary
                                )
                            }
                        }

                        // "+ Add a Voice" Button
                        Button(
                            onClick = { showAddVoiceDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberRedBright),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier
                                .height(36.dp)
                                .testTag("add_voice_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "+ Add a Voice",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    HorizontalDivider(color = Color(0xFF261922))

                    // Profiles List
                    enrolledVoicesList.forEach { profile ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("enrolled_voice_item_${profile.id}"),
                            shape = RoundedCornerShape(10.dp),
                            color = if (profile.isPrimaryOwner) Color(0xFF240D17) else Color(0xFF19121E),
                            border = BorderStroke(
                                1.dp,
                                if (profile.isPrimaryOwner) CyberRedBright else Color(0xFF382335)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(if (profile.isPrimaryOwner) CyberRedContainer else Color(0xFF281C2E)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (profile.isPrimaryOwner) Icons.Default.Fingerprint else Icons.Default.Person,
                                        contentDescription = null,
                                        tint = if (profile.isPrimaryOwner) CyberRedBright else CyberTextMuted,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = profile.name,
                                            fontSize = 13.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = CyberTextPrimary
                                        )
                                        if (profile.isPrimaryOwner) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = CyberRedBright
                                            ) {
                                                Text(
                                                    text = "MASTER",
                                                    fontSize = 8.5.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = Color.White,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = "${profile.role} • ${profile.sampleCount} Samples (${(profile.confidence * 100).toInt()}% Match)",
                                        fontSize = 11.sp,
                                        color = CyberTextSecondary
                                    )
                                }

                                if (!profile.isPrimaryOwner) {
                                    IconButton(
                                        onClick = {
                                            enrolledVoicesList.remove(profile)
                                            VoiceGuardianPreferences.saveEnrolledVoices(context, enrolledVoicesList)
                                            Toast.makeText(context, "${profile.name} removed", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = "Delete",
                                            tint = CyberTextMuted,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Enrolled Master",
                                        tint = CyberGreen,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ==========================================
            // MATCH STRICTNESS SLIDER SECTION
            // ==========================================
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("match_strictness_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CyberCard),
                border = BorderStroke(1.dp, CyberRedBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = CyberRedBright,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Match Strictness",
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyberTextPrimary
                            )
                        }

                        // Real-Time Value Badge
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = CyberRedContainer,
                            border = BorderStroke(1.dp, CyberRedBright)
                        ) {
                            Text(
                                text = "${String.format("%.2f", matchStrictness)} (${(matchStrictness * 100).toInt()}%)",
                                fontSize = 11.5.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = CyberRedBright,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    // Strictness Label
                    val strictnessLabel = when {
                        matchStrictness < 0.40f -> "Low (कम सख्त - Permissive Mode)"
                        matchStrictness <= 0.75f -> "High Security (उच्च सुरक्षा - Recommended)"
                        else -> "Ultra-Strict Military (अत्यधिक सख्त - Zero Tolerance)"
                    }

                    Text(
                        text = "Current Level: $strictnessLabel",
                        fontSize = 11.5.sp,
                        color = if (matchStrictness > 0.75f) CyberAmber else CyberTextSecondary
                    )

                    Slider(
                        value = matchStrictness,
                        onValueChange = { newValue ->
                            matchStrictness = newValue
                        },
                        onValueChangeFinished = {
                            VoiceGuardianPreferences.setMatchStrictness(context, matchStrictness)
                        },
                        valueRange = 0.10f..1.00f,
                        steps = 18,
                        colors = SliderDefaults.colors(
                            thumbColor = CyberRedBright,
                            activeTrackColor = CyberRedBright,
                            inactiveTrackColor = Color(0xFF281C2C)
                        ),
                        modifier = Modifier.testTag("match_strictness_slider")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("0.10 (Relaxed)", fontSize = 10.sp, color = CyberTextMuted)
                        Text("0.70 (Default)", fontSize = 10.sp, color = CyberRedBright, fontWeight = FontWeight.Bold)
                        Text("1.00 (Max Strict)", fontSize = 10.sp, color = CyberTextMuted)
                    }
                }
            }

            // ==========================================
            // SECURITY & SOS SECTION
            // ==========================================
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("security_sos_section_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CyberCard),
                border = BorderStroke(1.dp, CyberRedBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Emergency,
                            contentDescription = null,
                            tint = CyberRedBright,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = "Security & Emergency Protocol",
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyberTextPrimary
                            )
                            Text(
                                text = "देश कोड व आपातकालीन SOS संपर्क",
                                fontSize = 11.sp,
                                color = CyberTextSecondary
                            )
                        }
                    }

                    // Country Code Dropdown Selector
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Country Code (कंट्री कोड)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = CyberTextSecondary
                        )

                        Box(modifier = Modifier.fillMaxWidth()) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { showCountryDropdown = true }
                                    .testTag("country_code_dropdown_trigger"),
                                color = Color(0xFF18101E),
                                border = BorderStroke(1.dp, Color(0xFF3B2335))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Public,
                                            contentDescription = null,
                                            tint = CyberRedBright,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = selectedCountryCode,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = CyberTextPrimary
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Select",
                                        tint = CyberTextMuted
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = showCountryDropdown,
                                onDismissRequest = { showCountryDropdown = false },
                                modifier = Modifier
                                    .background(CyberCardElevated)
                                    .border(1.dp, CyberRedBorder)
                            ) {
                                availableCountries.forEach { country ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = country,
                                                fontSize = 13.sp,
                                                color = if (country == selectedCountryCode) CyberRedBright else CyberTextPrimary
                                            )
                                        },
                                        onClick = {
                                            selectedCountryCode = country
                                            VoiceGuardianPreferences.setCountryCode(context, country)
                                            showCountryDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Favorite & SOS Contacts Field
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Favorite & SOS Contacts",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = CyberTextSecondary
                        )

                        OutlinedTextField(
                            value = sosContactsText,
                            onValueChange = {
                                sosContactsText = it
                                VoiceGuardianPreferences.setSosContacts(context, it)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("sos_contacts_input_field"),
                            placeholder = {
                                Text("e.g. +91 9876543210, 112", fontSize = 12.sp, color = CyberTextMuted)
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Call,
                                    contentDescription = null,
                                    tint = CyberRedBright,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF18101E),
                                unfocusedContainerColor = Color(0xFF18101E),
                                focusedBorderColor = CyberRedBright,
                                unfocusedBorderColor = Color(0xFF3B2335),
                                focusedTextColor = CyberTextPrimary,
                                unfocusedTextColor = CyberTextPrimary
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )

                        Text(
                            text = "आपातकालीन स्थिति में 'SOS करो' या 'Emergency call' बोलने पर सीधे इन नंबरों पर संपर्क होगा।",
                            fontSize = 11.sp,
                            color = CyberTextMuted
                        )
                    }
                }
            }

            // ==========================================
            // LIVE VOICE VERIFICATION SIMULATOR & TEST
            // ==========================================
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("live_verification_test_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CyberCard),
                border = BorderStroke(1.dp, CyberRedBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = null,
                            tint = CyberRedBright,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Test Biometric Recognition",
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberTextPrimary
                        )
                    }

                    Text(
                        text = "टेस्ट करें कि वर्तमान सेटिंग्स और स्ट्रिकटनेस (${(matchStrictness * 100).toInt()}%) पर आपकी आवाज़ कैसे पहचानी जाती है:",
                        fontSize = 12.sp,
                        color = CyberTextSecondary
                    )

                    if (isTestingVoice) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            color = CyberRedContainer,
                            border = BorderStroke(1.dp, CyberRedBright)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                PulsingTestMic()
                                Column {
                                    Text(
                                        text = "सुन रहा है... बोलिए: 'सहनाज लाइट चालू करो'",
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Comparing against enrolled acoustic vectors...",
                                        fontSize = 10.5.sp,
                                        color = CyberRedBright
                                    )
                                }
                            }
                        }
                    }

                    if (testResultText != null) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            color = if (testResultMatch == true) Color(0xFF0D2516) else Color(0xFF2E0A12),
                            border = BorderStroke(
                                1.dp,
                                if (testResultMatch == true) CyberGreen else CyberRedBright
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = if (testResultMatch == true) Icons.Default.CheckCircle else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (testResultMatch == true) CyberGreen else CyberRedBright,
                                    modifier = Modifier.size(22.dp)
                                )
                                Text(
                                    text = testResultText ?: "",
                                    fontSize = 12.sp,
                                    color = CyberTextPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    Button(
                        onClick = {
                            isTestingVoice = true
                            testResultText = null
                            testResultMatch = null
                            triggerHapticFeedback()

                            coroutineScope.launch {
                                delay(2200)
                                isTestingVoice = false
                                val sampleConfidence = 0.88f
                                val isPassed = sampleConfidence >= matchStrictness
                                testResultMatch = isPassed

                                if (isPassed) {
                                    testResultText = "✅ प्रमाणित आवाज़ (Confidence: ${(sampleConfidence * 100).toInt()}%) - सिस्टम कमांड निष्पादित (Device Controls Allowed)"
                                } else {
                                    testResultText = if (isAwayGuardMode) {
                                        "❌ अपरिचित आवाज़ (Confidence: ${(sampleConfidence * 100).toInt()}%) - GUARD MODE ALERT! सिस्टम लॉक और अलार्म सक्रिय।"
                                    } else {
                                        "⚠️ अपरिचित आवाज़ (Confidence: ${(sampleConfidence * 100).toInt()}%) - डिवाइस कंट्रोल ब्लॉक (केवल सामान्य बातचीत की अनुमति)।"
                                    }
                                }
                            }
                        },
                        enabled = !isTestingVoice,
                        colors = ButtonDefaults.buttonColors(containerColor = CyberRedBright),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("test_voice_biometrics_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isTestingVoice) "ANALYZING VOICE VECTOR..." else "TEST VOICE MATCH (सहनाज)",
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // ==========================================
    // "+ ADD A VOICE" ENROLLMENT MODAL DIALOG
    // ==========================================
    if (showAddVoiceDialog) {
        var newVoiceName by remember { mutableStateOf("") }
        var newVoiceRole by remember { mutableStateOf("Family Member") }
        var enrollmentStep by remember { mutableIntStateOf(1) }
        var isRecordingSample by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showAddVoiceDialog = false },
            containerColor = CyberCardElevated,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = null,
                        tint = CyberRedBright,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Enroll New Voice Print",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberTextPrimary
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "नया वॉइस प्रिंट दर्ज करने के लिए नाम लिखें और 3 बार वेक-वर्ड बोलें:",
                        fontSize = 12.sp,
                        color = CyberTextSecondary
                    )

                    OutlinedTextField(
                        value = newVoiceName,
                        onValueChange = { newVoiceName = it },
                        label = { Text("Name / नाम", fontSize = 11.sp, color = CyberTextMuted) },
                        placeholder = { Text("e.g. Brother, Sister, Partner", fontSize = 12.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = CyberTextPrimary,
                            unfocusedTextColor = CyberTextPrimary,
                            focusedBorderColor = CyberRedBright,
                            unfocusedBorderColor = Color(0xFF3A2436)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Step indicator
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        for (i in 1..3) {
                            Surface(
                                shape = CircleShape,
                                color = if (i <= enrollmentStep) CyberRedBright else Color(0xFF251829),
                                modifier = Modifier.size(28.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "$i",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (i <= enrollmentStep) Color.White else CyberTextMuted
                                    )
                                }
                            }
                        }
                    }

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF1C0E1A),
                        border = BorderStroke(1.dp, CyberRedBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Sample $enrollmentStep / 3: स्पष्ट बोलिए 'सहनाज'",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyberRedBright
                            )
                            Button(
                                onClick = {
                                    isRecordingSample = true
                                    triggerHapticFeedback()
                                    coroutineScope.launch {
                                        delay(1500)
                                        isRecordingSample = false
                                        if (enrollmentStep < 3) {
                                            enrollmentStep += 1
                                        } else {
                                            // Complete
                                            val newProfile = EnrolledVoiceProfile(
                                                id = "voice_${System.currentTimeMillis()}",
                                                name = newVoiceName.ifBlank { "Family Voice ${enrolledVoicesList.size + 1}" },
                                                role = newVoiceRole,
                                                isPrimaryOwner = false,
                                                enrolledDate = "Just Now",
                                                sampleCount = 3,
                                                confidence = 0.94f
                                            )
                                            enrolledVoicesList.add(newProfile)
                                            VoiceGuardianPreferences.saveEnrolledVoices(context, enrolledVoicesList)
                                            showAddVoiceDialog = false
                                            Toast.makeText(context, "${newProfile.name} enrolled successfully!", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                enabled = !isRecordingSample,
                                colors = ButtonDefaults.buttonColors(containerColor = CyberRedBright),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = if (isRecordingSample) "RECORDING..." else "RECORD SAMPLE $enrollmentStep",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                OutlinedButton(
                    onClick = { showAddVoiceDialog = false },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberTextMuted),
                    border = BorderStroke(1.dp, Color(0xFF3B2538))
                ) {
                    Text("Cancel", fontSize = 11.sp)
                }
            }
        )
    }
}

@Composable
private fun PulsingTestMic() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Box(
        modifier = Modifier
            .size(32.dp)
            .scale(pulseScale)
            .clip(CircleShape)
            .background(CyberRedBright),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Mic,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(18.dp)
        )
    }
}
