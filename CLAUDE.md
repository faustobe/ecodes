# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Build and install to connected devices (auto-detects via ADB)
./build_and_install.sh

# Clean build
./gradlew clean
```

## Project Overview

E-Codes Reader is an Android app (Java) for scanning food product barcodes and displaying information about food additives (E-numbers). It uses ML Kit for barcode scanning and queries the Open Food Facts API for product data.

**Key specs:** minSdk 24, targetSdk 34, Java 1.8

## Architecture

### Data Flow: Barcode Scanning
```
MainActivity (CameraX + ML Kit barcode detection)
    → queryOpenFoodFacts(barcode) via OkHttp
    → parseOpenFoodFactsResponse() extracts E-codes from additives_tags + ingredients_text
    → ResultsActivity displays results with AllergenDetector.detectAllergens()
    → DatabaseHelper saves to scan_history
```

### Layer Structure

**UI Layer** (`app/src/main/java/it/faustobe/ecodes/ui/`)
- `MainActivity` - Barcode scanning + manual search entry point
- `ResultsActivity` - Displays scan results with color-coded danger meter
- `DetailActivity` - Single additive details
- `ProfileActivity` - User diet preferences, allergens, language selection
- `BaseActivity` - Common language handling

**Data Layer** (`app/src/main/java/it/faustobe/ecodes/data/`)
- `DatabaseHelper` - SQLite operations (bundled `ecodes_database.db` + runtime tables)
- `ProfileManager` - SharedPreferences for user profile
- `CustomAllergenManager` - Custom allergen CRUD

**Model Layer** (`app/src/main/java/it/faustobe/ecodes/model/`)
- `Additive`, `Product`, `ScanResult`

**Utils** (`app/src/main/java/it/faustobe/ecodes/utils/`)
- `AllergenDetector` - Multi-language allergen detection from ingredients and E-codes

### Database Schema

Pre-populated in `assets/ecodes_database.db` (version 5):
- `additives` - E-code data with classification (A-F risk levels), vegan/halal flags
- `additive_translations` - Multilingual notes (falls back to Italian)

Runtime tables created on first launch:
- `scan_history` - Barcode scan records with product details
- `favorites` - User-saved products

### Classification System

Additives use A-F risk classification:
- A (green): Non-toxic
- B (yellow): Low risk
- C (orange): Moderate
- D (red): High
- E (purple): Dangerous
- F (black): Not permitted in EU
- N (gray): Neutral

## Localization

The app supports runtime language switching. Languages require:
1. `res/values-XX/strings.xml` - UI strings
2. `assets/allergens/XX.json` - Allergen keywords for detection
3. `assets/functions/XX.json` - Additive function term mappings

See `ADDING_LANGUAGES.md` for detailed instructions.

## External API

**Open Food Facts API v2**
- Endpoint: `https://world.openfoodfacts.org/api/v2/product/{barcode}.json`
- User-Agent: `ECodeReader/1.0`
- Timeout: 15 seconds

## Key Dependencies

- CameraX 1.3.0 - Camera preview and capture
- ML Kit Barcode 17.2.0 - Barcode detection
- OkHttp 4.12.0 - HTTP client
- Material 1.11.0 - UI components
