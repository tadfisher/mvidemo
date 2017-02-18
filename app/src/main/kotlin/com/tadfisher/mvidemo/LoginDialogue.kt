package com.tadfisher.mvidemo

import com.tadfisher.mvidemo.LoginDialogue.Action
import com.tadfisher.mvidemo.LoginDialogue.State
import io.reactivex.Observable
import io.reactivex.Observable.combineLatest
import io.reactivex.Observable.just
import io.reactivex.functions.BiFunction

/**
 * Describes the behavior of user interactions with this application.
 *
 * We model this interaction as a round trip of input -> intent -> model -> view functions, where
 * the same UI component implements both "input" and "view".
 *
 * The [intent] function maps streams of raw input events to a stream of [actions][Action] which
 * represent the user's intent. Note that any combination of these input events can result in any
 * number of actions, which is why disambiguating these combinations explicitly is useful for
 * modeling and testing.
 *
 * The [model] function maps a stream of actions to a stream of [view states][State]. These
 * states should describe all of the moving parts of the view; for example, each widget that is
 * shown/hidden or enabled/disabled. Additionally, we add a terminal event to signal completion
 * of this interaction to the UI.
 *
 * Note that we do not implement the "view" function here. Stateless modeling of the Android view
 * hierarchy is left for a future exercise.
 */
class LoginDialogue constructor(val service: LoginService) {

  /** Convenience class to combine credential values. */
  data class Credentials(val username: String, val password: String) {
    val isValid = username.isNotEmpty() && password.isNotEmpty()
  }

  /** The available actions our user can take. */
  sealed class Action {

    /** The user has changed the view's username or password values. */
    data class ChangeCredentials(val credentials: Credentials) : Action()

    /** The user is attempting to log in. */
    data class AttemptLogin(val credentials: Credentials) : Action()
  }

  /** The complete state of our view. */
  data class State(
      val usernameFieldEnabled: Boolean = false,
      val passwordFieldEnabled: Boolean = false,
      val loginButtonEnabled: Boolean = false,
      val progressBarVisible: Boolean = false,
      val errorString: String? = null,
      val completed: Boolean = false
  )

  /** Map user input events to Actions. */
  fun intent(usernameTextChanges: Observable<CharSequence>,
             passwordTextChanges: Observable<CharSequence>,
             loginButtonClicks: Observable<Any>): Observable<Action> {

    // Combine credentials fields
    val credentialsEdits = combineLatest(usernameTextChanges, passwordTextChanges,
        BiFunction { u: CharSequence, p: CharSequence -> Credentials(u.toString(), p.toString()) })

    // Convert credentials changes to action
    val changeCredentialsActions = credentialsEdits.map { Action.ChangeCredentials(it) }

    // Convert login click to action
    val attemptLoginActions = loginButtonClicks.withLatestFrom(credentialsEdits,
        BiFunction { _: Any, c: Credentials -> Action.AttemptLogin(c) })

    return Observable.merge(changeCredentialsActions, attemptLoginActions)
  }

  /** Map Actions to view state. */
  fun model(actions: Observable<Action>): Observable<State> {
    return actions.flatMap {
      when (it) {

      // Toggle login button if credentials are populated
        is Action.ChangeCredentials ->
          if (it.credentials.isValid) {
            just(State(usernameFieldEnabled = true, passwordFieldEnabled = true,
                loginButtonEnabled = true))
          } else {
            just(State(usernameFieldEnabled = true, passwordFieldEnabled = true,
                loginButtonEnabled = false))
          }

      // Attempt a login, streaming view state in response to network events
        is Action.AttemptLogin ->
          service.login(it.credentials.username, it.credentials.password)
              .andThen(just(State(completed = true)))
              .startWith(just(State(progressBarVisible = true)))
              .onErrorReturn { State(usernameFieldEnabled = true, passwordFieldEnabled = true,
                  errorString = it.message) }
      }
    }
  }
}
