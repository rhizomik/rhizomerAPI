package net.rhizomik.rhizomer.model;

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

    @EmbeddedId
    private PondClassId id;

    private String uri;
    private String label;
    @OneToMany(fetch = FetchType.LAZY, orphanRemoval = true, mappedBy = "domain")
    private Map<String, Facet> facets = new HashMap<>();
    private int instanceCount;
    @ManyToOne
    @MapsId("pondId")
    private Pond pond;

    public Class() {
        this.id = new PondClassId();
    }

    public Class(Pond pond, String curie, String label, int instanceCount) throws URISyntaxException {
        this(pond, Curie.toUri(curie), label, instanceCount);
    }

    public Class(Pond pond, String namespace, String localName, String label, int instanceCount) throws URISyntaxException {
        this(pond, new URI(namespace+localName), label, instanceCount);
    }

    public Class(Pond pond, URI uri, String label, int instanceCount) {
        this.id = new PondClassId(pond, uri);
        this.uri = uri.toString();
        this.label = label;
        this.instanceCount = instanceCount;
        this.pond = pond;
        logger.debug("Created class: {}", super.toString());
    }

    public Map<String, Facet> getFacets() {
        if (facets.isEmpty() && getPond().getServer()!=null) {
            facets = new HashMap<String, Facet>();
            ResultSet result = getPond().querySelect(
                    Queries.getQueryClassFacets(getUri(), getPond().getQueryType(),
                                                getPond().getSampleSize(), this.getInstanceCount(), getPond().getCoverage()));
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

    public PondClassId getId() { return id; }

    public void setId(PondClassId id) {
        this.id = id;
    }

    public String getUri() { return uri; }

    public void setUri(String uri) {
        this.uri = uri;
        this.id.setClassCurie(uri);
    }

    public String getLabel() { return label; }

    public void setLabel(String label) { this.label = label; }

    public int getInstanceCount() { return instanceCount; }

    public void setInstanceCount(int instanceCount) { this.instanceCount = instanceCount; }

    public Pond getPond() { return pond; }

    public void setPond(Pond pond) {
        this.pond = pond;
        this.id.setPondId(pond.getId());
    }

    @Override
    public String toString() {
        return "Class{" +
                "id=" + getId() +
                ", label='" + label + '\'' +
                ", instanceCount=" + instanceCount +
                ", facets=" + facets + '}';
    }
}
