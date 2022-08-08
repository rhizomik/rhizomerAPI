package net.rhizomik.rhizomer.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Created by http://rhizomik.net/~roberto/
 */
public class ExpectedRange {
    public String id;
    public String uri;
    public String label;
    public String curie;
    public int timesUsed;
    public int differentValues;
    public boolean relation;

    public ExpectedRange() {}

    public ExpectedRange(String uri, String label, String curie, int timesUsed, int differentValues, boolean relation) {
        this.uri = uri;
        this.curie = curie;
        this.label = label;
        this.timesUsed = timesUsed;
        this.differentValues = differentValues;
        this.relation = relation;
    }

    public ExpectedRange(Range datasetRange) {
        this.uri = datasetRange.getId().toString();
        this.curie = id.split("/")[4];
        this.label = datasetRange.getLabel();
        this.timesUsed = datasetRange.getTimesUsed();
        this.differentValues = datasetRange.getDifferentValues();
        this.relation = datasetRange.isRelation();
    }

    @JsonIgnore
    public String getId() { return id; }

    public void setId(String datasetId, String classCurie, String facetCurie) {
        this.id = "/datasets/"+datasetId+
                  "/classes/"+classCurie+
                  "/facets/"+facetCurie+
                  "/ranges/"+Curie.uriStrToCurie(uri);
    }

    @Override
    public String toString() {
        return "ExpectedRange{" +
                "uri='" + uri + '\'' +
                ", label='" + label + '\'' +
                ", curie='" + curie + '\'' +
                ", timesUsed=" + timesUsed +
                ", differentValues=" + differentValues +
                ", relation=" + relation +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ExpectedRange that = (ExpectedRange) o;

        if (timesUsed != that.timesUsed) return false;
        if (differentValues != that.differentValues) return false;
        if (relation != that.relation) return false;
        if (!uri.equals(that.uri)) return false;
        if (!label.equals(that.label)) return false;
        return curie.equals(that.curie);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + uri.hashCode();
        result = 31 * result + label.hashCode();
        result = 31 * result + curie.hashCode();
        result = 31 * result + timesUsed;
        result = 31 * result + differentValues;
        result = 31 * result + (relation ? 1 : 0);
        return result;
    }
}
