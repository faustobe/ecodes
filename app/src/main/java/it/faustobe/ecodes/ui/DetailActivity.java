package it.faustobe.ecodes.ui;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import it.faustobe.ecodes.R;
import it.faustobe.ecodes.data.DatabaseHelper;
import it.faustobe.ecodes.model.Additive;

public class DetailActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        String code = getIntent().getStringExtra("code");

        // Get full additive data from database
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        Additive additive = dbHelper.getAdditiveByCode(code);

        if (additive == null) {
            finish();
            return;
        }

        String name = additive.getName();
        String classification = additive.getClassification();

        TextView codeText = findViewById(R.id.detailCode);
        TextView nameText = findViewById(R.id.detailName);
        TextView classificationText = findViewById(R.id.detailClassification);
        View dangerBarTrack = findViewById(R.id.dangerBarTrack);
        View dangerBarFill = findViewById(R.id.dangerBarFill);

        codeText.setText(code);
        nameText.setText(name);

        String classText = "";
        float dangerLevel = 0;
        int barColor = 0;

        if (classification == null) classification = "N";

        switch (classification) {
            case "A":
                classText = getString(R.string.classification_a_long);
                dangerLevel = 0.12f;
                barColor = 0xFF4CAF50; // Green
                break;
            case "B":
                classText = getString(R.string.classification_b_long);
                dangerLevel = 0.28f;
                barColor = 0xFF8BC34A; // Light green
                break;
            case "C":
                classText = getString(R.string.classification_c_long);
                dangerLevel = 0.45f;
                barColor = 0xFFFFEB3B; // Yellow
                break;
            case "D":
                classText = getString(R.string.classification_d_long);
                dangerLevel = 0.65f;
                barColor = 0xFFFF9800; // Orange
                break;
            case "E":
                classText = getString(R.string.classification_e_long);
                dangerLevel = 0.85f;
                barColor = 0xFFF44336; // Red
                break;
            case "F":
                classText = getString(R.string.classification_f_long);
                dangerLevel = 1.0f;
                barColor = 0xFFB71C1C; // Dark red
                break;
            case "N":
                classText = getString(R.string.classification_n_long);
                dangerLevel = 0.08f;
                barColor = 0xFF9E9E9E; // Gray
                break;
            default:
                classText = getString(R.string.classification_unknown_long);
                dangerLevel = 0.08f;
                barColor = 0xFF757575;
                break;
        }

        classificationText.setText(classText);

        // Setup danger bar
        final float level = dangerLevel;
        final int color = barColor;
        dangerBarTrack.getViewTreeObserver().addOnGlobalLayoutListener(
            new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    dangerBarTrack.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    int trackWidth = dangerBarTrack.getWidth();
                    int fillWidth = (int) (trackWidth * level);

                    ViewGroup.LayoutParams params = dangerBarFill.getLayoutParams();
                    params.width = fillWidth;
                    dangerBarFill.setLayoutParams(params);

                    GradientDrawable fillDrawable = new GradientDrawable();
                    fillDrawable.setShape(GradientDrawable.RECTANGLE);
                    fillDrawable.setCornerRadius(12f);
                    fillDrawable.setColor(color);
                    dangerBarFill.setBackground(fillDrawable);
                }
            }
        );

        // Show additional information
        showAdditionalInfo(additive);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.detail_title);
        }
    }

    private void showAdditionalInfo(Additive additive) {
        int visibleSections = 0;

        // ADI
        View adiLayout = findViewById(R.id.adiLayout);
        TextView adiText = findViewById(R.id.adiText);
        Float adiValue = additive.getAdi();
        if (adiValue != null) {
            if (visibleSections > 0) showDivider(visibleSections);

            // Formatta il valore numerico con unità di misura
            adiText.setText(String.format("%.1f mg/kg", adiValue));

            adiLayout.setVisibility(View.VISIBLE);
            visibleSections++;
        }

        // Dietary Info
        View dietarySection = findViewById(R.id.dietarySection);
        View veganLayout = findViewById(R.id.veganLayout);
        View halalLayout = findViewById(R.id.halalLayout);
        View allergenLayout = findViewById(R.id.allergenLayout);
        TextView veganText = findViewById(R.id.veganText);
        TextView halalText = findViewById(R.id.halalText);

        boolean hasDietaryInfo = false;

        // Vegan status - mostra sempre
        int vegan = additive.getVegan();
        veganText.setText(vegan == 1 ? getString(R.string.yes) : (vegan == 0 ? getString(R.string.no) : getString(R.string.unknown)));
        veganText.setTextColor(vegan == 1 ? 0xFF4CAF50 : (vegan == 0 ? 0xFFF44336 : 0xFFFF9800));
        veganLayout.setVisibility(View.VISIBLE);
        hasDietaryInfo = true;

        // Halal status - mostra sempre
        int halal = additive.getHalal();
        halalText.setText(halal == 1 ? getString(R.string.yes) : (halal == 0 ? getString(R.string.no) : getString(R.string.unknown)));
        halalText.setTextColor(halal == 1 ? 0xFF4CAF50 : (halal == 0 ? 0xFFF44336 : 0xFFFF9800));
        halalLayout.setVisibility(View.VISIBLE);
        hasDietaryInfo = true;

        // Allergen risk
        if (additive.getAllergenRisk() == 1) {
            allergenLayout.setVisibility(View.VISIBLE);
            hasDietaryInfo = true;
        }

        if (hasDietaryInfo) {
            if (visibleSections > 0) showDivider(visibleSections);
            dietarySection.setVisibility(View.VISIBLE);
            visibleSections++;
        }

        // EFSA Status
        View efsaLayout = findViewById(R.id.efsaLayout);
        TextView efsaText = findViewById(R.id.efsaText);
        if (additive.getEfsaStatus() != null && !additive.getEfsaStatus().isEmpty()) {
            if (visibleSections > 0) showDivider(visibleSections);
            String efsaStatus = "";
            switch (additive.getEfsaStatus()) {
                case "approved":
                    efsaStatus = getString(R.string.efsa_approved);
                    efsaText.setTextColor(0xFF4CAF50);
                    break;
                case "under_review":
                    efsaStatus = getString(R.string.efsa_under_review);
                    efsaText.setTextColor(0xFFFF9800);
                    break;
                case "banned":
                    efsaStatus = getString(R.string.efsa_banned);
                    efsaText.setTextColor(0xFFF44336);
                    break;
                case "caution":
                    efsaStatus = getString(R.string.efsa_caution);
                    efsaText.setTextColor(0xFFFF9800);
                    break;
                default:
                    efsaStatus = additive.getEfsaStatus();
                    efsaText.setTextColor(0xFF212121);
            }
            efsaText.setText(efsaStatus);
            efsaLayout.setVisibility(View.VISIBLE);
            visibleSections++;
        }

        // Notes
        View notesLayout = findViewById(R.id.notesLayout);
        TextView notesText = findViewById(R.id.notesText);
        if (additive.getNotes() != null && !additive.getNotes().isEmpty()) {
            if (visibleSections > 0) showDivider(visibleSections);
            notesText.setText(additive.getNotes());
            notesLayout.setVisibility(View.VISIBLE);
            visibleSections++;
        }
    }

    private void showDivider(int sectionIndex) {
        // Mostra il divider PRIMA della sezione corrente
        // sectionIndex 1 -> divider1, sectionIndex 2 -> divider2, etc.
        int dividerId = 0;
        switch (sectionIndex) {
            case 1: dividerId = R.id.divider1; break;
            case 2: dividerId = R.id.divider2; break;
            case 3: dividerId = R.id.divider3; break;
            case 4: dividerId = R.id.divider4; break;
        }
        if (dividerId != 0) {
            View divider = findViewById(dividerId);
            if (divider != null) {
                divider.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
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
        } else if (id == R.id.action_history) {
            startActivity(new Intent(this, HistoryActivity.class));
            return true;
        } else if (id == R.id.action_favorites) {
            startActivity(new Intent(this, FavoritesActivity.class));
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
