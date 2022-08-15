package net.rhizomik.rhizomer.model;

import javax.persistence.ElementCollection;
import javax.persistence.MappedSuperclass;
import java.util.HashMap;
import java.util.Map;

@MappedSuperclass
public class Labelled {
    @ElementCollection
    private Map<String, String> labels = new HashMap<>();

    public Labelled(String labels) {
        this.splitLabels(labels);
    }

    public Map<String, String> getLabels() { return labels; }

    public void setLabels(Map<String, String> labels) { this.labels = labels; }

    public void setLabelsStr(String labels) { this.splitLabels(labels); }

    public String getLabel(String lang) {
        if (lang == null || this.labels.isEmpty()) {
            return null;
        } else if (this.labels.containsKey(lang)) {
            return this.labels.get(lang);
        } else if (this.labels.containsKey("undefined")) {
            return this.labels.get("undefined");
        } else if (this.labels.containsKey("en")) {
            return this.labels.get("en");
        }
        return null;
    }

    public void splitLabels(String labelsString) {
        if (labelsString == null) return;
        for(String langLabel: labelsString.split(" \\|\\| ")) {
            String lang = "undefined";
            String label = langLabel;
            if (langLabel.contains("@")) {
                label = langLabel.split("@")[0];
                lang = langLabel.split("@")[1];
            }
            if (!this.labels.containsKey(lang)) {
                this.labels.put(lang, label);
            } else if (this.labels.get(lang).length() > label.length()) {
                this.labels.put(lang, label);
            }
        }
    }
}
