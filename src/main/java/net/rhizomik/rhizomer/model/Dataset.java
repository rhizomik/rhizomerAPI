package net.rhizomik.rhizomer.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import net.rhizomik.rhizomer.service.Queries.QueryType;
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
    private URL updateEndPoint;
    private QueryType queryType = QueryType.OPTIMIZED;
    private boolean inferenceEnabled = false;
    private int sampleSize = 0;
    private double coverage = 0.0;
    private String username;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
    private boolean isPublic = false;

    @ElementCollection
    private Set<String> datasetGraphs = new HashSet<>();
    @ElementCollection
    private Set<String> datasetOntologies = new HashSet<>();
    @OneToMany(fetch = FetchType.LAZY, orphanRemoval = true, mappedBy = "dataset")
    @OrderBy("instanceCount DESC")
    private List<Class> classes = new ArrayList<>();
    @ManyToOne
    @JsonBackReference
    @JsonIdentityReference(alwaysAsId = true)
    private User owner;

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
        this.updateEndPoint = sparqlEndPoint;
        this.datasetGraphs = graphs;
        this.datasetOntologies = ontologies;
    }

    public Dataset(String id, URL sparqlEndPoint, URL updateEndPoint, Set<String> graphs, Set<String> ontologies) throws MalformedURLException {
        this.id = id;
        this.sparqlEndPoint = sparqlEndPoint;
        this.updateEndPoint = updateEndPoint;
        this.datasetGraphs = graphs;
        this.datasetOntologies = ontologies;
    }

    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

    @JsonIgnore
    public List<Class> getClasses() { return new ArrayList<>(classes); }

    @JsonIgnore
    public List<Class> getClasses(int top) {
        int max = Integer.min(top, classes.size());
        return new ArrayList<>(classes.subList(0, max));
    }

    public void setClasses(List<Class> classes) { this.classes.clear(); this.classes.addAll(classes); }

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

    public URL getUpdateEndPoint() {
        if (updateEndPoint == null)
            return sparqlEndPoint;
        return updateEndPoint;
    }

    public void setUpdateEndPoint(URL updateEndPoint) { this.updateEndPoint = updateEndPoint; }

    public QueryType getQueryType() { return queryType; }

    public void setQueryType(QueryType queryType) { this.queryType = queryType; }

    public boolean isInferenceEnabled() { return inferenceEnabled; }

    public void setInferenceEnabled(boolean inferenceEnabled) { this.inferenceEnabled = inferenceEnabled; }

    public int getSampleSize() { return sampleSize; }

    public void setSampleSize(int sampleSize) { this.sampleSize = sampleSize; }

    public double getCoverage() { return coverage; }

    public void setCoverage(double coverage) { this.coverage = coverage; }

    public void setUsername(String username) { this.username = username; }

    public String getUsername() {
        return username;
    }

    public void setPassword(String password) { this.password = password; }

    public String getPassword() { return password; }

    public User getOwner() { return owner; }

    public void setOwner(User owner) { this.owner = owner; }

    public boolean isPublic() { return isPublic; }

    public void setPublic(boolean aPublic) { isPublic = aPublic; }

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
