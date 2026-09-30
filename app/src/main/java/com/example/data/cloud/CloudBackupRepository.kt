package com.example.data.cloud

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.example.R
import com.example.data.model.InstituteSettings
import com.google.android.gms.tasks.Task
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

private const val TAG = "CloudBackupRepo"

class CloudBackupRepository(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("cloud_backup_prefs", Context.MODE_PRIVATE)

    // Mandatory custom database ID per platform instructions
    private val databaseId: String by lazy {
        context.getString(R.string.firestore_database_id)
    }

    val db: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance(databaseId)
    }

    val auth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    private val credentialManager = CredentialManager.create(context)

    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    fun observeAuthState(): Flow<CloudUserState> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                trySend(
                    CloudUserState(
                        isAuthenticated = true,
                        userId = user.uid,
                        email = user.email,
                        displayName = user.displayName,
                        photoUrl = user.photoUrl?.toString()
                    )
                )
            } else {
                trySend(CloudUserState(isAuthenticated = false))
            }
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    suspend fun signInWithGoogle(): Result<FirebaseUser> = withContext(Dispatchers.IO) {
        try {
            val serverClientId = context.getString(R.string.default_web_client_id)
            val signInWithGoogleOption = GetSignInWithGoogleOption.Builder(serverClientId)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(signInWithGoogleOption)
                .build()

            val response = credentialManager.getCredential(
                context = context,
                request = request
            )

            val credential = response.credential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val authCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
                val authResult = auth.signInWithCredential(authCredential).awaitResult()
                val user = authResult.user ?: throw IllegalStateException("Firebase User is null after sign in")
                Result.success(user)
            } else {
                Result.failure(IllegalStateException("Unexpected credential format received"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Google Sign-In failed", e)
            Result.failure(e)
        }
    }

    fun signOut() {
        auth.signOut()
    }

    suspend fun uploadCloudBackup(
        instituteName: String,
        session: String,
        totalStudents: Int,
        totalPayments: Int,
        dataPayload: String
    ): Result<CloudBackupRecord> = withContext(Dispatchers.IO) {
        try {
            val user = auth.currentUser ?: throw IllegalStateException("Please sign in to Google to create a protected cloud backup.")
            val backupId = "backup_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}_${UUID.randomUUID().toString().take(6)}"
            val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

            val record = CloudBackupRecord(
                backupId = backupId,
                userId = user.uid,
                instituteName = instituteName,
                session = session,
                timestamp = timestamp,
                totalStudents = totalStudents,
                totalPayments = totalPayments,
                dataPayload = dataPayload
            )

            val map = hashMapOf(
                "backupId" to record.backupId,
                "userId" to record.userId,
                "instituteName" to record.instituteName,
                "session" to record.session,
                "timestamp" to record.timestamp,
                "totalStudents" to record.totalStudents,
                "totalPayments" to record.totalPayments,
                "dataPayload" to record.dataPayload
            )

            // Protected path matching zero-trust firestore rules: /users/{userId}/backups/{backupId}
            db.collection("users")
                .document(user.uid)
                .collection("backups")
                .document(backupId)
                .set(map)
                .awaitResult()

            // Update user profile summary on cloud
            val profileMap = hashMapOf(
                "userId" to user.uid,
                "name" to instituteName,
                "session" to session,
                "lastBackupId" to backupId,
                "lastBackupTimestamp" to timestamp,
                "totalStudents" to totalStudents,
                "totalPayments" to totalPayments
            )

            db.collection("users")
                .document(user.uid)
                .collection("profile")
                .document("settings")
                .set(profileMap)
                .awaitResult()

            prefs.edit()
                .putString("last_cloud_sync_time", timestamp)
                .putString("last_cloud_backup_id", backupId)
                .putInt("last_cloud_students_count", totalStudents)
                .apply()

            Result.success(record)
        } catch (e: Exception) {
            Log.e(TAG, "Cloud backup upload failed", e)
            Result.failure(e)
        }
    }

    fun observeCloudBackups(): Flow<List<CloudBackupRecord>> = callbackFlow {
        val user = auth.currentUser
        if (user == null) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        val registration = db.collection("users")
            .document(user.uid)
            .collection("backups")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(20)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Listen to cloud backups failed", error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        CloudBackupRecord(
                            backupId = doc.getString("backupId") ?: doc.id,
                            userId = doc.getString("userId") ?: "",
                            instituteName = doc.getString("instituteName") ?: "",
                            session = doc.getString("session") ?: "",
                            timestamp = doc.getString("timestamp") ?: "",
                            totalStudents = doc.getLong("totalStudents")?.toInt() ?: 0,
                            totalPayments = doc.getLong("totalPayments")?.toInt() ?: 0,
                            dataPayload = doc.getString("dataPayload") ?: ""
                        )
                    }
                    trySend(list)
                }
            }

        awaitClose { registration.remove() }
    }

    suspend fun deleteCloudBackup(backupId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val user = auth.currentUser ?: throw IllegalStateException("Not authenticated")
            db.collection("users")
                .document(user.uid)
                .collection("backups")
                .document(backupId)
                .delete()
                .awaitResult()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getLastCloudSyncTime(): String? = prefs.getString("last_cloud_sync_time", null)

    private suspend fun <T> Task<T>.awaitResult(): T = suspendCancellableCoroutine { continuation ->
        addOnSuccessListener { result ->
            if (continuation.isActive) continuation.resume(result)
        }
        addOnFailureListener { exception ->
            if (continuation.isActive) continuation.resumeWithException(exception)
        }
    }
}
