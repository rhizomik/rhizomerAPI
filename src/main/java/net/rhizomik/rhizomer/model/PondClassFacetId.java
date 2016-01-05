package net.rhizomik.rhizomer.model;

import com.fasterxml.jackson.annotation.JsonValue;

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

    public PondClassFacetId(String idStr) {
        String[] idComponents = idStr.split("/facets/");
        this.pondClassId = new PondClassId(idComponents[0]);
        this.facetCurie = idComponents[1];
    }

    public PondClassFacetId(Pond pond, URI classUri, URI facetUri) {
        this.pondClassId = new PondClassId(pond, classUri);
        this.facetCurie = new Curie(facetUri).toString();
    }

    public PondClassFacetId(PondClassId pondClassId, Curie facetCurie) {
        this.pondClassId = pondClassId;
        this.facetCurie = facetCurie.toString();
    }

    public PondClassId getPondClassId() { return pondClassId; }

    public void setPondClassId(PondClassId pondClassId) { this.pondClassId = pondClassId; }

    public String getFacetCurie() { return facetCurie; }

    public void setFacetCurie(URI facetUri) { this.facetCurie = new Curie(facetUri).toString(); }

    @Override
    @JsonValue
    public String toString() {
        return "/ponds/"+getPondClassId().getPondId()+"/classes/"+getPondClassId().getClassCurie()+"/facets/"+getFacetCurie();
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
