package it.faustobe.ecodes.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import it.faustobe.ecodes.R;
import it.faustobe.ecodes.data.CustomAllergenManager;
import it.faustobe.ecodes.data.ProfileManager;

public class ProfileActivity extends BaseActivity {
    private static final String PREFS_NAME = "app_prefs";
    private static final String KEY_LANGUAGE = "language";

    private ProfileManager profileManager;
    private SharedPreferences prefs;
    private CustomAllergenManager customAllergenManager;

    private SwitchMaterial switchVegan;
    private SwitchMaterial switchVegetarian;
    private SwitchMaterial switchHalal;
    private RadioGroup radioGroupCaution;
    private RadioGroup radioGroupLanguage;
    private MaterialButton btnClearProfile;
    private LinearLayout customAllergensContainer;
    private MaterialButton btnAddCustomAllergen;

    // Allergen switches mapped by type
    private java.util.Map<String, SwitchMaterial> allergenSwitches = new java.util.HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        profileManager = new ProfileManager(this);
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        customAllergenManager = new CustomAllergenManager(this);

        // Initialize views
        switchVegan = findViewById(R.id.switchVegan);
        switchVegetarian = findViewById(R.id.switchVegetarian);
        switchHalal = findViewById(R.id.switchHalal);
        radioGroupCaution = findViewById(R.id.radioGroupCaution);
        radioGroupLanguage = findViewById(R.id.radioGroupLanguage);
        btnClearProfile = findViewById(R.id.btnClearProfile);
        customAllergensContainer = findViewById(R.id.customAllergensContainer);
        btnAddCustomAllergen = findViewById(R.id.btnAddCustomAllergen);

        // Initialize allergen switches
        allergenSwitches.put("lactose", findViewById(R.id.switchAllergenLactose));
        allergenSwitches.put("gluten", findViewById(R.id.switchAllergenGluten));
        allergenSwitches.put("eggs", findViewById(R.id.switchAllergenEggs));
        allergenSwitches.put("peanuts", findViewById(R.id.switchAllergenPeanuts));
        allergenSwitches.put("tree_nuts", findViewById(R.id.switchAllergenTreeNuts));
        allergenSwitches.put("fish", findViewById(R.id.switchAllergenFish));
        allergenSwitches.put("crustaceans", findViewById(R.id.switchAllergenCrustaceans));
        allergenSwitches.put("mollusks", findViewById(R.id.switchAllergenMollusks));
        allergenSwitches.put("soy", findViewById(R.id.switchAllergenSoy));
        allergenSwitches.put("celery", findViewById(R.id.switchAllergenCelery));
        allergenSwitches.put("mustard", findViewById(R.id.switchAllergenMustard));
        allergenSwitches.put("sesame", findViewById(R.id.switchAllergenSesame));
        allergenSwitches.put("lupin", findViewById(R.id.switchAllergenLupin));
        allergenSwitches.put("sulfites", findViewById(R.id.switchAllergenSulfites));
        allergenSwitches.put("nickel", findViewById(R.id.switchAllergenNickel));

        // Load current profile
        loadProfile();

