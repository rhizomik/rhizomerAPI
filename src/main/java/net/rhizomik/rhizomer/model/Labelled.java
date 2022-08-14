package net.rhizomik.rhizomer.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Labelled {
    private HashMap<String, List<String>> labels = new HashMap<>();

    public Labelled(String labels) {
        this.splitLabels(labels);
    }

    public HashMap<String, List<String>> getLabels() { return labels; }

    public void setLabels(HashMap<String, List<String>> labels) { this.labels = labels; }

    public void splitLabels(String labelsString) {
        if (labelsString == null) return;
        for(String langLabel: labelsString.split(" \\|\\| ")) {
            String lang = "undefined";
            String label = langLabel;
            if (langLabel.contains("@")) {
                label = langLabel.split("@")[0];
                lang = langLabel.split("@")[1];
            }
            List<String> labels = this.labels.containsKey(lang) ? this.labels.get(lang) : new ArrayList<>();
            labels.add(label);
            this.labels.put(lang, labels);
        }
    }
}
