package com.faltenreich.diaguard.ui.view.preferences;

import android.content.Context;
import android.preference.Preference;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;

import com.faltenreich.diaguard.data.PreferenceHelper;
import com.mukesh.countrypicker.fragments.CountryPicker;
import com.mukesh.countrypicker.interfaces.CountryPickerListener;

/**
 * Created by Faltenreich on 08.11.2016.
 */

public class CountryPreference extends Preference implements Preference.OnPreferenceClickListener {

    public CountryPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnPreferenceClickListener(this);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        showCountryPicker();
        return true;
    }

    private void showCountryPicker() {
        final CountryPicker countryPicker = CountryPicker.newInstance("Select country");
        FragmentManager fragmentManager = ((AppCompatActivity) getContext()).getSupportFragmentManager();
        countryPicker.show(fragmentManager, CountryPicker.class.getSimpleName());
        countryPicker.setListener(new CountryPickerListener() {
            @Override
            public void onSelectCountry(String name, String code, String dialCode, int flagDrawableResID) {
                setCountry(code);
                countryPicker.dismiss();
            }
        });
    }

    private void setCountry(String countryCode) {
        PreferenceHelper.getInstance().setCountry(countryCode);
        setSummary(countryCode);
    }
}