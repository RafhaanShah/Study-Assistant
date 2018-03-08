package com.rafhaanshah.studyassistant;


import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class SetPasscodeTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class, true, false);

    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }

    @Test
    public void setPasscodeTest() {
        mActivityTestRule.launchActivity(new Intent());

        ViewInteraction keyboardButtonView = onView(
                allOf(withId(R.id.pin_code_button_1),
                        childAtPosition(
                                allOf(withId(R.id.pin_code_first_row),
                                        childAtPosition(
                                                withClassName(is("android.widget.LinearLayout")),
                                                0)),
                                0),
                        isDisplayed()));
        keyboardButtonView.perform(click());
        keyboardButtonView.perform(click());
        keyboardButtonView.perform(click());
        keyboardButtonView.perform(click());
        keyboardButtonView.perform(click());
        keyboardButtonView.perform(click());
        keyboardButtonView.perform(click());
        keyboardButtonView.perform(click());

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction editText = onView(
                allOf(childAtPosition(
                        childAtPosition(
                                withId(android.R.id.custom),
                                0),
                        0),
                        isDisplayed()));
        editText.perform(replaceText("Question"), closeSoftKeyboard());

        ViewInteraction editText2 = onView(
                allOf(childAtPosition(
                        childAtPosition(
                                withId(android.R.id.custom),
                                0),
                        1),
                        isDisplayed()));
        editText2.perform(replaceText("answer"), closeSoftKeyboard());

        ViewInteraction appCompatButton = onView(
                withText("Confirm"));
        appCompatButton.perform(click());
    }

    @Before
    public void setUpSharedPrefs() {
        Context context = getInstrumentation().getTargetContext();
        PreferenceManager.getDefaultSharedPreferences(context).edit().clear().commit();
    }
}
