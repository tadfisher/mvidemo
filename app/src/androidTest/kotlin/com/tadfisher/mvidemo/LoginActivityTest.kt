package com.tadfisher.mvidemo

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.*
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import org.hamcrest.Matchers.isEmptyOrNullString
import org.hamcrest.Matchers.not
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginActivityTest {

  @Rule @JvmField
  val activity = ActivityTestRule<LoginActivity>(LoginActivity::class.java)

  @Rule @JvmField
  val rx = RxIdlingResource.TestRule()

  @Test fun credentialChangeTogglesLoginButton() {
    onView(withId(R.id.login_button)).check(matches(not(isEnabled())))

    onView(withId(R.id.username)).perform(typeText("foo"))
    onView(withId(R.id.login_button)).check(matches(not(isEnabled())))

    onView(withId(R.id.password)).perform(typeText("bar"))
    onView(withId(R.id.login_button)).check(matches(isEnabled()))

    onView(withId(R.id.username)).perform(clearText())
    onView(withId(R.id.login_button)).check(matches(not(isEnabled())))
  }

  @Test fun loginFailsWithError() {
    onView(withId(R.id.username)).perform(typeText("foo"))
    onView(withId(R.id.password)).perform(typeText("bar"))
    closeSoftKeyboard()
    onView(withId(R.id.login_button)).perform(click())
    onView(withId(R.id.password_layout))
        .check(matches(hasTextInputLayoutErrorText(not(isEmptyOrNullString()))))
  }

  @Test fun loginSucceedsWithSnackbar() {
    onView(withId(R.id.username)).perform(typeText("hello"))
    onView(withId(R.id.password)).perform(typeText("world"))
    closeSoftKeyboard()
    onView(withId(R.id.login_button)).perform(click())
    onView(withId(R.id.password_layout))
        .check(matches(hasTextInputLayoutErrorText(isEmptyOrNullString())))
    onView(withId(android.support.design.R.id.snackbar_text)).check(matches(isDisplayed()))
  }
}
