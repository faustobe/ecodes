package it.faustobe.ecodes.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;
import it.faustobe.ecodes.R;
import java.util.Locale;

public class BaseActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "app_prefs";
    private static final String KEY_LANGUAGE = "language";
    private String currentLanguage;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(updateBaseContextLocale(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Save current language when activity is created
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        currentLanguage = prefs.getString(KEY_LANGUAGE, "it");
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Check if language has changed while activity was in background
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String newLanguage = prefs.getString(KEY_LANGUAGE, "it");
        if (!newLanguage.equals(currentLanguage)) {
            // Language changed, recreate activity
            recreate();
        }
    }

    private Context updateBaseContextLocale(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String language = prefs.getString(KEY_LANGUAGE, "it");

        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();
        configuration.setLocale(locale);

        return context.createConfigurationContext(configuration);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_howto) {
            startActivity(new Intent(this, HowToActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
