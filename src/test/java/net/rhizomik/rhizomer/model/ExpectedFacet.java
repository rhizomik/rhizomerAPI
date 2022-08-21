package net.rhizomik.rhizomer.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Objects;

/**
 * Created by http://rhizomik.net/~roberto/
 */
public class ExpectedFacet extends Labelled {
    public String id;
    public String uri;
    public String curie;
    public int timesUsed;
    public int differentValues;
    public String domainURI;
    public String range;
    public boolean relation;
    public boolean allBlank;

    public ExpectedFacet() { super(""); }

    public ExpectedFacet(String uri, String labels, int timesUsed, int differentValues, boolean relation,
                         String range, String domainURI, boolean allBlank) {
        super(labels);
        this.uri = uri;
        this.timesUsed = timesUsed;
        this.differentValues = differentValues;
        this.relation = relation;
        this.range = range;
    }

    public ExpectedFacet(Facet datasetFacet) {
        super("");
        this.uri = datasetFacet.getId().toString();
        this.curie = id.split("/")[4];
        this.setLabels(datasetFacet.getLabels());
        this.timesUsed = datasetFacet.getTimesUsed();
        this.differentValues = datasetFacet.getDifferentValues();
        this.range = datasetFacet.getRange();
        this.relation = datasetFacet.isRelation();
    }

    @JsonIgnore
    public String getId() { return id; }

    public String getRange() {
        return range;
    }

    public void setRange(String range) {
        try {
            this.range = Curie.uriStrToCurie(range);
        } catch (Exception e) {
            this.range = "";
        }
    }

    @Override
    public String toString() {
        return "ExpectedFacet{" +
                "uri='" + uri + '\'' +
                ", labels='" + getLabels() + '\'' +
                ", timesUsed=" + timesUsed +
                ", differentValues=" + differentValues +
                ", range='" + range + '\'' +
                ", relation=" + relation +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExpectedFacet)) return false;

        ExpectedFacet that = (ExpectedFacet) o;

        if (timesUsed != that.timesUsed) return false;
        if (differentValues != that.differentValues) return false;
        if (relation != that.relation) return false;
        if (!uri.equals(that.uri)) return false;
        if (getLabels() != null ? !getLabels().equals(that.getLabels()) : that.getLabels() != null) return false;
        return Objects.equals(range, that.range);

    }

    @Override
    public int hashCode() {
        int result = uri.hashCode();
        result = 31 * result + (getLabels() != null ? getLabels().hashCode() : 0);
        result = 31 * result + timesUsed;
        result = 31 * result + differentValues;
        result = 31 * result + (range != null ? range.hashCode() : 0);
        result = 31 * result + (relation ? 1 : 0);
        return result;
    }
}
