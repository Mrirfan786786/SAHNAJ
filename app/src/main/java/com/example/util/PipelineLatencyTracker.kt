package com.example.util

import android.util.Log

/**
 * Tracks and logs execution time across every stage of the voice assistant pipeline:
 * Voice / Audio -> Wake-word -> Speech-to-Text (STT) -> Intent Parser / Gemini -> Action Executor -> Text-to-Speech (TTS)
 */
class PipelineLatencyTracker {

    private var startTimestamp: Long = 0L
    private var wakeWordTimestamp: Long = 0L
    private var sttTimestamp: Long = 0L
    private var parserTimestamp: Long = 0L
    private var actionTimestamp: Long = 0L
    private var ttsTimestamp: Long = 0L

    private var currentCommand: String = ""
    private var parserType: String = ""

    fun startPipeline(initialTimestamp: Long = System.currentTimeMillis()) {
        startTimestamp = initialTimestamp
        wakeWordTimestamp = 0L
        sttTimestamp = 0L
        parserTimestamp = 0L
        actionTimestamp = 0L
        ttsTimestamp = 0L
        currentCommand = ""
        parserType = ""
    }

    fun markWakeWordDetected() {
        wakeWordTimestamp = System.currentTimeMillis()
        if (startTimestamp == 0L) startTimestamp = wakeWordTimestamp
        val latencyMs = wakeWordTimestamp - startTimestamp
        Log.d(TAG, "[PIPELINE LATENCY] Stage 1: Wake-word detected in ${latencyMs}ms")
    }

    fun markSttReceived(transcript: String) {
        sttTimestamp = System.currentTimeMillis()
        if (startTimestamp == 0L) startTimestamp = sttTimestamp
        currentCommand = transcript
        val refTime = if (wakeWordTimestamp > 0) wakeWordTimestamp else startTimestamp
        val stageDuration = sttTimestamp - refTime
        Log.d(TAG, "[PIPELINE LATENCY] Stage 2: STT Recognition complete in ${stageDuration}ms | Recognized: \"$transcript\"")
    }

    fun markParserComplete(type: String) {
        parserTimestamp = System.currentTimeMillis()
        parserType = type
        val refTime = if (sttTimestamp > 0) sttTimestamp else startTimestamp
        val stageDuration = parserTimestamp - refTime
        Log.d(TAG, "[PIPELINE LATENCY] Stage 3: Parser ($type) complete in ${stageDuration}ms")
    }

    fun markActionExecuted(actionName: String) {
        actionTimestamp = System.currentTimeMillis()
        val refTime = if (parserTimestamp > 0) parserTimestamp else startTimestamp
        val stageDuration = actionTimestamp - refTime
        Log.d(TAG, "[PIPELINE LATENCY] Stage 4: Action Executor ($actionName) executed in ${stageDuration}ms")
    }

    fun markTtsStarted() {
        ttsTimestamp = System.currentTimeMillis()
        val totalPipelineMs = ttsTimestamp - startTimestamp

        val wakeMs = if (wakeWordTimestamp > 0) wakeWordTimestamp - startTimestamp else 0
        val sttMs = if (sttTimestamp > 0) sttTimestamp - (if (wakeWordTimestamp > 0) wakeWordTimestamp else startTimestamp) else 0
        val parserMs = if (parserTimestamp > 0) parserTimestamp - sttTimestamp else 0
        val execMs = if (actionTimestamp > 0) actionTimestamp - parserTimestamp else 0
        val ttsInitMs = if (ttsTimestamp > 0 && actionTimestamp > 0) ttsTimestamp - actionTimestamp else 0

        Log.i(
            TAG,
            """
            ══════════════════════════════════════════════════════════════════
            ⚡ [PIPELINE LATENCY SUMMARY] TOTAL: ${totalPipelineMs}ms
            ├─ 1. Wake-Word Detection : ${wakeMs}ms
            ├─ 2. Speech-to-Text (STT) : ${sttMs}ms
            ├─ 3. Parsing ($parserType) : ${parserMs}ms
            ├─ 4. Action Execution    : ${execMs}ms
            └─ 5. TTS Voice Dispatch  : ${ttsInitMs}ms
            ══════════════════════════════════════════════════════════════════
            """.trimIndent()
        )
    }

    companion object {
        private const val TAG = "SAHNAJ_PERF"
    }
}