        // Setup listeners
        setupListeners();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.profile_title);
        }
    }

    private void loadProfile() {
        // Dietary preferences
        switchVegan.setChecked(profileManager.isVegan());
        switchVegetarian.setChecked(profileManager.isVegetarian());
        switchHalal.setChecked(profileManager.isHalal());

        // Caution level
        int cautionLevel = profileManager.getCautionLevel();
        switch (cautionLevel) {
            case ProfileManager.CAUTION_STRICT:
                ((RadioButton) findViewById(R.id.radioCautionStrict)).setChecked(true);
                break;
            case ProfileManager.CAUTION_MODERATE:
                ((RadioButton) findViewById(R.id.radioCautionModerate)).setChecked(true);
                break;
            case ProfileManager.CAUTION_PERMISSIVE:
                ((RadioButton) findViewById(R.id.radioCautionPermissive)).setChecked(true);
                break;
            case ProfileManager.CAUTION_ALL:
                ((RadioButton) findViewById(R.id.radioCautionAll)).setChecked(true);
                break;
        }

        // Allergens
        for (java.util.Map.Entry<String, SwitchMaterial> entry : allergenSwitches.entrySet()) {
            entry.getValue().setChecked(profileManager.hasAllergen(entry.getKey()));
        }

        // Language
        String currentLang = prefs.getString(KEY_LANGUAGE, "it");
        if ("en".equals(currentLang)) {
            ((RadioButton) findViewById(R.id.radioLangEnglish)).setChecked(true);
        } else {
            ((RadioButton) findViewById(R.id.radioLangItalian)).setChecked(true);
        }

        // Custom allergens
        loadCustomAllergens();
    }

    private void loadCustomAllergens() {
        customAllergensContainer.removeAllViews();

        java.util.List<CustomAllergenManager.CustomAllergen> customAllergens =
            customAllergenManager.getAllCustomAllergens();

        for (CustomAllergenManager.CustomAllergen allergen : customAllergens) {
            addCustomAllergenView(allergen);
        }
    }

    private void addCustomAllergenView(CustomAllergenManager.CustomAllergen allergen) {
        SwitchMaterial switchView = new SwitchMaterial(this);
        switchView.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT));

        // Costruisci il testo: nome + eventuali sinonimi (senza ripetere il nome)
        String displayText = allergen.name;
        if (allergen.keywords.size() > 1) {
            // Filtra le keywords rimuovendo quella uguale al nome
            java.util.List<String> synonyms = new java.util.ArrayList<>();
            for (String kw : allergen.keywords) {
                if (!kw.equalsIgnoreCase(allergen.name)) {
                    synonyms.add(kw);
                }
            }
            if (!synonyms.isEmpty()) {
                displayText += " (" + String.join(", ", synonyms) + ")";
            }
        }

        switchView.setText(displayText);
        switchView.setTextSize(14);
        switchView.setPadding(24, 24, 24, 24);
        switchView.setChecked(allergen.enabled);

        switchView.setOnCheckedChangeListener((buttonView, isChecked) -> {
            customAllergenManager.setCustomAllergenEnabled(allergen.id, isChecked);
            showSavedToast();
        });

        customAllergensContainer.addView(switchView);
    }

    private void setupListeners() {
        // Dietary preferences
        switchVegan.setOnCheckedChangeListener((buttonView, isChecked) -> {
            profileManager.setVegan(isChecked);
            showSavedToast();
        });

        switchVegetarian.setOnCheckedChangeListener((buttonView, isChecked) -> {
            profileManager.setVegetarian(isChecked);
            showSavedToast();
        });

        switchHalal.setOnCheckedChangeListener((buttonView, isChecked) -> {
            profileManager.setHalal(isChecked);
            showSavedToast();
        });

        // Caution level
        radioGroupCaution.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioCautionStrict) {
                profileManager.setCautionLevel(ProfileManager.CAUTION_STRICT);
            } else if (checkedId == R.id.radioCautionModerate) {
                profileManager.setCautionLevel(ProfileManager.CAUTION_MODERATE);
            } else if (checkedId == R.id.radioCautionPermissive) {
                profileManager.setCautionLevel(ProfileManager.CAUTION_PERMISSIVE);
            } else if (checkedId == R.id.radioCautionAll) {
                profileManager.setCautionLevel(ProfileManager.CAUTION_ALL);
            }
            showSavedToast();
        });

        // Allergens - setup listeners for all
        for (java.util.Map.Entry<String, SwitchMaterial> entry : allergenSwitches.entrySet()) {
            String allergenType = entry.getKey();
            entry.getValue().setOnCheckedChangeListener((buttonView, isChecked) -> {
                profileManager.setAllergen(allergenType, isChecked);
                showSavedToast();
            });
        }

        // Language selector
        radioGroupLanguage.setOnCheckedChangeListener((group, checkedId) -> {
            String newLang;
            if (checkedId == R.id.radioLangEnglish) {
                newLang = "en";
            } else {
                newLang = "it";
            }
            prefs.edit().putString(KEY_LANGUAGE, newLang).apply();

            // Recreate activity to apply new language immediately
            recreate();
        });

        // Clear profile button
        btnClearProfile.setOnClickListener(v -> showClearProfileDialog());

        // Add custom allergen button
        btnAddCustomAllergen.setOnClickListener(v -> showAddCustomAllergenDialog());
    }

    private void showAddCustomAllergenDialog() {
        final View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_custom_allergen, null);

        AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle(R.string.custom_allergen_dialog_title)
            .setView(dialogView)
            .setPositiveButton(R.string.action_add, null)
            .setNegativeButton(R.string.action_cancel, null)
            .create();

        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                com.google.android.material.textfield.TextInputEditText editAllergen =
                    dialogView.findViewById(R.id.editCustomAllergen);

                String input = "";
                if (editAllergen != null && editAllergen.getText() != null) {
                    input = editAllergen.getText().toString().trim();
                }

                if (input.isEmpty()) {
                    Toast.makeText(this, R.string.custom_allergen_required, Toast.LENGTH_SHORT).show();
                    return;
                }

                // Usa la prima parola come nome, tutto l'input come keywords
                String name = input.contains(",") ? input.split(",")[0].trim() : input;
                String keywords = input;

                customAllergenManager.addCustomAllergen(name, keywords);
                loadCustomAllergens();
                showSavedToast();
                dialog.dismiss();
            });
        });

        dialog.show();
    }

    private void showSavedToast() {
        Toast.makeText(this, R.string.profile_saved, Toast.LENGTH_SHORT).show();
    }

    private void showClearProfileDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.profile_clear_title)
                .setMessage(R.string.profile_clear_message)
                .setPositiveButton(R.string.profile_clear_confirm, (dialog, which) -> {
                    profileManager.clearProfile();
                    customAllergenManager.clearAll();
                    loadProfile(); // Reload default values
                    Toast.makeText(this, R.string.profile_cleared, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_profile, menu);
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
