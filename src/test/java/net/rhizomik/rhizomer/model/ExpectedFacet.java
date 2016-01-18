package net.rhizomik.rhizomer.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.net.URI;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Created by http://rhizomik.net/~roberto/
 */
public class ExpectedFacet {
    public String id;
    public String uri;
    public String label;
    public int uses;
    public int differentValues;
    public String range;
    private String ranges;
    public boolean relation;

    public ExpectedFacet() {}

    public ExpectedFacet(Facet datasetFacet) {
        this.uri = datasetFacet.getId().toString();
        this.label = datasetFacet.getLabel();
        this.uses = datasetFacet.getUses();
        this.differentValues = datasetFacet.getDifferentValues();
        this.ranges = datasetFacet.getRanges().stream().collect(Collectors.joining(", "));
        this.relation = datasetFacet.isRelation();
    }

    @JsonIgnore
    public String getId() { return id; }

    public String[] getRanges() {
        return ranges.split(", ");
    }

    public void setRanges(String[] rangesCuries) {
        this.ranges = Arrays.stream(rangesCuries).collect(Collectors.joining(", "));
    }

    public void setRangesUris(URI[] ranges) {
        this.ranges = Arrays.stream(ranges).map(range -> new Curie(range).toString()).collect(Collectors.joining(", "));
    }

    @Override
    public String toString() {
        return "ExpectedFacet{" +
                "uri='" + uri + '\'' +
                ", label='" + label + '\'' +
                ", uses=" + uses +
                ", differentValues=" + differentValues +
                ", ranges='" + ranges + '\'' +
                ", relation=" + relation +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExpectedFacet)) return false;

        ExpectedFacet that = (ExpectedFacet) o;

        if (uses != that.uses) return false;
        if (differentValues != that.differentValues) return false;
        if (relation != that.relation) return false;
        if (!uri.equals(that.uri)) return false;
        if (label != null ? !label.equals(that.label) : that.label != null) return false;
        return !(ranges != null ? !ranges.equals(that.ranges) : that.ranges != null);

    }

    @Override
    public int hashCode() {
        int result = uri.hashCode();
        result = 31 * result + (label != null ? label.hashCode() : 0);
        result = 31 * result + uses;
        result = 31 * result + differentValues;
        result = 31 * result + (ranges != null ? ranges.hashCode() : 0);
        result = 31 * result + (relation ? 1 : 0);
        return result;
    }
}
