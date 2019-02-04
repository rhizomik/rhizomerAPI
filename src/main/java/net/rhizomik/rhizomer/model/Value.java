package net.rhizomik.rhizomer.model;

/**
 * Created by http://rhizomik.net/~roberto/
 */
public class Value {
    private String value;
    private int count;
    private String uri;
    private String curie;
    private String label;

    public Value(String value, int count, String uri, String curie, String label) {
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
}
