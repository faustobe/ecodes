package it.faustobe.ecodes.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Size;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import it.faustobe.ecodes.R;
import it.faustobe.ecodes.data.DatabaseHelper;
import it.faustobe.ecodes.model.Additive;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.security.ProviderInstaller;
import com.google.common.util.concurrent.ListenableFuture;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends BaseActivity {
    private DatabaseHelper dbHelper;
    private RecyclerView recyclerView;
    private AdditiveAdapter adapter;
    private TextInputEditText searchEdit;
    private View resultsScrollView;
    private TextView resultsCount;
    private PreviewView previewView;
    private TextView statusText;
    private ProgressBar progressBar;
    private CardView cameraCard;
    private ExecutorService cameraExecutor;
    private BarcodeScanner barcodeScanner;
    private boolean isProcessing = false;
    private OkHttpClient httpClient;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    startCamera();
                } else {
                    Toast.makeText(this, R.string.toast_camera_permission, Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Update security provider for SSL on older Android versions
        updateSecurityProvider();

        // Initialize views
        dbHelper = new DatabaseHelper(this);
        searchEdit = findViewById(R.id.searchEdit);
        recyclerView = findViewById(R.id.recyclerView);
        resultsScrollView = findViewById(R.id.resultsScrollView);
        resultsCount = findViewById(R.id.resultsCount);
        previewView = findViewById(R.id.previewView);
        statusText = findViewById(R.id.statusText);
        progressBar = findViewById(R.id.progressBar);
        cameraCard = findViewById(R.id.cameraCard);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdditiveAdapter(new ArrayList<>(), this::openDetail);
        recyclerView.setAdapter(adapter);

        // App version
        TextView appVersionText = findViewById(R.id.appVersionText);
        try {
            String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            appVersionText.setText("E-Codes v" + versionName);
        } catch (Exception e) {
            appVersionText.setText("E-Codes v1.0");
        }

        // HTTP client setup
        httpClient = new OkHttpClient.Builder()
                .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                .build();

        // Camera setup
        cameraExecutor = Executors.newSingleThreadExecutor();
        barcodeScanner = BarcodeScanning.getClient();
        checkCameraPermission();

        // Ricerca manuale
        searchEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (query.isEmpty()) {
                    resultsScrollView.setVisibility(View.GONE);
                    cameraCard.setVisibility(View.VISIBLE);
                } else {
                    // Nascondi camera, mostra risultati
                    cameraCard.setVisibility(View.GONE);
                    List<Additive> results = dbHelper.searchAdditives(query);
                    adapter.updateData(results);
                    resultsScrollView.setVisibility(View.VISIBLE);

                    if (results.isEmpty()) {
                        resultsCount.setText("Nessun risultato trovato");
                    } else {
                        resultsCount.setText(results.size() + " risultat" + (results.size() == 1 ? "o trovato" : "i trovati"));
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindCameraUseCases(cameraProvider);
            } catch (Exception e) {
                Toast.makeText(this, R.string.toast_camera_error, Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindCameraUseCases(ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setTargetResolution(new Size(1280, 720))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        imageAnalysis.setAnalyzer(cameraExecutor, this::analyzeBarcodes);

        try {
            cameraProvider.unbindAll();
            Camera camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalysis);
        } catch (Exception e) {
            Toast.makeText(this, "Errore binding fotocamera", Toast.LENGTH_SHORT).show();
        }
    }

    private void analyzeBarcodes(@NonNull ImageProxy imageProxy) {
        if (isProcessing) {
            imageProxy.close();
            return;
        }

        InputImage image = InputImage.fromMediaImage(
                imageProxy.getImage(),
                imageProxy.getImageInfo().getRotationDegrees()
        );

        barcodeScanner.process(image)
                .addOnSuccessListener(barcodes -> {
                    for (Barcode barcode : barcodes) {
                        String rawValue = barcode.getRawValue();
                        if (rawValue != null && !isProcessing) {
                            isProcessing = true;
                            runOnUiThread(() -> {
                                statusText.setText(getString(R.string.main_barcode_found, rawValue));
                                progressBar.setVisibility(View.VISIBLE);
                            });
                            queryOpenFoodFacts(rawValue);
                            break;
                        }
                    }
                })
                .addOnFailureListener(e -> imageProxy.close())
                .addOnCompleteListener(task -> imageProxy.close());
    }

    private void updateSecurityProvider() {
        try {
            ProviderInstaller.installIfNeeded(this);
        } catch (GooglePlayServicesRepairableException e) {
            // Mostra dialogo per aggiornare Google Play Services
            // Ma non blocchiamo l'app
        } catch (GooglePlayServicesNotAvailableException e) {
            // Google Play Services non disponibile
            // L'app continuerà comunque a funzionare
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        }
        return false;
    }

    private void queryOpenFoodFacts(final String barcode) {
        // Verifica connettività prima di procedere
        if (!isNetworkAvailable()) {
            runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                statusText.setText(R.string.toast_no_network);
                Toast.makeText(MainActivity.this,
                    R.string.error_no_network,
                    Toast.LENGTH_LONG).show();

                new android.os.Handler().postDelayed(() -> {
                    statusText.setText(R.string.main_camera_status);
                    isProcessing = false;
                }, 3000);
            });
            return;
        }

        new Thread(() -> {
            try {
                String urlString = "https://world.openfoodfacts.org/api/v2/product/" + barcode + ".json";

                Request request = new Request.Builder()
                        .url(urlString)
                        .header("User-Agent", "ECodeReader/1.0")
                        .build();

                Response response = httpClient.newCall(request).execute();

                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    response.close();
                    parseOpenFoodFactsResponse(responseBody, barcode);
                } else {
                    response.close();
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        statusText.setText(R.string.error_product_not_found);
                        Toast.makeText(MainActivity.this,
                            R.string.toast_product_not_found,
                            Toast.LENGTH_LONG).show();

                        // Reset automatico dopo 3 secondi per permettere nuova scansione
                        new android.os.Handler().postDelayed(() -> {
                            statusText.setText(R.string.main_camera_status);
                            isProcessing = false;
                        }, 3000);
                    });
                }
            } catch (IOException e) {
                String errorMsg = e.getMessage() != null ? e.getMessage() : "Errore sconosciuto";
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    statusText.setText(R.string.error_connection);
                    Toast.makeText(MainActivity.this,
                        getString(R.string.toast_connection_error) + "\n" + errorMsg,
                        Toast.LENGTH_LONG).show();

                    // Reset automatico dopo 3 secondi per permettere nuova scansione
                    new android.os.Handler().postDelayed(() -> {
                        statusText.setText(R.string.main_camera_status);
                        isProcessing = false;
                    }, 3000);
                });
            }
        }).start();
    }

    private void parseOpenFoodFactsResponse(String jsonResponse, final String barcode) {
        try {
            JSONObject root = new JSONObject(jsonResponse);
            int status = root.getInt("status");

            if (status == 1) {
                JSONObject product = root.getJSONObject("product");
                String productName = product.optString("product_name", "Prodotto sconosciuto");

                // Extract Nutri-Score, Nova Score, Eco-Score
                String nutriscoreGrade = product.optString("nutriscore_grade", "");
                int novaGroup = product.optInt("nova_group", 0);
                String ecoscoreGrade = product.optString("ecoscore_grade", "");

                // Extract origin information
                String manufacturingPlaces = product.optString("manufacturing_places", "");
                String origins = product.optString("origins", "");

                // Extract brand and quantity
                String brands = product.optString("brands", "");
                String quantity = product.optString("quantity", "");
                if (quantity.isEmpty()) {
                    String productQuantity = product.optString("product_quantity", "");
                    String productUnit = product.optString("product_quantity_unit", "g");
                    if (!productQuantity.isEmpty()) {
                        quantity = productQuantity + " " + productUnit;
                    }
                }

                // Extract labels
                String labels = product.optString("labels", "");

                // Extract allergens and traces
                String allergens = extractTagsAsReadableList(product, "allergens_tags");
                String traces = extractTagsAsReadableList(product, "traces_tags");

                // Extract nutritional values
                float energyKcal = 0, fat = 0, saturatedFat = 0, carbs = 0, sugars = 0, fiber = 0, proteins = 0, salt = 0;
                if (product.has("nutriments")) {
                    JSONObject nutriments = product.getJSONObject("nutriments");
                    energyKcal = (float) nutriments.optDouble("energy-kcal_100g", 0);
                    fat = (float) nutriments.optDouble("fat_100g", 0);
                    saturatedFat = (float) nutriments.optDouble("saturated-fat_100g", 0);
                    carbs = (float) nutriments.optDouble("carbohydrates_100g", 0);
                    sugars = (float) nutriments.optDouble("sugars_100g", 0);
                    fiber = (float) nutriments.optDouble("fiber_100g", 0);
                    proteins = (float) nutriments.optDouble("proteins_100g", 0);
                    salt = (float) nutriments.optDouble("salt_100g", 0);
                }

                List<String> certainMatches = new ArrayList<>();

                // Cerca additivi
                String[] additiveFields = {
                    "additives_tags",
                    "additives_original_tags",
                    "additives_old_tags",
                    "additives_debug_tags"
                };

                for (String fieldName : additiveFields) {
                    if (product.has(fieldName)) {
                        try {
                            JSONArray additivesArray = product.getJSONArray(fieldName);
                            for (int i = 0; i < additivesArray.length(); i++) {
                                String additive = additivesArray.getString(i);
                                String eCode = extractECode(additive);
                                if (eCode != null) {
                                    certainMatches.add(eCode);
                                }
                            }
                        } catch (Exception e) {
                            // Ignora errori su singoli campi
                        }
                    }
                }

                // Cerca nel testo ingredienti
                String ingredientsText = "";
                String[] ingredientFields = {"ingredients_text_it", "ingredients_text_en", "ingredients_text"};

                for (String field : ingredientFields) {
                    if (product.has(field)) {
                        String text = product.optString(field, "");
                        if (!text.isEmpty() && !text.trim().startsWith("[") && !text.trim().startsWith("{")) {
                            ingredientsText = text;
                            break;
                        }
                    }
                }

                if (!ingredientsText.isEmpty()) {
                    Pattern pattern = Pattern.compile("E\\s*[0-9]{3,4}[a-z]?", Pattern.CASE_INSENSITIVE);
                    Matcher matcher = pattern.matcher(ingredientsText);
                    while (matcher.find()) {
                        String code = matcher.group().replaceAll("\\s+", "").toUpperCase();
                        if (isValidECode(code)) {
                            certainMatches.add(code);
                        }
                    }
                }

                // Rimuovi duplicati
                Set<String> uniqueCodes = new HashSet<>(certainMatches);
                final List<String> finalMatches = new ArrayList<>(uniqueCodes);
                final String finalIngredientsText = ingredientsText;
                final String finalNutriscore = nutriscoreGrade;
                final int finalNovaGroup = novaGroup;
                final String finalManufacturingPlaces = manufacturingPlaces;
                final String finalOrigins = origins;
                final String finalEcoscore = ecoscoreGrade;
                final String finalBrands = brands;
                final String finalQuantity = quantity;
                final String finalLabels = labels;
                final String finalAllergens = allergens;
                final String finalTraces = traces;
                final float finalEnergyKcal = energyKcal;
                final float finalFat = fat;
                final float finalSaturatedFat = saturatedFat;
                final float finalCarbs = carbs;
                final float finalSugars = sugars;
                final float finalFiber = fiber;
                final float finalProteins = proteins;
                final float finalSalt = salt;

                // Conta solo gli additivi presenti nel database (con deduplicazione)
                DatabaseHelper tempDb = new DatabaseHelper(MainActivity.this);
                java.util.Set<String> countedBaseCodes = new java.util.HashSet<>();
                for (String code : finalMatches) {
                    Additive additive = tempDb.getAdditiveByCode(code);
                    if (additive != null) {
                        // Normalizza al codice base per evitare duplicati (E965i -> E965)
                        String baseCode = normalizeToBaseCode(additive.getCode());
                        countedBaseCodes.add(baseCode);
                    }
                }
                final int displayCount = countedBaseCodes.size();

                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    statusText.setText(getString(R.string.main_additives_found, displayCount, productName));

                    // Mostra risultati
                    new android.os.Handler().postDelayed(() -> {
                        showResults(barcode, finalMatches, productName, finalIngredientsText,
                            finalNutriscore, finalNovaGroup, finalManufacturingPlaces, finalOrigins,
                            finalEcoscore, finalBrands, finalQuantity, finalLabels,
                            finalAllergens, finalTraces, finalEnergyKcal, finalFat, finalSaturatedFat,
                            finalCarbs, finalSugars, finalFiber, finalProteins, finalSalt);
                    }, 1500);
                });
            } else {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    statusText.setText(R.string.error_product_not_found);
                    Toast.makeText(MainActivity.this,
                        R.string.toast_product_not_found,
                        Toast.LENGTH_LONG).show();

                    // Reset automatico dopo 3 secondi
                    new android.os.Handler().postDelayed(() -> {
                        statusText.setText(R.string.main_camera_status);
                        isProcessing = false;
                    }, 3000);
                });
            }
        } catch (Exception e) {
            runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                statusText.setText(R.string.error_parsing);
                Toast.makeText(MainActivity.this,
                    R.string.toast_parsing_error,
                    Toast.LENGTH_LONG).show();

                // Reset automatico dopo 3 secondi
                new android.os.Handler().postDelayed(() -> {
                    statusText.setText(R.string.main_camera_status);
                    isProcessing = false;
                }, 3000);
            });
        }
    }

    private String extractECode(String tag) {
        Pattern pattern = Pattern.compile("(?:^|[^a-z])e[-\\s]*([0-9]{3,4}[a-z]?)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(tag);
        if (matcher.find()) {
            String code = "E" + matcher.group(1).replaceAll("[-\\s]+", "");
            code = code.toUpperCase();
            return isValidECode(code) ? code : null;
        }
        return null;
    }

    private boolean isValidECode(String code) {
        String numericPart = code.substring(1).replaceAll("[a-zA-Z]+$", "");
        try {
            int number = Integer.parseInt(numericPart);
            return (number >= 100 && number <= 199) ||
                   (number >= 200 && number <= 299) ||
                   (number >= 300 && number <= 399) ||
                   (number >= 400 && number <= 499) ||
                   (number >= 500 && number <= 599) ||
                   (number >= 600 && number <= 699) ||
                   (number >= 700 && number <= 799) ||
                   (number >= 900 && number <= 999) ||
                   (number >= 1100 && number <= 1199) ||
                   (number >= 1200 && number <= 1299) ||
                   (number >= 1400 && number <= 1599);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private String extractTagsAsReadableList(JSONObject product, String field) {
        StringBuilder result = new StringBuilder();
        if (product.has(field)) {
            try {
                JSONArray tagsArray = product.getJSONArray(field);
                for (int i = 0; i < tagsArray.length(); i++) {
                    String tag = tagsArray.getString(i);
                    // Convert "en:gluten" to "Gluten"
                    String readable = tag.replaceAll("^[a-z]{2}:", "");
                    readable = readable.replace("-", " ");
                    readable = readable.substring(0, 1).toUpperCase() + readable.substring(1);
                    if (result.length() > 0) result.append(", ");
                    result.append(readable);
                }
            } catch (Exception e) {
                // Ignore
            }
        }
        return result.toString();
    }

    private void showResults(String barcode, List<String> eCodes, String productName, String ingredientsText,
            String nutriscore, int novaGroup, String manufacturingPlaces, String origins,
            String ecoscore, String brands, String quantity, String labels,
            String allergens, String traces, float energyKcal, float fat, float saturatedFat,
            float carbs, float sugars, float fiber, float proteins, float salt) {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        List<String> codesNotInDb = new ArrayList<>();

        for (String code : eCodes) {
            if (dbHelper.getAdditiveByCode(code) == null) {
                codesNotInDb.add(code);
            }
        }

        Intent intent = new Intent(this, ResultsActivity.class);
        intent.putExtra("barcode", barcode);
        intent.putStringArrayListExtra("certainMatches", new ArrayList<>(eCodes));
        intent.putStringArrayListExtra("ambiguousMatches", new ArrayList<>());
        intent.putStringArrayListExtra("ambiguousPhrases", new ArrayList<>());
        intent.putExtra("productName", productName);
        intent.putStringArrayListExtra("missingCodes", new ArrayList<>(codesNotInDb));
        intent.putExtra("ingredientsText", ingredientsText);
        intent.putExtra("nutriscore", nutriscore);
        intent.putExtra("novaGroup", novaGroup);
        intent.putExtra("manufacturingPlaces", manufacturingPlaces);
        intent.putExtra("origins", origins);
        // New extras
        intent.putExtra("ecoscore", ecoscore);
        intent.putExtra("brands", brands);
        intent.putExtra("quantity", quantity);
        intent.putExtra("labels", labels);
        intent.putExtra("allergens", allergens);
        intent.putExtra("traces", traces);
        intent.putExtra("energyKcal", energyKcal);
        intent.putExtra("fat", fat);
        intent.putExtra("saturatedFat", saturatedFat);
        intent.putExtra("carbs", carbs);
        intent.putExtra("sugars", sugars);
        intent.putExtra("fiber", fiber);
        intent.putExtra("proteins", proteins);
        intent.putExtra("salt", salt);
        startActivity(intent);

        // Reset per nuova scansione
        isProcessing = false;
        statusText.setText(getString(R.string.main_camera_status));
    }

    private void openDetail(Additive additive) {
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra("code", additive.getCode());
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_howto) {
            startActivity(new Intent(this, HowToActivity.class));
            return true;
        } else if (id == R.id.action_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
            return true;
        } else if (id == R.id.action_favorites) {
            startActivity(new Intent(this, FavoritesActivity.class));
            return true;
        } else if (id == R.id.action_history) {
            startActivity(new Intent(this, HistoryActivity.class));
            return true;
        } else if (id == R.id.action_about) {
            startActivity(new Intent(this, AboutActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
        if (barcodeScanner != null) {
            barcodeScanner.close();
        }
    }

    /**
     * Normalizza un codice additivo al suo codice base rimuovendo suffissi variante.
     * Es: E965i -> E965, E965ii -> E965, E150d -> E150
     */
    private String normalizeToBaseCode(String code) {
        if (code == null || code.length() < 4) return code;
        String upper = code.toUpperCase();
        if (!upper.startsWith("E")) return upper;
        String base = upper.replaceAll("(I{1,3}|IV|V|VI|[A-F])$", "");
        return base.matches("E\\d{3,4}") ? base : upper;
    }
}
