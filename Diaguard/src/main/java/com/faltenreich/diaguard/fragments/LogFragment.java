package com.faltenreich.diaguard.fragments;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;

import com.faltenreich.diaguard.DiaguardApplication;
import com.faltenreich.diaguard.R;
import com.faltenreich.diaguard.helpers.ViewHelper;
import com.faltenreich.diaguard.ui.recycler.DayOfMonthDrawable;
import com.faltenreich.diaguard.ui.recycler.EndlessScrollListener;
import com.faltenreich.diaguard.ui.recycler.LogRecyclerAdapter;
import com.faltenreich.diaguard.ui.recycler.RecyclerItem;

import org.joda.time.DateTime;

/**
 * Created by Filip on 05.07.2015.
 */
public class LogFragment extends BaseFragment {

    private LinearLayoutManager linearLayoutManager;
    private RecyclerView recyclerView;
    private LogRecyclerAdapter recyclerAdapter;

    private DateTime firstVisibleDay;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_log, container, false);
        setHasOptionsMenu(true);
        getComponents(view);
        initialize();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        goToDay(firstVisibleDay);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        updateMonthForUi();
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

    private void getComponents(View view) {
        recyclerView = (RecyclerView) view.findViewById(R.id.list);
    }

    private void initialize() {
        firstVisibleDay = DateTime.now();

        // FIXME
        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        final View actionView = toolbar.findViewById(R.id.action);
        actionView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                action(actionView);
            }
        });
    }

    private void goToDay(DateTime day) {
        firstVisibleDay = day;

        linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerAdapter = new LogRecyclerAdapter(getActivity(), day);
        recyclerView.setAdapter(recyclerAdapter);
        recyclerView.scrollToPosition(day.dayOfMonth().get());
        updateMonthForUi();

        // Endless scroll
        recyclerView.addOnScrollListener(new EndlessScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(Direction direction) {
                recyclerAdapter.appendRows(direction);
            }
        });

        // Fragment updates
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                RecyclerItem item = recyclerAdapter.items.get(linearLayoutManager.findFirstVisibleItemPosition());
                firstVisibleDay = item.getDateTime();
                // Update month in Toolbar when section is being crossed
                boolean isScrollingUp = dy < 0;
                boolean isCrossingMonth = isScrollingUp ?
                        firstVisibleDay.dayOfMonth().get() == firstVisibleDay.dayOfMonth().getMaximumValue() :
                        firstVisibleDay.dayOfMonth().get() == firstVisibleDay.dayOfMonth().getMinimumValue();
                if (isCrossingMonth) {
                    updateMonthForUi();
                }
            }
        });
    }

    private void updateMonthForUi() {
        boolean isCurrentYear = firstVisibleDay.year().get() == DateTime.now().year().get();
        String format = "MMMM";
        if (!isCurrentYear) {
            if (ViewHelper.isLandscape(getActivity()) || ViewHelper.isLargeScreen(getActivity())) {
                format = "MMMM YYYY";
            } else {
                format = "MMM YYYY";
            }
        }
        getActionView().setText(firstVisibleDay.toString(format));
    }

    private void setTodayIcon(LayerDrawable icon, Context context) {
        DayOfMonthDrawable today = new DayOfMonthDrawable(context);
        today.setDayOfMonth(DateTime.now().dayOfMonth().get());
        icon.mutate();
        icon.setDrawableByLayerId(R.id.today_icon_day, today);
    }

    public void showDatePicker () {
        DatePickerFragment fragment = new DatePickerFragment() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                goToDay(DateTime.now().withYear(year).withMonthOfYear(month + 1).withDayOfMonth(day));
            }
        };
        Bundle bundle = new Bundle(1);
        bundle.putSerializable(DatePickerFragment.DATE, firstVisibleDay);
        fragment.setArguments(bundle);
        fragment.show(getActivity().getSupportFragmentManager(), "DatePicker");
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
    public String getTitle() {
        return DiaguardApplication.getContext().getString(R.string.log);
    }

    @Override
    public boolean hasAction() {
        return true;
    }

    @Override
    public void action(View view) {
        showDatePicker();
    }
}