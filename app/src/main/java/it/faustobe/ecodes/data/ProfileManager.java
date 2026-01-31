package it.faustobe.ecodes.data;

import android.content.Context;
import android.content.SharedPreferences;
import it.faustobe.ecodes.model.Additive;
import java.util.HashSet;
import java.util.Set;

/**
 * Gestisce il profilo utente con preferenze dietetiche e allergie.
 * Privacy-first: tutti i dati sono salvati SOLO localmente su SharedPreferences.
 * Nessun dato viene mai inviato a server esterni.
 */
public class ProfileManager {
    private static final String PREF_NAME = "user_profile";
    private static final String KEY_VEGAN = "vegan";
    private static final String KEY_VEGETARIAN = "vegetarian";
    private static final String KEY_HALAL = "halal";
    private static final String KEY_CAUTION_LEVEL = "caution_level";
    // Allergen keys (14 EU mandatory allergens + nickel)
    public static final String[] ALLERGEN_TYPES = {
        "lactose", "gluten", "eggs", "peanuts", "tree_nuts",
        "fish", "crustaceans", "mollusks", "soy", "celery",
        "mustard", "sesame", "lupin", "sulfites", "nickel"
    };

    public static final int CAUTION_STRICT = 0;      // Solo A
    public static final int CAUTION_MODERATE = 1;    // A + B
    public static final int CAUTION_PERMISSIVE = 2;  // A + B + C
    public static final int CAUTION_ALL = 3;         // Tutti

    private SharedPreferences prefs;

    public ProfileManager(Context context) {
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // Dietary preferences
    public void setVegan(boolean enabled) {
        prefs.edit().putBoolean(KEY_VEGAN, enabled).apply();
    }

    public boolean isVegan() {
        return prefs.getBoolean(KEY_VEGAN, false);
    }

    public void setVegetarian(boolean enabled) {
        prefs.edit().putBoolean(KEY_VEGETARIAN, enabled).apply();
    }

    public boolean isVegetarian() {
        return prefs.getBoolean(KEY_VEGETARIAN, false);
    }

    public void setHalal(boolean enabled) {
        prefs.edit().putBoolean(KEY_HALAL, enabled).apply();
    }

    public boolean isHalal() {
        return prefs.getBoolean(KEY_HALAL, false);
    }

    // Caution level
    public void setCautionLevel(int level) {
        prefs.edit().putInt(KEY_CAUTION_LEVEL, level).apply();
    }

    public int getCautionLevel() {
        return prefs.getInt(KEY_CAUTION_LEVEL, CAUTION_ALL);
    }

    // Generic allergen methods
    public void setAllergen(String allergenType, boolean enabled) {
        prefs.edit().putBoolean("allergen_" + allergenType, enabled).apply();
    }

    public boolean hasAllergen(String allergenType) {
        return prefs.getBoolean("allergen_" + allergenType, false);
    }

    // Legacy methods for backward compatibility
    public void setAllergenLactose(boolean enabled) { setAllergen("lactose", enabled); }
    public boolean hasAllergenLactose() { return hasAllergen("lactose"); }
    public void setAllergenGluten(boolean enabled) { setAllergen("gluten", enabled); }
    public boolean hasAllergenGluten() { return hasAllergen("gluten"); }
    public void setAllergenSulfites(boolean enabled) { setAllergen("sulfites", enabled); }
    public boolean hasAllergenSulfites() { return hasAllergen("sulfites"); }
    public void setAllergenNickel(boolean enabled) { setAllergen("nickel", enabled); }
    public boolean hasAllergenNickel() { return hasAllergen("nickel"); }

    // Check if any allergen is enabled
    public boolean hasAnyAllergenEnabled() {
        for (String type : ALLERGEN_TYPES) {
            if (hasAllergen(type)) return true;
        }
        return false;
    }

    // Reset all preferences
    public void clearProfile() {
        prefs.edit().clear().apply();
    }

    // Check if profile is configured
    public boolean hasActiveProfile() {
        return isVegan() || isVegetarian() || isHalal() ||
               hasAnyAllergenEnabled() ||
               getCautionLevel() != CAUTION_ALL;
    }

    /**
     * Verifica se un additivo è conforme al profilo utente
     * @return Alert message se non conforme, null se ok
     */
    public String checkAdditiveCompliance(Additive additive) {
        // Check dietary restrictions
        if (isVegan() && additive.getVegan() == 0) {
            return "⚠️ NON VEGANO";
        }

        if (isHalal() && additive.getHalal() == 0) {
            return "⚠️ NON HALAL";
        }

        // Check caution level
        int cautionLevel = getCautionLevel();
        String classification = additive.getClassification();

        if (cautionLevel == CAUTION_STRICT && !classification.equals("A")) {
            return "⚠️ SUPERA LIVELLO DI CAUTELA (solo A ammesso)";
        } else if (cautionLevel == CAUTION_MODERATE &&
                   !classification.equals("A") && !classification.equals("B")) {
            return "⚠️ SUPERA LIVELLO DI CAUTELA (solo A/B ammessi)";
        } else if (cautionLevel == CAUTION_PERMISSIVE &&
                   !classification.equals("A") && !classification.equals("B") &&
                   !classification.equals("C")) {
            return "⚠️ SUPERA LIVELLO DI CAUTELA (solo A/B/C ammessi)";
        }

        // Note: Allergen checks are handled by AllergenDetector, not here

        return null; // Compliant
    }

    /**
     * Conta quanti additivi in una lista NON sono conformi
     */
    public int countNonCompliantAdditives(java.util.List<Additive> additives) {
        int count = 0;
        for (Additive additive : additives) {
            if (checkAdditiveCompliance(additive) != null) {
                count++;
            }
        }
        return count;
    }
}
