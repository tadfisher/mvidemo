package com.tadfisher.mvidemo

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.design.widget.TextInputLayout
import android.view.View.INVISIBLE
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import butterknife.bindView
import com.jakewharton.rxbinding2.support.design.widget.error
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.view.enabled
import com.jakewharton.rxbinding2.view.visibility
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

  val snackbar: Snackbar by lazy {
    Snackbar.make(root, "Login successful", Snackbar.LENGTH_INDEFINITE)
        .setAction("Start over", {
          usernameField.text = ""
          passwordField.text = ""
          root.requestFocus()
          recreate()
        })
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.activity_login)

    // Kicks off the dialogue by subscribing to the model(intent(input)) composition.
    view(input()
        .compose(loginDialogue::intent)
        .compose(loginDialogue::model)
        .observeOn(mainThread())
        .share())
  }

  /**
   * Merge multiple streams of raw user input to a single Observable stream.
   */
  fun input(): Observable<LoginDialogue.Input> {
    return Observable.merge(
        usernameField.textChanges().map { LoginDialogue.Input.UsernameTextChange(it.toString()) },
        passwordField.textChanges().map { LoginDialogue.Input.PasswordTextChange(it.toString()) },
        loginButton.clicks().map { LoginDialogue.Input.LoginButtonClick }
    ).bindToLifecycle(this)
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
  fun view(states: Observable<LoginDialogue.State>) {
    states.map { it.usernameFieldEnabled }.subscribe(usernameField.enabled())
    states.map { it.passwordFieldEnabled }.subscribe(passwordField.enabled())
    states.map { it.loginButtonEnabled }.subscribe(loginButton.enabled())
    states.map { it.progressBarVisible }.subscribe(progressBar.visibility(INVISIBLE))
    states.map { it.errorString ?: "" }.subscribe(passwordLayout.error())
    states.map { it.completed }.subscribe(snackbar.shown())
  }
}
