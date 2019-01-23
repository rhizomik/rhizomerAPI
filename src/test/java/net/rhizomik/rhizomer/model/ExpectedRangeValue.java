package net.rhizomik.rhizomer.model;

/**
 * Created by http://rhizomik.net/~roberto/
 */
public class ExpectedRangeValue {
    public String value;
    public int count;
    public String curie;

    public ExpectedRangeValue() {}

    public ExpectedRangeValue(String value, int count) {
        this.value = value;
        this.count = count;
        this.curie = curie;
    }

    public String getValue() { return value; }

    public int getCount() { return count; }

    public String getCurie() { return curie; }

    public void setValue(String value) { this.value = value; }

    public void setCount(int count) { this.count = count; }

    public void setCurie(String curie) { this.curie = curie; }

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
            curie.equals(that.curie) &&
            value.equals(that.value);
    }

    @Override
    public String toString() {
        return "ExpectedRangeValue{" +
            "value='" + value + '\'' +
            ", count=" + count +
            ", curie=" + curie +
            '}';
    }
}
