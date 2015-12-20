package net.rhizomik.rhizomer.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

/**
 * Created by http://rhizomik.net/~roberto/
 */
@Entity
public class Facet extends CurieEntity {
    private static final Logger logger = LoggerFactory.getLogger(Facet.class);

    private String label;
    @ManyToOne
    @JsonBackReference
    private Class domain;
    private int uses;
    private int differentValues;
    String[] ranges;
    private boolean allLiteral;

    public Facet(Class domain, String curie, String label, int uses, int differentValues, String[] ranges, boolean allLiteral)
            throws URISyntaxException {
        this(domain, CurieEntity.curieToUri(curie), label, uses, differentValues, ranges, allLiteral);
    }

    public Facet(String curie, String label, int uses, int differentValues, String[] ranges, boolean allLiteral)
            throws URISyntaxException {
        this(null, CurieEntity.curieToUri(curie), label, uses, differentValues, ranges, allLiteral);
    }

    public Facet(Class domain, URI uri, String label, int uses, int differentValues, String[] ranges, boolean allLiteral) {
        super(uri);
        this.label = label;
        this.domain = domain;
        this.uses = uses;
        this.differentValues = differentValues;
        this.ranges = ranges;
        this.allLiteral = allLiteral;
        logger.info("\t Created facet {} with ranges: {}", uri, ranges);
    }

    public boolean isRelation() { return !allLiteral; }

    public String getLabel() { return label; }

    public void setLabel(String label) { this.label = label; }

    public String[] getRanges() { return ranges; }

    public String getRange() {
        return ranges.length > 0 ? ranges[0] : null; //TODO: compute supertype if multiple ranges
    }

    public Class getDomain() { return domain; }

    public void setDomain(Class domain) { this.domain = domain; }

    public int getUses() { return uses; }

    public int getDifferentValues() { return differentValues; }

    public boolean getAllLiteral() { return allLiteral; }

    @Override
    public String toString() {
        return "Facet{" +
                "curie=" + getId() +
                ", label='" + label + '\'' +
                ", domain=" + domain.getId() +
                ", uses=" + uses +
                ", differentValues=" + differentValues +
                ", ranges=" + Arrays.toString(ranges) +
                '}';
    }
}
