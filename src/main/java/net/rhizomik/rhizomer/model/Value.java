package net.rhizomik.rhizomer.model;

/**
 * Created by http://rhizomik.net/~roberto/
 */
public class Value extends Labelled {
    private String value;
    private int count;
    private String uri;
    private String curie;

    public Value(String value, int count, String uri, String curie, String labels) {
        super(labels);
        this.value = value;
        this.count = count;
        this.uri = uri;
        this.curie = curie;
    }

    public String getValue() { return value; }

    public int getCount() { return count; }

    public String getUri() { return uri; }

    public String getCurie() { return curie; }
}
