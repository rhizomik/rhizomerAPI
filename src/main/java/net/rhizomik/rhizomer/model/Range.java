package net.rhizomik.rhizomer.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import net.rhizomik.rhizomer.model.id.DatasetClassFacetRangeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by http://rhizomik.net/~roberto/
 */
@Entity
@Table(name = "`range`")
public class Range {
    private static final Logger logger = LoggerFactory.getLogger(Range.class);

    @EmbeddedId
    DatasetClassFacetRangeId id;

    private String uri;
    private String label;
    @ManyToOne
    @JsonBackReference
    private Facet facet;
    private int timesUsed;
    private int differentValues;
    private boolean allLiteral;

    public Range() {
        this.id = new DatasetClassFacetRangeId();
    }

    public Range(Facet facet, String curie, String label, int timesUsed, int differentValues, boolean allLiteral)
            throws URISyntaxException {
        this(facet, Curie.toUri(curie), label, timesUsed, differentValues, allLiteral);
    }

    public Range(String curie, String label, int timesUsed, int differentValues, boolean allLiteral) {
        this();
        this.id.setRangeCurie(curie);
        this.uri = Curie.curieToUriStr(curie);
        this.label = label;
        this.timesUsed = timesUsed;
        this.differentValues = differentValues;
        this.allLiteral = allLiteral;
    }

    public Range(Facet facet, URI rangeUri, String label, int timesUsed, int differentValues, boolean allLiteral) {
        this.id = new DatasetClassFacetRangeId(facet.getDomain().getDataset(), facet.getDomain().getUri(), facet.getUri(), rangeUri);
        this.uri = rangeUri.toString();
        this.label = label;
        this.facet = facet;
        this.timesUsed = timesUsed;
        this.differentValues = differentValues;
        this.allLiteral = allLiteral;
        logger.info("\t Created Range {} for Facet {}", this.getId(), facet.getId());
    }

    public boolean isRelation() { return !allLiteral; }

    public void setRelation(boolean isRelation) { this.allLiteral = !isRelation; }

    public DatasetClassFacetRangeId getId() { return id; }

    public URI getUri() {
        try { return new URI(uri); }
        catch (URISyntaxException e) { e.printStackTrace(); }
        return null;
    }

    public void setUri(URI uri) {
        this.uri = uri.toString();
        if (this.id == null) {
            this.id = new DatasetClassFacetRangeId();
            this.id.setRange(uri);
        }
        else
            this.id.setRange(uri);
    }

    public String getCurie() { return id.getRangeCurie(); }

    public String getLabel() { return label; }

    public void setLabel(String label) { this.label = label; }

    public Facet getFacet() { return facet; }

    public void setFacet(Facet facet) {
        this.facet = facet;
        if (this.id == null)
            this.id = new DatasetClassFacetRangeId(facet.getId(), new Curie(getUri()));
        else
            this.id.setDatasetClassFacetId(facet.getId());
    }

    public int getTimesUsed() { return timesUsed; }

    public int getDifferentValues() { return differentValues; }

    public void setDifferentValues(int differentValues) { this.differentValues = differentValues; }

    @JsonIgnore
    public boolean getAllLiteral() { return allLiteral; }

    public void setAllLiteral(boolean allLiteral) { this.allLiteral = allLiteral; }

    @Override
    public String toString() {
        return "Range{" +
                "id=" + getId() +
                ", label='" + label + '\'' +
                ", facet=" + facet.getId() +
                ", timesUsed=" + timesUsed +
                ", differentValues=" + differentValues +
                '}';
    }
}
