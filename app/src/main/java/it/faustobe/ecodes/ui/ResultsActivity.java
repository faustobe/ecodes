package it.faustobe.ecodes.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import androidx.core.widget.NestedScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import it.faustobe.ecodes.R;
import it.faustobe.ecodes.data.DatabaseHelper;
import it.faustobe.ecodes.data.ProfileManager;
import it.faustobe.ecodes.model.Additive;
import it.faustobe.ecodes.utils.AllergenDetector;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ResultsActivity extends BaseActivity {
    private RecyclerView certainRecyclerView;
    private RecyclerView ambiguousRecyclerView;
    private TextView noResultsText;
    private TextView resultsProductName;
    private TextView resultsAdditiveCount;
    private TextView certainTitle;
    private TextView ambiguousTitle;
    private LinearLayout certainSection;
    private LinearLayout ambiguousSection;
    private NestedScrollView resultsScrollView;
    private MaterialButton btnManualSearch;
    private MaterialButton btnShowWarning;
    private View warningCard;
    private DatabaseHelper dbHelper;
    private ProfileManager profileManager;
    private FloatingActionButton fabFavorite;
    private String currentBarcode;
    private String currentProductName;
    private String currentIngredients;
    private List<String> currentECodes;
    private List<Additive> allAdditives; // Per la condivisione
    private CardView ingredientsCard;
    private MaterialButton btnShowIngredients;
    private TextView ingredientsText;
    private boolean ingredientsExpanded = false;
    private CardView profileAlertCard;
    private TextView profileAlertMessage;
    private List<AllergenDetector.AllergenMatch> allergenMatches;
    // Allergens card
    private LinearLayout allergensInfoSection;
    // Allergen profile alert (card separata sempre visibile)
    private CardView allergenProfileAlertCard;
    private TextView allergenProfileAlertText;
    // Nutri-Score and Nova Score
    private CardView scoresCard;
    private LinearLayout nutriscoreSection;
    private LinearLayout novaSection;
    private TextView nutriscoreBadge;
    private TextView novaBadge;
    private TextView novaDescription;
    private String currentNutriscore;
    private int currentNovaGroup;
    // Origin info
    private CardView originCard;
    private LinearLayout manufacturingSection;
    private LinearLayout originsSection;
    private View originDivider;
    private TextView manufacturingText;
    private TextView originsText;
    private String currentManufacturingPlaces;
    private String currentOrigins;
    // New fields
    private CardView productInfoCard;
    private LinearLayout brandSection;
    private LinearLayout quantitySection;
    private TextView brandText;
    private TextView quantityText;
    private CardView labelsCard;
    private TextView labelsText;
    private CardView productAllergensCard;
    private LinearLayout allergensContainsSection;
    private LinearLayout allergensTracesSection;
    private TextView allergensContainsText;
    private TextView allergensTracesText;
    private CardView nutritionCard;
    private MaterialButton btnShowNutrition;
    private LinearLayout nutritionContent;
    private boolean nutritionExpanded = false;
    private TextView nutritionEnergy;
    private TextView nutritionFat;
    private TextView nutritionSaturatedFat;
    private TextView nutritionCarbs;
    private TextView nutritionSugars;
    private TextView nutritionFiber;
    private TextView nutritionProteins;
    private TextView nutritionSalt;
    private MaterialButton btnShowAllergens;
    private LinearLayout allergensContent;
    private boolean allergensExpanded = false;
    private CardView noAdditivesCard;
    private LinearLayout ecoscoreSection;
    private TextView ecoscoreBadge;
    private String currentEcoscore;
    private String currentBrands;
    private String currentQuantity;
    private String currentLabels;
    private String currentAllergens;
    private String currentTraces;
    private float currentEnergyKcal;
    private float currentFat;
    private float currentSaturatedFat;
    private float currentCarbs;
    private float currentSugars;
    private float currentFiber;
    private float currentProteins;
    private float currentSalt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        certainRecyclerView = findViewById(R.id.certainRecyclerView);
        ambiguousRecyclerView = findViewById(R.id.ambiguousRecyclerView);
        noResultsText = findViewById(R.id.noResultsText);
        resultsProductName = findViewById(R.id.resultsProductName);
        resultsAdditiveCount = findViewById(R.id.resultsAdditiveCount);
        certainTitle = findViewById(R.id.certainTitle);
        ambiguousTitle = findViewById(R.id.ambiguousTitle);
        certainSection = findViewById(R.id.certainSection);
        ambiguousSection = findViewById(R.id.ambiguousSection);
        resultsScrollView = findViewById(R.id.resultsScrollView);
        btnManualSearch = findViewById(R.id.btnManualSearch);
        btnShowWarning = findViewById(R.id.btnShowWarning);
        warningCard = findViewById(R.id.warningCard);
        fabFavorite = findViewById(R.id.fabFavorite);
        ingredientsCard = findViewById(R.id.ingredientsCard);
        btnShowIngredients = findViewById(R.id.btnShowIngredients);
        ingredientsText = findViewById(R.id.ingredientsText);
        profileAlertCard = findViewById(R.id.profileAlertCard);
        profileAlertMessage = findViewById(R.id.profileAlertMessage);
        // Scores views
        scoresCard = findViewById(R.id.scoresCard);
        nutriscoreSection = findViewById(R.id.nutriscoreSection);
        novaSection = findViewById(R.id.novaSection);
        nutriscoreBadge = findViewById(R.id.nutriscoreBadge);
        novaBadge = findViewById(R.id.novaBadge);
        novaDescription = findViewById(R.id.novaDescription);
        // Origin views
        originCard = findViewById(R.id.originCard);
        manufacturingSection = findViewById(R.id.manufacturingSection);
        originsSection = findViewById(R.id.originsSection);
        originDivider = findViewById(R.id.originDivider);
        manufacturingText = findViewById(R.id.manufacturingText);
        originsText = findViewById(R.id.originsText);
        // New views
        productInfoCard = findViewById(R.id.productInfoCard);
        brandSection = findViewById(R.id.brandSection);
        quantitySection = findViewById(R.id.quantitySection);
        brandText = findViewById(R.id.brandText);
        quantityText = findViewById(R.id.quantityText);
        labelsCard = findViewById(R.id.labelsCard);
        labelsText = findViewById(R.id.labelsText);
        productAllergensCard = findViewById(R.id.productAllergensCard);
        allergensInfoSection = findViewById(R.id.allergensInfoSection);
        allergensContainsSection = findViewById(R.id.allergensContainsSection);
        allergensTracesSection = findViewById(R.id.allergensTracesSection);
        allergensContainsText = findViewById(R.id.allergensContainsText);
        allergensTracesText = findViewById(R.id.allergensTracesText);
        allergenProfileAlertCard = findViewById(R.id.allergenProfileAlertCard);
        allergenProfileAlertText = findViewById(R.id.allergenProfileAlertText);
        btnShowAllergens = findViewById(R.id.btnShowAllergens);
        allergensContent = findViewById(R.id.allergensContent);
        nutritionCard = findViewById(R.id.nutritionCard);
        btnShowNutrition = findViewById(R.id.btnShowNutrition);
        nutritionContent = findViewById(R.id.nutritionContent);
        nutritionEnergy = findViewById(R.id.nutritionEnergy);
        nutritionFat = findViewById(R.id.nutritionFat);
        nutritionSaturatedFat = findViewById(R.id.nutritionSaturatedFat);
        nutritionCarbs = findViewById(R.id.nutritionCarbs);
        nutritionSugars = findViewById(R.id.nutritionSugars);
        nutritionFiber = findViewById(R.id.nutritionFiber);
        nutritionProteins = findViewById(R.id.nutritionProteins);
        nutritionSalt = findViewById(R.id.nutritionSalt);
        noAdditivesCard = findViewById(R.id.noAdditivesCard);
        ecoscoreSection = findViewById(R.id.ecoscoreSection);
        ecoscoreBadge = findViewById(R.id.ecoscoreBadge);

        dbHelper = new DatabaseHelper(this);
        profileManager = new ProfileManager(this);

        // Toggle card informativa
        btnShowWarning.setOnClickListener(v -> {
            if (warningCard.getVisibility() == View.GONE) {
                warningCard.setVisibility(View.VISIBLE);
                btnShowWarning.setIconResource(android.R.drawable.arrow_up_float);
            } else {
                warningCard.setVisibility(View.GONE);
                btnShowWarning.setIconResource(android.R.drawable.ic_dialog_alert);
            }
        });

        // Toggle ingredienti
        btnShowIngredients.setOnClickListener(v -> {
            ingredientsExpanded = !ingredientsExpanded;
            if (ingredientsExpanded) {
                ingredientsText.setVisibility(View.VISIBLE);
                btnShowIngredients.setIcon(getDrawable(android.R.drawable.arrow_up_float));
            } else {
                ingredientsText.setVisibility(View.GONE);
                btnShowIngredients.setIcon(getDrawable(android.R.drawable.arrow_down_float));
            }
        });

        // Toggle valori nutrizionali
        btnShowNutrition.setOnClickListener(v -> {
            nutritionExpanded = !nutritionExpanded;
            if (nutritionExpanded) {
                nutritionContent.setVisibility(View.VISIBLE);
                btnShowNutrition.setIcon(getDrawable(android.R.drawable.arrow_up_float));
            } else {
                nutritionContent.setVisibility(View.GONE);
                btnShowNutrition.setIcon(getDrawable(android.R.drawable.arrow_down_float));
            }
        });

        // Toggle allergeni
        btnShowAllergens.setOnClickListener(v -> {
            allergensExpanded = !allergensExpanded;
            if (allergensExpanded) {
                allergensContent.setVisibility(View.VISIBLE);
                btnShowAllergens.setIcon(getDrawable(android.R.drawable.arrow_up_float));
            } else {
                allergensContent.setVisibility(View.GONE);
                btnShowAllergens.setIcon(getDrawable(android.R.drawable.arrow_down_float));
            }
        });

        // Pulsante per ricerca manuale
        btnManualSearch.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });

        // Recupera dati prodotto (da savedInstanceState se disponibile, altrimenti dall'Intent)
        ArrayList<String> certainCodes;
        ArrayList<String> ambiguousCodes;
        ArrayList<String> ambiguousPhrases;
        ArrayList<String> missingCodes;
        String fullIngredients;

        if (savedInstanceState != null) {
            // Ripristina dati salvati (utile per cambio lingua)
            currentBarcode = savedInstanceState.getString("barcode");
            certainCodes = savedInstanceState.getStringArrayList("certainMatches");
            ambiguousCodes = savedInstanceState.getStringArrayList("ambiguousMatches");
            ambiguousPhrases = savedInstanceState.getStringArrayList("ambiguousPhrases");
            currentProductName = savedInstanceState.getString("productName");
            missingCodes = savedInstanceState.getStringArrayList("missingCodes");
            fullIngredients = savedInstanceState.getString("ingredientsText");
            currentIngredients = fullIngredients;
            currentNutriscore = savedInstanceState.getString("nutriscore");
            currentNovaGroup = savedInstanceState.getInt("novaGroup", 0);
            currentManufacturingPlaces = savedInstanceState.getString("manufacturingPlaces");
            currentOrigins = savedInstanceState.getString("origins");
            currentEcoscore = savedInstanceState.getString("ecoscore");
            currentBrands = savedInstanceState.getString("brands");
            currentQuantity = savedInstanceState.getString("quantity");
            currentLabels = savedInstanceState.getString("labels");
            currentAllergens = savedInstanceState.getString("allergens");
            currentTraces = savedInstanceState.getString("traces");
            currentEnergyKcal = savedInstanceState.getFloat("energyKcal", 0);
            currentFat = savedInstanceState.getFloat("fat", 0);
            currentSaturatedFat = savedInstanceState.getFloat("saturatedFat", 0);
            currentCarbs = savedInstanceState.getFloat("carbs", 0);
            currentSugars = savedInstanceState.getFloat("sugars", 0);
            currentFiber = savedInstanceState.getFloat("fiber", 0);
            currentProteins = savedInstanceState.getFloat("proteins", 0);
            currentSalt = savedInstanceState.getFloat("salt", 0);
        } else {
            // Prima apertura, leggi dall'Intent
            currentBarcode = getIntent().getStringExtra("barcode");
            certainCodes = getIntent().getStringArrayListExtra("certainMatches");
            ambiguousCodes = getIntent().getStringArrayListExtra("ambiguousMatches");
            ambiguousPhrases = getIntent().getStringArrayListExtra("ambiguousPhrases");
            currentProductName = getIntent().getStringExtra("productName");
            missingCodes = getIntent().getStringArrayListExtra("missingCodes");
            fullIngredients = getIntent().getStringExtra("ingredientsText");
            currentIngredients = fullIngredients;
            currentNutriscore = getIntent().getStringExtra("nutriscore");
            currentNovaGroup = getIntent().getIntExtra("novaGroup", 0);
            currentManufacturingPlaces = getIntent().getStringExtra("manufacturingPlaces");
            currentOrigins = getIntent().getStringExtra("origins");
            // New data
            currentEcoscore = getIntent().getStringExtra("ecoscore");
            currentBrands = getIntent().getStringExtra("brands");
            currentQuantity = getIntent().getStringExtra("quantity");
            currentLabels = getIntent().getStringExtra("labels");
            currentAllergens = getIntent().getStringExtra("allergens");
            currentTraces = getIntent().getStringExtra("traces");
            currentEnergyKcal = getIntent().getFloatExtra("energyKcal", 0);
            currentFat = getIntent().getFloatExtra("fat", 0);
            currentSaturatedFat = getIntent().getFloatExtra("saturatedFat", 0);
            currentCarbs = getIntent().getFloatExtra("carbs", 0);
            currentSugars = getIntent().getFloatExtra("sugars", 0);
            currentFiber = getIntent().getFloatExtra("fiber", 0);
            currentProteins = getIntent().getFloatExtra("proteins", 0);
            currentSalt = getIntent().getFloatExtra("salt", 0);
        }

        // Setup all product info displays
        setupProductInfoDisplay();
        setupScoresDisplay();
        setupLabelsDisplay();
        setupOriginDisplay();
        setupProductAllergensDisplay();
        setupNutritionDisplay();

        // Mostra card ingredienti se disponibili
        if (fullIngredients != null && !fullIngredients.isEmpty()) {
            ingredientsCard.setVisibility(View.VISIBLE);
            ingredientsText.setText(formatIngredients(fullIngredients));
        }

        // Salva tutti i codici E trovati (per allergen detection)
        currentECodes = new ArrayList<>();
        if (certainCodes != null) currentECodes.addAll(certainCodes);
        if (ambiguousCodes != null) currentECodes.addAll(ambiguousCodes);

        // Detect allergens if user has enabled allergen tracking
        AllergenDetector allergenDetector = new AllergenDetector(this);
        allergenMatches = allergenDetector.detectAllergens(fullIngredients, currentECodes);

        // Inizializza lista per condivisione
        allAdditives = new ArrayList<>();

        // Il salvataggio in cronologia viene fatto dopo il processing degli additivi
        // per salvare solo i codici E validi (presenti nel database)

        // Setup FAB preferiti
        updateFavoriteIcon();
        fabFavorite.setOnClickListener(v -> toggleFavorite());

        // Setup header con nome prodotto
        if (currentProductName != null && !currentProductName.isEmpty()) {
            resultsProductName.setText(currentProductName);
        } else {
            resultsProductName.setText(getString(R.string.results_title));
        }

        // Se non ci sono additivi, mostra il card "prodotto senza additivi"
        // ma continua a mostrare le altre info (ingredienti, origine, nutrizionali, ecc.)
        if ((certainCodes == null || certainCodes.isEmpty()) &&
            (ambiguousCodes == null || ambiguousCodes.isEmpty())) {
            noAdditivesCard.setVisibility(View.VISIBLE);
            // Don't return - let other sections display
        }

        int totalCount = 0;

        // Set per evitare duplicati (es: E965 e E965i sono lo stesso additivo base)
        Set<String> addedCodes = new HashSet<>();

        // Gestione match certi
        if (certainCodes != null && !certainCodes.isEmpty()) {
            List<Additive> certainAdditives = new ArrayList<>();
            for (String code : certainCodes) {
                Additive additive = dbHelper.getAdditiveByCode(code);
                if (additive != null) {
                    // Normalizza al codice base per deduplicazione (E965i -> E965)
                    String baseCode = normalizeToBaseCode(additive.getCode());
                    if (!addedCodes.contains(baseCode)) {
                        addedCodes.add(baseCode);
                        certainAdditives.add(additive);
                        allAdditives.add(additive);
                    }
                }
            }

            if (!certainAdditives.isEmpty()) {
                certainSection.setVisibility(View.VISIBLE);
                LinearLayoutManager layoutManager = new LinearLayoutManager(this);
                certainRecyclerView.setLayoutManager(layoutManager);
                certainRecyclerView.setHasFixedSize(false);
                certainRecyclerView.setNestedScrollingEnabled(false);
                AdditiveAdapter certainAdapter = new AdditiveAdapter(certainAdditives, this::openDetail, profileManager);
                certainRecyclerView.setAdapter(certainAdapter);
                totalCount += certainAdditives.size();
            }
        }

        // Gestione match ambigui
        if (ambiguousCodes != null && !ambiguousCodes.isEmpty()) {
            List<Additive> ambiguousAdditives = new ArrayList<>();
            for (String code : ambiguousCodes) {
                Additive additive = dbHelper.getAdditiveByCode(code);
                if (additive != null) {
                    String baseCode = normalizeToBaseCode(additive.getCode());
                    if (!addedCodes.contains(baseCode)) {
                        addedCodes.add(baseCode);
                        ambiguousAdditives.add(additive);
                        allAdditives.add(additive);
                    }
                }
            }

            if (!ambiguousAdditives.isEmpty()) {
                ambiguousSection.setVisibility(View.VISIBLE);

                // Crea messaggio con frasi ambigue trovate
                String phrasesText = "";
                if (ambiguousPhrases != null && !ambiguousPhrases.isEmpty()) {
                    phrasesText = " [" + String.join(", ", ambiguousPhrases) + "]";
                }

                ambiguousTitle.setText("⚠ Possibili additivi - Verifica (" + ambiguousAdditives.size() + ")" + phrasesText);

                // Aggiorna descrizione con frasi trovate
                TextView ambiguousDescription = findViewById(R.id.ambiguousDescription);
                if (ambiguousDescription != null && ambiguousPhrases != null && !ambiguousPhrases.isEmpty()) {
                    ambiguousDescription.setText("Trovate frasi ambigue: '" + String.join("', '", ambiguousPhrases) +
                        "'. Controlla l'etichetta per confermare quale additivo è presente.");
                }

                LinearLayoutManager ambiguousLayoutManager = new LinearLayoutManager(this);
                ambiguousRecyclerView.setLayoutManager(ambiguousLayoutManager);
                ambiguousRecyclerView.setHasFixedSize(false);
                ambiguousRecyclerView.setNestedScrollingEnabled(false);
                AdditiveAdapter ambiguousAdapter = new AdditiveAdapter(ambiguousAdditives, this::openDetail, profileManager);
                ambiguousRecyclerView.setAdapter(ambiguousAdapter);
                totalCount += ambiguousAdditives.size();
            }
        }

        // Mostra codici mancanti dal database
        View missingCodesCard = findViewById(R.id.missingCodesCard);
        TextView missingCodesWarning = findViewById(R.id.missingCodesWarning);
        if (missingCodes != null && !missingCodes.isEmpty()) {
            missingCodesWarning.setText("Codici rilevati ma non in database (" + missingCodes.size() + "): " + String.join(", ", missingCodes));
            missingCodesCard.setVisibility(View.VISIBLE);
        } else {
            missingCodesCard.setVisibility(View.GONE);
        }

        // Salva in cronologia SOLO i codici E validi (presenti nel database)
        // Questo assicura che il conteggio nella cronologia corrisponda a quello mostrato
        boolean fromHistory = getIntent().getBooleanExtra("fromHistory", false);
        if (!fromHistory && currentBarcode != null && currentProductName != null) {
            // Costruisci lista di soli codici E validi da allAdditives
            List<String> validECodes = new ArrayList<>();
            for (Additive additive : allAdditives) {
                validECodes.add(additive.getCode());
            }
            dbHelper.addToHistory(currentBarcode, currentProductName, validECodes, currentIngredients,
                    currentNutriscore, currentNovaGroup, currentManufacturingPlaces, currentOrigins,
                    currentEcoscore, currentBrands, currentQuantity, currentLabels, currentAllergens, currentTraces,
                    currentEnergyKcal, currentFat, currentSaturatedFat, currentCarbs, currentSugars, currentFiber, currentProteins, currentSalt);
            // Aggiorna currentECodes per coerenza
            currentECodes = validECodes;
        }

        // Mostra contatore additivi nell'header
        if (totalCount == 0) {
            // Show "no additives" card if not already shown
            noAdditivesCard.setVisibility(View.VISIBLE);
            resultsAdditiveCount.setText(getString(R.string.results_no_additives_count));
        } else {
            String countText = totalCount == 1
                ? getString(R.string.additives_detected_singular, totalCount)
                : getString(R.string.additives_detected_plural, totalCount);
            resultsAdditiveCount.setText(countText);
        }
        resultsAdditiveCount.setVisibility(View.VISIBLE);

        // Check profile alerts
        checkProfileAlerts();

        // Show allergen alerts
        showAllergenAlerts();

        // ActionBar sempre con titolo generico
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.results_title);
        }
    }

    private void checkProfileAlerts() {
        // Check if user has an active profile
        if (!profileManager.hasActiveProfile()) {
            return; // No profile configured, no alerts
        }

        if (allAdditives == null || allAdditives.isEmpty()) {
            return; // No additives to check
        }

        // Count non-compliant additives
        int nonCompliantCount = profileManager.countNonCompliantAdditives(allAdditives);

        if (nonCompliantCount == 0) {
            return; // All good, no alerts needed
        }

        // Build compact alert message (details are shown in individual additive cards)
        String alertMsg = nonCompliantCount + " additiv" +
                (nonCompliantCount == 1 ? "o" : "i") +
                " non conform" + (nonCompliantCount == 1 ? "e" : "i") +
                " al tuo profilo";

        // Show compact alert card
        profileAlertMessage.setText(alertMsg);
        profileAlertCard.setVisibility(View.VISIBLE);
    }

    private void showAllergenAlerts() {
        if (allergenMatches == null || allergenMatches.isEmpty()) {
            allergenProfileAlertCard.setVisibility(View.GONE);
            return; // No allergens detected in profile
        }

        // Build alert message
        StringBuilder alertMsg = new StringBuilder();

        for (AllergenDetector.AllergenMatch match : allergenMatches) {
            alertMsg.append("• ").append(match.getAllergenName(this));

            // Show found keywords
            if (!match.foundKeywords.isEmpty()) {
                alertMsg.append(" (").append(String.join(", ", match.foundKeywords)).append(")");
            }

            // Show found E-codes
            if (!match.foundECodes.isEmpty()) {
                alertMsg.append(" [").append(String.join(", ", match.foundECodes)).append("]");
            }

            alertMsg.append("\n");
        }

        // Show alert in separate always-visible card
        allergenProfileAlertText.setText(alertMsg.toString().trim());
        allergenProfileAlertCard.setVisibility(View.VISIBLE);
    }

    private void showNoResults() {
        resultsScrollView.setVisibility(View.GONE);
        noResultsText.setVisibility(View.VISIBLE);

        // Se c'è un nome prodotto, significa che il barcode è stato trovato ma non ha dati
        String productName = getIntent().getStringExtra("productName");
        if (productName != null && !productName.isEmpty()) {
            noResultsText.setText("Prodotto: " + productName + "\n\n" +
                "Il database non contiene informazioni sugli ingredienti.\n\n" +
                "Cerca manualmente i codici E che vedi sull'etichetta (es: E440, E471)");
        }
    }

    private void openDetail(Additive additive) {
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra("code", additive.getCode());
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_results, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_share) {
            shareResults();
            return true;
        } else if (id == R.id.action_home) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
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

    private void toggleFavorite() {
        if (currentBarcode == null || currentProductName == null || currentECodes == null) {
            return;
        }

        if (dbHelper.isFavorite(currentBarcode)) {
            dbHelper.removeFromFavorites(currentBarcode);
            Toast.makeText(this, getString(R.string.favorites_removed), Toast.LENGTH_SHORT).show();
        } else {
            dbHelper.addToFavorites(currentBarcode, currentProductName, currentECodes, currentIngredients,
                    currentNutriscore, currentNovaGroup, currentManufacturingPlaces, currentOrigins,
                    currentEcoscore, currentBrands, currentQuantity, currentLabels, currentAllergens, currentTraces,
                    currentEnergyKcal, currentFat, currentSaturatedFat, currentCarbs, currentSugars, currentFiber, currentProteins, currentSalt);
            Toast.makeText(this, getString(R.string.favorites_added), Toast.LENGTH_SHORT).show();
        }
        updateFavoriteIcon();
    }

    private void updateFavoriteIcon() {
        if (currentBarcode != null && dbHelper.isFavorite(currentBarcode)) {
            fabFavorite.setImageResource(android.R.drawable.star_big_on);
            fabFavorite.setImageTintList(android.content.res.ColorStateList.valueOf(0xFFFFD700)); // Oro
        } else {
            fabFavorite.setImageResource(android.R.drawable.star_big_off);
            fabFavorite.setImageTintList(android.content.res.ColorStateList.valueOf(0xFFFFFFFF)); // Bianco
        }
    }

    private void shareResults() {
        StringBuilder shareText = new StringBuilder();

        // Intestazione
        shareText.append("📱 E-Codes - Analisi Prodotto\n");
        shareText.append("━━━━━━━━━━━━━━━━━━━━━━\n\n");

        // Informazioni prodotto
        if (currentProductName != null && !currentProductName.isEmpty()) {
            shareText.append("🏷️ Prodotto: ").append(currentProductName).append("\n");
        }
        if (currentBarcode != null && !currentBarcode.isEmpty()) {
            shareText.append("📊 Barcode: ").append(currentBarcode).append("\n");
        }
        shareText.append("\n");

        // Additivi trovati
        if (allAdditives.isEmpty()) {
            shareText.append("✅ Nessun additivo rilevato\n");
            shareText.append("Prodotto senza additivi alimentari.\n");
        } else {
            shareText.append("🔬 Additivi trovati: ").append(allAdditives.size()).append("\n\n");

            for (Additive additive : allAdditives) {
                shareText.append("• ").append(additive.getCode()).append(" - ").append(additive.getName()).append("\n");
                shareText.append("  ").append(getClassificationEmoji(additive.getClassification()))
                         .append(" ").append(getClassificationText(additive.getClassification())).append("\n");

                // ADI se disponibile
                if (additive.getAdi() != null) {
                    shareText.append("  ADI: ").append(String.format("%.1f mg/kg", additive.getAdi())).append("\n");
                }

                shareText.append("\n");
            }
        }

        shareText.append("━━━━━━━━━━━━━━━━━━━━━━\n");
        shareText.append("Analizzato con E-Codes App");

        // Crea intent di condivisione
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Analisi Additivi - " + currentProductName);
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText.toString());

        // Lancia il chooser
        startActivity(Intent.createChooser(shareIntent, "Condividi tramite"));
    }

    private String getClassificationEmoji(String classification) {
        switch (classification) {
            case "A": return "✅";
            case "B": return "⚠️";
            case "C": return "🟠";
            case "D": return "🔴";
            case "E": return "☢️";
            case "F": return "🚫";
            case "N": return "⚪";
            default: return "❓";
        }
    }

    private String getClassificationText(String classification) {
        if (classification == null) {
            return getString(R.string.classification_unknown_short);
        }
        switch (classification) {
            case "A": return getString(R.string.classification_a_short);
            case "B": return getString(R.string.classification_b_short);
            case "C": return getString(R.string.classification_c_short);
            case "D": return getString(R.string.classification_d_short);
            case "E": return getString(R.string.classification_e_short);
            case "F": return getString(R.string.classification_f_short);
            case "N": return getString(R.string.classification_n_short);
            default: return getString(R.string.classification_unknown_short);
        }
    }

    private String formatIngredients(String raw) {
        if (raw == null || raw.isEmpty()) {
            return "";
        }

        String formatted = raw;

        // Rimuove tag HTML se presenti
        formatted = formatted.replaceAll("<[^>]*>", "");

        // Decodifica entità HTML comuni
        formatted = formatted.replace("&quot;", "\"");
        formatted = formatted.replace("&amp;", "&");
        formatted = formatted.replace("&lt;", "<");
        formatted = formatted.replace("&gt;", ">");
        formatted = formatted.replace("&nbsp;", " ");
        formatted = formatted.replace("&#39;", "'");

        // Sostituisce underscore con spazi
        formatted = formatted.replace("_", " ");

        // Rimuove spazi multipli
        formatted = formatted.replaceAll("\\s+", " ");

        // Capitalizza prima lettera
        if (formatted.length() > 0) {
            formatted = formatted.substring(0, 1).toUpperCase() + formatted.substring(1);
        }

        // Rimuove spazi all'inizio e fine
        formatted = formatted.trim();

        return formatted;
    }

    @Override
    protected void onSaveInstanceState(android.os.Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("barcode", currentBarcode);
        outState.putString("productName", currentProductName);
        outState.putString("ingredientsText", currentIngredients);
        outState.putStringArrayList("certainMatches", new ArrayList<>(currentECodes));
        outState.putStringArrayList("ambiguousMatches", new ArrayList<>());
        outState.putStringArrayList("ambiguousPhrases", new ArrayList<>());
        outState.putStringArrayList("missingCodes", new ArrayList<>());
        outState.putString("nutriscore", currentNutriscore);
        outState.putInt("novaGroup", currentNovaGroup);
        outState.putString("manufacturingPlaces", currentManufacturingPlaces);
        outState.putString("origins", currentOrigins);
        outState.putString("ecoscore", currentEcoscore);
        outState.putString("brands", currentBrands);
        outState.putString("quantity", currentQuantity);
        outState.putString("labels", currentLabels);
        outState.putString("allergens", currentAllergens);
        outState.putString("traces", currentTraces);
        outState.putFloat("energyKcal", currentEnergyKcal);
        outState.putFloat("fat", currentFat);
        outState.putFloat("saturatedFat", currentSaturatedFat);
        outState.putFloat("carbs", currentCarbs);
        outState.putFloat("sugars", currentSugars);
        outState.putFloat("fiber", currentFiber);
        outState.putFloat("proteins", currentProteins);
        outState.putFloat("salt", currentSalt);
    }

    private void setupScoresDisplay() {
        boolean hasNutriscore = currentNutriscore != null && !currentNutriscore.isEmpty();
        boolean hasNova = currentNovaGroup >= 1 && currentNovaGroup <= 4;
        boolean hasEcoscore = currentEcoscore != null && !currentEcoscore.isEmpty() && !currentEcoscore.equals("unknown");

        if (!hasNutriscore && !hasNova && !hasEcoscore) {
            scoresCard.setVisibility(View.GONE);
            return;
        }

        scoresCard.setVisibility(View.VISIBLE);

        // Nutri-Score display - CERCHIO
        if (hasNutriscore) {
            nutriscoreSection.setVisibility(View.VISIBLE);
            String grade = currentNutriscore.toUpperCase();
            nutriscoreBadge.setText(grade);
            int bgColor = getScoreColor(grade);
            android.graphics.drawable.GradientDrawable drawable = new android.graphics.drawable.GradientDrawable();
            drawable.setShape(android.graphics.drawable.GradientDrawable.OVAL);
            drawable.setColor(bgColor);
            nutriscoreBadge.setBackground(drawable);
        } else {
            nutriscoreSection.setVisibility(View.GONE);
        }

        // Nova Score display - QUADRATO
        if (hasNova) {
            novaSection.setVisibility(View.VISIBLE);
            novaBadge.setText(String.valueOf(currentNovaGroup));
            int bgColor;
            int descResId;
            switch (currentNovaGroup) {
                case 1: bgColor = 0xFF038141; descResId = R.string.nova_1; break;
                case 2: bgColor = 0xFF85BB2F; descResId = R.string.nova_2; break;
                case 3: bgColor = 0xFFEE8100; descResId = R.string.nova_3; break;
                case 4: bgColor = 0xFFE63E11; descResId = R.string.nova_4; break;
                default: bgColor = 0xFF9E9E9E; descResId = R.string.nutriscore_not_available; break;
            }
            android.graphics.drawable.GradientDrawable drawable = new android.graphics.drawable.GradientDrawable();
            drawable.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
            drawable.setCornerRadius(8f);
            drawable.setColor(bgColor);
            novaBadge.setBackground(drawable);
            novaDescription.setText(descResId);
        } else {
            novaSection.setVisibility(View.GONE);
        }

        // Eco-Score display - FOGLIA (cerchio verde)
        if (hasEcoscore) {
            ecoscoreSection.setVisibility(View.VISIBLE);
            String grade = currentEcoscore.toUpperCase();
            ecoscoreBadge.setText(grade);
            int bgColor = getScoreColor(grade);
            android.graphics.drawable.GradientDrawable drawable = new android.graphics.drawable.GradientDrawable();
            drawable.setShape(android.graphics.drawable.GradientDrawable.OVAL);
            drawable.setColor(bgColor);
            ecoscoreBadge.setBackground(drawable);
        } else {
            ecoscoreSection.setVisibility(View.GONE);
        }
    }

    private int getScoreColor(String grade) {
        switch (grade) {
            case "A": return 0xFF038141;
            case "B": return 0xFF85BB2F;
            case "C": return 0xFFFECB02;
            case "D": return 0xFFEE8100;
            case "E": return 0xFFE63E11;
            default: return 0xFF9E9E9E;
        }
    }

    private void setupOriginDisplay() {
        boolean hasManufacturing = currentManufacturingPlaces != null && !currentManufacturingPlaces.trim().isEmpty();
        boolean hasOrigins = currentOrigins != null && !currentOrigins.trim().isEmpty();

        if (!hasManufacturing && !hasOrigins) {
            originCard.setVisibility(View.GONE);
            return;
        }

        originCard.setVisibility(View.VISIBLE);

        // Manufacturing places
        if (hasManufacturing) {
            manufacturingSection.setVisibility(View.VISIBLE);
            manufacturingText.setText(formatOriginText(currentManufacturingPlaces));
        } else {
            manufacturingSection.setVisibility(View.GONE);
        }

        // Origins (ingredients)
        if (hasOrigins) {
            originsSection.setVisibility(View.VISIBLE);
            originsText.setText(formatOriginText(currentOrigins));
        } else {
            originsSection.setVisibility(View.GONE);
        }

        // Show divider only if both are visible
        originDivider.setVisibility(hasManufacturing && hasOrigins ? View.VISIBLE : View.GONE);
    }

    private String formatOriginText(String text) {
        if (text == null || text.isEmpty()) return "";

        String[] parts = text.split(",");
        StringBuilder formatted = new StringBuilder();

        for (int i = 0; i < parts.length; i++) {
            String part = parts[i].trim();
            if (!part.isEmpty()) {
                if (part.length() > 1) {
                    part = part.substring(0, 1).toUpperCase() + part.substring(1);
                } else {
                    part = part.toUpperCase();
                }
                if (formatted.length() > 0) {
                    formatted.append(", ");
                }
                formatted.append(part);
            }
        }

        return formatted.toString();
    }

    private void setupProductInfoDisplay() {
        boolean hasBrand = currentBrands != null && !currentBrands.trim().isEmpty();
        boolean hasQuantity = currentQuantity != null && !currentQuantity.trim().isEmpty();

        if (!hasBrand && !hasQuantity) {
            productInfoCard.setVisibility(View.GONE);
            return;
        }

        productInfoCard.setVisibility(View.VISIBLE);

        if (hasBrand) {
            brandSection.setVisibility(View.VISIBLE);
            brandText.setText(currentBrands);
        } else {
            brandSection.setVisibility(View.GONE);
        }

        if (hasQuantity) {
            quantitySection.setVisibility(View.VISIBLE);
            quantityText.setText(currentQuantity);
        } else {
            quantitySection.setVisibility(View.GONE);
        }
    }

    private void setupLabelsDisplay() {
        if (currentLabels == null || currentLabels.trim().isEmpty()) {
            labelsCard.setVisibility(View.GONE);
            return;
        }

        labelsCard.setVisibility(View.VISIBLE);
        labelsText.setText(currentLabels);
    }

    private void setupProductAllergensDisplay() {
        boolean hasAllergens = currentAllergens != null && !currentAllergens.trim().isEmpty();
        boolean hasTraces = currentTraces != null && !currentTraces.trim().isEmpty();

        // La card sarà mostrata se ci sono info API o alert profilo (gestito in showAllergenAlerts)
        if (!hasAllergens && !hasTraces) {
            allergensInfoSection.setVisibility(View.GONE);
            return;
        }

        // Mostra la sezione info
        allergensInfoSection.setVisibility(View.VISIBLE);
        productAllergensCard.setVisibility(View.VISIBLE);

        if (hasAllergens) {
            allergensContainsSection.setVisibility(View.VISIBLE);
            allergensContainsText.setText(currentAllergens);
        } else {
            allergensContainsSection.setVisibility(View.GONE);
        }

        if (hasTraces) {
            allergensTracesSection.setVisibility(View.VISIBLE);
            allergensTracesText.setText(currentTraces);
        } else {
            allergensTracesSection.setVisibility(View.GONE);
        }
    }

    private void setupNutritionDisplay() {
        // Show nutrition card only if we have at least energy value
        if (currentEnergyKcal <= 0) {
            nutritionCard.setVisibility(View.GONE);
            return;
        }

        nutritionCard.setVisibility(View.VISIBLE);

        nutritionEnergy.setText(String.format("%.0f %s", currentEnergyKcal, getString(R.string.nutrition_kcal)));
        nutritionFat.setText(String.format("%.1f %s", currentFat, getString(R.string.nutrition_g)));
        nutritionSaturatedFat.setText(String.format("%.1f %s", currentSaturatedFat, getString(R.string.nutrition_g)));
        nutritionCarbs.setText(String.format("%.1f %s", currentCarbs, getString(R.string.nutrition_g)));
        nutritionSugars.setText(String.format("%.1f %s", currentSugars, getString(R.string.nutrition_g)));
        nutritionFiber.setText(String.format("%.1f %s", currentFiber, getString(R.string.nutrition_g)));
        nutritionProteins.setText(String.format("%.1f %s", currentProteins, getString(R.string.nutrition_g)));
        nutritionSalt.setText(String.format("%.2f %s", currentSalt, getString(R.string.nutrition_g)));
    }

    /**
     * Normalizza un codice additivo al suo codice base rimuovendo suffissi variante.
     * Es: E965i -> E965, E965ii -> E965, E150d -> E150, E553b -> E553
     */
    private String normalizeToBaseCode(String code) {
        if (code == null || code.length() < 4) return code;

        String upper = code.toUpperCase();
        if (!upper.startsWith("E")) return upper;

        // Rimuovi suffissi romani (i, ii, iii, iv, v, vi) o lettere (a, b, c, d, e, f)
        String base = upper.replaceAll("(I{1,3}|IV|V|VI|[A-F])$", "");

        // Verifica che rimanga un codice valido
        if (base.matches("E\\d{3,4}")) {
            return base;
        }
        return upper;
    }
}
