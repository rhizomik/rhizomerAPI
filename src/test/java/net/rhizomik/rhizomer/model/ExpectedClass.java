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
    public String curie;
    public int instanceCount = 0;
    public int facetsCount = 0;
    public List<ExpectedFacet> facets = new ArrayList<>();

    public ExpectedClass() {}

    public ExpectedClass(String id, String uri, String label, String curie, int instanceCount) {
        this.id = id;
        this.uri = uri;
        this.curie = curie;
        this.label = label;
        this.instanceCount = instanceCount;
    }

    public ExpectedClass(Class datasetClass) {
        this.id = datasetClass.getId().toString();
        this.uri = datasetClass.getUri().toString();
        this.curie = id.split("/")[4];
        this.label = datasetClass.getLabel();
        this.instanceCount = datasetClass.getInstanceCount();
        this.facetsCount = datasetClass.getFacetsCount();
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
                "uri='" + getUri() + '\'' +
                ", label='" + label + '\'' +
                ", instanceCount=" + instanceCount +
                ", facetsCount=" + facetsCount +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExpectedClass that = (ExpectedClass) o;
        if (instanceCount != that.instanceCount) return false;
        if (facetsCount != that.facetsCount) return false;
        if (!getUri().equals(that.getUri())) return false;
        return label != null ? label.equals(that.label) : that.label == null;
    }
}
