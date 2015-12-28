package net.rhizomik.rhizomer.model;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.update.UpdateExecutionFactory;
import com.hp.hpl.jena.update.UpdateFactory;
import com.hp.hpl.jena.update.UpdateProcessor;
import com.hp.hpl.jena.update.UpdateRequest;
import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.StringWriter;
import java.net.URL;
import java.util.List;

/**
 * Created by http://rhizomik.net/~roberto/
 */
@Entity
public class Server {
    private static final Logger logger = LoggerFactory.getLogger(Server.class);

    @Id
    private String id;
    private String endpoint;

    public Server() {
        super();
    }

    public Server(URL endpoint) {
        this.id = new Curie(endpoint.toString()).toString();
        this.endpoint = endpoint.toString();
    }

    public String getEndpoint() { return endpoint; }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
        this.id = new Curie(endpoint.toString()).toString();
    }

    public String getId() { return id; }

    public ResultSet querySelect(Query query) {
        return this.querySelect(query, null, null);
    }

    public ResultSet querySelect(Query query, List<String> graphs, List<String> ontologies) {
        graphs.forEach(query::addGraphURI);
        logger.debug("Sending to {} query: \n{}", getEndpoint(), query);
        QueryExecution q = QueryExecutionFactory.sparqlService(getEndpoint(), query, graphs, ontologies);
        return ResultSetFactory.copyResults(q.execSelect());
    }

    public void loadOntology(String graph, String uri) {
        Model model = RDFDataMgr.loadModel(uri);
        loadModel(graph, model);
    }

    public void loadModel(String graph, Model model) {
        StringWriter out = new StringWriter();
        RDFDataMgr.write(out, model, Lang.NTRIPLES);
        String insertString = "INSERT DATA { GRAPH <" + graph + "> { " + out.toString() + " } } ";
        UpdateRequest update = UpdateFactory.create(insertString);
        logger.debug("Sending to {} query: \n{}", getEndpoint(), update.toString());
        UpdateProcessor processor = UpdateExecutionFactory.createRemote(update, getEndpoint());
        try {
            processor.execute();
        } catch (HttpException e) {
            logger.error(e.getMessage());
        }
    }

    public Model queryConstruct(Query query, List<String> graphs) {
        graphs.forEach(query::addGraphURI);
        logger.debug("Sending to {} query: \n{}", getEndpoint(), query);
        QueryExecution q = QueryExecutionFactory.sparqlService(getEndpoint(), query, graphs, null);
        return q.execConstruct();
    }

    public void queryUpdate(UpdateRequest update) {
        logger.debug("Sending to {} query: \n{}", getEndpoint(), update.toString());
        UpdateProcessor processor = UpdateExecutionFactory.createRemote(update, getEndpoint());
        try {
            processor.execute();
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }
}
