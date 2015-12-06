package net.rhizomik.rhizomer.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

/**
 * Created by http://rhizomik.net/~roberto/
 */
@Entity
public class Facet {
    private static final Logger logger = LoggerFactory.getLogger(Facet.class);

    @Id
    private URI uri;
    private String curie;
    private String label;
    @ManyToOne
    @JsonBackReference
    private Class domain;
    private int uses;
    private int values;
    String[] ranges;
    private boolean allLiteral;

    public Facet(Class domain, String curie, String label, int uses, int values, String[] ranges, boolean allLiteral)
            throws URISyntaxException {
        this(domain, CURIE.toURI(curie), label, uses, values, ranges, allLiteral);
    }

    public Facet(String curie, String label, int uses, int values, String[] ranges, boolean allLiteral)
            throws URISyntaxException {
        this(null, CURIE.toURI(curie), label, uses, values, ranges, allLiteral);
    }

    public Facet(Class domain, URI uri, String label, int uses, int values, String[] ranges, boolean allLiteral) {
        this.uri = uri;
        this.label = label;
        this.domain = domain;
        this.uses = uses;
        this.values = values;
        this.ranges = ranges;
        this.allLiteral = allLiteral;
        logger.info("\t Created facet {} with ranges: {}", uri, ranges);
    }

    public boolean isRelation() { return !allLiteral; }

    public URI getUri() { return uri; }

    public String getLabel() { return label; }

    public void setLabel(String label) { this.label = label; }

    public String[] getRanges() { return ranges; }

    public String getRange() {
        return ranges.length > 0 ? ranges[0] : null; //TODO: compute supertype if multiple ranges
    }

    public Class getDomain() { return domain; }

    public void setDomain(Class domain) { this.domain = domain; }

    public int getUses() { return uses; }

    public int getValues() { return values; }

    public boolean getAllLiteral() { return allLiteral; }

    @Override
    public String toString() {
        return "Facet{" +
                "uri=" + uri +
                ", label='" + label + '\'' +
                ", domain=" + domain.getUri() +
                ", uses=" + uses +
                ", values=" + values +
                ", ranges=" + Arrays.toString(ranges) +
                '}';
    }
}
