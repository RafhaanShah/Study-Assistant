package com.rafhaanshah.studyassistant;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

import io.realm.Realm;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.longClick;
import static android.support.test.espresso.action.ViewActions.pressImeActionButton;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class ScheduleEventTest {

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
    public void scheduleEventTest() {
        mActivityTestRule.launchActivity(new Intent());

        ViewInteraction floatingActionButton = onView(
                allOf(withId(R.id.fab),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.content),
                                        0),
                                2),
                        isDisplayed()));
        floatingActionButton.perform(click());

        ViewInteraction appCompatEditText = onView(
                allOf(withId(R.id.et_title),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.ScrollView")),
                                        0),
                                1)));
        appCompatEditText.perform(scrollTo(), replaceText("Homework"), closeSoftKeyboard());

        ViewInteraction appCompatEditText2 = onView(
                allOf(withId(R.id.et_date),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.ScrollView")),
                                        0),
                                5)));
        appCompatEditText2.perform(scrollTo(), click());

        ViewInteraction appCompatButton2 = onView(
                allOf(withId(android.R.id.button1), withText("OK"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.ScrollView")),
                                        0),
                                3)));
        appCompatButton2.perform(scrollTo(), click());

        ViewInteraction appCompatEditText3 = onView(
                allOf(withId(R.id.et_time),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.ScrollView")),
                                        0),
                                6)));
        appCompatEditText3.perform(scrollTo(), click());

        ViewInteraction appCompatButton3 = onView(
                allOf(withId(android.R.id.button1), withText("OK"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.ScrollView")),
                                        0),
                                3)));
        appCompatButton3.perform(scrollTo(), click());

        ViewInteraction actionMenuItemView = onView(
                allOf(withId(R.id.menu_btn_save_event), withText("Save"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.toolbar),
                                        2),
                                0),
                        isDisplayed()));
        actionMenuItemView.perform(click());

        ViewInteraction textView = onView(
                allOf(withId(R.id.tv_event_title), withText("Homework"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.card_view),
                                        0),
                                1),
                        isDisplayed()));
        textView.check(matches(withText("Homework")));

        ViewInteraction recyclerView = onView(
                allOf(withId(R.id.fragment_recycler_view),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.content),
                                        0),
                                0),
                        isDisplayed()));
        recyclerView.check(matches(isDisplayed()));
        recyclerView.perform(actionOnItemAtPosition(0, longClick()));

        ViewInteraction textView2 = onView(
                allOf(withId(android.R.id.title), withText("Mark as Completed"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("com.android.internal.view.menu.ListMenuItemView")),
                                        0),
                                0),
                        isDisplayed()));
        textView2.perform(click());

        ViewInteraction actionMenuItemView2 = onView(
                allOf(withId(R.id.menu_btn_history), withContentDescription("My History"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.toolbar),
                                        1),
                                2),
                        isDisplayed()));
        actionMenuItemView2.perform(click());

        ViewInteraction appCompatImageView = onView(
                allOf(withId(R.id.search_button), withContentDescription("Search"),
                        childAtPosition(
                                allOf(withId(R.id.search_bar),
                                        childAtPosition(
                                                withId(R.id.menu_btn_search),
                                                0)),
                                1),
                        isDisplayed()));
        appCompatImageView.perform(click());

        ViewInteraction searchAutoComplete = onView(
                allOf(withId(R.id.search_src_text),
                        childAtPosition(
                                allOf(withId(R.id.search_plate),
                                        childAtPosition(
                                                withId(R.id.search_edit_frame),
                                                1)),
                                0),
                        isDisplayed()));
        searchAutoComplete.perform(replaceText("home"), closeSoftKeyboard());

        ViewInteraction textView3 = onView(
                allOf(withId(R.id.tv_event_title), withText("Homework"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.card_view),
                                        0),
                                1),
                        isDisplayed()));
        textView3.check(matches(withText("Homework")));

        ViewInteraction searchAutoComplete2 = onView(
                allOf(withId(R.id.search_src_text), withText("home"),
                        childAtPosition(
                                allOf(withId(R.id.search_plate),
                                        childAtPosition(
                                                withId(R.id.search_edit_frame),
                                                1)),
                                0),
                        isDisplayed()));
        searchAutoComplete2.perform(pressImeActionButton());

        ViewInteraction recyclerView2 = onView(
                allOf(withId(R.id.fragment_recycler_view),
                        childAtPosition(
                                withClassName(is("android.support.design.widget.CoordinatorLayout")),
                                0)));
        recyclerView2.perform(actionOnItemAtPosition(0, click()));

        ViewInteraction appCompatImageButton = onView(
                allOf(withId(R.id.btn_edit), withContentDescription("Edit Button"),
                        childAtPosition(
                                allOf(withId(R.id.title_layout),
                                        childAtPosition(
                                                withId(R.id.card_view),
                                                0)),
                                2),
                        isDisplayed()));
        appCompatImageButton.perform(click());

        onView(withId(R.id.et_title)).check(matches(withText("Homework")));

        ViewInteraction appCompatButton4 = onView(
                allOf(withId(R.id.btn_delete_event), withText("Delete"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.ScrollView")),
                                        0),
                                12)));
        appCompatButton4.perform(scrollTo(), click());

        ViewInteraction appCompatButton5 = onView(
                allOf(withId(android.R.id.button1), withText("Yes"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.ScrollView")),
                                        0),
                                3)));
        appCompatButton5.perform(scrollTo(), click());

        ViewInteraction textView4 = onView(
                allOf(withId(R.id.tv_empty), withText("You have no events! Press the button on the bottom right to add a new one."),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.content),
                                        0),
                                1),
                        isDisplayed()));
        textView4.check(matches(isDisplayed()));
    }

    @Before
    public void setUp() {
        Context context = getInstrumentation().getTargetContext();
        SharedPreferences preferenceManager = PreferenceManager.getDefaultSharedPreferences(context);
        preferenceManager.edit().clear()
                .putString("PASSWORD_SALT_PREFERENCE_KEY", "[-120, 28, -65, 39, -115, -26, 54, 9, 108, -60, 101, 121," +
                        "-113, -96, -26, 106, 20, -15, -50, -40, 84, -74, -35, 65, -54, -37, -69, 110, 110, 125, 84, -123, 49, -6, -97, -32, 59," +
                        "60, -57, -47, 1, -115, -26, 65, 12, -51, 63, -15, 41, -4, -118, 32, 87, -46, -68, 103, 121, 34, 126, 21, 119, 33, -97, -24," +
                        "-4, -91, 64, 11, 45, -85, 77, 106, 103, -51, 119, -114, -27, 69, 48, 106, 23, -122, -55, 7, -30, 30, -14, 12, 68, -30, -14, -17," +
                        "91, 82, -34, 120, -128, -9, 10, -91, 31, -15, -76, 113, -26, -85, 108, 26, -111, 112, -112, -38, 110, 119, 118, -122, 41, 67, 8, 41," +
                        "-112, -69, -109, 53, -100, 5, 105, -65, 2, -33, 59, -45, 87, -119, 107, -73, 34, -66, -4, 113, -74, 75, 118, 8, -34, -79, 70, 16, 89," +
                        "-8, 11, -124, 46, -42, -3, -11, 31, 89, -52, -54, -118, -80, 1, -18, 9, 107, 81, 9, 53, -93, -53, -56, -109, -94, -44, -78, 41, 19, 56," +
                        "-92, -91, 122, -86, -118, 38, 54, 90, -83, 59, -17, -49, -57, 77, -92, 30, 16, 41, -67, 32, 95, 52, -8, -48, -56, -72, -67, -38, -57, -35," +
                        "-58, -68, -3, 24, 125, 115, 24, 93, 18, 107, 14, -59, 1, -79, 109, -23, 34, 68, -116, 37, -5, -81, 82, -50, 97, 42, 113, -41, -90, -83, 40," +
                        "-84, -74, -105, -106, -79, -79, 63, 98, -95, 39, -22, 12, 45, 66, -40, -9]")
                .putString("PASSCODE", "79c6e53812ea5b473d556cb32194c3004750bd5d08d6161b7ce8e986bc29452e")
                .putBoolean("PREF_PASSCODE_SET", true)
                .putString("PREF_QUESTION", "Question")
                .putString("PREF_ANSWER", "0db52f4076c082518412afd3dd3576e2cb0c63703fd7fed5e23ade60efef31d9")
                .putString("ALGORITHM", "2")
                .commit();
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.deleteAll();
            }
        });
        realm.close();
    }

    private void typePasscode() {
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
    }
}
