package net.rhizomik.rhizomer.model.id;

import com.fasterxml.jackson.annotation.JsonValue;
import net.rhizomik.rhizomer.model.Curie;
import net.rhizomik.rhizomer.model.Dataset;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.net.URI;

/**
 * Created by http://rhizomik.net/~roberto/
 */
@Embeddable
public class DatasetClassFacetId implements Serializable {
    DatasetClassId datasetClassId;
    @Column(length = 128)
    String facetCurie;

    public DatasetClassFacetId() {}

    public DatasetClassFacetId(String idStr) {
        String[] idComponents = idStr.split("/facets/");
        this.datasetClassId = new DatasetClassId(idComponents[0]);
        this.facetCurie = idComponents[1];
    }

    public DatasetClassFacetId(Dataset dataset, URI classUri, URI facetUri) {
        this.datasetClassId = new DatasetClassId(dataset, classUri);
        this.facetCurie = new Curie(facetUri).toString();
    }

    public DatasetClassFacetId(DatasetClassId datasetClassId, Curie facetCurie) {
        this.datasetClassId = datasetClassId;
        this.facetCurie = facetCurie.toString();
    }

    public DatasetClassFacetId(DatasetClassId datasetClassId, URI facetUri) {
        this.datasetClassId = datasetClassId;
        this.facetCurie = new Curie(facetUri).toString();
    }

    public DatasetClassId getDatasetClassId() { return datasetClassId; }

    public void setDatasetClassId(DatasetClassId datasetClassId) { this.datasetClassId = datasetClassId; }

    public String getFacetCurie() { return facetCurie; }

    public void setFacetCurie(URI facetUri) { this.facetCurie = new Curie(facetUri).toString(); }

    @Override
    @JsonValue
    public String toString() {
        return "/datasets/"+getDatasetClassId().getDatasetId()+"/classes/"+getDatasetClassId().getClassCurie()+"/facets/"+getFacetCurie();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DatasetClassFacetId that = (DatasetClassFacetId) o;
        if (!datasetClassId.equals(that.datasetClassId)) return false;
        return facetCurie.equals(that.facetCurie);
    }

    @Override
    public int hashCode() {
        int result = datasetClassId.hashCode();
        result = 31 * result + facetCurie.hashCode();
        return result;
    }
}
