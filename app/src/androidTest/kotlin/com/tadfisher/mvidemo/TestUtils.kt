package com.tadfisher.mvidemo

import android.support.design.widget.TextInputLayout
import android.support.test.espresso.matcher.BoundedMatcher
import android.view.View
import android.view.View.VISIBLE
import org.hamcrest.Description
import org.hamcrest.Matcher

fun hasTextInputLayoutErrorText(stringMatcher: Matcher<String>): Matcher<View> {
  checkNotNull(stringMatcher)
  return object : BoundedMatcher<View, TextInputLayout>(TextInputLayout::class.java) {

    override fun describeTo(description: Description) {
      description.appendText("with error: ")
      stringMatcher.describeTo(description)
    }

    public override fun matchesSafely(textInputLayout: TextInputLayout): Boolean {
      return stringMatcher.matches(
          if (textInputLayout.findViewById(R.id.textinput_error)?.visibility != VISIBLE)
            null else textInputLayout.error?.toString())
    }
  }
}
