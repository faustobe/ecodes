package it.faustobe.ecodes.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import it.faustobe.ecodes.R;
import it.faustobe.ecodes.data.DatabaseHelper;
import it.faustobe.ecodes.model.Product;
import java.util.ArrayList;
import java.util.List;

public class FavoritesActivity extends BaseActivity {
    private DatabaseHelper dbHelper;
    private RecyclerView recyclerView;
    private ProductAdapter adapter;
    private LinearLayout emptyState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.favorites_title);
        }

        dbHelper = new DatabaseHelper(this);
        recyclerView = findViewById(R.id.recyclerView);
        emptyState = findViewById(R.id.emptyState);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ProductAdapter(dbHelper.getFavorites(), this::openProduct);
        recyclerView.setAdapter(adapter);

        // Swipe to delete
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                List<Product> favorites = dbHelper.getFavorites();
                Product product = favorites.get(position);

                // Rimuovi dal database
                dbHelper.removeFromFavorites(product.getBarcode());

                // Aggiorna l'adapter
                adapter.updateData(dbHelper.getFavorites());
                updateUI();

                Toast.makeText(FavoritesActivity.this, "Rimosso dai preferiti", Toast.LENGTH_SHORT).show();
            }
        });
        itemTouchHelper.attachToRecyclerView(recyclerView);

        updateUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Ricarica i preferiti quando si torna all'activity
        adapter.updateData(dbHelper.getFavorites());
        updateUI();
    }

    private void updateUI() {
        if (adapter.getItemCount() == 0) {
            recyclerView.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyState.setVisibility(View.GONE);
        }
    }

    private void openProduct(Product product) {
        // Riapri ResultsActivity con i dati del prodotto
        Intent intent = new Intent(this, ResultsActivity.class);
        intent.putExtra("barcode", product.getBarcode());
        intent.putStringArrayListExtra("certainMatches", new ArrayList<>(product.getECodes()));
        intent.putStringArrayListExtra("ambiguousMatches", new ArrayList<>());
        intent.putStringArrayListExtra("ambiguousPhrases", new ArrayList<>());
        intent.putExtra("productName", product.getProductName());
        intent.putStringArrayListExtra("missingCodes", new ArrayList<>());
        intent.putExtra("ingredientsText", product.getIngredients());
        intent.putExtra("nutriscore", product.getNutriscore());
        intent.putExtra("novaGroup", product.getNovaGroup());
        intent.putExtra("manufacturingPlaces", product.getManufacturingPlaces());
        intent.putExtra("origins", product.getOrigins());
        // Nuovi campi
        intent.putExtra("ecoscore", product.getEcoscore());
        intent.putExtra("brands", product.getBrand());
        intent.putExtra("quantity", product.getQuantity());
        intent.putExtra("labels", product.getLabels());
        intent.putExtra("allergens", product.getAllergens());
        intent.putExtra("traces", product.getTraces());
        intent.putExtra("energyKcal", product.getEnergyKcal());
        intent.putExtra("fat", product.getFat());
        intent.putExtra("saturatedFat", product.getSaturatedFat());
        intent.putExtra("carbs", product.getCarbohydrates());
        intent.putExtra("sugars", product.getSugars());
        intent.putExtra("fiber", product.getFiber());
        intent.putExtra("proteins", product.getProteins());
        intent.putExtra("salt", product.getSalt());
        intent.putExtra("fromHistory", true); // Non salvare di nuovo in cronologia
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_favorites, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_home) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_history) {
            startActivity(new Intent(this, HistoryActivity.class));
            return true;
        } else if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
