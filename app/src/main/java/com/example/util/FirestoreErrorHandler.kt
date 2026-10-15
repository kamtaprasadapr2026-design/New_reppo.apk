package com.example.util

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import org.json.JSONArray
import org.json.JSONObject

fun handleFirestoreError(
    exception: Exception,
    operationType: OperationType,
    path: String? = null
): String {
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    val providerInfoList = currentUser?.providerData?.map { userInfo ->
        JSONObject().apply {
            put("providerId", userInfo.providerId)
            put("email", userInfo.email)
        }
    } ?: emptyList()

    val authInfo = JSONObject().apply {
        put("userId", currentUser?.uid)
        put("email", currentUser?.email)
        put("emailVerified", currentUser?.isEmailVerified)
        put("tenantId", currentUser?.tenantId)
        put("providerInfo", JSONArray(providerInfoList))
    }

    val json = JSONObject().apply {
        put("error", exception.message ?: exception.toString())
        put("operationType", operationType.value)
        put("path", path)
        put("authInfo", authInfo)
    }

    val jsonString = json.toString()
    Log.e("FirestoreError", "Firestore Error: $jsonString")
    return jsonString
}
