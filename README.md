# E-Codes - Scanner Additivi Alimentari

App Android per la scansione e identificazione degli additivi alimentari (codici E) nei prodotti.

## Architettura del Progetto

```
app/src/main/
├── java/it/faustobe/ecodes/
│   ├── ui/                    # Activity e UI
│   ├── data/                  # Database e gestione dati
│   ├── model/                 # Modelli dati
│   ├── adapter/               # RecyclerView Adapters
│   └── utils/                 # Utility classes
├── res/
│   ├── layout/                # Layout XML
│   ├── values/                # Stringhe IT (default)
│   ├── values-en/             # Stringhe EN
│   └── menu/                  # Menu XML
└── assets/
    ├── ecodes_database.db     # Database SQLite pre-popolato
    └── allergens/             # JSON allergeni multilingua
```

## Componenti Principali

### Activities (`ui/`)

| File | Descrizione |
|------|-------------|
| `BaseActivity.java` | Activity base con gestione lingua |
| `MainActivity.java` | Scansione barcode con ML Kit + ricerca manuale |
| `ResultsActivity.java` | Visualizzazione additivi trovati, scores, alert allergeni |
| `DetailActivity.java` | Dettaglio singolo additivo |
| `HistoryActivity.java` | Cronologia scansioni |
| `FavoritesActivity.java` | Prodotti preferiti |
| `ProfileActivity.java` | Configurazione profilo (dieta, allergeni, lingua) |
| `AboutActivity.java` | Info app e autore |
| `SplashActivity.java` | Splash screen iniziale |
| `HowToActivity.java` | Guida utilizzo |
| `ChangelogActivity.java` | Novità versione |

### Data Layer (`data/`)

| File | Descrizione |
|------|-------------|
| `DatabaseHelper.java` | SQLite helper - gestione DB additivi, cronologia, preferiti |
| `ProfileManager.java` | SharedPreferences per preferenze utente (dieta, allergeni, caution level) |
| `CustomAllergenManager.java` | Gestione allergeni personalizzati (JSON in SharedPreferences) |

### Models (`model/`)

| File | Descrizione |
|------|-------------|
| `Product.java` | Prodotto scansionato (barcode, nome, e-codes, ingredienti, nutriscore, nova) |
| `Additive.java` | Additivo alimentare (codice E, nome, classificazione, ADI, status EFSA, vegan/halal) |

### Adapters (`adapter/`)

| File | Descrizione |
|------|-------------|
| `AdditiveAdapter.java` | Lista additivi in ResultsActivity |
| `ProductAdapter.java` | Lista prodotti in History/Favorites |
| `SearchAdapter.java` | Risultati ricerca manuale |

### Utils (`utils/`)

| File | Descrizione |
|------|-------------|
| `AllergenDetector.java` | Rilevamento allergeni da ingredienti e E-codes |
| `LocaleHelper.java` | Gestione cambio lingua runtime |

## Database Schema

### Tabelle Principali (in `ecodes_database.db`)

#### `additives`
```sql
CREATE TABLE additives (
    id INTEGER PRIMARY KEY,
    code TEXT NOT NULL,           -- Es: "E440"
    name TEXT NOT NULL,           -- Nome additivo
    classification TEXT,          -- A-F, N (Non-toxic to Dangerous)
    adi REAL,                     -- Acceptable Daily Intake (mg/kg)
    efsa_status TEXT,             -- approved, under_review, banned, caution
    allergen_risk INTEGER,        -- 0 o 1
    vegan INTEGER,                -- -1 (unknown), 0 (no), 1 (yes)
    halal INTEGER,                -- -1 (unknown), 0 (no), 1 (yes)
    notes TEXT,
    last_updated TEXT
);
```

#### `additive_translations`
```sql
CREATE TABLE additive_translations (
    id INTEGER PRIMARY KEY,
    additive_code TEXT NOT NULL,
    language TEXT NOT NULL,       -- "it", "en"
    notes TEXT
);
```

### Tabelle User Data (create a runtime)

#### `scan_history`
```sql
CREATE TABLE scan_history (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    barcode TEXT NOT NULL,
    product_name TEXT NOT NULL,
    e_codes TEXT NOT NULL,        -- Comma-separated
    ingredients TEXT,
    nutriscore TEXT,              -- A-E
    nova_group INTEGER DEFAULT 0, -- 1-4
    scanned_at TEXT NOT NULL
);
```

#### `favorites`
```sql
CREATE TABLE favorites (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    barcode TEXT NOT NULL UNIQUE,
    product_name TEXT NOT NULL,
    e_codes TEXT NOT NULL,
    ingredients TEXT,
    nutriscore TEXT,
    nova_group INTEGER DEFAULT 0,
    added_at TEXT NOT NULL
);
```

## API Integration

### Open Food Facts API
- **Endpoint**: `https://world.openfoodfacts.org/api/v2/product/{barcode}.json`
- **Client**: OkHttp3
- **Dati estratti**:
  - `product_name`
  - `additives_tags` (e varianti)
  - `ingredients_text_it`, `ingredients_text_en`, `ingredients_text`
  - `nutriscore_grade` (A-E)
  - `nova_group` (1-4)

