package net.rhizomik.rhizomer.model;

import net.rhizomik.rhizomer.service.PrefixCCMap;
import org.springframework.hateoas.Identifiable;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by http://rhizomik.net/~roberto/
 */
@MappedSuperclass
public class CurieEntity implements Identifiable<String> {

    @Id
    private String curie;
    private String uriStr;

    static public PrefixCCMap prefix = new PrefixCCMap();

    public CurieEntity() {}

    public CurieEntity(String uriStr) {
        this.uriStr = uriStr;
        this.curie = prefix.abbreviate(uriStr).replace(':', ';');
    }

    public CurieEntity(URI uri) {
        this.uriStr = uri.toString();
        this.curie = prefix.abbreviate(uriStr).replace(':', ';');
    }

    public String getUriStr() {
        return uriStr;
    }

    public void setUriStr(String uriStr) {
        this.uriStr = uriStr;
        this.curie = prefix.abbreviate(uriStr).replace(':', ';');
    }

    public URI toURI() throws URISyntaxException {
        return new URI(uriStr);
    }

    static public String curieToUriStr(String curie) {
        return prefix.expand(curie);
    }

    static public URI curieToUri(String curie) throws URISyntaxException {
        return new URI(prefix.expand(curie));
    }

    @Override
    public String getId() { return curie.replace(';', ':'); }

    @Override
    public String toString() { return curie.replace(';', ':'); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CurieEntity curie = (CurieEntity) o;
        return uriStr.equals(curie.uriStr);
    }

    @Override
    public int hashCode() {
        return uriStr.hashCode();
    }
}
