package net.rhizomik.rhizomer.model.id;

import com.fasterxml.jackson.annotation.JsonValue;
import net.rhizomik.rhizomer.model.Curie;
import net.rhizomik.rhizomer.model.Dataset;

import javax.persistence.Embeddable;
import java.io.Serializable;
import java.net.URI;

/**
 * Created by http://rhizomik.net/~roberto/
 */
@Embeddable
public class DatasetClassId implements Serializable {
    private String datasetId;
    private String classCurie;

    public DatasetClassId() {}

    public DatasetClassId(String idStr) {
        String[] idComponents = idStr.split("/");
        this.datasetId = idComponents[1];
        this.classCurie = idComponents[3];
    }

    public DatasetClassId(Dataset dataset, URI uri) {
        this.datasetId = dataset.getId();
        this.classCurie = new Curie(uri).toString();
    }

    public DatasetClassId(Dataset dataset, Curie classCurie) {
        this.datasetId = dataset.getId();
        this.classCurie = classCurie.toString();
    }

    public String getDatasetId() { return datasetId; }

    public void setDatasetId(String datasetId) { this.datasetId = datasetId; }

    public String getClassCurie() { return classCurie; }

    public void setClassCurie(URI classUri) {
        this.classCurie = new Curie(classUri).toString();
    }

    @Override
    @JsonValue
    public String toString() {
        return "/datasets/"+datasetId+"/classes/"+classCurie;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DatasetClassId that = (DatasetClassId) o;
        if (!datasetId.equals(that.datasetId)) return false;
        return classCurie.equals(that.classCurie);
    }

    @Override
    public int hashCode() {
        int result = datasetId.hashCode();
        result = 31 * result + classCurie.hashCode();
        return result;
    }
}
