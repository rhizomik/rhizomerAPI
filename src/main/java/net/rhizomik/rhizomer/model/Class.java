package net.rhizomik.rhizomer.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
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
public class Class {
    private static final Logger logger = LoggerFactory.getLogger(Class.class);

    @EmbeddedId
    private PondClassId id;

    private String uri;
    private String label;
    @OneToMany(fetch = FetchType.LAZY, orphanRemoval = true, mappedBy = "domain")
    @JsonManagedReference
    private List<Facet> facets = new ArrayList<>();
    private int instanceCount;
    @ManyToOne
    @MapsId("pondId")
    @JsonBackReference
    private Pond pond;

    public Class() {
        this.id = new PondClassId();
    }

    public Class(Pond pond, String curie, String label, int instanceCount) throws URISyntaxException {
        this(pond, Curie.toUri(curie), label, instanceCount);
    }

    public Class(Pond pond, String namespace, String localName, String label, int instanceCount) throws URISyntaxException {
        this(pond, new URI(namespace+localName), label, instanceCount);
    }

    public Class(Pond pond, URI uri, String label, int instanceCount) {
        this.id = new PondClassId(pond, uri);
        this.uri = uri.toString();
        this.label = label;
        this.instanceCount = instanceCount;
        this.pond = pond;
        logger.debug("Created class: {}", super.toString());
    }

    public List<Facet> getFacets() { return facets; }

    public void setFacets(List<Facet> facets) { this.facets = facets; }

    public void addFacet(Facet facet) { facets.add(facet); }

    public PondClassId getId() { return id; }

    public void setId(PondClassId id) {
        this.id = id;
    }

    public URI getUri() {
        try { return new URI(uri); }
        catch (URISyntaxException e) { e.printStackTrace(); }
        return null;
    }

    public void setUri(URI uri) {
        this.uri = uri.toString();
        this.id.setClassCurie(uri);
    }

    public String getLabel() { return label; }

    public void setLabel(String label) { this.label = label; }

    public int getInstanceCount() { return instanceCount; }

    public void setInstanceCount(int instanceCount) { this.instanceCount = instanceCount; }

    public Pond getPond() { return pond; }

    public void setPond(Pond pond) {
        this.pond = pond;
        this.id.setPondId(pond.getId());
    }

    @Override
    public String toString() {
        return "Class{" +
                "id=" + getId() +
                ", label='" + label + '\'' +
                ", instanceCount=" + instanceCount +
                ", facets=" + facets + '}';
    }
}
