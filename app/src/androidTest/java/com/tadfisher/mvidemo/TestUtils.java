package com.tadfisher.mvidemo;

import static android.support.test.espresso.core.deps.guava.base.Preconditions.checkNotNull;

import android.support.design.widget.TextInputLayout;
import android.support.test.espresso.matcher.BoundedMatcher;
import android.view.View;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

class TestUtils {

  static Matcher<View> hasTextInputLayoutErrorText(final Matcher<String> stringMatcher) {
    checkNotNull(stringMatcher);
    return new BoundedMatcher<View, TextInputLayout>(TextInputLayout.class) {

      @Override
      public void describeTo(Description description) {
        description.appendText("with error: ");
        stringMatcher.describeTo(description);
      }

      @Override
      public boolean matchesSafely(TextInputLayout textInputLayout) {
        CharSequence error = textInputLayout.getError();
        String errorString = error == null ? null : error.toString();
        return error != null && stringMatcher.matches(errorString);
      }
    };
  }
}
