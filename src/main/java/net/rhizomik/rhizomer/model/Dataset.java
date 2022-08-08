package net.rhizomik.rhizomer.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.*;
import java.util.*;
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

    @OneToMany(fetch = FetchType.LAZY, orphanRemoval = true, mappedBy = "dataset", cascade = CascadeType.ALL)
    @OrderBy("instanceCount DESC")
    private List<Class> classes = new ArrayList<>();
    private String owner;

    public Dataset() {}

    public Dataset(String id) {
        this.id = id;
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
        Stream<Class> selected = classes.stream();
        if (!containing.isEmpty())
            selected = classes.stream()
            .filter(c -> c.getUri().toString().toLowerCase().contains(containing.toLowerCase()) ||
                c.getLabel().toLowerCase().contains(containing.toLowerCase()));
        if (top >= 0)
            selected = selected.sorted(Comparator.comparingInt(Class::getInstanceCount).reversed()).limit(top);
        return selected.collect(Collectors.toList());
    }

    public void setClasses(List<Class> classes) { this.classes.clear(); this.classes.addAll(classes); }

    public void addClass(Class aClass) { classes.add(aClass); }

    public void removeClass(Class aClass) { classes.remove(aClass); }

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
