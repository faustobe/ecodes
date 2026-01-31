package it.faustobe.ecodes.ui;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import it.faustobe.ecodes.R;

public class HowToActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_howto);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.howto_title);
        }

        setupClassificationBars();
        setupNutriScoreBadges();
        setupNovaBadges();
        setupEcoScoreBadges();
    }

    private void setupClassificationBars() {
        // Safe (green, 12%)
        setupClassificationItem(R.id.classificationSafe, 0.12f, 0xFF4CAF50,
                getString(R.string.howto_classification_safe));
        // Intolerance (light green, 28%)
        setupClassificationItem(R.id.classificationIntolerance, 0.28f, 0xFF8BC34A,
                getString(R.string.howto_classification_intolerance));
        // Suspect (yellow, 45%)
        setupClassificationItem(R.id.classificationSuspect, 0.45f, 0xFFFFEB3B,
                getString(R.string.howto_classification_suspect));
        // Risky (orange, 65%)
        setupClassificationItem(R.id.classificationRisky, 0.65f, 0xFFFF9800,
                getString(R.string.howto_classification_risky));
        // Dangerous (red, 85%)
        setupClassificationItem(R.id.classificationDangerous, 0.85f, 0xFFF44336,
                getString(R.string.howto_classification_dangerous));
        // Banned (dark red, 100%)
        setupClassificationItem(R.id.classificationBanned, 1.0f, 0xFFB71C1C,
                getString(R.string.howto_classification_banned));
    }

    private void setupClassificationItem(int containerId, float level, int color, String text) {
        View container = findViewById(containerId);
        if (container == null) return;

        View track = container.findViewById(R.id.dangerBarTrack);
        View fill = container.findViewById(R.id.dangerBarFill);
        TextView textView = container.findViewById(R.id.classificationText);

        textView.setText(text);

        track.post(() -> {
            int trackWidth = track.getWidth();
            int fillWidth = (int) (trackWidth * level);

            ViewGroup.LayoutParams params = fill.getLayoutParams();
            params.width = fillWidth;
            fill.setLayoutParams(params);

            GradientDrawable fillDrawable = new GradientDrawable();
            fillDrawable.setShape(GradientDrawable.RECTANGLE);
            fillDrawable.setCornerRadius(8f);
            fillDrawable.setColor(color);
            fill.setBackground(fillDrawable);
        });
    }

    private void setupNutriScoreBadges() {
        setupNutriScoreItem(R.id.nutriscoreA, "A", 0xFF1B5E20,
                getString(R.string.howto_nutriscore_a));
        setupNutriScoreItem(R.id.nutriscoreB, "B", 0xFF558B2F,
                getString(R.string.howto_nutriscore_b));
        setupNutriScoreItem(R.id.nutriscoreC, "C", 0xFFF9A825,
                getString(R.string.howto_nutriscore_c));
        setupNutriScoreItem(R.id.nutriscoreD, "D", 0xFFE65100,
                getString(R.string.howto_nutriscore_d));
        setupNutriScoreItem(R.id.nutriscoreE, "E", 0xFFB71C1C,
                getString(R.string.howto_nutriscore_e));
    }

    private void setupNutriScoreItem(int containerId, String letter, int color, String text) {
        View container = findViewById(containerId);
        if (container == null) return;

        TextView badge = container.findViewById(R.id.nutriscore_badge);
        TextView textView = container.findViewById(R.id.nutriscore_text);

        badge.setText(letter);
        textView.setText(text);

        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setColor(color);
        badge.setBackground(drawable);
    }

    private void setupNovaBadges() {
        setupNovaItem(R.id.nova1, "1", 0xFF4CAF50,
                getString(R.string.howto_nova_1));
        setupNovaItem(R.id.nova2, "2", 0xFF8BC34A,
                getString(R.string.howto_nova_2));
        setupNovaItem(R.id.nova3, "3", 0xFFFF9800,
                getString(R.string.howto_nova_3));
        setupNovaItem(R.id.nova4, "4", 0xFFF44336,
                getString(R.string.howto_nova_4));
    }

    private void setupNovaItem(int containerId, String number, int color, String text) {
        View container = findViewById(containerId);
        if (container == null) return;

        TextView badge = container.findViewById(R.id.nova_badge);
        TextView textView = container.findViewById(R.id.nova_text);

        badge.setText(number);
        textView.setText(text);

        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setCornerRadius(6f);
        drawable.setColor(color);
        badge.setBackground(drawable);
    }

    private void setupEcoScoreBadges() {
        setupEcoScoreItem(R.id.ecoscoreA, "A", 0xFF1B5E20,
                getString(R.string.howto_ecoscore_a));
        setupEcoScoreItem(R.id.ecoscoreB, "B", 0xFF558B2F,
                getString(R.string.howto_ecoscore_b));
        setupEcoScoreItem(R.id.ecoscoreC, "C", 0xFFF9A825,
                getString(R.string.howto_ecoscore_c));
        setupEcoScoreItem(R.id.ecoscoreD, "D", 0xFFE65100,
                getString(R.string.howto_ecoscore_d));
        setupEcoScoreItem(R.id.ecoscoreE, "E", 0xFFB71C1C,
                getString(R.string.howto_ecoscore_e));
    }

    private void setupEcoScoreItem(int containerId, String letter, int color, String text) {
        View container = findViewById(containerId);
        if (container == null) return;

        TextView badge = container.findViewById(R.id.ecoscore_badge);
        TextView textView = container.findViewById(R.id.ecoscore_text);

        badge.setText(letter);
        textView.setText(text);

        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setColor(color);
        badge.setBackground(drawable);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_howto, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_home) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
            return true;
        } else if (id == R.id.action_history) {
            startActivity(new Intent(this, HistoryActivity.class));
            return true;
        } else if (id == R.id.action_favorites) {
            startActivity(new Intent(this, FavoritesActivity.class));
            return true;
        } else if (id == R.id.action_about) {
            startActivity(new Intent(this, AboutActivity.class));
            return true;
        } else if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
