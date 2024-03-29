package net.rhizomik.rhizomer.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.stream.Collectors;
import net.rhizomik.rhizomer.model.id.DatasetClassId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by http://rhizomik.net/~roberto/
 */
@Entity
public class Class extends Labelled {
    private static final Logger logger = LoggerFactory.getLogger(Class.class);

    @EmbeddedId
    private DatasetClassId id;

    private String uri;
    @OneToMany(fetch = FetchType.LAZY, orphanRemoval = true, mappedBy = "domain", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Facet> facets = new ArrayList<>();
    private int instanceCount;
    @ManyToOne
    @MapsId("datasetId")
    @JsonBackReference
    private Dataset dataset;

    public Class() {
        super(null);
        this.id = new DatasetClassId();
    }

    public Class(Dataset dataset, String curie, String labels, int instanceCount) throws URISyntaxException {
        this(dataset, Curie.toUri(curie), labels, instanceCount);
    }

    public Class(Dataset dataset, String namespace, String localName, String labels, int instanceCount)
            throws URISyntaxException {
        this(dataset, new URI(namespace+localName), labels, instanceCount);
    }

    public Class(Dataset dataset, URI uri, String labels, int instanceCount) {
        super(labels);
        this.id = new DatasetClassId(dataset, uri);
        this.uri = uri.toString();
        this.instanceCount = instanceCount;
        this.dataset = dataset;
        logger.debug("Created class: {}", super.toString());
    }

    public List<Facet> getFacets(float relevance) {
        return facets.stream().filter(
            facet -> ((float)facet.getTimesUsed() / this.getInstanceCount()) > relevance).collect(
            Collectors.toCollection(ArrayList::new));
    }

    public List<Facet> getFacets() { return facets; }

    public int getFacetsCount() { return facets.size(); }

    public void setFacets(List<Facet> facets) { this.facets.clear(); this.facets.addAll(facets); }

    public void addFacet(Facet facet) { facets.add(facet); }

    public void removeFacet(Facet aFacet) { facets.remove(aFacet); }

    public DatasetClassId getId() { return id; }

    public void setId(DatasetClassId id) {
        this.id = id;
    }

    public String getCurie() { return id.getClassCurie(); }

    public URI getUri() {
        try { return new URI(uri); }
        catch (URISyntaxException e) { e.printStackTrace(); }
        return null;
    }

    public void setUri(URI uri) {
        this.uri = uri.toString();
        this.id.setClassCurie(uri);
    }

    public int getInstanceCount() { return instanceCount; }

    public void setInstanceCount(int instanceCount) { this.instanceCount = instanceCount; }

    public Dataset getDataset() { return dataset; }

    public void setDataset(Dataset dataset) {
        this.dataset = dataset;
        this.id.setDatasetId(dataset.getId());
    }

    @Override
    public String toString() {
        return "Class{" +
                "id=" + getId() +
                ", labels='" + getLabels() + '\'' +
                ", instanceCount=" + instanceCount +
                ", facets=" + facets + '}';
    }
}
