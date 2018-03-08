package com.rafhaanshah.studyassistant;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.test.espresso.Espresso;
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
import org.hamcrest.core.IsInstanceOf;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.realm.Realm;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.longClick;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class FlashCardSetTest {

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
    public void flashCardSetTest() {
        mActivityTestRule.launchActivity(new Intent());
        typePasscode();

        ViewInteraction bottomNavigationItemView = onView(
                allOf(withId(R.id.navigation_flash_cards),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.navigation),
                                        0),
                                1),
                        isDisplayed()));
        bottomNavigationItemView.perform(click());

        ViewInteraction floatingActionButton = onView(
                allOf(withId(R.id.fab),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.content),
                                        0),
                                2),
                        isDisplayed()));
        floatingActionButton.perform(click());

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
                        allOf(withId(android.R.id.custom),
                                childAtPosition(
                                        withClassName(is("android.widget.FrameLayout")),
                                        0)),
                        0),
                        isDisplayed()));
        editText.perform(replaceText("New Set"), closeSoftKeyboard());

        ViewInteraction appCompatButton = onView(
                withText("Confirm"));
        appCompatButton.perform(click());

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction appCompatEditText = onView(
                allOf(withId(R.id.et_flash_card),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.card_view),
                                        0),
                                1),
                        isDisplayed()));
        appCompatEditText.perform(replaceText("A"), closeSoftKeyboard());

        ViewInteraction cardView = onView(
                allOf(withId(R.id.card_view),
                        childAtPosition(
                                allOf(withId(R.id.container),
                                        withParent(withId(R.id.view_pager))),
                                0),
                        isDisplayed()));
        cardView.perform(click());

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction appCompatEditText2 = onView(
                allOf(withId(R.id.et_flash_card),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.card_view),
                                        0),
                                1),
                        isDisplayed()));
        appCompatEditText2.perform(replaceText("B"), closeSoftKeyboard());

        ViewInteraction cardView2 = onView(
                allOf(withId(R.id.card_view),
                        childAtPosition(
                                allOf(withId(R.id.container),
                                        withParent(withId(R.id.view_pager))),
                                0),
                        isDisplayed()));
        cardView2.perform(click());

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction textView = onView(
                allOf(withId(R.id.tv_flash_card), withText("A"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.card_view),
                                        0),
                                0),
                        isDisplayed()));
        //textView.check(matches(withText("A")));

        ViewInteraction cardView3 = onView(
                allOf(withId(R.id.card_view),
                        childAtPosition(
                                allOf(withId(R.id.container),
                                        withParent(withId(R.id.view_pager))),
                                0),
                        isDisplayed()));
        cardView3.perform(click());

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction textView2 = onView(
                allOf(withId(R.id.tv_flash_card), withText("B"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.card_view),
                                        0),
                                0),
                        isDisplayed()));
        //textView2.check(matches(withText("B")));

        Espresso.closeSoftKeyboard();
        ViewInteraction actionMenuItemView = onView(
                allOf(withId(R.id.menu_btn_add_flash_card), withContentDescription("Add"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.toolbar),
                                        3),
                                0),
                        isDisplayed()));
        actionMenuItemView.perform(click());

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction appCompatEditText3 = onView(
                allOf(withId(R.id.et_flash_card),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.card_view),
                                        0),
                                1),
                        isDisplayed()));
        appCompatEditText3.perform(replaceText("C"), closeSoftKeyboard());

        ViewInteraction textView3 = onView(
                allOf(withText("Card 2/2"),
                        childAtPosition(
                                allOf(withId(R.id.toolbar),
                                        childAtPosition(
                                                IsInstanceOf.<View>instanceOf(android.widget.LinearLayout.class),
                                                0)),
                                2),
                        isDisplayed()));
        textView3.check(matches(withText("Card 2/2")));

        Espresso.closeSoftKeyboard();

        ViewInteraction actionMenuItemView3 = onView(
                allOf(withId(R.id.menu_btn_search_flash_card), withContentDescription("Search"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.toolbar),
                                        3),
                                2),
                        isDisplayed()));
        actionMenuItemView3.perform(click());

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction editText2 = onView(
                allOf(childAtPosition(
                        allOf(withId(android.R.id.custom),
                                childAtPosition(
                                        withClassName(is("android.widget.FrameLayout")),
                                        0)),
                        0),
                        isDisplayed()));
        editText2.perform(replaceText("1"), closeSoftKeyboard());

        ViewInteraction appCompatButton2 = onView(
                withText("Confirm"));
        appCompatButton2.perform(click());

        ViewInteraction textView4 = onView(
                allOf(withText("Card 1/2"),
                        childAtPosition(
                                allOf(withId(R.id.toolbar),
                                        childAtPosition(
                                                IsInstanceOf.<View>instanceOf(android.widget.LinearLayout.class),
                                                0)),
                                2),
                        isDisplayed()));
        textView4.check(matches(withText("Card 1/2")));

        Espresso.closeSoftKeyboard();

        ViewInteraction actionMenuItemView4 = onView(
                allOf(withId(R.id.menu_btn_delete_flash_card), withContentDescription("Delete"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.toolbar),
                                        3),
                                3),
                        isDisplayed()));
        actionMenuItemView4.perform(click());

        ViewInteraction textView5 = onView(
                allOf(withId(R.id.tv_flash_card), withText("C"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.card_view),
                                        0),
                                0),
                        isDisplayed()));
        textView5.check(matches(withText("C")));

        ViewInteraction textView6 = onView(
                allOf(withText("Card 1/1"),
                        childAtPosition(
                                allOf(withId(R.id.toolbar),
                                        childAtPosition(
                                                IsInstanceOf.<View>instanceOf(android.widget.LinearLayout.class),
                                                0)),
                                2),
                        isDisplayed()));
        textView6.check(matches(withText("Card 1/1")));

        pressBack();

        ViewInteraction recyclerView = onView(
                allOf(withId(R.id.fragment_recycler_view),
                        childAtPosition(
                                withClassName(is("android.support.design.widget.CoordinatorLayout")),
                                0)));
        recyclerView.perform(actionOnItemAtPosition(0, longClick()));

        ViewInteraction appCompatTextView = onView(
                allOf(withId(android.R.id.title), withText("Rename"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("com.android.internal.view.menu.ListMenuItemView")),
                                        0),
                                0),
                        isDisplayed()));
        appCompatTextView.perform(click());

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction editText3 = onView(
                allOf(withText("New Set"),
                        childAtPosition(
                                allOf(withId(android.R.id.custom),
                                        childAtPosition(
                                                withClassName(is("android.widget.FrameLayout")),
                                                0)),
                                0),
                        isDisplayed()));
        editText3.perform(replaceText("Set"));

        ViewInteraction editText4 = onView(
                allOf(withText("Set"),
                        childAtPosition(
                                allOf(withId(android.R.id.custom),
                                        childAtPosition(
                                                withClassName(is("android.widget.FrameLayout")),
                                                0)),
                                0),
                        isDisplayed()));
        editText4.perform(closeSoftKeyboard());

        ViewInteraction appCompatButton3 = onView(
                withText("Confirm"));
        appCompatButton3.perform(click());

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
        searchAutoComplete.perform(replaceText("set"), closeSoftKeyboard());

        ViewInteraction textView8 = onView(
                allOf(withId(R.id.tv_flash_card_set_title), withText("Set"),
                        childAtPosition(
                                allOf(withId(R.id.flash_card_view),
                                        childAtPosition(
                                                withId(R.id.flash_card_layout),
                                                0)),
                                0),
                        isDisplayed()));
        textView8.check(matches(withText("Set")));

        ViewInteraction recyclerView2 = onView(
                allOf(withId(R.id.fragment_recycler_view),
                        childAtPosition(
                                withClassName(is("android.support.design.widget.CoordinatorLayout")),
                                0)));
        recyclerView2.perform(actionOnItemAtPosition(0, longClick()));

        ViewInteraction appCompatTextView2 = onView(
                allOf(withId(android.R.id.title), withText("Delete"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("com.android.internal.view.menu.ListMenuItemView")),
                                        0),
                                0),
                        isDisplayed()));
        appCompatTextView2.perform(click());

        ViewInteraction appCompatButton4 = onView(
                withText("Yes"));
        appCompatButton4.perform(click());

        pressBack();
        pressBack();

        ViewInteraction textView9 = onView(
                allOf(withId(R.id.tv_empty), withText("There are no flash card sets! Press the button on the bottom right to get started."),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.content),
                                        0),
                                1),
                        isDisplayed()));
        textView9.check(matches(isDisplayed()));

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
