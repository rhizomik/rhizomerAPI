package net.rhizomik.rhizomer.service;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.update.UpdateExecutionFactory;
import com.hp.hpl.jena.update.UpdateFactory;
import com.hp.hpl.jena.update.UpdateProcessor;
import com.hp.hpl.jena.update.UpdateRequest;
import net.rhizomik.rhizomer.model.Pond;
import net.rhizomik.rhizomer.model.Queries;
import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.StringWriter;
import java.net.URI;
import java.net.URL;
import java.util.List;

/**
 * Created by http://rhizomik.net/~roberto/
 */
@Service
public class SPARQLService {
    private static final Logger logger = LoggerFactory.getLogger(SPARQLService.class);

    public ResultSet querySelect(URL sparqlEndpoint, Query query) {
        return this.querySelect(sparqlEndpoint, query, null, null);
    }

    public ResultSet querySelect(URL sparqlEndpoint, Query query, List<String> graphs, List<String> ontologies) {
        graphs.forEach(query::addGraphURI);
        logger.debug("Sending to {} query: \n{}", sparqlEndpoint, query);
        QueryExecution q = QueryExecutionFactory.sparqlService(sparqlEndpoint.toString(), query, graphs, ontologies);
        return ResultSetFactory.copyResults(q.execSelect());
    }

    public Model queryConstruct(URL sparqlEndpoint, Query query, List<String> graphs) {
        graphs.forEach(query::addGraphURI);
        logger.debug("Sending to {} query: \n{}", sparqlEndpoint, query);
        QueryExecution q = QueryExecutionFactory.sparqlService(sparqlEndpoint.toString(), query, graphs, null);
        return q.execConstruct();
    }

    public void queryUpdate(URL sparqlEndpoint, UpdateRequest update) {
        logger.debug("Sending to {} query: \n{}", sparqlEndpoint, update.toString());
        UpdateProcessor processor = UpdateExecutionFactory.createRemote(update, sparqlEndpoint.toString());
        try {
            processor.execute();
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    public int countGraphTriples(URL sparqlEndPoint, String graph) {
        Query countTriples = Queries.getQueryCountTriples();
        countTriples.addGraphURI(graph);
        ResultSet result = querySelect(sparqlEndPoint, countTriples);
        int count = -1;
        while (result.hasNext()) {
            QuerySolution soln = result.nextSolution();
            if (soln.contains("?n"))
                count = soln.getLiteral("?n").getInt();
        }
        return count;
    }

    public void loadOntology(URL sparqlEndpoint, String graph, String uri) {
        Model model = RDFDataMgr.loadModel(uri);
        loadModel(sparqlEndpoint, graph, model);
    }

    public void loadModel(URL sparqlEndpoint, String graph, Model model) {
        StringWriter out = new StringWriter();
        RDFDataMgr.write(out, model, Lang.NTRIPLES);
        String insertString = "INSERT DATA { GRAPH <" + graph + "> { " + out.toString() + " } } ";
        UpdateRequest update = UpdateFactory.create(insertString);
        logger.debug("Sending to {} query: \n{}", sparqlEndpoint, update.toString());
        UpdateProcessor processor = UpdateExecutionFactory.createRemote(update, sparqlEndpoint.toString());
        try {
            processor.execute();
        } catch (HttpException e) {
            logger.error(e.getMessage());
        }
    }

    public void clearGraph(URL sparqlEndPoint, URI pondOntologiesGraph) {
        UpdateRequest clearGraph = Queries.getClearGraph(pondOntologiesGraph.toString());
        queryUpdate(sparqlEndPoint, clearGraph);
    }

    public void inferTypes(Pond pond) {
        List<String> targetGraphs = pond.getPondGraphs();
        targetGraphs.add(pond.getPondOntologiesGraph().toString());
        UpdateRequest createGraph = Queries.getCreateGraph(pond.getPondInferenceGraph().toString());
        queryUpdate(pond.getSparqlEndPoint(), createGraph);
        UpdateRequest update = Queries.getUpdateInferTypes(targetGraphs, pond.getPondInferenceGraph().toString());
        queryUpdate(pond.getSparqlEndPoint(), update);
    }

    public void inferTypesConstruct(Pond pond) {
        UpdateRequest createGraph = Queries.getCreateGraph(pond.getPondInferenceGraph().toString());
        queryUpdate(pond.getSparqlEndPoint(), createGraph);
        List<String> targetGraphs = pond.getPondGraphs();
        targetGraphs.add(pond.getPondOntologiesGraph().toString());
        Model inferredModel = queryConstruct(pond.getSparqlEndPoint(), Queries.getQueryInferTypes(), targetGraphs);
        /*File inferenceOut = new File(pond.getId() + "-inference.ttl");
        try {
            RDFDataMgr.write(new FileOutputStream(inferenceOut), inferredModel, Lang.TURTLE);
        } catch (FileNotFoundException e) {
            logger.error(e.getMessage());
        }*/
        loadModel(pond.getSparqlEndPoint(), pond.getPondInferenceGraph().toString(), inferredModel);
    }
}