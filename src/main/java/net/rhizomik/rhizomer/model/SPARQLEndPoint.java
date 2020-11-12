package net.rhizomik.rhizomer.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger logger = LoggerFactory.getLogger(Dataset.class);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    private URL queryEndPoint;
    private String queryUsername;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String queryPassword;

    private boolean writable = false;
    private URL updateEndPoint;
    private String updateUsername;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String updatePassword;

    private ServerType type = ServerType.GENERIC;
    public enum ServerType {
        GENERIC,
        VIRTUOSO,
        NEPTUNE;
    }
    private String timeout;

    @ManyToOne
    @JsonBackReference
    private Dataset dataset;
    @ElementCollection
    private Set<String> graphs = new HashSet<>();

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
        List<String> graphsList = new ArrayList<>(this.graphs);
        return graphsList;
    }

    public void setTimeout(String timeout) {
        if (this.type == ServerType.VIRTUOSO)
            this.timeout = "0";
        else
            this.timeout = timeout;
    }
}
