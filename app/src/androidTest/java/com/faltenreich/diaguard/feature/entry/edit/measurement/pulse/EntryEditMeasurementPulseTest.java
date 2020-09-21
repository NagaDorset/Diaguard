package com.faltenreich.diaguard.feature.entry.edit.measurement.pulse;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.faltenreich.diaguard.R;
import com.faltenreich.diaguard.feature.entry.edit.EntryEditActivity;
import com.faltenreich.diaguard.shared.data.database.entity.Category;
import com.faltenreich.diaguard.test.espresso.matcher.EditTextMatcher;
import com.faltenreich.diaguard.test.junit.rule.CleanUpData;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class EntryEditMeasurementPulseTest {

    private ActivityScenario<EntryEditActivity> scenario;

    @Rule public final TestRule dataCleanUp = new CleanUpData();

    @Before
    public void setup() {
        Intent intent = new Intent(InstrumentationRegistry.getInstrumentation().getTargetContext(), EntryEditActivity.class);
        intent.putExtra(EntryEditActivity.EXTRA_CATEGORY, Category.PULSE);
        scenario = ActivityScenario.launch(intent);
    }

    @Test
    public void confirmingEmptyValue_shouldShowWarning() {
        Espresso.onView(ViewMatchers.withId(R.id.fab))
            .perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withId(R.id.editText))
            .check(ViewAssertions.matches(EditTextMatcher.hasErrorText(R.string.validator_value_empty)));
    }

    @Test
    public void confirmingValueBelowMinimum_shouldShowWarning() {
        Espresso.onView(ViewMatchers.withId(R.id.editText))
            .perform(ViewActions.replaceText("9"));
        Espresso.onView(ViewMatchers.withId(R.id.fab))
            .perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withId(R.id.editText))
            .check(ViewAssertions.matches(EditTextMatcher.hasErrorText(R.string.validator_value_unrealistic)));
    }

    @Test
    public void confirmingValueAboveMaximum_shouldShowWarning() {
        Espresso.onView(ViewMatchers.withId(R.id.editText))
            .perform(ViewActions.replaceText("201"));
        Espresso.onView(ViewMatchers.withId(R.id.fab))
            .perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withId(R.id.editText))
            .check(ViewAssertions.matches(EditTextMatcher.hasErrorText(R.string.validator_value_unrealistic)));
    }

    @Test
    public void confirmingValidValue_shouldFinishActivity() {
        scenario.onActivity(activity -> {
            Espresso.onView(ViewMatchers.withId(R.id.editText))
                .perform(ViewActions.replaceText("100"));
            Espresso.onView(ViewMatchers.withId(R.id.fab))
                .perform(ViewActions.click());
            Assert.assertTrue(activity.isFinishing());
        });
    }
}
