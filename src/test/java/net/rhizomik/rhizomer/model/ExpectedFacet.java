package net.rhizomik.rhizomer.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Created by http://rhizomik.net/~roberto/
 */
public class ExpectedFacet {
    public String id;
    public String uri;
    public String curie;
    public String label;
    public int timesUsed;
    public int differentValues;
    public String range;
    public boolean relation;

    public ExpectedFacet() {}

    public ExpectedFacet(Facet datasetFacet) {
        this.uri = datasetFacet.getId().toString();
        this.curie = id.split("/")[4];
        this.label = datasetFacet.getLabel();
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
                ", label='" + label + '\'' +
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
        if (label != null ? !label.equals(that.label) : that.label != null) return false;
        return !(range != null ? !range.equals(that.range) : that.range != null);

    }

    @Override
    public int hashCode() {
        int result = uri.hashCode();
        result = 31 * result + (label != null ? label.hashCode() : 0);
        result = 31 * result + timesUsed;
        result = 31 * result + differentValues;
        result = 31 * result + (range != null ? range.hashCode() : 0);
        result = 31 * result + (relation ? 1 : 0);
        return result;
    }
}
