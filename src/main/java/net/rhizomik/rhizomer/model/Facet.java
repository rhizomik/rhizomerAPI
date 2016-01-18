package net.rhizomik.rhizomer.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by http://rhizomik.net/~roberto/
 */
@Entity
public class Facet {
    private static final Logger logger = LoggerFactory.getLogger(Facet.class);

    @EmbeddedId
    DatasetClassFacetId id;

    private String uri;
    private String label;
    @ManyToOne
    @MapsId("datasetClassId")
    @JsonBackReference
    private Class domain;
    private int uses;
    private int differentValues;
    @ElementCollection
    private List<String> ranges = new ArrayList<>();
    private boolean allLiteral;

    public Facet() {
        this.id = new DatasetClassFacetId();
    }

    public Facet(Class domain, String curie, String label, int uses, int differentValues, String[] rangesCuries, boolean allLiteral)
            throws URISyntaxException {
        this(domain, Curie.toUri(curie), label, uses, differentValues, rangesCuries, allLiteral);
    }

    public Facet(String curie, String label, int uses, int differentValues, String[] rangesCuries, boolean allLiteral)
            throws URISyntaxException {
        this(null, Curie.toUri(curie), label, uses, differentValues, rangesCuries, allLiteral);
    }

    public Facet(Class domain, URI uri, String label, int uses, int differentValues, String[] rangesCuries, boolean allLiteral) {
        this.id = new DatasetClassFacetId(domain.getDataset(), domain.getUri(), uri);
        this.uri = uri.toString();
        this.label = label;
        this.domain = domain;
        this.uses = uses;
        this.differentValues = differentValues;
        this.ranges = Arrays.asList(rangesCuries);
        this.allLiteral = allLiteral;
        logger.info("\t Created facet {} with rangesCuries: {}", uri, rangesCuries);
    }

    public boolean isRelation() { return !allLiteral; }

    public void setRelation(boolean isRelation) { this.allLiteral = !isRelation; }

    public DatasetClassFacetId getId() { return id; }

    public URI getUri() {
        try { return new URI(uri); }
        catch (URISyntaxException e) { e.printStackTrace(); }
        return null;
    }

    public void setUri(URI uri) {
        this.uri = uri.toString();
        this.id.setFacetCurie(uri);
    }

    public String getLabel() { return label; }

    public void setLabel(String label) { this.label = label; }

    public List<String> getRanges() { return ranges; }

    public String getRange() {
        return ranges.size() > 0 ? ranges.get(0) : null; //TODO: compute supertype if multiple rangesCuries
    }

    public Class getDomain() { return domain; }

    public void setDomain(Class domain) {
        this.domain = domain;
        this.id.setDatasetClassId(domain.getId());
    }

    public int getUses() { return uses; }

    public int getDifferentValues() { return differentValues; }

    @JsonIgnore
    public boolean getAllLiteral() { return allLiteral; }

    @Override
    public String toString() {
        return "Facet{" +
                "id=" + getId() +
                ", label='" + label + '\'' +
                ", domain=" + domain.getId() +
                ", uses=" + uses +
                ", differentValues=" + differentValues +
                ", rangesCuries=" + ranges +
                '}';
    }
}
