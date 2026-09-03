package com.example.data.model

enum class ResultStatus {
    SUCCESS,
    FAILURE,
    CANCELLED,
    REQUIRES_CONFIRMATION,
    NOT_SUPPORTED
}

data class ExecutionResult(
    val status: ResultStatus,
    val spokenResponse: String,
    val detail: String? = null,
    val pendingAction: StructuredAction? = null
) {
    val isSuccess: Boolean get() = status == ResultStatus.SUCCESS
}
