package com.faltenreich.diaguard.ui.fragment;


import android.app.DatePickerDialog;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.LayoutRes;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;

import com.faltenreich.diaguard.R;
import com.faltenreich.diaguard.ui.view.DayOfMonthDrawable;

import org.joda.time.DateTime;

/**
 * A simple {@link Fragment} subclass.
 */
public abstract class DateFragment extends BaseFragment implements BaseFragment.ToolbarCallback {

    private DateTime day;

    public DateFragment(@LayoutRes int layoutResourceId) {
        super(layoutResourceId);
    }

    public void setDay(DateTime day) {
        this.day = day;
        updateLabels();
    }

    public DateTime getDay() {
        return day;
    }

    @Override
    public void onViewCreated (View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);
        initialize();
    }

    @Override
    public void onResume() {
        super.onResume();
        goToDay(day);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateLabels();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.date, menu);

        MenuItem menuItem = menu.findItem(R.id.action_today);
        if (menuItem != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                LayerDrawable icon = (LayerDrawable) menuItem.getIcon();
                setTodayIcon(icon, getActivity());
            } else {
                menuItem.setIcon(R.drawable.ic_action_today);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_today:
                goToDay(DateTime.now());
                break;
        }
        return true;
    }

    @Override
    public void action() {
        showDatePicker();
    }

    @CallSuper
    protected void initialize() {
        day = DateTime.now().withHourOfDay(0).withMinuteOfHour(0);
    }

    @CallSuper
    protected void goToDay(DateTime day) {
        this.day = day;
        updateLabels();
    }

    private void setTodayIcon(LayerDrawable icon, Context context) {
        DayOfMonthDrawable today = new DayOfMonthDrawable(context);
        today.setDayOfMonth(DateTime.now().dayOfMonth().get());
        icon.mutate();
        icon.setDrawableByLayerId(R.id.today_icon_day, today);
    }

    private void showDatePicker() {
        DatePickerFragment.newInstance(day, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                goToDay(DateTime.now().withYear(year).withMonthOfYear(month + 1).withDayOfMonth(day));
            }
        }).show(getActivity().getSupportFragmentManager());
    }

    protected abstract void updateLabels();
}