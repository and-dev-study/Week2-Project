package com.androidhuman.example.simplegithub.ui.signin

import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.customtabs.CustomTabsIntent
import android.view.View
import com.androidhuman.example.simplegithub.BuildConfig
import com.androidhuman.example.simplegithub.R
import com.androidhuman.example.simplegithub.util.extensions.plusAssign
import com.androidhuman.example.simplegithub.util.rx.AutoClearedDisposable
import com.androidhuman.example.simplegithub.ui.main.MainActivity
import dagger.android.support.DaggerAppCompatActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_sign_in.*
import org.jetbrains.anko.clearTask
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.longToast
import org.jetbrains.anko.newTask
import javax.inject.Inject

// 실행
// 1. 객체 주입을 위해 상속을 DaggerAppCompatActivity 로 변경
// 2. AuthApi 와 AuthTokenPreference 를 대거를 통해 주입받도록 별도의 프로퍼티 선언
// 3. 생성한 프로퍼티를 뷰모델 팩토리 생성자의 인자로 전달
// 4. 뷰모델 펙토리에 직접 inject 받도록 다시 리펙토링

class SignInActivity : DaggerAppCompatActivity() { // 1. DaggerAppCompatActivity 변경

    internal val disposables = AutoClearedDisposable(this)

    internal val viewDisposables
            = AutoClearedDisposable(lifecycleOwner = this, alwaysClearOnStop = false)

    // 3. 프로퍼티를 inject 한 인자로 변경
     @Inject lateinit var viewModelFactory : SignInViewModelFactory

    lateinit var viewModel: SignInViewModel
//
//    // 2. 별도의 프로퍼티 선언
//    @Inject lateinit var authApi: AuthApi
//
//    @Inject lateinit var tokenProvider: AuthTokenPreference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        viewModel = ViewModelProviders.of(
                this, viewModelFactory)[SignInViewModel::class.java]

        lifecycle += disposables
        lifecycle += viewDisposables

        btnActivitySignInStart.setOnClickListener {
            val authUri = Uri.Builder().scheme("https").authority("github.com")
                    .appendPath("login")
                    .appendPath("oauth")
                    .appendPath("authorize")
                    .appendQueryParameter("client_id", BuildConfig.GITHUB_CLIENT_ID)
                    .build()

            val intent = CustomTabsIntent.Builder().build()
            intent.launchUrl(this@SignInActivity, authUri)
        }

        viewDisposables += viewModel.accessToken
                .filter { !it.isEmpty }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { launchMainActivity() }

        viewDisposables += viewModel.message
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { message -> showError(message) }

        viewDisposables += viewModel.isLoading
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { isLoading ->
                    if (isLoading) {
                        showProgress()
                    } else {
                        hideProgress()
                    }
                }

        disposables += viewModel.loadAccessToken()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        showProgress()

        val code = intent.data?.getQueryParameter("code")
                ?: throw IllegalStateException("No code exists")

        getAccessToken(code)
    }

    private fun getAccessToken(code: String) {
        disposables += viewModel.requestAccessToken(
                BuildConfig.GITHUB_CLIENT_ID, BuildConfig.GITHUB_CLIENT_SECRET, code)
    }

    private fun showProgress() {
        btnActivitySignInStart.visibility = View.GONE
        pbActivitySignIn.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        btnActivitySignInStart.visibility = View.VISIBLE
        pbActivitySignIn.visibility = View.GONE
    }

    private fun showError(message: String) {
        longToast(message)
    }

    private fun launchMainActivity() {
        startActivity(intentFor<MainActivity>().clearTask().newTask())
    }
}
