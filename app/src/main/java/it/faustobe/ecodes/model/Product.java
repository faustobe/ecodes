package it.faustobe.ecodes.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Product {
    private int id;
    private String barcode;
    private String productName;
    private List<String> eCodes;
    private String ingredients;
    private String timestamp;
    private String nutriscore;
    private int novaGroup;
    private String manufacturingPlaces;
    private String origins;
    // New fields
    private String ecoscore;
    private String brand;
    private String quantity;
    private String labels;
    private String allergens;
    private String traces;
    // Nutritional values (per 100g)
    private float energyKcal;
    private float fat;
    private float saturatedFat;
    private float carbohydrates;
    private float sugars;
    private float fiber;
    private float proteins;
    private float salt;

    public Product(int id, String barcode, String productName, String eCodesString, String ingredients, String timestamp) {
        this(id, barcode, productName, eCodesString, ingredients, timestamp, null, 0, null, null);
    }

    public Product(int id, String barcode, String productName, String eCodesString, String ingredients, String timestamp, String nutriscore, int novaGroup) {
        this(id, barcode, productName, eCodesString, ingredients, timestamp, nutriscore, novaGroup, null, null);
    }

    public Product(int id, String barcode, String productName, String eCodesString, String ingredients, String timestamp, String nutriscore, int novaGroup, String manufacturingPlaces, String origins) {
        this(id, barcode, productName, eCodesString, ingredients, timestamp, nutriscore, novaGroup, manufacturingPlaces, origins,
             null, null, null, null, null, null, 0, 0, 0, 0, 0, 0, 0, 0);
    }

    // Full constructor with all fields
    public Product(int id, String barcode, String productName, String eCodesString, String ingredients, String timestamp,
                   String nutriscore, int novaGroup, String manufacturingPlaces, String origins,
                   String ecoscore, String brand, String quantity, String labels, String allergens, String traces,
                   float energyKcal, float fat, float saturatedFat, float carbohydrates, float sugars, float fiber, float proteins, float salt) {
        this.id = id;
        this.barcode = barcode;
        this.productName = productName;
        this.ingredients = ingredients;
        this.timestamp = timestamp;
        this.nutriscore = nutriscore;
        this.novaGroup = novaGroup;
        this.manufacturingPlaces = manufacturingPlaces;
        this.origins = origins;
        this.ecoscore = ecoscore;
        this.brand = brand;
        this.quantity = quantity;
        this.labels = labels;
        this.allergens = allergens;
        this.traces = traces;
        this.energyKcal = energyKcal;
        this.fat = fat;
        this.saturatedFat = saturatedFat;
        this.carbohydrates = carbohydrates;
        this.sugars = sugars;
        this.fiber = fiber;
        this.proteins = proteins;
        this.salt = salt;

        // Parse comma-separated E codes
        if (eCodesString != null && !eCodesString.isEmpty()) {
            this.eCodes = new ArrayList<>(Arrays.asList(eCodesString.split(",")));
        } else {
            this.eCodes = new ArrayList<>();
        }
    }

    public Product(String barcode, String productName, List<String> eCodes) {
        this.barcode = barcode;
        this.productName = productName;
        this.eCodes = eCodes != null ? eCodes : new ArrayList<>();
    }

    // Getters
    public int getId() { return id; }
    public String getBarcode() { return barcode; }
    public String getProductName() { return productName; }
    public List<String> getECodes() { return eCodes; }
    public String getIngredients() { return ingredients; }
    public String getTimestamp() { return timestamp; }
    public int getECodesCount() { return eCodes.size(); }
    public String getNutriscore() { return nutriscore; }
    public int getNovaGroup() { return novaGroup; }
    public String getManufacturingPlaces() { return manufacturingPlaces; }
    public String getOrigins() { return origins; }
    public String getEcoscore() { return ecoscore; }
    public String getBrand() { return brand; }
    public String getQuantity() { return quantity; }
    public String getLabels() { return labels; }
    public String getAllergens() { return allergens; }
    public String getTraces() { return traces; }
    public float getEnergyKcal() { return energyKcal; }
    public float getFat() { return fat; }
    public float getSaturatedFat() { return saturatedFat; }
    public float getCarbohydrates() { return carbohydrates; }
    public float getSugars() { return sugars; }
    public float getFiber() { return fiber; }
    public float getProteins() { return proteins; }
    public float getSalt() { return salt; }

    public String getECodesString() {
        return eCodes.isEmpty() ? "" : String.join(",", eCodes);
    }

    // Setters for new fields
    public void setEcoscore(String ecoscore) { this.ecoscore = ecoscore; }
    public void setBrand(String brand) { this.brand = brand; }
    public void setQuantity(String quantity) { this.quantity = quantity; }
    public void setLabels(String labels) { this.labels = labels; }
    public void setAllergens(String allergens) { this.allergens = allergens; }
    public void setTraces(String traces) { this.traces = traces; }
    public void setEnergyKcal(float energyKcal) { this.energyKcal = energyKcal; }
    public void setFat(float fat) { this.fat = fat; }
    public void setSaturatedFat(float saturatedFat) { this.saturatedFat = saturatedFat; }
    public void setCarbohydrates(float carbohydrates) { this.carbohydrates = carbohydrates; }
    public void setSugars(float sugars) { this.sugars = sugars; }
    public void setFiber(float fiber) { this.fiber = fiber; }
    public void setProteins(float proteins) { this.proteins = proteins; }
    public void setSalt(float salt) { this.salt = salt; }
}
