package com.github.maoabc.aterm;

import android.os.Bundle;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;


public class ATermPreferenceFragment extends PreferenceFragmentCompat {

    private final Preference.OnPreferenceChangeListener mPreferenceChangeListener = (preference, newValue) -> {
        ListPreference listPreference1 = (ListPreference) preference;
        int index = listPreference1.findIndexOfValue(newValue.toString());
        CharSequence[] entries = listPreference1.getEntries();
        preference.setSummary(entries[index]);
        return true;
    };

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        getPreferenceManager().setSharedPreferencesName(ATermService.PREFERENCES_NAME);
        addPreferencesFromResource(R.xml.aterm_preferences);
        setListPreferenceListener((ListPreference) findPreference(ATermSettings.FONTSIZE_KEY));
        setListPreferenceListener((ListPreference) findPreference(ATermSettings.COLOR_KEY));
//        setListPreferenceListener((ListPreference) findPreference(TermSettings.CONTROLKEY_KEY));
//        setListPreferenceListener((ListPreference) findPreference(TermSettings.FNKEY_KEY));
        setListPreferenceListener((ListPreference) findPreference(ATermSettings.TERMTYPE_KEY));
    }

    private void setListPreferenceListener(ListPreference listPreference) {
        if (listPreference == null) {
            return;
        }
        CharSequence entry = listPreference.getEntry();
        if (entry != null) {
            listPreference.setSummary(entry);
        }
        listPreference.setOnPreferenceChangeListener(mPreferenceChangeListener);
    }
}
