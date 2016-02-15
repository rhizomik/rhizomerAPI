package net.rhizomik.rhizomer.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by http://rhizomik.net/~roberto/
 */
@Entity
public class Dataset {
    private static final Logger logger = LoggerFactory.getLogger(Dataset.class);

    @Id
    private String id;
    private URL sparqlEndPoint;
    private Queries.QueryType queryType = Queries.QueryType.FULL;
    private boolean inferenceEnabled = false;
    private int sampleSize = 0;
    private double coverage = 0.0;

    @ElementCollection
    private Set<String> datasetGraphs = new HashSet<>();
    @ElementCollection
    private Set<String> datasetOntologies = new HashSet<>();
    @OneToMany(fetch = FetchType.LAZY, orphanRemoval = true, mappedBy = "dataset")
    private List<Class> classes = new ArrayList<>();

    public Dataset() {}

    public Dataset(String id) {
        this.id = id;
    }

    public Dataset(String id, URL serverUrl) throws MalformedURLException {
        this(id, serverUrl, null, null);
    }

    public Dataset(String id, URL sparqlEndPoint, Set<String> graphs, Set<String> ontologies) throws MalformedURLException {
        this.id = id;
        this.sparqlEndPoint = sparqlEndPoint;
        this.datasetGraphs = graphs;
        this.datasetOntologies = ontologies;
    }

    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

    @JsonIgnore
    public List<Class> getClasses() { return new ArrayList<>(classes); }

    public void setClasses(List<Class> classes) { this.classes = classes; }

    public void addClass(Class aClass) { classes.add(aClass); }

    public void removeClass(Class aClass) { classes.remove(aClass); }

    @JsonIgnore
    public List<String> getDatasetGraphs() {
        ArrayList<String> copyDatasetGraphs = new ArrayList<>(datasetGraphs);
        if (isInferenceEnabled())
            copyDatasetGraphs.add(this.getDatasetInferenceGraph().toString());
        return copyDatasetGraphs;
    }

    public void setDatasetGraphs(Set<String> datasetGraphs) { this.datasetGraphs = datasetGraphs; }

    public void addDatasetGraph(String graph) { this.datasetGraphs.add(graph); }

    public void addDatasetOntology(String ontology) { this.datasetOntologies.add(ontology); }

    @JsonIgnore
    public List<String> getDatasetOntologies() { return new ArrayList<>(datasetOntologies); }

    public void setDatasetOntologies(Set<String> datasetOntologies) { this.datasetOntologies = datasetOntologies; }

    public URL getSparqlEndPoint() { return sparqlEndPoint; }

    public void setSparqlEndPoint(URL sparqlEndPoint) { this.sparqlEndPoint = sparqlEndPoint; }

    public Queries.QueryType getQueryType() { return queryType; }

    public void setQueryType(Queries.QueryType queryType) { this.queryType = queryType; }

    public boolean isInferenceEnabled() { return inferenceEnabled; }

    public void setInferenceEnabled(boolean inferenceEnabled) { this.inferenceEnabled = inferenceEnabled; }

    public int getSampleSize() { return sampleSize; }

    public void setSampleSize(int sampleSize) { this.sampleSize = sampleSize; }

    public double getCoverage() { return coverage; }

    public void setCoverage(double coverage) { this.coverage = coverage; }

    @JsonIgnore
    public URI getDatasetUri() {
        URI datasetURI = null;
        try {
            datasetURI = new URI("http://rhizomik.net/dataset/"+getId());
        } catch (URISyntaxException e) {
            logger.error(e.getMessage());
        }
        return datasetURI;
    }

    @JsonIgnore
    public java.net.URI getDatasetOntologiesGraph() {
        URI datasetOntologiesGraphURI = null;
        try {
            datasetOntologiesGraphURI = new URI(getDatasetUri()+"/ontologies");
        } catch (URISyntaxException e) {
            logger.error(e.getMessage());
        }
        return datasetOntologiesGraphURI;
    }

    @JsonIgnore
    public java.net.URI getDatasetInferenceGraph() {
        URI datasetInferenceGraphURI = null;
        try {
            datasetInferenceGraphURI = new URI(getDatasetUri()+"/inference");
        } catch (URISyntaxException e) {
            logger.error(e.getMessage());
        }
        return datasetInferenceGraphURI;
    }
}
