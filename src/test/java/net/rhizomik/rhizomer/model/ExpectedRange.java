package net.rhizomik.rhizomer.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Created by http://rhizomik.net/~roberto/
 */
public class ExpectedRange extends Labelled {
    public String id;
    public String uri;
    public String curie;
    public int timesUsed;
    public int differentValues;
    public boolean relation;

    public ExpectedRange() { super(""); }

    public ExpectedRange(String uri, String labels, String curie, int timesUsed, int differentValues, boolean relation) {
        super(labels);
        this.uri = uri;
        this.curie = curie;
        this.timesUsed = timesUsed;
        this.differentValues = differentValues;
        this.relation = relation;
    }

    public ExpectedRange(Range datasetRange) {
        super("");
        this.uri = datasetRange.getId().toString();
        this.curie = id.split("/")[4];
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
                ", labels='" + getLabels() + '\'' +
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
        if (!getLabels().equals(that.getLabels())) return false;
        return curie.equals(that.curie);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + uri.hashCode();
        result = 31 * result + getLabels().hashCode();
        result = 31 * result + curie.hashCode();
        result = 31 * result + timesUsed;
        result = 31 * result + differentValues;
        result = 31 * result + (relation ? 1 : 0);
        return result;
    }
}
