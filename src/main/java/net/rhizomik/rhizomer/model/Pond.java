package net.rhizomik.rhizomer.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
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
public class Pond {
    private static final Logger logger = LoggerFactory.getLogger(Pond.class);

    @Id
    private String id;
    @ElementCollection
    private Set<String> pondGraphs = new HashSet<>();
    @ElementCollection
    private Set<String> pondOntologies = new HashSet<>();
    @ManyToOne
    private Server server;
    @OneToMany(fetch = FetchType.LAZY, orphanRemoval = true, mappedBy = "pond")
    @JsonManagedReference
    private List<Class> classes = new ArrayList<>();
    private Queries.QueryType queryType = Queries.QueryType.SIMPLE;
    private boolean inferenceEnabled = true;
    private int sampleSize = 0;
    private double coverage = 0.0;

    public Pond() {}

    public Pond(String id) {
        this.id = id;
    }

    public Pond(String id, URL serverUrl) throws MalformedURLException {
        this(id, serverUrl, null, null);
    }

    public Pond(String id, URL serverUrl, Set<String> graphs, Set<String> ontologies) throws MalformedURLException {
        this.id = id;
        this.server = new Server(serverUrl);
        this.pondGraphs = graphs;
        this.pondOntologies = ontologies;
    }

    public List<Class> getClasses() {
        return classes;
    }

    public void setClasses(List<Class> classes) { this.classes = classes; }

    public void addClass(Class aClass) { classes.add(aClass); }

    public void addPondGraph(String graph) { this.pondGraphs.add(graph); }

    public void addPondOntology(String ontology) { this.pondOntologies.add(ontology); }

    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

    public List<String> getPondGraphsStrings() {
        ArrayList<String> copyPondGraphs = new ArrayList<>(pondGraphs);
        if (isInferenceEnabled())
            copyPondGraphs.add(this.getPondInferenceGraph().toString());
        return copyPondGraphs;
    }

    public void setPondGraphs(Set<String> pondGraphs) { this.pondGraphs = pondGraphs; }

    public List<String> getPondOntologies() { return new ArrayList<>(pondOntologies); }

    public void setPondOntologies(Set<String> pondOntologies) { this.pondOntologies = pondOntologies; }

    public Server getServer() { return server; }

    public void setServer(Server server) { this.server = server; }

    public Queries.QueryType getQueryType() { return queryType; }

    public void setQueryType(Queries.QueryType queryType) { this.queryType = queryType; }

    public URI getPondUri() {
        URI pondURI = null;
        try {
            pondURI = new URI("http://rhizomik.net/pond/"+getId());
        } catch (URISyntaxException e) {
            logger.error(e.getMessage());
        }
        return pondURI;
    }

    public URI getPondOntologiesGraph() {
        URI pondOntologiesGraphURI = null;
        try {
            pondOntologiesGraphURI = new URI(getPondUri()+"/ontologies");
        } catch (URISyntaxException e) {
            logger.error(e.getMessage());
        }
        return pondOntologiesGraphURI;
    }

    public URI getPondInferenceGraph() {
        URI pondInferenceGraphURI = null;
        try {
            pondInferenceGraphURI = new URI(getPondUri()+"/inference");
        } catch (URISyntaxException e) {
            logger.error(e.getMessage());
        }
        return pondInferenceGraphURI;
    }

    public boolean isInferenceEnabled() { return inferenceEnabled; }

    public void setInferenceEnabled(boolean inferenceEnabled) { this.inferenceEnabled = inferenceEnabled; }

    public int getSampleSize() { return sampleSize; }

    public void setSampleSize(int sampleSize) { this.sampleSize = sampleSize; }

    public double getCoverage() { return coverage; }

    public void setCoverage(double coverage) { this.coverage = coverage; }
}
