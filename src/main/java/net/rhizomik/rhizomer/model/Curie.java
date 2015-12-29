package net.rhizomik.rhizomer.model;

import net.rhizomik.rhizomer.service.PrefixCCMap;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by http://rhizomik.net/~roberto/
 */
public class Curie {

    private String curie;
    private String uriStr;

    static public PrefixCCMap prefix = new PrefixCCMap();

    public Curie() {}

    public Curie(String curie) {
        this.uriStr = prefix.expand(curie);
        this.curie = curie;
    }

    public Curie(URI uri) {
        this.uriStr = uri.toString();
        this.curie = prefix.abbreviate(uriStr);
    }

    public String getUriStr() {
        return uriStr;
    }

    public void setUriStr(String uriStr) {
        this.uriStr = uriStr;
        this.curie = prefix.abbreviate(uriStr);
    }

    public URI toURI() throws URISyntaxException {
        return new URI(uriStr);
    }

    static public String curieToUriStr(String curie) {
        return prefix.expand(curie);
    }

    static public URI toUri(String curie) throws URISyntaxException {
        return new URI(prefix.expand(curie));
    }

    @Override
    public String toString() { return curie; }
}
