/*
 * Copyright (C) 2016 SpiritCroc
 * Email: spiritcroc@gmail.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.spiritcroc.colorflash;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;

public class SettingsFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String KEY_FLASH_DELAY = "tmp_flash_delay";
    private static final String KEY_CATEGORY_RANDOM = "pref_category_flash_mode_random";
    private static final String KEY_CATEGORY_COLORS = "pref_category_flash_mode_colors";

    private EditTextPreference flashDelayPreference;
    private ListPreference flashModePreference;
    private Preference randomPreferenceCategory;
    private Preference colorsPreferenceCategory;
    private Preference[] colorPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        flashDelayPreference = (EditTextPreference) findPreference(KEY_FLASH_DELAY);
        flashModePreference = (ListPreference) findPreference(Settings.FLASH_MODE);
        randomPreferenceCategory = findPreference(KEY_CATEGORY_RANDOM);
        colorsPreferenceCategory = findPreference(KEY_CATEGORY_COLORS);
        colorPreferences = new Preference[] {
                findPreference(Settings.COLOR_1),
                findPreference(Settings.COLOR_2),
                findPreference(Settings.COLOR_3),
                findPreference(Settings.COLOR_4),
                findPreference(Settings.COLOR_5),
                findPreference(Settings.COLOR_6)
        };

        SeekBarPreference grayThresholdPreference =
                (SeekBarPreference) findPreference(Settings.GRAY_THRESHOLD);
        grayThresholdPreference.setMax(254);
        grayThresholdPreference.setDefault(127);
        grayThresholdPreference.setDialogMessage(getString(R.string.pref_gray_threshold_dialog));

        SeekBarPreference contrastThresholdPreference =
                (SeekBarPreference) findPreference(Settings.CONTRAST_THRESHOLD);
        contrastThresholdPreference.setMax(511);
        contrastThresholdPreference.setDefault(127);
        contrastThresholdPreference
                .setDialogMessage(getString(R.string.pref_contrast_threshold_dialog));
    }

    private void init() {
        setFlashDelaySummary();
        updateFlashMode();
    }

    @Override
    public void onResume() {
        super.onResume();
        addTmpPreferences();
        getPreferenceManager().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
        init();
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceManager().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
        removeTmpPreferences();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (KEY_FLASH_DELAY.equals(key)) {
            setFlashDelay();
        } else if (Settings.FLASH_MODE.equals(key)) {
            updateFlashMode();
        }
    }

    private void addTmpPreferences() {
        SharedPreferences sp = getPreferenceManager().getSharedPreferences();
        sp.edit()
                .putString(KEY_FLASH_DELAY, "" + sp.getInt(Settings.FLASH_DELAY, 100))
                .apply();
    }

    private void removeTmpPreferences() {
        getPreferenceManager().getSharedPreferences().edit()
                .remove(KEY_FLASH_DELAY)
                .apply();
    }

    private void setFlashDelay() {
        SharedPreferences sp = getPreferenceManager().getSharedPreferences();
        try {
            int intValue = Integer.parseInt(sp.getString(KEY_FLASH_DELAY, null));
            sp.edit()
                    .putInt(Settings.FLASH_DELAY, intValue)
                    .apply();
            setFlashDelaySummary(intValue);
        } catch (Exception e) {
            e.printStackTrace();
            setFlashDelaySummary();
        }
    }

    private void setFlashDelaySummary() {
        SharedPreferences sp = getPreferenceManager().getSharedPreferences();
        setFlashDelaySummary(sp.getInt(Settings.FLASH_DELAY, 100));
    }

    private void setFlashDelaySummary(int value) {
        flashDelayPreference.setSummary(getString(R.string.pref_flash_delay_summary, value));
        flashDelayPreference.setText("" + value);
    }

    private void updateFlashMode() {
        int flashMode = Integer.parseInt(getPreferenceManager().getSharedPreferences()
                .getString(Settings.FLASH_MODE, "0"));

        flashModePreference.setSummary(getResources()
                .getStringArray(R.array.pref_flash_mode_entries)
                [flashModePreference.findIndexOfValue(flashModePreference.getValue())]);

        if (flashMode == 0) {
            randomPreferenceCategory.setEnabled(true);
            colorsPreferenceCategory.setEnabled(false);
        } else {
            randomPreferenceCategory.setEnabled(false);
            colorsPreferenceCategory.setEnabled(true);
            for (int i = 0; i < colorPreferences.length; i++) {
                colorPreferences[i].setEnabled(flashMode > i);
            }
        }
    }
}
