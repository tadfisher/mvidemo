package com.tadfisher.mvidemo

import com.tadfisher.mvidemo.LoginDialogue.Action
import com.tadfisher.mvidemo.LoginDialogue.State
import io.reactivex.Observable
import io.reactivex.Observable.just
import rx.lang.kotlin.combineLatest
import rx.lang.kotlin.merge
import rx.lang.kotlin.ofType
import rx.lang.kotlin.withLatestFrom

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

  /** The raw user inputs to this dialogue. */
  sealed class Input {
    data class UsernameTextChange(val text: String) : Input()
    data class PasswordTextChange(val text: String) : Input()
    object LoginButtonClick : Input()
  }

  /** The available actions our user can take. */
  sealed class Action {
    /** The user has changed the view's username or password values. */
    data class ChangeCredentials(val credentials: Credentials) : Action()

    /** The user is attempting to log in. */
    data class AttemptLogin(val credentials: Credentials) : Action()
  }

  /** The mutable state of our view. */
  data class State(
      val usernameFieldEnabled: Boolean = false,
      val passwordFieldEnabled: Boolean = false,
      val loginButtonEnabled: Boolean = false,
      val progressBarVisible: Boolean = false,
      val errorString: String? = null,
      val completed: Boolean = false
  )

  /** Map user input events to Actions. */
  fun intent(inputs: Observable<Input>): Observable<Action> {
    // Combine credentials fields
    val credentialsEdits = listOf(
        inputs.ofType<Input.UsernameTextChange>().map { it.text },
        inputs.ofType<Input.PasswordTextChange>().map { it.text }
    ).combineLatest { (username, password) -> Credentials(username, password) }
        // We don't want to duplicate this work for each subscriber
        .share()

    return listOf(
        // Convert credentials changes to action
        credentialsEdits.map { Action.ChangeCredentials(it) },

        // Convert login click to action
        inputs.ofType<Input.LoginButtonClick>().withLatestFrom(credentialsEdits,
            { _, credentials -> Action.AttemptLogin(credentials) })
    ).merge()
  }

  /** Map Actions to view state. */
  fun model(actions: Observable<Action>): Observable<State> {
    return actions.flatMap {
      when (it) {
        is Action.ChangeCredentials ->
          // Toggle login button if credentials are populated
          just(State(
              usernameFieldEnabled = true,
              passwordFieldEnabled = true,
              loginButtonEnabled = it.credentials.isValid))

        is Action.AttemptLogin ->
          // Attempt a login, streaming view state in response to network events
          service.login(it.credentials.username, it.credentials.password)
              .andThen(just(State(completed = true)))
              .startWith(just(State(progressBarVisible = true)))
              .onErrorReturn { State(
                  usernameFieldEnabled = true,
                  passwordFieldEnabled = true,
                  errorString = it.message) }
      }
    }
  }
}
