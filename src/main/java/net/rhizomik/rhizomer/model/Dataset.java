package net.rhizomik.rhizomer.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.persistence.CascadeType;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;

import lombok.Data;
import net.rhizomik.rhizomer.service.Queries.QueryType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by http://rhizomik.net/~roberto/
 */
@Entity
@Data
public class Dataset {
    private static final Logger logger = LoggerFactory.getLogger(Dataset.class);

    @Id
    private String id;

    private QueryType queryType = QueryType.OPTIMIZED;
    private boolean inferenceEnabled = false;
    private int sampleSize = 0;
    private double coverage = 0.0;
    private boolean isPublic = false;

    @ElementCollection
    private Set<String> datasetOntologies = new HashSet<>();
    @OneToMany(fetch = FetchType.LAZY, orphanRemoval = true, mappedBy = "dataset", cascade = CascadeType.ALL)
    @OrderBy("instanceCount DESC")
    private List<Class> classes = new ArrayList<>();
    private String owner;

    public Dataset() {}

    public Dataset(String id) {
        this.id = id;
    }

    public Dataset(String id, Set<String> ontologies) throws MalformedURLException {
        this.id = id;
        this.datasetOntologies = ontologies;
    }

    @JsonIgnore
    public List<Class> getClasses() { return new ArrayList<>(classes); }

    public List<Class> getClasses(int top) {
        int max = Integer.min(top, classes.size());
        return new ArrayList<>(classes.subList(0, max));
    }

    public List<Class> getClassesContaining(String containing) {
        return getClassesContaining(containing, -1);
    }

    public List<Class> getClassesContaining(String containing, int top) {
        Stream<Class> selected = classes.stream()
            .filter(c -> c.getUri().toString().toLowerCase().contains(containing.toLowerCase()) ||
                c.getLabel().toLowerCase().contains(containing.toLowerCase()));
        if (top >= 0)
            selected = selected.limit(top);
        return selected.collect(Collectors.toList());
    }

    public void setClasses(List<Class> classes) { this.classes.clear(); this.classes.addAll(classes); }

    public void addClass(Class aClass) { classes.add(aClass); }

    public void removeClass(Class aClass) { classes.remove(aClass); }

    public void addDatasetOntology(String ontology) { this.datasetOntologies.add(ontology); }

    @JsonIgnore
    public List<String> getDatasetOntologies() { return new ArrayList<>(datasetOntologies); }

    public void setDatasetOntologies(Set<String> datasetOntologies) { this.datasetOntologies = datasetOntologies; }

    @JsonIgnore
    public URI getDatasetUri() {
        URI datasetURI = null;
        try {
            datasetURI = new URI("http://" + InetAddress.getLocalHost().getHostName() + "/dataset/"+getId());
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return datasetURI;
    }

    @JsonIgnore
    public URI getDatasetOntologiesGraph() {
        URI datasetOntologiesGraphURI = null;
        try {
            datasetOntologiesGraphURI = new URI(getDatasetUri()+"/ontologies");
        } catch (URISyntaxException e) {
            logger.error(e.getMessage());
        }
        return datasetOntologiesGraphURI;
    }

    @JsonIgnore
    public URI getDatasetInferenceGraph() {
        URI datasetInferenceGraphURI = null;
        try {
            datasetInferenceGraphURI = new URI(getDatasetUri()+"/inference");
        } catch (URISyntaxException e) {
            logger.error(e.getMessage());
        }
        return datasetInferenceGraphURI;
    }
}
