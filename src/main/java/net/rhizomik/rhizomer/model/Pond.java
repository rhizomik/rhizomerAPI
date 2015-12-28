package net.rhizomik.rhizomer.model;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.update.UpdateRequest;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by http://rhizomik.net/~roberto/
 */
@Entity
public class Pond {
    private static final Logger logger = LoggerFactory.getLogger(Pond.class);

    @Id
    private String id;
    @ElementCollection
    private List<String> pondGraphs = new ArrayList<>();
    @ManyToOne
    private Server server;
    @OneToMany(fetch = FetchType.LAZY, orphanRemoval = true, mappedBy = "pond")
    private List<Class> classes = new ArrayList<>();
    private Queries.QueryType queryType = Queries.QueryType.SIMPLE;
    private boolean inferenceEnabled = true;
    private int sampleSize = 0;
    private double coverage = 0.0;

    public Pond() {}

    public Pond(String id) {
        this.id = id;
    }

    public Pond(String id, URL serverUrl) throws MalformedURLException {
        this(id, serverUrl, null, null);
    }

    public Pond(String id, URL serverUrl, List<String> graphs, List<String> ontologies) throws MalformedURLException {
        this.id = id;
        this.server = new Server(serverUrl);
        this.pondGraphs = graphs;
        if (ontologies != null)
            ontologies.forEach(this::addPondOntology);
    }

    public List<Class> getClasses() {
        if (classes.isEmpty() && getServer()!=null ) {
            if (isInferenceEnabled())
                inferTypes();
            classes = new ArrayList<>();
            ResultSet result = querySelect(Queries.getQueryClasses(queryType));
            while (result.hasNext()) {
                QuerySolution soln = result.nextSolution();
                Resource r = soln.getResource("?class");
                int count = soln.getLiteral("?n").getInt();
                try { addClass(r.getURI(), new Class(this, new URI(r.getURI()), r.getLocalName(), count));
                } catch (URISyntaxException e) { logger.error("URI syntax error: {}", r.getURI()); }
            }
        }
        return classes;
    }

    public void setClasses(List<Class> classes) { this.classes = classes; }

    public void addPondOntology(String url) {
        getServer().loadOntology(getPondOntologiesGraph().toString(), url);
    }

    public void inferTypes() {
        List<String> targetGraphs = getPondGraphsStrings();
        targetGraphs.add(getPondOntologiesGraph().toString());
        UpdateRequest createGraph = Queries.getCreateGraph(getPondInferenceGraph().toString());
        getServer().queryUpdate(createGraph);
        UpdateRequest update = Queries.getUpdateInferTypes(targetGraphs, getPondInferenceGraph().toString());
        getServer().queryUpdate(update);
    }

    public void inferTypesConstruct() {
        UpdateRequest createGraph = Queries.getCreateGraph(getPondInferenceGraph().toString());
        getServer().queryUpdate(createGraph);
        List<String> targetGraphs = getPondGraphsStrings();
        targetGraphs.add(getPondOntologiesGraph().toString());
        Model inferredModel = getServer().queryConstruct(Queries.getQueryInferTypes(), targetGraphs);
        File inferenceOut = new File(id + "-inference.ttl");
        try {
            RDFDataMgr.write(new FileOutputStream(inferenceOut), inferredModel, Lang.TURTLE);
        } catch (FileNotFoundException e) {
            logger.error(e.getMessage());
        }
        //getServer().loadModel(getPondInferenceGraph().toString(), inferredModel);
    }

    private void addClass(String classUri, Class aClass) { classes.add(aClass); }

    public ResultSet querySelect(Query query) {
        return getServer().querySelect(query, getPondGraphsStrings(), null);
    }

    public ResultSet querySelect(Query query, List<String> graphs) {
        return getServer().querySelect(query, graphs, null);
    }

    public void addPondGraph(String graph) { this.pondGraphs.add(graph); }

    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

    public List<String> getPondGraphsStrings() {
        return pondGraphs;
    }

    public void setPondGraphs(List<String> pondGraphs) { this.pondGraphs = pondGraphs; }

    public Server getServer() { return server; }

    public void setServer(Server server) { this.server = server; }

    public Queries.QueryType getQueryType() { return queryType; }

    public void setQueryType(Queries.QueryType queryType) { this.queryType = queryType; }

    public URI getPondUri() {
        URI pondURI = null;
        try {
            pondURI = new URI("http://ontolake.net/pond/"+getId());
        } catch (URISyntaxException e) {
            logger.error(e.getMessage());
        }
        return pondURI;
    }

    public URI getPondOntologiesGraph() {
        URI pondOntologiesGraphURI = null;
        try {
            pondOntologiesGraphURI = new URI(getPondUri()+"/ontologies");
        } catch (URISyntaxException e) {
            logger.error(e.getMessage());
        }
        return pondOntologiesGraphURI;
    }

    public URI getPondInferenceGraph() {
        URI pondInferenceGraphURI = null;
        try {
            pondInferenceGraphURI = new URI(getPondUri()+"/inference");
        } catch (URISyntaxException e) {
            logger.error(e.getMessage());
        }
        return pondInferenceGraphURI;
    }

    public boolean isInferenceEnabled() { return inferenceEnabled; }

    public void setInferenceEnabled(boolean inferenceEnabled) { this.inferenceEnabled = inferenceEnabled; }

    public int getSampleSize() { return sampleSize; }

    public void setSampleSize(int sampleSize) { this.sampleSize = sampleSize; }

    public double getCoverage() { return coverage; }

    public void setCoverage(double coverage) { this.coverage = coverage; }
}
