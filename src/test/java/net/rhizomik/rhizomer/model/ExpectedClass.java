package net.rhizomik.rhizomer.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by http://rhizomik.net/~roberto/
 */
public class ExpectedClass extends Labelled {
    public String id;
    public String uri;
    public String curie;
    public int instanceCount = 0;
    public int facetsCount = 0;
    public List<ExpectedFacet> facets = new ArrayList<>();

    public ExpectedClass() { super(""); }

    public ExpectedClass(String id, String uri, String labels, String curie, int instanceCount) {
        super(labels);
        this.id = id;
        this.uri = uri;
        this.curie = curie;
        this.instanceCount = instanceCount;
    }

    public ExpectedClass(Class datasetClass) {
        super("");
        this.id = datasetClass.getId().toString();
        this.uri = datasetClass.getUri().toString();
        this.curie = id.split("/")[4];
        this.setLabels(datasetClass.getLabels());
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
                ", labels='" + getLabels() + '\'' +
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
        return getLabels() != null ? getLabels().equals(that.getLabels()) : that.getLabels() == null;
    }
}
