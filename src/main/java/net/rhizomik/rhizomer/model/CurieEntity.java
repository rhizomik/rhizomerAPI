package net.rhizomik.rhizomer.model;

import net.rhizomik.rhizomer.service.PrefixCCMap;
import org.springframework.data.domain.Persistable;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by http://rhizomik.net/~roberto/
 */
@MappedSuperclass
public class CurieEntity implements Persistable<String>{

    @Id
    private String curie;
    private String uriStr;

    static public PrefixCCMap prefix = new PrefixCCMap();

    public CurieEntity() {}

    public CurieEntity(String uriStr) {
        this.uriStr = uriStr;
        this.curie = prefix.abbreviate(uriStr).replace(':', '_');
    }

    public CurieEntity(URI uri) {
        this.uriStr = uri.toString();
        this.curie = prefix.abbreviate(uriStr).replace(':', '_');
    }

    public String getUriStr() {
        return uriStr;
    }

    public void setUriStr(String uriStr) {
        this.uriStr = uriStr;
        this.curie = prefix.abbreviate(uriStr).replace(':', '_');
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
    public String getId() {
        return curie;
    }

    @Override
    public boolean isNew() {
        return curie == null;
    }

    @Override
    public String toString() {
        return curie;
    }

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
