package it.faustobe.ecodes.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import it.faustobe.ecodes.R;
import java.util.Random;

public class SplashActivity extends BaseActivity {
    private static final String PREFS_NAME = "app_prefs";
    private static final String KEY_FIRST_LAUNCH = "first_launch";
    private static final int SPLASH_DISPLAY_LENGTH = 1500; // 2.5 secondi

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Hide action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Check if first launch
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isFirstLaunch = prefs.getBoolean(KEY_FIRST_LAUNCH, true);

        // After first launch: show only random nutrition tip
        if (!isFirstLaunch) {
            findViewById(R.id.splashWelcomeText).setVisibility(View.GONE);
            findViewById(R.id.splashFeaturesText).setVisibility(View.GONE);
            findViewById(R.id.splashPrivacyText).setVisibility(View.GONE);

            TextView descriptionText = findViewById(R.id.splashDescriptionText);
            String[] tips = getResources().getStringArray(R.array.nutrition_tips);
            String randomTip = tips[new Random().nextInt(tips.length)];
            descriptionText.setText("💡 " + randomTip);
        }

        // Set version
        TextView versionText = findViewById(R.id.splashVersionText);
        try {
            String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            versionText.setText("v" + versionName);
        } catch (Exception e) {
            versionText.setText("v1.0");
        }

        // Fade in animation
        LinearLayout rootLayout = findViewById(R.id.splashRootLayout);
        AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(800);
        rootLayout.startAnimation(fadeIn);

        // Auto-transition to MainActivity after delay
        new Handler().postDelayed(() -> {
            // Mark first launch as completed
            if (isFirstLaunch) {
                prefs.edit().putBoolean(KEY_FIRST_LAUNCH, false).apply();
            }
            startMainActivity();
        }, SPLASH_DISPLAY_LENGTH);
    }

    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
