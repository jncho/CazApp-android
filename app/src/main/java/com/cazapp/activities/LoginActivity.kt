package com.cazapp.activities

import android.app.Activity
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cazapp.R
import kotlinx.android.synthetic.main.activity_login.*
import android.content.Intent
import android.opengl.Visibility
import com.google.android.material.snackbar.Snackbar
import android.util.Log
import android.view.View
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.mongodb.stitch.android.core.Stitch
import com.mongodb.stitch.core.auth.providers.anonymous.AnonymousCredential
import com.mongodb.stitch.core.auth.providers.google.GoogleCredential
import java.lang.IllegalArgumentException


class LoginActivity : AppCompatActivity() {

    companion object {
        const val RC_SIGN_IN = 433
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        /*for (user in Stitch.getDefaultAppClient().auth.listUsers()){
            Stitch.getDefaultAppClient().auth.removeUserWithId(user.id)
        }*/

        if (Stitch.getDefaultAppClient().auth.isLoggedIn) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        card_google.setOnClickListener {
            loading_login.visibility = View.VISIBLE
            frame_loading_login.visibility = View.VISIBLE
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestServerAuthCode(getString(R.string.server_client_id))
                .build()
            val signInIntent = GoogleSignIn.getClient(this, gso).signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }

        skip.setOnClickListener {
            loading_login.visibility = View.VISIBLE
            frame_loading_login.visibility = View.VISIBLE
            val anonUser =
                Stitch.getDefaultAppClient().auth.listUsers().filter { it.loggedInProviderName == "anon-user" }
            if (!anonUser.isEmpty()) {
                try {
                    if (!anonUser[0].isLoggedIn){
                        Stitch.getDefaultAppClient().auth.removeUserWithId(anonUser[0].id)
                        loginNewAnonymousUser()
                    }else {
                        Stitch.getDefaultAppClient().auth.switchToUserWithId(anonUser[0].id)
                        loading_login.visibility = View.GONE
                        frame_loading_login.visibility = View.GONE
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                    }
                }catch (e:IllegalArgumentException){
                    loginNewAnonymousUser()
                }

            }else{
                loginNewAnonymousUser()
            }

        }
    }

    private fun loginNewAnonymousUser(){
        Stitch.getDefaultAppClient().auth.loginWithCredential(AnonymousCredential()).addOnCompleteListener {
            Log.d("Cazapp", "Login with anonymous user")
            loading_login.visibility = View.GONE
            frame_loading_login.visibility = View.GONE
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        loading_login.visibility = View.GONE
        frame_loading_login.visibility = View.GONE
        if (resultCode == Activity.RESULT_CANCELED) {
            Log.e("Cazapp", "Error logging in with Google")
            Snackbar.make(layout_login, "Login error. Maybe you don't have internet access?", Snackbar.LENGTH_LONG)
                .show()
            return
        }
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleGoogleSignInResult(task)
            return
        }
    }

    private fun handleGoogleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            val googleCredential = GoogleCredential(account!!.serverAuthCode)

            loading_login.visibility = View.VISIBLE
            frame_loading_login.visibility = View.VISIBLE

            Stitch.getDefaultAppClient().auth.loginWithCredential(googleCredential).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    loading_login.visibility = View.GONE
                    frame_loading_login.visibility = View.GONE
                    Log.d("Cazapp", "Login with user: ${task.result!!.profile.name}")
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)

                } else {
                    Log.e("Cazapp", "Error logging in with Google", task.exception)
                    Snackbar.make(
                        layout_login,
                        "Login error. Maybe you don't have internet access?",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }
        } catch (e: ApiException) {
            Log.w("Cazapp", "signInResult:failed code=" + e.statusCode)
        }

    }

}