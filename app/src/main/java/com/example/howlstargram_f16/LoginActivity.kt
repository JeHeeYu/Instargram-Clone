package com.example.howlstargram_f16

import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.facebook.*
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_login.*
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import com.facebook.appevents.AppEventsLogger
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.firebase.auth.FacebookAuthProvider
import java.util.*


class LoginActivity : AppCompatActivity() {
    var auth: FirebaseAuth? = null

    // Google 로그인 상태 변수
    var googleSignInClient : GoogleSignInClient? = null

    // Google 로그인 시 사용할 Request Code
    var GOOGLE_LOGIN_CODE = 9001

    // 페이스북 로그인 결과 가져올 콜백
    var callbackManager : CallbackManager? = null

    // 로그인이 성공했을 경우 메인 화면으로 이동
    fun moveMainPage(user: FirebaseUser?) {
        if(user != null) {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    // Facebook ID Login을 위한 Hashkey 출력 함수
    fun printHashKey() {
        try {
            val info = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
            for (signature in info.signatures) {
                val md: MessageDigest = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                val hashKey: String = String(Base64.encode(md.digest(), 0))
                Log.i("TAG", "printHashKey() Hash Key: $hashKey")
            }
        } catch (e: NoSuchAlgorithmException) {
            Log.e("TAG", "printHashKey()", e)
        } catch (e: Exception) {
            Log.e("TAG", "printHashKey()", e)
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

    // Facebook 로그인 성공 시 Firebase로 데이터 전송
    fun handleFacebookAccessToken(token : AccessToken?) {
        var credential = FacebookAuthProvider.getCredential(token?.token!!)
        auth?.signInWithCredential(credential)
            ?.addOnCompleteListener { task ->
                // 로그인 성공 시 메인 페이지로 이동
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

    fun facebookLogin() {
        LoginManager.getInstance()
            .logInWithReadPermissions(this, Arrays.asList("public_profile", "email"))

        LoginManager.getInstance()
            .registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
                // Facebook 로그인 성공 시
                override fun onCancel() {

                }

                override fun onError(error: FacebookException) {

                }

                // Facebook 로그인 성공 시
                override fun onSuccess(result: LoginResult) {
                    handleFacebookAccessToken(result?.accessToken)
                }
            })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        callbackManager?.onActivityResult(requestCode, resultCode, data)

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

        // Google 로그인 버튼 클릭 시
        google_sign_in_button.setOnClickListener {
            // 처음 단계
            googleLogin()
        }

        // Facebook 로그인 버튼 클릭 시
        facebook_login_button.setOnClickListener {
            facebookLogin()
        }

        var gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Hasykey 출력
        //printHashKey()

        callbackManager = CallbackManager.Factory.create()
    }
}