package it.faustobe.ecodes.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ScanResult implements Serializable {
    private List<String> certainMatches;
    private List<String> ambiguousMatches;
    private List<String> ambiguousPhrases;

    public ScanResult() {
        this.certainMatches = new ArrayList<>();
        this.ambiguousMatches = new ArrayList<>();
        this.ambiguousPhrases = new ArrayList<>();
    }

    public List<String> getCertainMatches() {
        return certainMatches;
    }

    public void setCertainMatches(List<String> certainMatches) {
        this.certainMatches = certainMatches;
    }

    public List<String> getAmbiguousMatches() {
        return ambiguousMatches;
    }

    public void setAmbiguousMatches(List<String> ambiguousMatches) {
        this.ambiguousMatches = ambiguousMatches;
    }

    public List<String> getAmbiguousPhrases() {
        return ambiguousPhrases;
    }

    public void setAmbiguousPhrases(List<String> ambiguousPhrases) {
        this.ambiguousPhrases = ambiguousPhrases;
    }

    public void addCertainMatch(String code) {
        if (!certainMatches.contains(code)) {
            certainMatches.add(code);
        }
    }

    public void addAmbiguousMatch(String code) {
        // Non aggiungere se già nei match certi
        if (!certainMatches.contains(code) && !ambiguousMatches.contains(code)) {
            ambiguousMatches.add(code);
        }
    }

    public void addAmbiguousPhrase(String phrase) {
        if (!ambiguousPhrases.contains(phrase)) {
            ambiguousPhrases.add(phrase);
        }
    }
}
