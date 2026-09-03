package com.example.data.model

enum class ActionType {
    OPEN_APP,
    CALL_CONTACT,
    DIAL_NUMBER,
    MAKE_CALL,
    SEND_SMS,
    SEND_WHATSAPP_MESSAGE,
    WHATSAPP_MESSAGE,
    SEND_WHATSAPP,
    YOUTUBE_SEARCH,
    PLAY_YOUTUBE,
    WEB_SEARCH,
    FIND_AND_TAP,
    FIND_AND_TYPE,
    OPEN_SETTINGS,
    DEVICE_SETTING,
    SET_ALARM,
    SET_REMINDER,
    GO_HOME,
    GO_BACK,
    OPEN_RECENTS,
    SCROLL_UP,
    SCROLL_DOWN,
    SWIPE_LEFT,
    SWIPE_RIGHT,
    TAP_TEXT,
    LONG_PRESS,
    TYPE_TEXT,
    OPEN_NOTIFICATION_PANEL,
    OPEN_QUICK_SETTINGS,
    STOP_ACTION,
    ASK_CONFIRMATION,
    SPEAK_TEXT,
    GENERAL_QUESTION,
    GENERAL_QNA,
    OPEN_PAYWALL,
    SUBSCRIPTION_QUERY,
    SYSTEM_DIAGNOSTICS,
    MORNING_BRIEFING,
    NIGHT_ROUTINE,
    READ_SCREEN_TEXT,
    AUTOMOTIVE_DIAGNOSTICS,
    ANSWER_CALL,
    REJECT_CALL,
    EMERGENCY_SOS,
    FAKE_SHUTDOWN,
    INSTALL_APP,
    UNKNOWN;

    companion object {
        fun fromString(value: String?): ActionType {
            if (value.isNullOrBlank()) return UNKNOWN
            val clean = value.trim().uppercase()
            return when (clean) {
                "INSTALL_APP", "INSTALL", "DOWNLOAD_APP", "DOWNLOAD", "PLAY_STORE_INSTALL", "APP_STORE_DOWNLOAD" -> INSTALL_APP
                "AUTOMOTIVE_DIAGNOSTICS", "CAR_DIAGNOSTICS", "VEHICLE_DIAGNOSTICS", "OBD_SCAN", "DTC_LOOKUP" -> AUTOMOTIVE_DIAGNOSTICS
                "ANSWER_CALL", "ACCEPT_CALL", "PICK_CALL", "RECEIVE_CALL" -> ANSWER_CALL
                "REJECT_CALL", "DECLINE_CALL", "CUT_CALL", "END_CALL" -> REJECT_CALL
                "EMERGENCY_SOS", "SOS", "EMERGENCY", "HELP_SOS" -> EMERGENCY_SOS
                "FAKE_SHUTDOWN", "STEALTH_SHUTDOWN", "DECOY_SHUTDOWN" -> FAKE_SHUTDOWN
                "READ_SCREEN_TEXT", "READ_SCREEN", "INSPECT_SCREEN", "SCREEN_READER", "READ_TEXT" -> READ_SCREEN_TEXT
                "SYSTEM_DIAGNOSTICS", "DIAGNOSTICS", "SYSTEM_STATUS", "STATUS_CHECK" -> SYSTEM_DIAGNOSTICS
                "MORNING_BRIEFING", "DAILY_BRIEFING", "BRIEFING" -> MORNING_BRIEFING
                "NIGHT_ROUTINE", "BEDTIME_ROUTINE", "DND_ROUTINE" -> NIGHT_ROUTINE
                "OPEN_PAYWALL", "SUBSCRIPTION_QUERY", "PAYWALL", "SUBSCRIPTION", "PREMIUM", "VIP_PLANS" -> OPEN_PAYWALL
                "SEND_WHATSAPP_MESSAGE", "WHATSAPP_MESSAGE", "SEND_WHATSAPP", "WHATSAPP" -> SEND_WHATSAPP
                "MAKE_CALL", "CALL", "CALL_CONTACT", "DIAL", "DIAL_NUMBER" -> MAKE_CALL
                "PLAY_YOUTUBE", "YOUTUBE_SEARCH", "YOUTUBE_PLAY", "PLAY_SONG" -> PLAY_YOUTUBE
                "DEVICE_SETTING", "TOGGLE_SETTING", "SETTINGS", "OPEN_SETTINGS" -> DEVICE_SETTING
                "SET_ALARM", "ALARM" -> SET_ALARM
                "SET_REMINDER", "REMINDER" -> SET_REMINDER
                "GENERAL_QNA", "GENERAL_QUESTION", "QNA", "CHAT" -> GENERAL_QNA
                "OPEN_APP", "LAUNCH_APP" -> OPEN_APP
                "SEND_SMS", "SMS" -> SEND_SMS
                "WEB_SEARCH" -> WEB_SEARCH
                "FIND_AND_TAP", "TAP_TEXT" -> FIND_AND_TAP
                "FIND_AND_TYPE", "TYPE_TEXT" -> FIND_AND_TYPE
                "GO_HOME" -> GO_HOME
                "GO_BACK" -> GO_BACK
                "OPEN_RECENTS" -> OPEN_RECENTS
                "SCROLL_UP" -> SCROLL_UP
                "SCROLL_DOWN" -> SCROLL_DOWN
                "SWIPE_LEFT" -> SWIPE_LEFT
                "SWIPE_RIGHT" -> SWIPE_RIGHT
                "LONG_PRESS" -> LONG_PRESS
                "OPEN_NOTIFICATION_PANEL" -> OPEN_NOTIFICATION_PANEL
                "OPEN_QUICK_SETTINGS" -> OPEN_QUICK_SETTINGS
                "STOP_ACTION" -> STOP_ACTION
                "ASK_CONFIRMATION" -> ASK_CONFIRMATION
                "SPEAK_TEXT" -> SPEAK_TEXT
                else -> {
                    try {
                        valueOf(clean)
                    } catch (e: Exception) {
                        UNKNOWN
                    }
                }
            }
        }
    }
}
