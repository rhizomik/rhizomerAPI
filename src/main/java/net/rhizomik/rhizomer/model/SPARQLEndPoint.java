package net.rhizomik.rhizomer.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Data
public class SPARQLEndPoint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    private URL queryEndPoint;
    private String queryUsername;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String queryPassword;

    private boolean writable = false;

    private boolean inferenceEnabled = false;
    private URL updateEndPoint;
    private String updateUsername;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String updatePassword;

    private ServerType type = ServerType.GENERIC;
    public enum ServerType {
        GENERIC,
        FUSEKI,
        FUSEKI_LUCENE,
        VIRTUOSO,
        MARKLOGIC,
        STARDOG;
    }
    private String timeout;

    @ManyToOne
    @JsonBackReference
    private Dataset dataset;
    @ElementCollection
    private Set<String> graphs = new HashSet<>();
    @ElementCollection
    private Set<String> ontologyGraphs = new HashSet<>();

    public SPARQLEndPoint() {}

    public SPARQLEndPoint(Dataset dataset) {
        this.dataset = dataset;
    }
    public URL getUpdateEndPoint() {
        if (!writable)
            return null;
        else if (updateEndPoint != null)
            return updateEndPoint;
        else
            return queryEndPoint;
    }

    public String getUpdateUsername() {
        if (!writable)
            return null;
        else if (updateUsername != null)
            return updateUsername;
        else
            return queryUsername;
    }

    public String getUpdatePassword() {
        if (!writable)
            return null;
        else if (updatePassword != null)
            return updatePassword;
        else
            return queryPassword;
    }

    public void addGraph(String graph) {
        this.graphs.add(graph);
    }

    public List<String> getGraphs() {
        Set<String> graphs = new HashSet<>(this.graphs);
        if (graphs.size() > 0 & isInferenceEnabled()) {
            graphs.add(getDatasetInferenceGraph());
        }
        return new ArrayList<>(graphs);
    }

    public void addOntologyGraph(String graph) {
        this.ontologyGraphs.add(graph);
    }

    public List<String> getOntologyGraphs() {
        return new ArrayList<>(this.ontologyGraphs);
    }

    @JsonIgnore
    public String getDatasetInferenceGraph() {
        return dataset.getDatasetUri()+"/inference";
    }
}
