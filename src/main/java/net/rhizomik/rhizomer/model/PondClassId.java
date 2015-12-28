package net.rhizomik.rhizomer.model;

import javax.persistence.Embeddable;
import java.io.Serializable;
import java.net.URI;

/**
 * Created by http://rhizomik.net/~roberto/
 */
@Embeddable
public class PondClassId implements Serializable {
    String pondId;
    String classCurie;

    public PondClassId() {}

    public PondClassId(String uriStr) {
        this.classCurie = uriStr.split("@")[0];
        this.pondId = uriStr.split("@")[1];
    }

    public PondClassId(Pond pond, URI uri) {
        this(pond, uri.toString());
    }

    public PondClassId(Pond pond, String uriStr) {
        this.pondId = pond.getId();
        this.classCurie = new Curie(uriStr).toString();
    }

    public String getPondId() {
        return pondId;
    }

    public void setPondId(String pondId) {
        this.pondId = pondId;
    }

    public String getClassCurie() {
        return classCurie;
    }

    public void setClassCurie(String classUriStr) {
        this.classCurie = new Curie(classUriStr).toString();
    }

    @Override
    public String toString() {
        return classCurie.replace(':', '_') + '@' + pondId;
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
