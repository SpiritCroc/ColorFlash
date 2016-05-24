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

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends Activity implements View.OnLongClickListener {

    private static final String TAG = "MainActivity";
    private static final boolean DEBUG = false;

    private View view;
    private Toast greetingToast;

    private Handler flashHandler = new Handler();

    private boolean enableGreetingToast;
    private int flashDelay;
    private int flashMode;
    private boolean noGrey;
    private int grayThreshold;
    private boolean highContrast;
    private int contrastThreshold;
    private int[] colors;

    private int colorIndex = 0;
    private int previousColor = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        view = new View(this);
        view.setOnLongClickListener(this);
        setContentView(view);

        greetingToast =
                Toast.makeText(getApplicationContext(), R.string.toast_greeting, Toast.LENGTH_LONG);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Get preferences
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        enableGreetingToast = sp.getBoolean(Settings.GREETING_TOAST, true);
        flashDelay = sp.getInt(Settings.FLASH_DELAY, 100);
        flashMode = Integer.parseInt(sp.getString(Settings.FLASH_MODE, "0"));
        noGrey = sp.getBoolean(Settings.NO_GRAY, true);
        grayThreshold = sp.getInt(Settings.GRAY_THRESHOLD, 127);
        highContrast = sp.getBoolean(Settings.HIGH_CONTRAST, true);
        contrastThreshold = sp.getInt(Settings.CONTRAST_THRESHOLD, 127);
        colors = new int[] {
                sp.getInt(Settings.COLOR_1, 0xffff0000),
                sp.getInt(Settings.COLOR_2, 0xff00ff00),
                sp.getInt(Settings.COLOR_3, 0xff0000ff),
                sp.getInt(Settings.COLOR_4, 0xff00ffff),
                sp.getInt(Settings.COLOR_5, 0xffff00ff),
                sp.getInt(Settings.COLOR_6, 0xffffff00)
        };

        // Set up activity appearance
        if (sp.getBoolean(Settings.IMMERSIVE, true)) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
        view.setKeepScreenOn(sp.getBoolean(Settings.KEEP_SCREEN_ON, true));

        // Greeting toast
        if (enableGreetingToast) {
            greetingToast.show();
        }

        flash.run();
    }

    @Override
    protected void onPause() {
        super.onPause();
        flashHandler.removeCallbacks(flash);
        greetingToast.cancel();
    }

    @Override
    public boolean onLongClick(View view) {
        startActivity(new Intent(this, SettingsActivity.class));
        return true;
    }

    private Runnable flash = new Runnable() {
        @Override
        public void run() {
            if (DEBUG) Log.d(TAG, "Update view color");
            flashHandler.removeCallbacks(this);
            flashHandler.postDelayed(this, flashDelay);
            view.setBackgroundColor(getNextColor());
        }
    };

    private int getNextColor() {
        if (flashMode > 0) {
            if (++colorIndex >= flashMode) {
                colorIndex = 0;
            }
            return colors[colorIndex];
        } else {
            return getRandColor();
        }
    }

    private int getRandColor() {
        int color = (int) (Math.random()*(Color.WHITE-Color.BLACK)) + Color.BLACK;
        return noGrey && isGrey(color) || highContrast && isLowContrast(color) ?
                getRandColor() : (previousColor = color);
    }

    private boolean isGrey(int color) {
        int max = Math.max(Math.max(Color.red(color), Color.green(color)), Color.blue(color));
        int min = Math.min(Math.min(Color.red(color), Color.green(color)), Color.blue(color));
        if (DEBUG) Log.d(TAG, "Colors got gray diff: " + (max - min));
        return max - min < grayThreshold;
    }

    private boolean isLowContrast(int color) {
        if (previousColor == 0) {
            // No previous color available, contrast is high enough
            return false;
        }
        int diff = Math.abs(Color.red(previousColor) - Color.red(color)) +
                Math.abs(Color.green(previousColor) - Color.green(color)) +
                Math.abs(Color.blue(previousColor) - Color.blue(color));
        if (DEBUG) Log.d(TAG, "Colors got contrast diff: " + diff);
        return diff < contrastThreshold;
    }
}
