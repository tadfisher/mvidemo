package com.tadfisher.mvidemo

import com.tadfisher.mvidemo.LoginDialogue.Input
import com.tadfisher.mvidemo.LoginDialogue.Input.*
import io.reactivex.Completable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import org.junit.Before
import org.junit.Test

class LoginDialogueTest {

  val inputs: Subject<Input> = PublishSubject.create()

  val actions: Subject<LoginDialogue.Action> = PublishSubject.create()

  lateinit var dialogue: LoginDialogue

  @Before
  fun setup() {
    dialogue = LoginDialogue(TestLoginService())
  }

  @Test
  fun mapsChangeCredentialsIntent() {
    val testObserver = dialogue.intent(inputs).test()

    inputs.onNext(UsernameTextChange(""))
    inputs.onNext(PasswordTextChange(""))
    inputs.onNext(UsernameTextChange("foo"))
    inputs.onNext(PasswordTextChange("bar"))

    testObserver.assertValues(
        changeCredentialsAction("", ""),
        changeCredentialsAction("foo", ""),
        changeCredentialsAction("foo", "bar"))
  }

  @Test
  fun mapsAttemptLoginIntent() {
    val testObserver = dialogue.intent(inputs).test()

    inputs.onNext(UsernameTextChange("foo"))
    inputs.onNext(PasswordTextChange("bar"))
    inputs.onNext(LoginButtonClick)

    inputs.onNext(UsernameTextChange("baz"))
    inputs.onNext(LoginButtonClick)

    testObserver.assertValues(
        changeCredentialsAction("foo", "bar"),
        attemptLoginAction("foo", "bar"),
        changeCredentialsAction("baz", "bar"),
        attemptLoginAction("baz", "bar"))
  }

  @Test
  fun modelsChangeCredentialsState() {
    val validState = state(usernameFieldEnabled = true, passwordFieldEnabled = true,
        loginButtonEnabled = true)
    val invalidState = validState.copy(loginButtonEnabled = false)

    val testObserver = dialogue.model(actions).test()

    actions.onNext(changeCredentialsAction("", ""))
    actions.onNext(changeCredentialsAction("foo", ""))
    actions.onNext(changeCredentialsAction("", "bar"))
    actions.onNext(changeCredentialsAction("foo", "bar"))
    actions.onNext(changeCredentialsAction("", ""))

    testObserver.assertValues(
        invalidState,
        invalidState,
        invalidState,
        validState,
        invalidState)
  }

  @Test
  fun modelsAttemptLoginState() {
    val progressState = state(progressBarVisible = true)
    val completedState = state(completed = true)
    val errorState = state(usernameFieldEnabled = true, passwordFieldEnabled = true,
        errorString = "error")

    val testObserver = dialogue.model(actions).test()

    actions.onNext(attemptLoginAction("foo", "bar"))
    actions.onNext(attemptLoginAction("hello", "bar"))
    actions.onNext(attemptLoginAction("foo", "world"))
    actions.onNext(attemptLoginAction("hello", "world"))

    testObserver.assertValues(
        progressState, errorState,
        progressState, errorState,
        progressState, errorState,
        progressState, completedState)
  }

  private fun changeCredentialsAction(username: String, password: String)
      : LoginDialogue.Action.ChangeCredentials {
    return LoginDialogue.Action.ChangeCredentials(LoginDialogue.Credentials(username, password))
  }

  private fun attemptLoginAction(username: String, password: String)
      : LoginDialogue.Action.AttemptLogin {
    return LoginDialogue.Action.AttemptLogin(LoginDialogue.Credentials(username, password))
  }

  private fun state(usernameFieldEnabled: Boolean = false,
                    passwordFieldEnabled: Boolean = false,
                    loginButtonEnabled: Boolean = false,
                    progressBarVisible: Boolean = false,
                    errorString: String? = null,
                    completed: Boolean = false): LoginDialogue.State {
    return LoginDialogue.State(
        usernameFieldEnabled,
        passwordFieldEnabled,
        loginButtonEnabled,
        progressBarVisible,
        errorString,
        completed)
  }

  private class TestLoginService : LoginService {
    override fun login(username: String, password: String): Completable {
      return Completable.defer {
        if (username == "hello" && password == "world") {
          Completable.complete()
        } else {
          Completable.error(IllegalStateException("error"))
        }
      }
    }
  }
}
