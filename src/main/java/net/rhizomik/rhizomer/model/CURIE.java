package net.rhizomik.rhizomer.model;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by http://rhizomik.net/~roberto/
 */
public class CURIE {
    private static BiMap<String, String> prefixes;

    static public void setPrefixes(Map<String, String> prefixes) {
        CURIE.prefixes = HashBiMap.create(prefixes);
    }

    public static Map<String, String> getPrefixes() {
        if (prefixes == null)
            return new HashMap<>();
        else
            return prefixes;
    }

    static public String toURIString(String curie) {
        return prefixes.get(curie.split(":")[0]) + curie.split(":")[1];
    }

    static public URI toURI(String curie) throws URISyntaxException {
        return new URI(toURIString(curie));
    }

    public static String fromString(String uriString) {
        String prefix, reference;
        if (uriString.contains("#")) {
            prefix = uriString.split("#")[0]+"#";
            reference = uriString.split("#")[1];
        }
        else {
            prefix = uriString.substring(0, uriString.lastIndexOf('/')+1);
            reference = uriString.substring(uriString.lastIndexOf('/')+1);
        }
        return prefixes.inverse().get(prefix) + ":" + reference;
    }

    public static String fromURI(URI uri) {
        return fromString(uri.toString());
    }
}
