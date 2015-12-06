package net.rhizomik.rhizomer.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by http://rhizomik.net/~roberto/
 */
@Entity
public class Class {
    private static final Logger logger = LoggerFactory.getLogger(Class.class);

    @Id
    private URI uri;
    private String label;
    @OneToMany(fetch = FetchType.LAZY, orphanRemoval = true, mappedBy = "domain")
    private Map<String, Facet> facets;
    private int count;
    @ManyToOne
    @JsonBackReference
    private Pond pond;

    public Class(Pond pond, String curie, String label, int count) throws URISyntaxException {
        this(pond, CURIE.toURI(curie), label, count);
    }

    public Class(Pond pond, String namespace, String localName, String label, int count) throws URISyntaxException {
        this(pond, new URI(namespace+localName), label, count);
    }

    public Class(Pond pond, URI uri, String label, int count) {
        this.pond = pond;
        this.uri = uri;
        this.label = label;
        this.count = count;
        logger.debug("Created class: {}", uri);
    }

    public Map<String, Facet> getFacets() {
        if (facets == null) {
            facets = new HashMap<String, Facet>();
            ResultSet result = getPond().querySelect(
                    Queries.getQueryClassFacets(getUri().toString(), getPond().getQueryType(),
                                                getPond().getSampleSize(), this.getCount(), getPond().getCoverage()));
            while (result.hasNext()) {
                QuerySolution soln = result.nextSolution();
                if (soln.contains("?property")) {
                    Resource r = soln.getResource("?property");
                    int uses = soln.getLiteral("?uses").getInt();
                    int values = soln.getLiteral("?values").getInt();
                    String[] ranges = {};
                    if (soln.contains("?ranges"))
                        ranges = soln.getLiteral("?ranges").getString().split(",");

                    boolean allLiteralBoolean;
                    Literal allLiteral = soln.getLiteral("?allLiteral");
                    if(allLiteral.getDatatypeURI().equals("http://www.w3.org/2001/XMLSchema#integer"))
                        allLiteralBoolean = (allLiteral.getInt() != 0);
                    else
                        allLiteralBoolean = allLiteral.getBoolean();
                    try {
                        addFacet(r.getURI(), new Facet(this,
                                new URI(r.getURI()), r.getLocalName(), uses, values, ranges, allLiteralBoolean));
                    } catch (URISyntaxException e) {
                        logger.error("URI syntax error: {}", r.getURI());
                    }
                }
            }
        }
        return facets;
    }

    private void addFacet(String propertyUri, Facet facet) {
        facets.put(propertyUri, facet);
    }

    public URI getUri() { return uri; }

    public String getLabel() { return label; }

    public void setLabel(String label) { this.label = label; }

    public int getCount() { return count; }

    public void setCount(int count) { this.count = count; }

    public Pond getPond() { return pond; }

    @Override
    public String toString() {
        return "Class{" +
                "uri='" + uri + '\'' +
                ", label='" + label + '\'' +
                ", count=" + count +
                ", facets=" + facets + '}';
    }
}
