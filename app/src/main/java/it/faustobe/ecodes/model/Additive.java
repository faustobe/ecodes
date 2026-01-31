package it.faustobe.ecodes.model;

public class Additive {
    private int id;
    private String code;
    private String name;
    private String classification;
    private Float adi;  // Cambiato da String a Float (null = non specificato)
    private String efsaStatus;
    private int allergenRisk;
    private int vegan;
    private int halal;
    private String notes;
    private String lastUpdated;

    public Additive(int id, String code, String name, String classification,
                    Float adi, String efsaStatus, int allergenRisk,
                    int vegan, int halal, String notes, String lastUpdated) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.classification = classification;
        this.adi = adi;
        this.efsaStatus = efsaStatus;
        this.allergenRisk = allergenRisk;
        this.vegan = vegan;
        this.halal = halal;
        this.notes = notes;
        this.lastUpdated = lastUpdated;
    }

    public int getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getClassification() { return classification; }
    public Float getAdi() { return adi; }
    public String getEfsaStatus() { return efsaStatus; }
    public int getAllergenRisk() { return allergenRisk; }
    public int getVegan() { return vegan; }
    public int getHalal() { return halal; }
    public String getNotes() { return notes; }
    public String getLastUpdated() { return lastUpdated; }

    public String getClassificationText() {
        switch (classification) {
            case "A": return "Non tossico";
            case "B": return "Pericolo di intolleranza";
            case "C": return "Prodotto sospetto";
            case "D": return "Forte sospetto di tossicità";
            case "E": return "PERICOLOSO!";
            case "F": return "Non ammesso nell'UE";
            case "N": return "Neutro";
            default: return "Sconosciuto";
        }
    }

    public int getClassificationColor() {
        switch (classification) {
            case "A": return 0xFF4CAF50; // Verde
            case "B": return 0xFFFFEB3B; // Giallo
            case "C": return 0xFFFF9800; // Arancione
            case "D": return 0xFFF44336; // Rosso
            case "E": return 0xFF9C27B0; // Viola (molto pericoloso)
            case "F": return 0xFF212121; // Nero (vietato)
            case "N": return 0xFF9E9E9E; // Grigio
            default: return 0xFF757575;
        }
    }
}
