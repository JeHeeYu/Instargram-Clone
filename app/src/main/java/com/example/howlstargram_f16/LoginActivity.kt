package com.example.howlstargram_f16

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    var auth: FirebaseAuth? = null

    // Google 로그인 상태 변수
    var googleSignInClient : GoogleSignInClient? = null

    // Google 로그인 시 사용할 Request Code
    var GOOGLE_LOGIN_CODE = 9001

    // 로그인이 성공했을 경우 메인 화면으로 이동
    fun moveMainPage(user: FirebaseUser?) {
        if(user != null) {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    // 로그인할 경우
    fun signinEamil() {
        auth?.signInWithEmailAndPassword(email_edittext.text.toString(), password_edittext.text.toString())?.addOnCompleteListener {
                task ->
            // ID와 Password가 다 맞았을 경우
            if(task.isSuccessful) {
                moveMainPage(task.result?.user)
            }

            // ID or Password가 틀렸을 경우
            else {
                Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    fun signinAndSignup() {
        auth?.createUserWithEmailAndPassword(
            email_edittext.text.toString(),
            password_edittext.text.toString()
        )?.addOnCompleteListener { task ->

            // ID가 생성되었을 경우
            if (task.isSuccessful) {
                moveMainPage(task.result?.user)
            }
            // ID 생성 오류 발생했을 경우
            else if (task.exception?.message.isNullOrEmpty()) {
                Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
            }
            // 회원가입 및 오류가 아닐 경우
            else {
                signinEamil()
            }
        }
    }

    // Google 로그인
    fun googleLogin() {
        var singInIntent = googleSignInClient?.signInIntent
        startActivityForResult(singInIntent, GOOGLE_LOGIN_CODE)
    }

    fun firebaseAuthWithGoogle(account : GoogleSignInAccount) {
        var credential = GoogleAuthProvider.getCredential(account?.idToken, null)
        auth?.signInWithCredential(credential)
            ?.addOnCompleteListener { task ->
                // ID가 생성되었을 경우
                if (task.isSuccessful) {
                    moveMainPage(task.result?.user)
                }
                // ID 생성 오류 발생했을 경우
                else if (task.exception?.message.isNullOrEmpty()) {
                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
                }
                // 회원가입 및 오류가 아닐 경우
                else {
                    signinEamil()
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == GOOGLE_LOGIN_CODE) {
            var result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            if(result.isSuccess) {
                var account = result.signInAccount

                // 두 번째 단계
                firebaseAuthWithGoogle(account!!)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        auth = FirebaseAuth.getInstance()
        email_login_button.setOnClickListener {
            signinAndSignup()
        }

        google_sign_in_button.setOnClickListener {
            // 처음 단계
            googleLogin()
        }

        var  gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }
}