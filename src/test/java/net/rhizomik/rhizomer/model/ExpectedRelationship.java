package net.rhizomik.rhizomer.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.net.URI;

/**
 * Created by http://rhizomik.net/~roberto/
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExpectedRelationship {
    String classCurie;
    String propertyCurie;
    String rangeCurie;
    int uses;

    public ExpectedRelationship() {}

    public ExpectedRelationship(String classCurie, String propertyCurie, String rangeCurie, int uses) {
        this.classCurie = classCurie;
        this.propertyCurie = propertyCurie;
        this.rangeCurie = rangeCurie;
        this.uses = uses;
    }
}
