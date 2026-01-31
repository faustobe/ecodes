package it.faustobe.ecodes.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import it.faustobe.ecodes.model.Additive;
import it.faustobe.ecodes.model.Product;
import org.json.JSONObject;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "ecodes_database.db";
    private static final int DATABASE_VERSION = 5;  // Incrementato per aggiungere tabella translations
    private static final String PREFS_NAME = "app_prefs";
    private static final String KEY_LANGUAGE = "language";
    private Context context;
    private String databasePath;
    private Map<String, String> functionMappings;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
        this.databasePath = context.getDatabasePath(DATABASE_NAME).getPath();
        this.functionMappings = new HashMap<>();
        copyDatabase();
        createAdditionalTables();
        loadFunctionMappings();
    }

    private void createAdditionalTables() {
        SQLiteDatabase db = this.getWritableDatabase();

        // Crea tabelle cronologia e preferiti se non esistono
        db.execSQL("CREATE TABLE IF NOT EXISTS scan_history (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "barcode TEXT NOT NULL, " +
                "product_name TEXT NOT NULL, " +
                "e_codes TEXT NOT NULL, " +
                "ingredients TEXT, " +
                "scanned_at TEXT NOT NULL)");

        db.execSQL("CREATE INDEX IF NOT EXISTS idx_scan_history_date ON scan_history(scanned_at DESC)");

        db.execSQL("CREATE TABLE IF NOT EXISTS favorites (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "barcode TEXT NOT NULL UNIQUE, " +
                "product_name TEXT NOT NULL, " +
                "e_codes TEXT NOT NULL, " +
                "ingredients TEXT, " +
                "added_at TEXT NOT NULL)");

        db.execSQL("CREATE INDEX IF NOT EXISTS idx_favorites_barcode ON favorites(barcode)");

        // Aggiungi colonna ingredients se le tabelle esistevano già senza questa colonna
        try {
            Cursor cursor = db.rawQuery("PRAGMA table_info(scan_history)", null);
            boolean hasIngredientsColumn = false;
            while (cursor.moveToNext()) {
                if (cursor.getString(1).equals("ingredients")) {
                    hasIngredientsColumn = true;
                    break;
                }
            }
            cursor.close();

            if (!hasIngredientsColumn) {
                db.execSQL("ALTER TABLE scan_history ADD COLUMN ingredients TEXT");
            }

            cursor = db.rawQuery("PRAGMA table_info(favorites)", null);
            hasIngredientsColumn = false;
            while (cursor.moveToNext()) {
                if (cursor.getString(1).equals("ingredients")) {
                    hasIngredientsColumn = true;
                    break;
                }
            }
            cursor.close();

            if (!hasIngredientsColumn) {
                db.execSQL("ALTER TABLE favorites ADD COLUMN ingredients TEXT");
            }

            // Aggiungi colonne nutriscore e nova_group se non esistono
            addColumnIfNotExists(db, "scan_history", "nutriscore", "TEXT");
            addColumnIfNotExists(db, "scan_history", "nova_group", "INTEGER DEFAULT 0");
            addColumnIfNotExists(db, "favorites", "nutriscore", "TEXT");
            addColumnIfNotExists(db, "favorites", "nova_group", "INTEGER DEFAULT 0");

            // Aggiungi colonne manufacturing_places e origins se non esistono
            addColumnIfNotExists(db, "scan_history", "manufacturing_places", "TEXT");
            addColumnIfNotExists(db, "scan_history", "origins", "TEXT");
            addColumnIfNotExists(db, "favorites", "manufacturing_places", "TEXT");
            addColumnIfNotExists(db, "favorites", "origins", "TEXT");

            // Aggiungi nuove colonne per dati prodotto completi
            addColumnIfNotExists(db, "scan_history", "ecoscore", "TEXT");
            addColumnIfNotExists(db, "scan_history", "brand", "TEXT");
            addColumnIfNotExists(db, "scan_history", "quantity", "TEXT");
            addColumnIfNotExists(db, "scan_history", "labels", "TEXT");
            addColumnIfNotExists(db, "scan_history", "allergens", "TEXT");
            addColumnIfNotExists(db, "scan_history", "traces", "TEXT");
            addColumnIfNotExists(db, "scan_history", "energy_kcal", "REAL DEFAULT 0");
            addColumnIfNotExists(db, "scan_history", "fat", "REAL DEFAULT 0");
            addColumnIfNotExists(db, "scan_history", "saturated_fat", "REAL DEFAULT 0");
            addColumnIfNotExists(db, "scan_history", "carbohydrates", "REAL DEFAULT 0");
            addColumnIfNotExists(db, "scan_history", "sugars", "REAL DEFAULT 0");
            addColumnIfNotExists(db, "scan_history", "fiber", "REAL DEFAULT 0");
            addColumnIfNotExists(db, "scan_history", "proteins", "REAL DEFAULT 0");
            addColumnIfNotExists(db, "scan_history", "salt", "REAL DEFAULT 0");

            addColumnIfNotExists(db, "favorites", "ecoscore", "TEXT");
            addColumnIfNotExists(db, "favorites", "brand", "TEXT");
            addColumnIfNotExists(db, "favorites", "quantity", "TEXT");
            addColumnIfNotExists(db, "favorites", "labels", "TEXT");
            addColumnIfNotExists(db, "favorites", "allergens", "TEXT");
            addColumnIfNotExists(db, "favorites", "traces", "TEXT");
            addColumnIfNotExists(db, "favorites", "energy_kcal", "REAL DEFAULT 0");
            addColumnIfNotExists(db, "favorites", "fat", "REAL DEFAULT 0");
            addColumnIfNotExists(db, "favorites", "saturated_fat", "REAL DEFAULT 0");
            addColumnIfNotExists(db, "favorites", "carbohydrates", "REAL DEFAULT 0");
            addColumnIfNotExists(db, "favorites", "sugars", "REAL DEFAULT 0");
            addColumnIfNotExists(db, "favorites", "fiber", "REAL DEFAULT 0");
            addColumnIfNotExists(db, "favorites", "proteins", "REAL DEFAULT 0");
            addColumnIfNotExists(db, "favorites", "salt", "REAL DEFAULT 0");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addColumnIfNotExists(SQLiteDatabase db, String tableName, String columnName, String columnType) {
        Cursor cursor = db.rawQuery("PRAGMA table_info(" + tableName + ")", null);
        boolean hasColumn = false;
        while (cursor.moveToNext()) {
            if (cursor.getString(1).equals(columnName)) {
                hasColumn = true;
                break;
            }
        }
        cursor.close();
        if (!hasColumn) {
            db.execSQL("ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + columnType);
        }
    }

    private void copyDatabase() {
        try {
            java.io.File dbFile = new java.io.File(databasePath);

            // Copia il database SOLO se non esiste già
            // Questo preserva cronologia e preferiti
            if (!dbFile.exists()) {
                // Crea la directory se non esiste
                dbFile.getParentFile().mkdirs();

                // Copia il database dagli assets
                InputStream input = context.getAssets().open(DATABASE_NAME);
                OutputStream output = new FileOutputStream(databasePath);
                byte[] buffer = new byte[1024];
                int length;
                while ((length = input.read(buffer)) > 0) {
                    output.write(buffer, 0, length);
                }
                output.flush();
                output.close();
                input.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Crea tabelle cronologia e preferiti se non esistono
        db.execSQL("CREATE TABLE IF NOT EXISTS scan_history (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "barcode TEXT NOT NULL, " +
                "product_name TEXT NOT NULL, " +
                "e_codes TEXT NOT NULL, " +
                "ingredients TEXT, " +
                "scanned_at TEXT NOT NULL)");

        db.execSQL("CREATE INDEX IF NOT EXISTS idx_scan_history_date ON scan_history(scanned_at DESC)");

        db.execSQL("CREATE TABLE IF NOT EXISTS favorites (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "barcode TEXT NOT NULL UNIQUE, " +
                "product_name TEXT NOT NULL, " +
                "e_codes TEXT NOT NULL, " +
                "ingredients TEXT, " +
                "added_at TEXT NOT NULL)");

        db.execSQL("CREATE INDEX IF NOT EXISTS idx_favorites_barcode ON favorites(barcode)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Upgrade from version 2 to 3 or 3 to 4: add ingredients column if missing
        if (oldVersion < 4) {
            try {
                // Check if column exists in scan_history
                Cursor cursor = db.rawQuery("PRAGMA table_info(scan_history)", null);
                boolean hasIngredientsColumn = false;
                while (cursor.moveToNext()) {
                    if (cursor.getString(1).equals("ingredients")) {
                        hasIngredientsColumn = true;
                        break;
                    }
                }
                cursor.close();

                if (!hasIngredientsColumn) {
                    db.execSQL("ALTER TABLE scan_history ADD COLUMN ingredients TEXT");
                }

                // Check if column exists in favorites
                cursor = db.rawQuery("PRAGMA table_info(favorites)", null);
                hasIngredientsColumn = false;
                while (cursor.moveToNext()) {
                    if (cursor.getString(1).equals("ingredients")) {
                        hasIngredientsColumn = true;
                        break;
                    }
                }
                cursor.close();

                if (!hasIngredientsColumn) {
                    db.execSQL("ALTER TABLE favorites ADD COLUMN ingredients TEXT");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Upgrade to version 5: add translations table
        if (oldVersion < 5) {
            try {
                // Create translations table
                db.execSQL("CREATE TABLE IF NOT EXISTS additive_translations (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "additive_id INTEGER NOT NULL, " +
                        "language_code TEXT NOT NULL, " +
                        "notes TEXT NOT NULL, " +
                        "FOREIGN KEY (additive_id) REFERENCES additives(id), " +
                        "UNIQUE(additive_id, language_code))");

                // Create index for performance
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_translations_lookup " +
                        "ON additive_translations(additive_id, language_code)");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void loadFunctionMappings() {
        // Carica AUTOMATICAMENTE tutti i file JSON nella cartella functions/
        // Aggiungere una nuova lingua = solo aggiungere il file, ZERO modifiche al codice
        try {
            String[] files = context.getAssets().list("functions");
            if (files != null) {
                for (String filename : files) {
                    if (filename.endsWith(".json")) {
                        try {
                            InputStream is = context.getAssets().open("functions/" + filename);
                            int size = is.available();
                            byte[] buffer = new byte[size];
                            is.read(buffer);
                            is.close();
                            String json = new String(buffer, StandardCharsets.UTF_8);
                            JSONObject mappings = new JSONObject(json);

                            // Aggiungi tutti i mapping al dizionario
                            Iterator<String> keys = mappings.keys();
                            while (keys.hasNext()) {
                                String key = keys.next();
                                String value = mappings.getString(key);
                                functionMappings.put(key.toLowerCase(), value.toLowerCase());
                            }
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

    public List<Additive> getAllAdditives() {
        List<Additive> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM additives ORDER BY code", null);
        String currentLang = getCurrentLanguage();

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                String fallbackNotes = getStringOrNull(cursor, 9);
                String translatedNotes = getTranslatedNotes(id, currentLang, fallbackNotes);

                list.add(new Additive(
                    id,                    // id
                    cursor.getString(1),   // code
                    cursor.getString(2),   // name
                    cursor.getString(3),   // classification
                    getFloatOrNull(cursor, 4),  // adi (ora Float)
                    getStringOrNull(cursor, 5),  // efsa_status
                    cursor.getInt(6),      // allergen_risk
                    cursor.getInt(7),      // vegan
                    cursor.getInt(8),      // halal
                    translatedNotes,       // notes (tradotte)
                    getStringOrNull(cursor, 10)  // last_updated
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public List<Additive> searchAdditives(String query) {
        List<Additive> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Espandi la query con i mapping delle funzioni
        List<String> searchTerms = new ArrayList<>();
        searchTerms.add(query.toLowerCase());

        // Cerca PARZIALMENTE nelle chiavi del mapping
        // Se digiti "preservat", trova "preservative" → "conservante"
        for (Map.Entry<String, String> entry : functionMappings.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            // Se la chiave contiene il termine cercato (o viceversa)
            if (key.contains(query.toLowerCase()) || query.toLowerCase().contains(key)) {
                if (!searchTerms.contains(value)) {
                    searchTerms.add(value);
                }
            }
        }

        // Costruisci la query SQL dinamicamente
        StringBuilder sql = new StringBuilder("SELECT * FROM additives WHERE ");
        List<String> params = new ArrayList<>();

        boolean first = true;
        for (String term : searchTerms) {
            if (!first) {
                sql.append(" OR ");
            }
            sql.append("(code LIKE ? OR name LIKE ? OR notes LIKE ?)");
            params.add("%" + term + "%");
            params.add("%" + term + "%");
            params.add("%" + term + "%");
            first = false;
        }

        sql.append(" ORDER BY code");

        Cursor cursor = db.rawQuery(sql.toString(), params.toArray(new String[0]));
        String currentLang = getCurrentLanguage();

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                String fallbackNotes = getStringOrNull(cursor, 9);
                String translatedNotes = getTranslatedNotes(id, currentLang, fallbackNotes);

                list.add(new Additive(
                    id,                    // id
                    cursor.getString(1),   // code
                    cursor.getString(2),   // name
                    cursor.getString(3),   // classification
                    getFloatOrNull(cursor, 4),  // adi (ora Float)
                    getStringOrNull(cursor, 5),  // efsa_status
                    cursor.getInt(6),      // allergen_risk
                    cursor.getInt(7),      // vegan
                    cursor.getInt(8),      // halal
                    translatedNotes,       // notes (tradotte)
                    getStringOrNull(cursor, 10)  // last_updated
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public Additive getAdditiveByCode(String code) {
        SQLiteDatabase db = this.getReadableDatabase();
        String currentLang = getCurrentLanguage();

        // Prima cerca il codice esatto
        Additive additive = findAdditiveByExactCode(db, code, currentLang);

        // Se non trovato e il codice ha un suffisso variante (i, ii, iii, a, b, c, d),
        // prova a cercare il codice padre
        if (additive == null && code != null) {
            String parentCode = getParentCode(code);
            if (parentCode != null && !parentCode.equals(code)) {
                additive = findAdditiveByExactCode(db, parentCode, currentLang);
            }
        }

        return additive;
    }

    /**
     * Estrae il codice padre rimuovendo il suffisso variante (i, ii, iii, iv, a, b, c, d, e, f)
     * Es: E965ii -> E965, E150d -> E150, E553b -> E553
     */
    private String getParentCode(String code) {
        if (code == null || code.length() < 4) return null;

        // Pattern: E + numeri + eventuale suffisso (i, ii, iii, iv, v, vi, a, b, c, d, e, f)
        String upper = code.toUpperCase();
        if (!upper.startsWith("E")) return null;

        // Rimuovi suffissi romani (i, ii, iii, iv, v, vi) o lettere (a, b, c, d, e, f)
        String parent = upper.replaceAll("(I{1,3}|IV|V|VI|[A-F])$", "");

        // Verifica che rimanga un codice valido (E + almeno 3 cifre)
        if (parent.matches("E\\d{3,4}")) {
            return parent;
        }
        return null;
    }

    private Additive findAdditiveByExactCode(SQLiteDatabase db, String code, String currentLang) {
        Cursor cursor = db.rawQuery(
            "SELECT * FROM additives WHERE code = ? COLLATE NOCASE",
            new String[]{code}
        );

        Additive additive = null;
        if (cursor.moveToFirst()) {
            int id = cursor.getInt(0);
            String fallbackNotes = getStringOrNull(cursor, 9);
            String translatedNotes = getTranslatedNotes(id, currentLang, fallbackNotes);

            additive = new Additive(
                id,                    // id
                cursor.getString(1),   // code
                cursor.getString(2),   // name
                cursor.getString(3),   // classification
                getFloatOrNull(cursor, 4),  // adi (ora Float)
                getStringOrNull(cursor, 5),  // efsa_status
                cursor.getInt(6),      // allergen_risk
                cursor.getInt(7),      // vegan
                cursor.getInt(8),      // halal
                translatedNotes,       // notes (tradotte)
                getStringOrNull(cursor, 10)  // last_updated
            );
        }
        cursor.close();
        return additive;
    }

    private String getStringOrNull(Cursor cursor, int columnIndex) {
        return cursor.isNull(columnIndex) ? null : cursor.getString(columnIndex);
    }

    private Float getFloatOrNull(Cursor cursor, int columnIndex) {
        return cursor.isNull(columnIndex) ? null : cursor.getFloat(columnIndex);
    }

    /**
     * Recupera le note tradotte per un additivo.
     * Se non trova la traduzione nella lingua richiesta, fa fallback su italiano.
     *
     * @param additiveId ID dell'additivo
     * @param languageCode Codice lingua (es. "en", "es")
     * @param fallbackNotes Note in italiano da usare se traduzione mancante
     * @return Note tradotte o fallback
     */
    private String getTranslatedNotes(int additiveId, String languageCode, String fallbackNotes) {
        // Se la lingua è italiana, ritorna direttamente le note dal database
        if (languageCode == null || languageCode.equals("it")) {
            return fallbackNotes;
        }

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
            "SELECT notes FROM additive_translations WHERE additive_id = ? AND language_code = ?",
            new String[]{String.valueOf(additiveId), languageCode}
        );

        String translatedNotes = null;
        if (cursor.moveToFirst()) {
            translatedNotes = cursor.getString(0);
        }
        cursor.close();

        // Se non trova traduzione, ritorna le note italiane
        return translatedNotes != null ? translatedNotes : fallbackNotes;
    }

    /**
     * Ottiene il codice lingua corrente dalle SharedPreferences
     */
    private String getCurrentLanguage() {
        android.content.SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_LANGUAGE, "it");
    }

    // ==================== SCAN HISTORY (PRODUCTS) ====================

    public void addToHistory(String barcode, String productName, List<String> eCodes, String ingredients) {
        addToHistory(barcode, productName, eCodes, ingredients, null, 0, null, null);
    }

    public void addToHistory(String barcode, String productName, List<String> eCodes, String ingredients, String nutriscore, int novaGroup) {
        addToHistory(barcode, productName, eCodes, ingredients, nutriscore, novaGroup, null, null);
    }

    public void addToHistory(String barcode, String productName, List<String> eCodes, String ingredients, String nutriscore, int novaGroup, String manufacturingPlaces, String origins) {
        addToHistory(barcode, productName, eCodes, ingredients, nutriscore, novaGroup, manufacturingPlaces, origins,
                null, null, null, null, null, null, 0, 0, 0, 0, 0, 0, 0, 0);
    }

    public void addToHistory(String barcode, String productName, List<String> eCodes, String ingredients,
                             String nutriscore, int novaGroup, String manufacturingPlaces, String origins,
                             String ecoscore, String brand, String quantity, String labels, String allergens, String traces,
                             float energyKcal, float fat, float saturatedFat, float carbohydrates, float sugars, float fiber, float proteins, float salt) {
        SQLiteDatabase db = this.getWritableDatabase();
        String timestamp = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(new java.util.Date());
        String eCodesString = eCodes.isEmpty() ? "" : String.join(",", eCodes);

        // Prima controlla se esiste già in cronologia
        Cursor cursor = db.rawQuery("SELECT id FROM scan_history WHERE barcode = ?", new String[]{barcode});
        if (cursor.moveToFirst()) {
            // Esiste già, aggiorna il timestamp e i dati
            db.execSQL("UPDATE scan_history SET product_name = ?, e_codes = ?, ingredients = ?, nutriscore = ?, nova_group = ?, " +
                            "manufacturing_places = ?, origins = ?, ecoscore = ?, brand = ?, quantity = ?, labels = ?, allergens = ?, traces = ?, " +
                            "energy_kcal = ?, fat = ?, saturated_fat = ?, carbohydrates = ?, sugars = ?, fiber = ?, proteins = ?, salt = ?, scanned_at = ? WHERE barcode = ?",
                    new Object[]{productName, eCodesString, ingredients, nutriscore, novaGroup,
                            manufacturingPlaces, origins, ecoscore, brand, quantity, labels, allergens, traces,
                            energyKcal, fat, saturatedFat, carbohydrates, sugars, fiber, proteins, salt, timestamp, barcode});
        } else {
            // Non esiste, inserisci nuovo record
            db.execSQL("INSERT INTO scan_history (barcode, product_name, e_codes, ingredients, nutriscore, nova_group, " +
                            "manufacturing_places, origins, ecoscore, brand, quantity, labels, allergens, traces, " +
                            "energy_kcal, fat, saturated_fat, carbohydrates, sugars, fiber, proteins, salt, scanned_at) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    new Object[]{barcode, productName, eCodesString, ingredients, nutriscore, novaGroup,
                            manufacturingPlaces, origins, ecoscore, brand, quantity, labels, allergens, traces,
                            energyKcal, fat, saturatedFat, carbohydrates, sugars, fiber, proteins, salt, timestamp});
        }
        cursor.close();
    }

    public List<Product> getHistory(int limit) {
        List<Product> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
            "SELECT id, barcode, product_name, e_codes, ingredients, scanned_at, nutriscore, nova_group, manufacturing_places, origins, " +
            "ecoscore, brand, quantity, labels, allergens, traces, energy_kcal, fat, saturated_fat, carbohydrates, sugars, fiber, proteins, salt " +
            "FROM scan_history ORDER BY scanned_at DESC LIMIT ?",
            new String[]{String.valueOf(limit)}
        );

        if (cursor.moveToFirst()) {
            do {
                list.add(new Product(
                    cursor.getInt(cursor.getColumnIndex("id")),
                    cursor.getString(cursor.getColumnIndex("barcode")),
                    cursor.getString(cursor.getColumnIndex("product_name")),
                    cursor.getString(cursor.getColumnIndex("e_codes")),
                    cursor.getString(cursor.getColumnIndex("ingredients")),
                    cursor.getString(cursor.getColumnIndex("scanned_at")),
                    cursor.getString(cursor.getColumnIndex("nutriscore")),
                    cursor.getInt(cursor.getColumnIndex("nova_group")),
                    cursor.getString(cursor.getColumnIndex("manufacturing_places")),
                    cursor.getString(cursor.getColumnIndex("origins")),
                    cursor.getString(cursor.getColumnIndex("ecoscore")),
                    cursor.getString(cursor.getColumnIndex("brand")),
                    cursor.getString(cursor.getColumnIndex("quantity")),
                    cursor.getString(cursor.getColumnIndex("labels")),
                    cursor.getString(cursor.getColumnIndex("allergens")),
                    cursor.getString(cursor.getColumnIndex("traces")),
                    cursor.getFloat(cursor.getColumnIndex("energy_kcal")),
                    cursor.getFloat(cursor.getColumnIndex("fat")),
                    cursor.getFloat(cursor.getColumnIndex("saturated_fat")),
                    cursor.getFloat(cursor.getColumnIndex("carbohydrates")),
                    cursor.getFloat(cursor.getColumnIndex("sugars")),
                    cursor.getFloat(cursor.getColumnIndex("fiber")),
                    cursor.getFloat(cursor.getColumnIndex("proteins")),
                    cursor.getFloat(cursor.getColumnIndex("salt"))
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public void removeFromHistory(String barcode) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM scan_history WHERE barcode = ?", new Object[]{barcode});
    }

    public void clearHistory() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM scan_history");
    }

    // ==================== FAVORITES (PRODUCTS) ====================

    public void addToFavorites(String barcode, String productName, List<String> eCodes, String ingredients) {
        addToFavorites(barcode, productName, eCodes, ingredients, null, 0, null, null);
    }

    public void addToFavorites(String barcode, String productName, List<String> eCodes, String ingredients, String nutriscore, int novaGroup) {
        addToFavorites(barcode, productName, eCodes, ingredients, nutriscore, novaGroup, null, null);
    }

    public void addToFavorites(String barcode, String productName, List<String> eCodes, String ingredients, String nutriscore, int novaGroup, String manufacturingPlaces, String origins) {
        addToFavorites(barcode, productName, eCodes, ingredients, nutriscore, novaGroup, manufacturingPlaces, origins,
                null, null, null, null, null, null, 0, 0, 0, 0, 0, 0, 0, 0);
    }

    public void addToFavorites(String barcode, String productName, List<String> eCodes, String ingredients,
                               String nutriscore, int novaGroup, String manufacturingPlaces, String origins,
                               String ecoscore, String brand, String quantity, String labels, String allergens, String traces,
                               float energyKcal, float fat, float saturatedFat, float carbohydrates, float sugars, float fiber, float proteins, float salt) {
        SQLiteDatabase db = this.getWritableDatabase();
        String timestamp = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(new java.util.Date());
        String eCodesString = eCodes.isEmpty() ? "" : String.join(",", eCodes);

        try {
            db.execSQL("INSERT INTO favorites (barcode, product_name, e_codes, ingredients, nutriscore, nova_group, " +
                            "manufacturing_places, origins, ecoscore, brand, quantity, labels, allergens, traces, " +
                            "energy_kcal, fat, saturated_fat, carbohydrates, sugars, fiber, proteins, salt, added_at) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    new Object[]{barcode, productName, eCodesString, ingredients, nutriscore, novaGroup,
                            manufacturingPlaces, origins, ecoscore, brand, quantity, labels, allergens, traces,
                            energyKcal, fat, saturatedFat, carbohydrates, sugars, fiber, proteins, salt, timestamp});
        } catch (android.database.sqlite.SQLiteConstraintException e) {
            // Already exists, update all data
            db.execSQL("UPDATE favorites SET product_name = ?, e_codes = ?, ingredients = ?, nutriscore = ?, nova_group = ?, " +
                            "manufacturing_places = ?, origins = ?, ecoscore = ?, brand = ?, quantity = ?, labels = ?, allergens = ?, traces = ?, " +
                            "energy_kcal = ?, fat = ?, saturated_fat = ?, carbohydrates = ?, sugars = ?, fiber = ?, proteins = ?, salt = ? WHERE barcode = ?",
                    new Object[]{productName, eCodesString, ingredients, nutriscore, novaGroup,
                            manufacturingPlaces, origins, ecoscore, brand, quantity, labels, allergens, traces,
                            energyKcal, fat, saturatedFat, carbohydrates, sugars, fiber, proteins, salt, barcode});
        }
    }

    public void removeFromFavorites(String barcode) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM favorites WHERE barcode = ?", new Object[]{barcode});
    }

    public boolean isFavorite(String barcode) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
            "SELECT COUNT(*) FROM favorites WHERE barcode = ?",
            new String[]{barcode}
        );
        boolean exists = false;
        if (cursor.moveToFirst()) {
            exists = cursor.getInt(0) > 0;
        }
        cursor.close();
        return exists;
    }

    public List<Product> getFavorites() {
        List<Product> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
            "SELECT id, barcode, product_name, e_codes, ingredients, added_at, nutriscore, nova_group, manufacturing_places, origins, " +
            "ecoscore, brand, quantity, labels, allergens, traces, energy_kcal, fat, saturated_fat, carbohydrates, sugars, fiber, proteins, salt " +
            "FROM favorites ORDER BY added_at DESC",
            null
        );

        if (cursor.moveToFirst()) {
            do {
                list.add(new Product(
                    cursor.getInt(cursor.getColumnIndex("id")),
                    cursor.getString(cursor.getColumnIndex("barcode")),
                    cursor.getString(cursor.getColumnIndex("product_name")),
                    cursor.getString(cursor.getColumnIndex("e_codes")),
                    cursor.getString(cursor.getColumnIndex("ingredients")),
                    cursor.getString(cursor.getColumnIndex("added_at")),
                    cursor.getString(cursor.getColumnIndex("nutriscore")),
                    cursor.getInt(cursor.getColumnIndex("nova_group")),
                    cursor.getString(cursor.getColumnIndex("manufacturing_places")),
                    cursor.getString(cursor.getColumnIndex("origins")),
                    cursor.getString(cursor.getColumnIndex("ecoscore")),
                    cursor.getString(cursor.getColumnIndex("brand")),
                    cursor.getString(cursor.getColumnIndex("quantity")),
                    cursor.getString(cursor.getColumnIndex("labels")),
                    cursor.getString(cursor.getColumnIndex("allergens")),
                    cursor.getString(cursor.getColumnIndex("traces")),
                    cursor.getFloat(cursor.getColumnIndex("energy_kcal")),
                    cursor.getFloat(cursor.getColumnIndex("fat")),
                    cursor.getFloat(cursor.getColumnIndex("saturated_fat")),
                    cursor.getFloat(cursor.getColumnIndex("carbohydrates")),
                    cursor.getFloat(cursor.getColumnIndex("sugars")),
                    cursor.getFloat(cursor.getColumnIndex("fiber")),
                    cursor.getFloat(cursor.getColumnIndex("proteins")),
                    cursor.getFloat(cursor.getColumnIndex("salt"))
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }
}
