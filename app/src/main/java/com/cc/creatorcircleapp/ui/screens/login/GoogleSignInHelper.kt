package com.cc.creatorcircleapp.ui.screens.login

import android.content.Context
import android.util.Log
import com.cc.creatorcircleapp.utils.Constants
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task

fun getGoogleSignInClient(context: Context): GoogleSignInClient {
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(Constants.GOOGLE_CLIENT_ID)
        .requestEmail()
        .build()
    return GoogleSignIn.getClient(context, gso)
}

fun handleGoogleSignInResult(task: Task<GoogleSignInAccount>, onSuccess: (String?) -> Unit, onError: (String) -> Unit) {
    try {
        val account = task.getResult(ApiException::class.java)
        val idToken = account?.idToken
        onSuccess(idToken)
    } catch (e: ApiException) {
        Log.e("GoogleSignIn", "Sign in failed: ${e.statusCode}")
        onError("Google sign-in failed: ${e.statusCode}")
    }
}
