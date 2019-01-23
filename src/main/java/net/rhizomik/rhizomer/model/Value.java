package net.rhizomik.rhizomer.model;

/**
 * Created by http://rhizomik.net/~roberto/
 */
public class Value {
    private String value;
    private int count;
    private String curie;

    public Value(String value, int count, String curie) {
        this.value = value;
        this.count = count;
        this.curie = curie;
    }

    public String getValue() { return value; }

    public int getCount() { return count; }

    public String getCurie() { return curie; }
}
