package it.faustobe.ecodes.data;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Gestisce gli allergeni/intolleranze personalizzate dell'utente.
 * Storage in SharedPreferences come JSON per semplicità e scalabilità.
 */
public class CustomAllergenManager {
    private static final String PREFS_NAME = "custom_allergens";
    private static final String KEY_ALLERGENS = "allergens_list";

    private SharedPreferences prefs;

    public CustomAllergenManager(Context context) {
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Rappresenta un allergene personalizzato
     */
    public static class CustomAllergen {
        public String id;
        public String name;
        public List<String> keywords;
        public boolean enabled;

        public CustomAllergen(String id, String name, List<String> keywords, boolean enabled) {
            this.id = id;
            this.name = name;
            this.keywords = keywords;
            this.enabled = enabled;
        }

        public JSONObject toJson() {
            try {
                JSONObject json = new JSONObject();
                json.put("id", id);
                json.put("name", name);
                json.put("keywords", new JSONArray(keywords));
                json.put("enabled", enabled);
                return json;
            } catch (Exception e) {
                return null;
            }
        }

        public static CustomAllergen fromJson(JSONObject json) {
            try {
                String id = json.getString("id");
                String name = json.getString("name");
                boolean enabled = json.optBoolean("enabled", true);

                List<String> keywords = new ArrayList<>();
                JSONArray keywordsArray = json.getJSONArray("keywords");
                for (int i = 0; i < keywordsArray.length(); i++) {
                    keywords.add(keywordsArray.getString(i).toLowerCase().trim());
                }

                return new CustomAllergen(id, name, keywords, enabled);
            } catch (Exception e) {
                return null;
            }
        }
    }

    /**
     * Ottiene tutti gli allergeni personalizzati
     */
    public List<CustomAllergen> getAllCustomAllergens() {
        List<CustomAllergen> allergens = new ArrayList<>();
        String json = prefs.getString(KEY_ALLERGENS, "[]");

        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                CustomAllergen allergen = CustomAllergen.fromJson(array.getJSONObject(i));
                if (allergen != null) {
                    allergens.add(allergen);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return allergens;
    }

    /**
     * Ottiene solo gli allergeni abilitati
     */
    public List<CustomAllergen> getEnabledCustomAllergens() {
        List<CustomAllergen> all = getAllCustomAllergens();
        List<CustomAllergen> enabled = new ArrayList<>();
        for (CustomAllergen allergen : all) {
            if (allergen.enabled) {
                enabled.add(allergen);
            }
        }
        return enabled;
    }

    /**
     * Aggiunge un nuovo allergene personalizzato
     */
    public CustomAllergen addCustomAllergen(String name, String keywordsString) {
        // Genera ID univoco
        String id = "custom_" + System.currentTimeMillis();

        // Parse keywords
        List<String> keywords = parseKeywords(keywordsString);

        // Crea allergene
        CustomAllergen allergen = new CustomAllergen(id, name.trim(), keywords, true);

        // Salva
        List<CustomAllergen> allergens = getAllCustomAllergens();
        allergens.add(allergen);
        saveAllergens(allergens);

        return allergen;
    }

    /**
     * Rimuove un allergene personalizzato
     */
    public void removeCustomAllergen(String id) {
        List<CustomAllergen> allergens = getAllCustomAllergens();
        allergens.removeIf(a -> a.id.equals(id));
        saveAllergens(allergens);
    }

    /**
     * Abilita/disabilita un allergene personalizzato
     */
    public void setCustomAllergenEnabled(String id, boolean enabled) {
        List<CustomAllergen> allergens = getAllCustomAllergens();
        for (CustomAllergen allergen : allergens) {
            if (allergen.id.equals(id)) {
                allergen.enabled = enabled;
                break;
            }
        }
        saveAllergens(allergens);
    }

    /**
     * Verifica se ci sono allergeni custom abilitati
     */
    public boolean hasEnabledCustomAllergens() {
        return !getEnabledCustomAllergens().isEmpty();
    }

    /**
     * Parse delle keywords da stringa separata da virgola
     */
    private List<String> parseKeywords(String keywordsString) {
        List<String> keywords = new ArrayList<>();
        if (keywordsString != null && !keywordsString.isEmpty()) {
            String[] parts = keywordsString.split(",");
            for (String part : parts) {
                String trimmed = part.trim().toLowerCase();
                if (!trimmed.isEmpty()) {
                    keywords.add(trimmed);
                }
            }
        }
        return keywords;
    }

    /**
     * Salva la lista di allergeni
     */
    private void saveAllergens(List<CustomAllergen> allergens) {
        try {
            JSONArray array = new JSONArray();
            for (CustomAllergen allergen : allergens) {
                JSONObject json = allergen.toJson();
                if (json != null) {
                    array.put(json);
                }
            }
            prefs.edit().putString(KEY_ALLERGENS, array.toString()).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Cancella tutti gli allergeni personalizzati
     */
    public void clearAll() {
        prefs.edit().clear().apply();
    }
}
