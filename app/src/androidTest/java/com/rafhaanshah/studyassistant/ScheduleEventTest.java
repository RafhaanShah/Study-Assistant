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
        typePasscode();

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

        ViewInteraction appCompatButton = onView(
                withText("OK"));
        appCompatButton.perform(click());

        ViewInteraction appCompatEditText3 = onView(
                allOf(withId(R.id.et_time),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.ScrollView")),
                                        0),
                                6)));
        appCompatEditText3.perform(scrollTo(), click());

        ViewInteraction appCompatButton2 = onView(
                withText("OK"));
        appCompatButton2.perform(click());

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

        ViewInteraction appCompatButton3 = onView(
                withText("Yes"));
        appCompatButton3.perform(click());

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
                .putString("PASSWORD_SALT_PREFERENCE_KEY", "[-19, -11, 112, -92, 83, -18, 85, 14, -63, 72, 48, 55, 85, -1, 13, -49, -23, 53, -64, -90, 2, -93, -125, -16, -84, -82, 38, -34, 2, 76, 77, 117, 72, -2, 26, 17, 57, 50, 108, 25, -29, -60, -83, 94, -44, 18, -83, -6, -15, 72, 74, -64, 109, -10, -100, -31, 118, 47, 114, 62, 117, -79, 78, -50, 32, -100, 18, 72, -15, 104, 113, 98, -88, -118, -21, 90, -60, -39, -19, -7, -27, 109, 70, -28, 108, 66, 68, 8, -117, -126, 56, 38, 44, 7, 79, 60, -108, 79, 21, 6, 0, 115, 2, -112, 1, 86, 28, -122, 32, -112, 59, -26, -80, -28, -9, -8, 15, 102, 27, -98, -112, 56, 32, 32, -93, 14, -56, 24, 109, -86, 109, -70, -58, 93, 124, 47, -10, -94, 22, -68, 38, 30, 33, -1, -109, 3, 66, 39, -119, -105, 3, -32, -116, 16, -23, -70, 58, -15, 111, -85, -85, -125, 25, 45, 109, -50, -73, 40, -28, 46, 97, -85, 17, -123, -55, -125, -9, 16, 103, -98, 57, -78, 125, -98, -30, -38, 116, -69, 5, 90, -47, 100, 94, -91, 30, 114, -76, 7, -73, -88, -113, -98, -89, 75, -30, -62, -8, 21, -47, 71, 16, 103, 69, 61, -23, 19, -63, 67, -34, -91, -113, -33, 52, 38, -102, -102, 87, -60, -99, -40, 50, -29, 120, -82, 41, -39, -85, -6, 109, -73, 22, -117, -122, 85, -4, 51, -103, -88, 1, 85, 4, 77, 89, 67, -60, -58]")
                .putString("PASSCODE", "728ead185bc393ef75ed9b8e1bad4a64c559f54701128bca4ebd7242d076436d")
                .putString("ALGORITHM", "2")
                .putBoolean("PREF_PASSCODE_SET", true)
                .putString("PREF_QUESTION", "Question")
                .putString("PREF_ANSWER", "0db52f4076c082518412afd3dd3576e2cb0c63703fd7fed5e23ade60efef31d9")
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
