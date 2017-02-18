package com.tadfisher.mvidemo

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.design.widget.TextInputLayout
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import butterknife.bindView
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.widget.textChanges
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity
import com.trello.rxlifecycle2.kotlin.bindToLifecycle
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread

/**
 * Driver UI for the login dialogue.
 *
 * The UI is responsible for two things: producing input events and consuming state changes.
 *
 * We model input events as streams of events produced by RxBinding.
 *
 * We apply state changes by subscribing to the result of the view(model(intent)) function
 * composition. The [view] function is implemented here for convenience, and to decouple
 * [LoginDialogue] from the specifics of this UI.
 */
class LoginActivity : RxAppCompatActivity() {

  val loginDialogue = LoginDialogue(FakeLoginService())

  val root: ViewGroup by bindView(R.id.root)
  val usernameField: TextView by bindView(R.id.username)
  val passwordField: TextView by bindView(R.id.password)
  val passwordLayout: TextInputLayout by bindView(R.id.password_layout)
  val loginButton: Button by bindView(R.id.login_button)
  val progressBar: ProgressBar by bindView(R.id.login_progress)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.activity_login)

    // Kicks off the dialogue by emitting input events to the view(model(intent)) composition.
    view(
        loginDialogue.model(
            loginDialogue.intent(
                usernameTextChanges = usernameField.textChanges(),
                passwordTextChanges = passwordField.textChanges(),
                loginButtonClicks = loginButton.clicks())))
  }

  /**
   * The "view" in view(model(intent)). This function consumes the stream of states produced by
   * [LoginDialogue.model] and applies those states to the UI.
   *
   * Technically, updating the UI is a side-effect, since the UI maintains state independently of
   * the dialogue. A purely functional approach would model the entire view hierarchy in
   * [LoginDialogue], and the UI would apply that state to the rendered view tree. In practice,
   * there isn't a simple intermediate representation for Android views (unlike an HTML DOM), so
   * we can make do by carefully modeling state.
   */
  private fun view(state: Observable<LoginDialogue.State>) {
    state.observeOn(mainThread())
        .bindToLifecycle(this)
        .subscribe { state ->
          usernameField.isEnabled = state.usernameFieldEnabled
          passwordField.isEnabled = state.passwordFieldEnabled
          loginButton.isEnabled = state.loginButtonEnabled
          progressBar.visibility = if (state.progressBarVisible) VISIBLE else INVISIBLE
          passwordLayout.error = state.errorString

          if (state.completed) {
            Snackbar.make(root, "Login successful", Snackbar.LENGTH_INDEFINITE)
                .setAction("Start over", { recreate() })
                .show()
          }
        }
  }
}
