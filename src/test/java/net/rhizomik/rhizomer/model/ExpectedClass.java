package net.rhizomik.rhizomer.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by http://rhizomik.net/~roberto/
 */
public class ExpectedClass {
    public String id;
    public String uri;
    public String label;
    public int instanceCount = 0;
    public List<ExpectedFacet> facets = new ArrayList<>();

    public ExpectedClass() {}

    public ExpectedClass(Class pondClass) {
        this.id = pondClass.getId().toString();
        this.uri = pondClass.getUri().toString();
        this.label = pondClass.getLabel();
        this.instanceCount = pondClass.getInstanceCount();
    }

    public String getUri() {
        if (uri == null) {
            String curie = id.split("/")[4];
            this.uri = Curie.curieToUriStr(curie);
        }
        return uri;
    }

    @Override
    public String toString() {
        return "ExpectedClass{" +
                "uri='" + id + '\'' +
                ", uri='" + getUri() + '\'' +
                ", label='" + label + '\'' +
                ", instanceCount=" + instanceCount +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExpectedClass that = (ExpectedClass) o;
        if (instanceCount != that.instanceCount) return false;
        if (!id.equals(that.id)) return false;
        if (!getUri().equals(that.getUri())) return false;
        return label != null ? label.equals(that.label) : that.label == null;
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + getUri().hashCode();
        result = 31 * result + (label != null ? label.hashCode() : 0);
        result = 31 * result + instanceCount;
        return result;
    }
}
