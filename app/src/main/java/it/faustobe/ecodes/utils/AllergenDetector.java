package it.faustobe.ecodes.utils;

import android.content.Context;
import android.content.SharedPreferences;
import it.faustobe.ecodes.R;
import it.faustobe.ecodes.data.CustomAllergenManager;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class AllergenDetector {
    private static final String PREFS_NAME_PROFILE = "user_profile";
    private static final String PREFS_NAME_APP = "app_prefs";
    private static final String KEY_LANGUAGE = "language";

    private Context context;
    private List<JSONObject> allergensDataList;
    private SharedPreferences profilePrefs;
    private SharedPreferences appPrefs;
    private CustomAllergenManager customAllergenManager;

    public AllergenDetector(Context context) {
        this.context = context;
        this.profilePrefs = context.getSharedPreferences(PREFS_NAME_PROFILE, Context.MODE_PRIVATE);
        this.appPrefs = context.getSharedPreferences(PREFS_NAME_APP, Context.MODE_PRIVATE);
        this.customAllergenManager = new CustomAllergenManager(context);
        this.allergensDataList = new ArrayList<>();
        loadAllergensData();
    }

    private void loadAllergensData() {
        // Carica AUTOMATICAMENTE tutti i file JSON nella cartella allergens/
        // Aggiungere una nuova lingua = solo aggiungere il file, ZERO modifiche al codice
        try {
            String[] files = context.getAssets().list("allergens");
            if (files != null) {
                for (String filename : files) {
                    if (filename.endsWith(".json")) {
                        try {
                            InputStream is = context.getAssets().open("allergens/" + filename);
                            int size = is.available();
                            byte[] buffer = new byte[size];
                            is.read(buffer);
                            is.close();
                            String json = new String(buffer, StandardCharsets.UTF_8);
                            allergensDataList.add(new JSONObject(json));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<AllergenMatch> detectAllergens(String ingredientsText, List<String> eCodes) {
        List<AllergenMatch> matches = new ArrayList<>();

        if (ingredientsText == null) {
            ingredientsText = "";
        }

        // Convert to lowercase for case-insensitive matching
        String ingredientsLower = ingredientsText.toLowerCase();

        // Check each allergen category (14 EU mandatory allergens + nickel)
        String[] allergenTypes = {
            "lactose", "gluten", "eggs", "peanuts", "tree_nuts",
            "fish", "crustaceans", "mollusks", "soy", "celery",
            "mustard", "sesame", "lupin", "sulfites", "nickel"
        };

        for (String allergenType : allergenTypes) {
            if (profilePrefs.getBoolean("allergen_" + allergenType, false)) {
                AllergenMatch match = checkAllergen(allergenType, ingredientsLower, eCodes);
                if (match != null) {
                    matches.add(match);
                }
            }
        }

        // Check custom allergens
        List<CustomAllergenManager.CustomAllergen> customAllergens = customAllergenManager.getEnabledCustomAllergens();
        for (CustomAllergenManager.CustomAllergen custom : customAllergens) {
            AllergenMatch match = checkCustomAllergen(custom, ingredientsLower);
            if (match != null) {
                matches.add(match);
            }
        }

        return matches;
    }

    private AllergenMatch checkCustomAllergen(CustomAllergenManager.CustomAllergen custom, String ingredientsLower) {
        Set<String> foundKeywords = new HashSet<>();

        for (String keyword : custom.keywords) {
            if (ingredientsLower.contains(keyword.toLowerCase())) {
                foundKeywords.add(keyword);
            }
        }

        if (!foundKeywords.isEmpty()) {
            return new AllergenMatch(custom.id, custom.name, foundKeywords, new HashSet<>());
        }

        return null;
    }

    private AllergenMatch checkAllergen(String allergenType, String ingredientsLower, List<String> eCodes) {
        Set<String> foundKeywords = new HashSet<>();
        Set<String> foundECodes = new HashSet<>();

        // Cerca in TUTTE le lingue caricate per rilevare allergeni indipendentemente dalla lingua degli ingredienti
        for (JSONObject allergensData : allergensDataList) {
            checkAllergenInLanguage(allergensData, allergenType, ingredientsLower, eCodes, foundKeywords, foundECodes);
        }

        // If any match found, return AllergenMatch
        if (!foundKeywords.isEmpty() || !foundECodes.isEmpty()) {
            return new AllergenMatch(allergenType, foundKeywords, foundECodes);
        }

        return null;
    }

    private void checkAllergenInLanguage(JSONObject allergensData, String allergenType,
                                         String ingredientsLower, List<String> eCodes,
                                         Set<String> foundKeywords, Set<String> foundECodes) {
        try {
            if (!allergensData.has(allergenType)) {
                return;
            }

            JSONObject allergen = allergensData.getJSONObject(allergenType);

            // Load exclusion patterns first
            Set<String> exclusions = new HashSet<>();
            if (allergen.has("exclusions")) {
                JSONArray exclusionsArray = allergen.getJSONArray("exclusions");
                for (int i = 0; i < exclusionsArray.length(); i++) {
                    exclusions.add(exclusionsArray.getString(i).toLowerCase());
                }
            }

            // Check if any exclusion pattern is present in ingredients
            boolean hasExclusion = false;
            for (String exclusion : exclusions) {
                if (ingredientsLower.contains(exclusion)) {
                    hasExclusion = true;
                    break;
                }
            }

            // If exclusion found, skip keyword detection for this allergen
            // (E-codes are still checked as they are more specific)
            if (!hasExclusion && allergen.has("keywords")) {
                JSONArray keywords = allergen.getJSONArray("keywords");
                for (int i = 0; i < keywords.length(); i++) {
                    String keyword = keywords.getString(i).toLowerCase();
                    // Use word boundary matching to avoid false positives
                    if (matchesAsWholeWord(ingredientsLower, keyword)) {
                        foundKeywords.add(keyword);
                    }
                }
            }

            // Check E-codes (always check, even with exclusions, as E-codes are specific)
            if (allergen.has("e_codes") && eCodes != null) {
                JSONArray allergenECodes = allergen.getJSONArray("e_codes");
                for (int i = 0; i < allergenECodes.length(); i++) {
                    String eCode = allergenECodes.getString(i);
                    for (String productECode : eCodes) {
                        if (productECode.equalsIgnoreCase(eCode)) {
                            foundECodes.add(eCode);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Check if a keyword appears as a whole word in the text.
     * This prevents false positives like "latte" in "scarlatto".
     */
    private boolean matchesAsWholeWord(String text, String keyword) {
        // For multi-word keywords (e.g., "siero di latte"), use simple contains
        if (keyword.contains(" ")) {
            return text.contains(keyword);
        }

        // For single-word keywords, use word boundary matching
        // Word boundaries: start/end of string, spaces, punctuation, parentheses, etc.
        String regex = "(?<![a-zàèéìòùáéíóú])" + Pattern.quote(keyword) + "(?![a-zàèéìòùáéíóú])";
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(text);
        return matcher.find();
    }

    public static class AllergenMatch {
        public String allergenType;
        public String customName; // For custom allergens
        public Set<String> foundKeywords;
        public Set<String> foundECodes;

        public AllergenMatch(String allergenType, Set<String> foundKeywords, Set<String> foundECodes) {
            this.allergenType = allergenType;
            this.customName = null;
            this.foundKeywords = foundKeywords;
            this.foundECodes = foundECodes;
        }

        public AllergenMatch(String allergenType, String customName, Set<String> foundKeywords, Set<String> foundECodes) {
            this.allergenType = allergenType;
            this.customName = customName;
            this.foundKeywords = foundKeywords;
            this.foundECodes = foundECodes;
        }

        public String getAllergenName(Context context) {
            // If custom name is set, return it
            if (customName != null) {
                return customName;
            }

            // Map allergen type to user-friendly name
            switch (allergenType) {
                case "lactose":
                    return context.getString(R.string.profile_allergen_lactose);
                case "gluten":
                    return context.getString(R.string.profile_allergen_gluten);
                case "eggs":
                    return context.getString(R.string.profile_allergen_eggs);
                case "peanuts":
                    return context.getString(R.string.profile_allergen_peanuts);
                case "tree_nuts":
                    return context.getString(R.string.profile_allergen_tree_nuts);
                case "fish":
                    return context.getString(R.string.profile_allergen_fish);
                case "crustaceans":
                    return context.getString(R.string.profile_allergen_crustaceans);
                case "mollusks":
                    return context.getString(R.string.profile_allergen_mollusks);
                case "soy":
                    return context.getString(R.string.profile_allergen_soy);
                case "celery":
                    return context.getString(R.string.profile_allergen_celery);
                case "mustard":
                    return context.getString(R.string.profile_allergen_mustard);
                case "sesame":
                    return context.getString(R.string.profile_allergen_sesame);
                case "lupin":
                    return context.getString(R.string.profile_allergen_lupin);
                case "sulfites":
                    return context.getString(R.string.profile_allergen_sulfites);
                case "nickel":
                    return context.getString(R.string.profile_allergen_nickel);
                default:
                    return allergenType;
            }
        }
    }
}
