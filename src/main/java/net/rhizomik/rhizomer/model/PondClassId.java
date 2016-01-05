package net.rhizomik.rhizomer.model;

import com.fasterxml.jackson.annotation.JsonValue;

import javax.persistence.Embeddable;
import java.io.Serializable;
import java.net.URI;

/**
 * Created by http://rhizomik.net/~roberto/
 */
@Embeddable
public class PondClassId implements Serializable {
    private String pondId;
    private String classCurie;

    public PondClassId() {}

    public PondClassId(String idStr) {
        String[] idComponents = idStr.split("/");
        this.pondId = idComponents[1];
        this.classCurie = idComponents[3];
    }

    public PondClassId(Pond pond, URI uri) {
        this.pondId = pond.getId();
        this.classCurie = new Curie(uri).toString();
    }

    public PondClassId(Pond pond, Curie classCurie) {
        this.pondId = pond.getId();
        this.classCurie = classCurie.toString();
    }

    public String getPondId() { return pondId; }

    public void setPondId(String pondId) { this.pondId = pondId; }

    public String getClassCurie() { return classCurie; }

    public void setClassCurie(URI classUri) {
        this.classCurie = new Curie(classUri).toString();
    }

    @Override
    @JsonValue
    public String toString() {
        return "/ponds/"+pondId+"/classes/"+classCurie;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PondClassId that = (PondClassId) o;
        if (!pondId.equals(that.pondId)) return false;
        return classCurie.equals(that.classCurie);
    }

    @Override
    public int hashCode() {
        int result = pondId.hashCode();
        result = 31 * result + classCurie.hashCode();
        return result;
    }
}
