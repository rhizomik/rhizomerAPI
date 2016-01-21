package net.rhizomik.rhizomer.model;

import com.fasterxml.jackson.annotation.JsonValue;

import javax.persistence.Embeddable;
import java.io.Serializable;
import java.net.URI;

/**
 * Created by http://rhizomik.net/~roberto/
 */
@Embeddable
public class DatasetClassFacetRangeId implements Serializable {
    DatasetClassFacetId datasetClassFacetId;
    String rangeCurie;

    public DatasetClassFacetRangeId() {}

    public DatasetClassFacetRangeId(String idStr) {
        String[] idComponents = idStr.split("/ranges/");
        this.datasetClassFacetId = new DatasetClassFacetId(idComponents[0]);
        this.rangeCurie = idComponents[1];
    }

    public DatasetClassFacetRangeId(Dataset dataset, URI classUri, URI facetUri, URI rangeUri) {
        this.datasetClassFacetId = new DatasetClassFacetId(dataset, classUri, facetUri);
        this.rangeCurie = new Curie(rangeUri).toString();
    }

    public DatasetClassFacetRangeId(DatasetClassFacetId datasetClassFacetId, Curie rangeCurie) {
        this.datasetClassFacetId = datasetClassFacetId;
        this.rangeCurie = rangeCurie.toString();
    }

    public DatasetClassFacetId getDatasetClassFacetId() { return datasetClassFacetId; }

    public void setDatasetClassFacetId(DatasetClassFacetId datasetClassFacetId) { this.datasetClassFacetId = datasetClassFacetId; }

    public String getRangeCurie() { return rangeCurie; }

    public void setRangeCurie(URI rangeUri) { this.rangeCurie = new Curie(rangeUri).toString(); }

    @Override
    @JsonValue
    public String toString() {
        return getDatasetClassFacetId().toString()+"/ranges/"+getRangeCurie();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DatasetClassFacetRangeId that = (DatasetClassFacetRangeId) o;
        if (!datasetClassFacetId.equals(that.datasetClassFacetId)) return false;
        return rangeCurie.equals(that.rangeCurie);
    }

    @Override
    public int hashCode() {
        int result = datasetClassFacetId.hashCode();
        result = 31 * result + rangeCurie.hashCode();
        return result;
    }
}
