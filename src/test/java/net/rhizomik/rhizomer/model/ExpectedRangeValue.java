package net.rhizomik.rhizomer.model;

/**
 * Created by http://rhizomik.net/~roberto/
 */
public class ExpectedRangeValue {
    public String value;
    public int count;
    public String uri;
    public String curie;
    public String label;

    public ExpectedRangeValue() {}

    public ExpectedRangeValue(String value, String curie, String uri, String label, int count) {
        this.value = value;
        this.count = count;
        this.uri = uri;
        this.curie = curie;
        this.label = label;
    }

    public String getValue() { return value; }

    public int getCount() { return count; }

    public String getUri() { return uri; }

    public String getCurie() { return curie; }

    public String getLabel() { return label; }

    public void setValue(String value) { this.value = value; }

    public void setCount(int count) { this.count = count; }

    public void setUri(String uri) { this.uri = uri; }

    public void setCurie(String curie) { this.curie = curie; }

    public void setLabel(String label) { this.label = label; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ExpectedRangeValue that = (ExpectedRangeValue) o;
        return count == that.count &&
            uri.equals(that.uri) &&
            curie.equals(that.curie) &&
            label.equals(that.label) &&
            value.equals(that.value);
    }

    @Override
    public String toString() {
        return "ExpectedRangeValue{" +
            "value='" + value + '\'' +
            ", count=" + count +
            ", uri=" + uri +
            ", curie=" + curie +
            ", label=" + label +
            '}';
    }
}