## Sistema di Classificazione Additivi

La classificazione usa una **barra colorata "danger meter"** invece di lettere, per evitare confusione con il Nutri-Score (A-E).

| Livello | Significato | Riempimento | Colore |
|---------|-------------|-------------|--------|
| A | Non tossico | 12% | Verde |
| B | Rischio intolleranza | 28% | Verde chiaro |
| C | Prodotto sospetto | 45% | Giallo |
| D | Forte sospetto tossicità | 65% | Arancione |
| E | PERICOLOSO | 85% | Rosso |
| F | Non ammesso in UE | 100% | Rosso scuro |
| N | Neutro | 8% | Grigio |

**Visualizzazione**: Una barra orizzontale dove maggiore è il riempimento, maggiore è il pericolo.

## Sistema Allergeni

### Allergeni EU Obbligatori (14 + Nichel)
Definiti in `assets/allergens/it.json` e `assets/allergens/en.json`:
- Lattosio, Glutine, Uova, Arachidi, Frutta a guscio
- Pesce, Crostacei, Molluschi, Soia, Sedano
- Senape, Sesamo, Lupini, Solfiti, Nichel

### Allergeni Custom
Gestiti da `CustomAllergenManager.java`, salvati in SharedPreferences come JSON:
```json
{
  "id": "custom_123456789",
  "name": "Istamina",
  "keywords": ["istamina", "pomodoro", "fragola"],
  "enabled": true
}
```

## Nutri-Score e Nova Score

### Nutri-Score (A-E)
Valutazione nutrizionale complessiva:
- **A** (verde scuro): Qualità nutrizionale alta
- **B** (verde): Buona
- **C** (giallo): Media
- **D** (arancione): Bassa
- **E** (rosso): Scarsa

### Nova Score (1-4)
Livello di trasformazione:
- **1** (verde): Alimenti non trasformati
- **2** (verde chiaro): Ingredienti culinari
- **3** (arancione): Alimenti trasformati
- **4** (rosso): Ultra-trasformati

## Flusso Scansione

```
1. MainActivity: Scansione barcode (ML Kit)
   ↓
2. queryOpenFoodFacts(): Richiesta API
   ↓
3. parseOpenFoodFactsResponse(): Estrazione dati
   - Nome prodotto
   - E-codes da additives_tags
   - E-codes da ingredients_text (regex)
   - Nutri-Score e Nova Score
   ↓
4. showResults(): Avvia ResultsActivity
   ↓
5. ResultsActivity:
   - Query DB locale per dettagli additivi
   - Rileva allergeni (AllergenDetector)
   - Mostra Nutri-Score e Nova Score
   - Salva in cronologia
   ↓
6. DetailActivity: Dettaglio additivo selezionato
```

## Localizzazione

L'app supporta Italiano (default) e Inglese:
- `res/values/strings.xml` - Italiano
- `res/values-en/strings.xml` - Inglese

Il cambio lingua avviene in runtime tramite `LocaleHelper` e viene applicato in `BaseActivity.attachBaseContext()`.

## SharedPreferences

### `app_prefs`
- `language`: "it" o "en"

### `user_profile`
- `vegan`, `vegetarian`, `halal`: boolean
- `caution_level`: 0-3 (strict, moderate, permissive, all)
- `allergen_{type}`: boolean per ogni allergene

### `custom_allergens`
- `allergens_list`: JSON array degli allergeni custom

## Build e Dipendenze

### Dipendenze Principali (build.gradle)
```gradle
// Camera e ML
implementation 'androidx.camera:camera-camera2:1.3.0'
implementation 'com.google.mlkit:barcode-scanning:17.2.0'

// Network
implementation 'com.squareup.okhttp3:okhttp:4.12.0'

// UI
implementation 'com.google.android.material:material:1.9.0'
implementation 'androidx.cardview:cardview:1.0.0'
```

### Requisiti
- Min SDK: 24 (Android 7.0)
- Target SDK: 34 (Android 14)
- Permessi: CAMERA, INTERNET

## Note per lo Sviluppo

### Aggiungere una nuova lingua per allergeni
1. Creare `assets/allergens/{lang}.json` con la stessa struttura di `it.json`
2. Il file viene caricato automaticamente da `AllergenDetector.loadAllergensData()`

### Aggiungere un nuovo allergene standard
1. Aggiungere entry in tutti i file `allergens/*.json`
2. Aggiungere switch in `activity_profile.xml`
3. Aggiungere mapping in `ProfileActivity.allergenSwitches`
4. Aggiungere stringa label in `strings.xml` (IT e EN)
5. Aggiungere case in `AllergenDetector.AllergenMatch.getAllergenName()`

### Modificare lo schema database
1. Incrementare `DATABASE_VERSION` in `DatabaseHelper.java`
2. Aggiungere migrazione in `createAdditionalTables()` usando `addColumnIfNotExists()`

## Autore

**Fausto Bernardini**

---

*Ultimo aggiornamento: Gennaio 2026*
