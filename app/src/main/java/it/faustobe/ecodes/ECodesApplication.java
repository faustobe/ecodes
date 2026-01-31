package it.faustobe.ecodes;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import java.util.Locale;

public class ECodesApplication extends Application {
    private static final String PREFS_NAME = "app_prefs";
    private static final String KEY_LANGUAGE = "language";

    @Override
    public void onCreate() {
        super.onCreate();
        applyLanguage();
    }

    private void applyLanguage() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String language = prefs.getString(KEY_LANGUAGE, "it"); // Default: Italian

        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Resources resources = getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }
}
