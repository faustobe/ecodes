# 🌍 Guida per Aggiungere una Nuova Lingua

Questa guida spiega come aggiungere una nuova lingua all'app E-Codes in modo completo.

Il sistema è progettato per richiedere **ZERO modifiche al codice Java** per le funzionalità di base, e solo **minime modifiche** per l'interfaccia di selezione lingua.

---

## 📋 Indice

1. [Panoramica del Sistema](#panoramica-del-sistema)
2. [Procedura Completa](#procedura-completa)
3. [Esempio: Aggiungere lo Spagnolo](#esempio-aggiungere-lo-spagnolo)
4. [Troubleshooting](#troubleshooting)

---

## 🎯 Panoramica del Sistema

L'app supporta il multilinguismo attraverso 3 componenti:

| Componente | Scopo | File | Modifiche Codice |
|------------|-------|------|------------------|
| **UI Strings** | Testo interfaccia utente | `values-XX/strings.xml` | ❌ ZERO |
| **Allergen Detection** | Rilevamento allergeni negli ingredienti | `allergens/XX.json` | ❌ ZERO |
| **Function Search** | Ricerca per funzione (colorante, conservante, etc.) | `functions/XX.json` | ❌ ZERO |
| **Language Selector** | Pulsanti selezione lingua | XML + Java | ✅ 2 modifiche minime |

### Come Funziona

- **Caricamento Automatico**: I file JSON vengono caricati automaticamente all'avvio
- **Detection Multilingua**: Gli allergeni vengono cercati in TUTTE le lingue contemporaneamente
- **Ricerca Intelligente**: I termini vengono mappati automaticamente all'italiano (lingua del database)

---

## 📝 Procedura Completa

### Passo 1: Traduzioni UI (Obbligatorio)

**File da creare:** `app/src/main/res/values-XX/strings.xml`

Dove `XX` è il codice lingua ISO 639-1:
- `es` = Spagnolo
- `fr` = Francese
- `de` = Tedesco
- `pt` = Portoghese
- etc.

**Contenuto:**

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <!-- ==================== APP ==================== -->
    <string name="app_name">E-Codes</string>

    <!-- ==================== SPLASH ==================== -->
    <string name="splash_loading">Caricamento...</string>

    <!-- ==================== MAIN ==================== -->
    <string name="main_scan_description">Inquadra il codice a barre del prodotto per analizzare gli additivi</string>
    <string name="main_camera_status">Posiziona il codice a barre nel riquadro</string>
    <string name="main_search_hint">Inserisci codice E o nome additivo...</string>

    <!-- ==================== PROFILE ==================== -->
    <string name="profile_title">Profilo Personale</string>
    <string name="profile_dietary">Preferenze alimentari</string>
    <string name="profile_vegan">Vegano</string>
    <string name="profile_vegetarian">Vegetariano</string>
    <string name="profile_halal">Halal</string>

    <string name="profile_caution">Livello di cautela</string>
    <string name="profile_caution_strict">Rigoroso (solo A - non tossici)</string>
    <string name="profile_caution_moderate">Moderato (A + B)</string>
    <string name="profile_caution_permissive">Permissivo (A + B + C)</string>
    <string name="profile_caution_all">Tutti gli additivi</string>

    <string name="profile_allergens">Allergie e intolleranze</string>
    <string name="profile_allergen_lactose">Lattosio</string>
    <string name="profile_allergen_gluten">Glutine</string>
    <string name="profile_allergen_nickel">Nichel</string>
    <string name="profile_allergen_sulfites">Solfiti</string>

    <string name="profile_language">Lingua / Language</string>
    <string name="profile_save">Salva Profilo</string>
    <string name="profile_saved">Profilo salvato con successo</string>

    <!-- ==================== RESULTS ==================== -->
    <string name="results_title">Risultati Analisi</string>
    <string name="results_certain_title">Additivi rilevati</string>
    <string name="results_ambiguous_title">Possibili additivi</string>
    <string name="results_no_results">Nessun additivo trovato</string>
    <string name="results_show_warning">Mostra informazioni</string>
    <string name="results_show_ingredients">Mostra tutti gli ingredienti</string>
    <string name="results_hide_ingredients">Nascondi ingredienti</string>
    <string name="results_manual_search">Ricerca Manuale</string>

    <!-- ==================== ALLERGEN ALERTS ==================== -->
    <string name="allergen_alert_title">⚠️ ALLERGEN ALERT</string>
    <string name="allergen_found_intro">Il prodotto contiene ingredienti che potrebbero causare reazioni allergiche in base al tuo profilo:</string>
    <string name="allergen_keywords_found">Ingredienti:</string>
    <string name="allergen_ecodes_found">Codici E:</string>

    <!-- ==================== DETAIL ==================== -->
    <string name="detail_title">Dettagli Additivo</string>
    <string name="detail_classification">Classificazione</string>
    <string name="detail_adi">ADI (mg/kg/giorno)</string>
    <string name="detail_efsa_status">Status EFSA</string>
    <string name="detail_allergen_risk">Rischio allergie</string>
    <string name="detail_dietary">Compatibilità alimentare</string>
    <string name="detail_notes">Note</string>
    <string name="detail_last_updated">Ultimo aggiornamento</string>

    <!-- ==================== CLASSIFICATION ==================== -->
    <string name="classification_a">A - Non tossico</string>
    <string name="classification_b">B - Bassa tossicità</string>
    <string name="classification_c">C - Moderata tossicità</string>
    <string name="classification_d">D - Alta tossicità</string>
    <string name="classification_unknown">Non classificato</string>

    <!-- ==================== HISTORY / FAVORITES ==================== -->
    <string name="history_title">Cronologia Scansioni</string>
    <string name="favorites_title">Preferiti</string>
    <string name="empty_history">Nessuna scansione recente</string>
    <string name="empty_favorites">Nessun preferito salvato</string>

    <!-- ==================== MENU ==================== -->
    <string name="action_home">Home</string>
    <string name="action_history">Cronologia</string>
    <string name="action_favorites">Preferiti</string>
    <string name="action_profile">Profilo</string>
    <string name="action_share">Condividi</string>
    <string name="action_howto">Come usare</string>
    <string name="action_about">Info</string>
    <string name="action_changelog">Novità</string>
    <string name="action_clear_history">Cancella cronologia</string>

    <!-- ==================== TOASTS ==================== -->
    <string name="toast_camera_permission">Permesso fotocamera necessario per la scansione</string>
    <string name="toast_camera_error">Errore nell\'avvio della fotocamera</string>
    <string name="toast_barcode_not_found">Codice a barre non trovato nel database</string>
    <string name="toast_network_error">Errore di connessione. Verifica la connessione internet.</string>
    <string name="toast_added_to_favorites">Aggiunto ai preferiti</string>
    <string name="toast_removed_from_favorites">Rimosso dai preferiti</string>

    <!-- ==================== ABOUT ==================== -->
    <string name="about_title">Info</string>
    <string name="about_version">Versione</string>
    <string name="about_developer">Sviluppatore</string>
    <string name="about_description">App per l\'analisi degli additivi alimentari</string>

    <!-- ==================== HOW TO ==================== -->
    <string name="howto_title">Come Usare</string>
    <string name="howto_scan">Scansiona il codice a barre del prodotto</string>
    <string name="howto_search">Cerca manualmente per codice E o nome</string>
    <string name="howto_profile">Configura il tuo profilo alimentare</string>
</resources>
```

**⚠️ IMPORTANTE**: Traduci TUTTE le stringhe nel tuo idioma.

---

### Passo 2: Allergen Detection (Opzionale ma raccomandato)

**File da creare:** `app/src/main/assets/allergens/XX.json`

**Contenuto:**

```json
{
  "lactose": {
    "keywords": [
      "parola1", "parola2", "parola3"
    ],
    "e_codes": ["E270", "E325", "E326", "E327", "E472b", "E480", "E481", "E482"]
  },
  "gluten": {
    "keywords": [
      "parola1", "parola2", "parola3"
    ],
    "e_codes": ["E1404", "E1410", "E1412", "E1413", "E1414", "E1420", "E1422", "E1440", "E1442", "E1450"]
  },
  "nickel": {
    "keywords": [
      "parola1", "parola2", "parola3"
    ],
    "e_codes": ["E150c", "E150d", "E952"]
  },
  "sulfites": {
    "keywords": [
      "parola1", "parola2", "parola3"
    ],
    "e_codes": ["E220", "E221", "E222", "E223", "E224", "E226", "E227", "E228"]
  }
}
```

**Come scegliere le keywords:**
- Inserisci i nomi degli ingredienti che contengono quell'allergene
- Usa la forma **singolare e plurale**
- Includi **sinonimi e varianti**
- Le keyword devono essere in **minuscolo**

---

### Passo 3: Function Search Mapping (Opzionale ma raccomandato)

**File da creare:** `app/src/main/assets/functions/XX.json`

**Contenuto:**

```json
{
  "termine_lingua_nuova1": "conservante",
  "termine_lingua_nuova2": "colorante",
  "termine_lingua_nuova3": "dolcificante",
  "termine_lingua_nuova4": "antiossidante",
  "termine_lingua_nuova5": "emulsionante",
  "termine_lingua_nuova6": "addensante",
  "termine_lingua_nuova7": "stabilizzante",
  "termine_lingua_nuova8": "acidificante",
  "termine_lingua_nuova9": "correttore",
  "termine_lingua_nuova10": "gelificante",
  "termine_lingua_nuova11": "agente lievitante",
  "termine_lingua_nuova12": "esaltatore",
  "termine_lingua_nuova13": "umidificante",
  "termine_lingua_nuova14": "antiagglomerante"
}
```

**⚠️ IMPORTANTE**:
- Le **chiavi** sono i termini nella nuova lingua
- I **valori** devono essere sempre in **italiano** (lingua del database)
- Puoi aggiungere **sinonimi** multipli per la stessa funzione

---

### Passo 4: Aggiungere la Nuova Lingua al Selettore UI

#### 4.1 Modifica XML

**File:** `app/src/main/res/layout/activity_profile.xml`

**Trova il RadioGroup (linea ~266):**

```xml
<RadioGroup
    android:id="@+id/radioGroupLanguage"
    android:layout_width="match_parent"
    android:layout_height="wrap_content">

    <RadioButton
        android:id="@+id/radioLangItalian"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="🇮🇹 Italiano"
        android:textSize="14sp"
        android:padding="8dp"
        android:checked="true" />

    <RadioButton
        android:id="@+id/radioLangEnglish"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="🇬🇧 English"
        android:textSize="14sp"
        android:padding="8dp" />

</RadioGroup>
```

**Aggiungi il nuovo RadioButton PRIMA di `</RadioGroup>`:**

```xml
    <RadioButton
        android:id="@+id/radioLangYOURLANG"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="🇫🇷 Français"
        android:textSize="14sp"
        android:padding="8dp" />
```

Sostituisci:
- `radioLangYOURLANG` con un ID univoco (es. `radioLangSpanish`, `radioLangFrench`)
- `🇫🇷 Français` con la bandiera e nome della tua lingua

#### 4.2 Modifica Java - Selezione Lingua

**File:** `app/src/main/java/it/faustobe/ecodes/ui/ProfileActivity.java`

**Trova il listener (linea ~159):**

```java
radioGroupLanguage.setOnCheckedChangeListener((group, checkedId) -> {
    String newLang;
    if (checkedId == R.id.radioLangEnglish) {
        newLang = "en";
    } else {
        newLang = "it";
    }
    prefs.edit().putString(KEY_LANGUAGE, newLang).apply();
    recreate();
});
```

**Modifica in:**

```java
radioGroupLanguage.setOnCheckedChangeListener((group, checkedId) -> {
    String newLang;
    if (checkedId == R.id.radioLangEnglish) {
        newLang = "en";
    } else if (checkedId == R.id.radioLangYOURLANG) {
        newLang = "XX";  // Codice ISO della tua lingua
    } else {
        newLang = "it";
    }
    prefs.edit().putString(KEY_LANGUAGE, newLang).apply();
    recreate();
});
```

#### 4.3 Modifica Java - Caricamento Lingua Salvata

**Trova (linea ~98):**

```java
String currentLang = prefs.getString(KEY_LANGUAGE, "it");
if (currentLang.equals("en")) {
    ((RadioButton) findViewById(R.id.radioLangEnglish)).setChecked(true);
} else {
    ((RadioButton) findViewById(R.id.radioLangItalian)).setChecked(true);
}
```

**Modifica in:**

```java
String currentLang = prefs.getString(KEY_LANGUAGE, "it");
if (currentLang.equals("en")) {
    ((RadioButton) findViewById(R.id.radioLangEnglish)).setChecked(true);
} else if (currentLang.equals("XX")) {  // Codice ISO della tua lingua
    ((RadioButton) findViewById(R.id.radioLangYOURLANG)).setChecked(true);
} else {
    ((RadioButton) findViewById(R.id.radioLangItalian)).setChecked(true);
}
```

---

## 📚 Esempio: Aggiungere lo Spagnolo

### File da creare:

```
app/src/main/res/values-es/strings.xml
app/src/main/assets/allergens/es.json
app/src/main/assets/functions/es.json
```

### allergens/es.json

```json
{
  "lactose": {
    "keywords": ["leche", "lactosa", "suero de leche", "caseína", "mantequilla", "nata", "crema", "yogur", "queso"],
    "e_codes": ["E270", "E325", "E326", "E327", "E472b", "E480", "E481", "E482"]
  },
  "gluten": {
    "keywords": ["trigo", "cebada", "centeno", "espelta", "avena", "malta", "harina", "almidón"],
    "e_codes": ["E1404", "E1410", "E1412", "E1413", "E1414", "E1420", "E1422", "E1440", "E1442", "E1450"]
  },
  "nickel": {
    "keywords": ["cacao", "chocolate", "avellana", "almendra", "nuez"],
    "e_codes": ["E150c", "E150d", "E952"]
  },
  "sulfites": {
    "keywords": ["sulfito", "dióxido de azufre", "metabisulfito", "bisulfito"],
    "e_codes": ["E220", "E221", "E222", "E223", "E224", "E226", "E227", "E228"]
  }
}
```

### functions/es.json

```json
{
  "colorante": "colorante",
  "conservante": "conservante",
  "antioxidante": "antiossidante",
  "edulcorante": "dolcificante",
  "endulzante": "dolcificante",
  "emulsionante": "emulsionante",
  "emulgente": "emulsionante",
  "espesante": "addensante",
  "estabilizante": "stabilizzante",
  "acidificante": "acidificante",
  "corrector de acidez": "correttore",
  "regulador de acidez": "correttore",
  "gelificante": "gelificante",
  "gasificante": "agente lievitante",
  "leudante": "agente lievitante",
  "potenciador del sabor": "esaltatore",
  "realzador": "esaltatore",
  "humectante": "umidificante",
  "antiaglomerante": "antiagglomerante",
  "antiapelmazante": "antiagglomerante"
}
```

### Modifiche Java

**activity_profile.xml** - Aggiungi:
```xml
<RadioButton
    android:id="@+id/radioLangSpanish"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:text="🇪🇸 Español"
    android:textSize="14sp"
    android:padding="8dp" />
```

**ProfileActivity.java** - Listener:
```java
} else if (checkedId == R.id.radioLangSpanish) {
    newLang = "es";
```

**ProfileActivity.java** - Caricamento:
```java
} else if (currentLang.equals("es")) {
    ((RadioButton) findViewById(R.id.radioLangSpanish)).setChecked(true);
```

---

## ✅ Checklist Finale

- [ ] File `values-XX/strings.xml` creato e tradotto completamente
- [ ] File `allergens/XX.json` creato con keyword appropriate
- [ ] File `functions/XX.json` creato con mapping funzioni
- [ ] RadioButton aggiunto in `activity_profile.xml`
- [ ] Listener modificato in `ProfileActivity.java` (setOnCheckedChangeListener)
- [ ] Caricamento lingua modificato in `ProfileActivity.java` (getString)
- [ ] Build dell'app: `./gradlew assembleDebug`
- [ ] Test selezione della nuova lingua nell'interfaccia ProfileActivity
- [ ] Test detection allergeni con ingredienti nella nuova lingua
- [ ] Test ricerca funzioni con termini nella nuova lingua

---

## 🔧 Troubleshooting

### Problema: L'app non mostra la nuova lingua

**Causa**: File `strings.xml` non trovato o nome cartella errato

**Soluzione**:
- Verifica che la cartella sia `values-XX` (es. `values-es`, NON `values-spa`)
- Usa codici ISO 639-1 (2 lettere): https://en.wikipedia.org/wiki/List_of_ISO_639-1_codes

### Problema: Gli allergeni non vengono rilevati

**Causa**: File JSON malformato o keyword errate

**Soluzione**:
- Verifica la sintassi JSON su https://jsonlint.com
- Controlla che le keyword siano in **minuscolo**
- Testa con ingredienti noti (es. "leche" per lactose in spagnolo)

### Problema: La ricerca per funzione non funziona

**Causa**: Mapping non corretto nel file `functions/XX.json`

**Soluzione**:
- I **valori** devono essere sempre in **italiano** (lingua del database)
- Le **chiavi** sono i termini nella nuova lingua
- Esempio CORRETTO: `"preservative": "conservante"`
- Esempio ERRATO: `"conservante": "preservative"`

### Problema: RadioButton non appare

**Causa**: ID duplicato o sintassi XML errata

**Soluzione**:
- Verifica che l'ID sia univoco (non usare ID già esistenti)
- Controlla che il RadioButton sia DENTRO il RadioGroup
- Rebuild del progetto: `./gradlew clean assembleDebug`

---

## 📞 Supporto

Per problemi o domande, apri una issue su GitHub con:
- Lingua che stai cercando di aggiungere
- File creati/modificati
- Log degli errori (se presenti)

---

Riepilogo dello stato attuale:
  - ✅ 246 additivi tradotti in inglese (64% completato)
  - ✅ 139 additivi rimanenti da tradurre (36%)
  - ✅ Sistema di traduzioni con fallback automatico all'italiano operativo
  - ✅ App testata e funzionante con cambio lingua inglese/italiano

  Sistema implementato:
  - Tabella additive_translations con indice ottimizzato
  - Metodi getTranslatedNotes() e getCurrentLanguage() in DatabaseHelper
  - Tutti i metodi di query aggiornati per usare le traduzioni
  - Fallback automatico all'italiano se traduzione mancante

**Ultimo aggiornamento**: Gennaio 2026
**Versione app**: 1.x
