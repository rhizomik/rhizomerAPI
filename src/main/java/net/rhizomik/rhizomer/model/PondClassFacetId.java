package net.rhizomik.rhizomer.model;

import javax.persistence.Embeddable;
import java.io.Serializable;
import java.net.URI;

/**
 * Created by http://rhizomik.net/~roberto/
 */
@Embeddable
public class PondClassFacetId implements Serializable {
    PondClassId pondClassId;
    String facetCurie;

    public PondClassFacetId() {}

    public PondClassFacetId(String uriStr) {
        this.facetCurie = uriStr.split("-")[0];
        this.pondClassId = new PondClassId(uriStr.split("-")[1]);
    }

    public PondClassFacetId(Pond pond, URI classUri, URI facetUri) {
        this(pond, classUri.toString(), facetUri.toString());
    }

    public PondClassFacetId(Pond pond, String classUriStr, String facetUriStr) {
        this.pondClassId = new PondClassId(pond, classUriStr);
        this.facetCurie = new Curie(facetUriStr).toString();
    }

    public void setPondClassId(PondClassId pondClassId) {
        this.pondClassId = pondClassId;
    }

    public void setFacetCurie(String facetUriStr) {
        this.facetCurie = new Curie(facetUriStr).toString();
    }

    @Override
    public String toString() {
        return facetCurie.replace(':', '_') + '-' + pondClassId.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PondClassFacetId that = (PondClassFacetId) o;
        if (!pondClassId.equals(that.pondClassId)) return false;
        return facetCurie.equals(that.facetCurie);
    }

    @Override
    public int hashCode() {
        int result = pondClassId.hashCode();
        result = 31 * result + facetCurie.hashCode();
        return result;
    }
}
