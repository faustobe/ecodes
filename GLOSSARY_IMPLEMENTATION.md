# Implementazione Glossario Ingredienti Naturali Processati

## Obiettivo

Aggiungere un glossario di ingredienti naturali processati (destrosio, succo concentrato, sale iodato, etc.) che attualmente non vengono rilevati dal sistema di scansione. Soluzione 100% offline tramite estensione database SQLite esistente.

## Schema Database

Aggiungere in `DatabaseHelper.java`:

```sql
CREATE TABLE ingredients (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    code TEXT UNIQUE NOT NULL,
    category TEXT NOT NULL,
    origin TEXT,
    glycemic_index INTEGER,
    vegan INTEGER DEFAULT 1,
    halal INTEGER DEFAULT 1
);

CREATE TABLE ingredient_translations (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    ingredient_id INTEGER NOT NULL,
    language_code TEXT NOT NULL,
    names TEXT NOT NULL,
    description TEXT,
    FOREIGN KEY (ingredient_id) REFERENCES ingredients(id),
    UNIQUE(ingredient_id, language_code)
);

CREATE INDEX idx_ingredient_translations_lookup
ON ingredient_translations(ingredient_id, language_code);
```

**Campi:**
- `code`: identificatore univoco (es. "dextrose", "iodized_salt")
- `category`: sugar, salt, concentrate, extract, starch, fiber, protein, fat, acid, other
- `names`: JSON array di nomi/sinonimi nella lingua (es. `["destrosio", "glucosio", "D-glucosio"]`)
- `glycemic_index`: NULL se non applicabile

## Modifiche DatabaseHelper.java

### 1. Incrementare versione
```java
private static final int DATABASE_VERSION = 6; // era 5
```

### 2. Aggiungere in onUpgrade()
```java
if (oldVersion < 6) {
    db.execSQL("CREATE TABLE ingredients (...)");
    db.execSQL("CREATE TABLE ingredient_translations (...)");
    db.execSQL("CREATE INDEX idx_ingredient_translations_lookup ...");
    populateIngredientsFromAssets(db);
}
```

### 3. Metodi CRUD da aggiungere
```java
// Cerca ingrediente per nome (ricerca parziale in names JSON)
public List<Ingredient> searchIngredients(String query, String language)

// Ottieni ingrediente per code
public Ingredient getIngredientByCode(String code, String language)

// Ottieni tutti per categoria
public List<Ingredient> getIngredientsByCategory(String category, String language)

// Rileva ingredienti in testo (per integrazione con scan)
public List<Ingredient> detectIngredientsInText(String text, String language)
```

## Model: Ingredient.java

Creare in `app/src/main/java/it/faustobe/ecodes/model/`:

```java
public class Ingredient {
    private int id;
    private String code;
    private String category;
    private String origin;
    private Integer glycemicIndex;  // nullable
    private boolean vegan;
    private boolean halal;

    // Campi tradotti
    private List<String> names;
    private String description;

    // Getters, setters, costruttore
}
```

## Dati Iniziali

Creare `app/src/main/assets/ingredients/data.json`:

```json
{
  "ingredients": [
    {
      "code": "dextrose",
      "category": "sugar",
      "origin": "corn/wheat",
      "glycemic_index": 100,
      "vegan": true,
      "halal": true
    }
  ],
  "translations": {
    "it": {
      "dextrose": {
        "names": ["destrosio", "glucosio", "D-glucosio", "zucchero d'uva"],
        "description": "Zucchero semplice derivato da amido di mais o frumento"
      }
    },
    "en": {
      "dextrose": {
        "names": ["dextrose", "glucose", "D-glucose", "grape sugar", "corn sugar"],
        "description": "Simple sugar derived from corn or wheat starch"
      }
    }
  }
}
```

## Categorie Previste

| Categoria | Esempi |
|-----------|--------|
| sugar | destrosio, fruttosio, maltosio, sciroppo di glucosio, zucchero invertito |
| salt | sale iodato, sale marino, sale rosa, glutammato |
| concentrate | succo concentrato, concentrato di pomodoro, estratto di malto |
| starch | amido modificato, fecola, maltodestrine |
| fiber | inulina, pectina, cellulosa |
| protein | proteine isolate, caseinati, glutine |
| fat | oli idrogenati, grassi vegetali, mono/digliceridi |
| acid | acido citrico, acido lattico, acido ascorbico |
| extract | estratto di lievito, aromi naturali |
| other | lecitina, carragenina |

## Integrazione con Scansione

In `ResultsActivity.java`, dopo il rilevamento additivi E-xxx, aggiungere:

```java
// Rileva ingredienti processati nel testo ingredienti
List<Ingredient> detectedIngredients = dbHelper.detectIngredientsInText(
    ingredientsText,
    currentLanguage
);

// Mostra in sezione separata o integrata con additivi
```

## UI Suggerita

1. **In ResultsActivity**: sezione "Ingredienti Processati" sotto gli additivi
2. **Nuova IngredientDetailActivity**: dettaglio singolo ingrediente (opzionale, può riusare DetailActivity)
3. **In MainActivity**: ricerca manuale estesa a ingredienti oltre che additivi

## File da Modificare

| File | Modifica |
|------|----------|
| `DatabaseHelper.java` | Schema, CRUD, popolazione |
| `Ingredient.java` | Nuovo model |
| `ResultsActivity.java` | Integrazione rilevamento |
| `MainActivity.java` | Ricerca estesa (opzionale) |
| `assets/ingredients/data.json` | Dati iniziali |

## Stima Voci Iniziali (priorità)

**Fase 1 - Core (~150 voci):**
- Zuccheri e dolcificanti: 30
- Sali e esaltatori: 15
- Concentrati e succhi: 25
- Amidi e addensanti: 20
- Grassi e oli processati: 25
- Acidi e regolatori: 20
- Estratti e aromi: 15

**Fase 2 - Estensione (~200 voci aggiuntive)**

## Note Implementative

- Usare stesso pattern di `additive_translations` per fallback lingua (IT default)
- Ricerca in `names` JSON: `names LIKE '%' || ? || '%'`
- Per performance su testi lunghi, costruire regex da tutti i nomi e compilare una volta
- Considerare FTS (Full-Text Search) se >500 voci
