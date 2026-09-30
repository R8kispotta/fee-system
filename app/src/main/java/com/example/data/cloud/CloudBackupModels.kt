package com.example.data.cloud

data class CloudBackupRecord(
    val backupId: String = "",
    val userId: String = "",
    val instituteName: String = "",
    val session: String = "",
    val timestamp: String = "",
    val totalStudents: Int = 0,
    val totalPayments: Int = 0,
    val dataPayload: String = ""
)

sealed interface CloudSyncState {
    object Idle : CloudSyncState
    data class InProgress(val message: String) : CloudSyncState
    data class Success(val message: String) : CloudSyncState
    data class Error(val errorMessage: String) : CloudSyncState
}

data class CloudUserState(
    val isAuthenticated: Boolean = false,
    val userId: String? = null,
    val email: String? = null,
    val displayName: String? = null,
    val photoUrl: String? = null
)
