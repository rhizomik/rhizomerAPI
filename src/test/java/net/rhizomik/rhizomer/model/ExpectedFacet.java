package net.rhizomik.rhizomer.model;

import java.net.URI;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Created by http://rhizomik.net/~roberto/
 */
public class ExpectedFacet {
    public String uri;
    public String label;
    public int uses;
    public int differentValues;
    private String ranges;
    public boolean allLiteral;

    public ExpectedFacet(Facet pondFacet) {
        this.uri = pondFacet.getId().toString();
        this.label = pondFacet.getLabel();
        this.uses = pondFacet.getUses();
        this.differentValues = pondFacet.getDifferentValues();
        this.ranges = pondFacet.getRanges().stream().collect(Collectors.joining(", "));
        this.allLiteral = pondFacet.getAllLiteral();
    }

    public String[] getRanges() {
        return ranges.split(", ");
    }

    public void setRanges(URI[] ranges) {
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
                ", allLiteral=" + allLiteral +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExpectedFacet)) return false;

        ExpectedFacet that = (ExpectedFacet) o;

        if (uses != that.uses) return false;
        if (differentValues != that.differentValues) return false;
        if (allLiteral != that.allLiteral) return false;
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
        result = 31 * result + (allLiteral ? 1 : 0);
        return result;
    }
}
