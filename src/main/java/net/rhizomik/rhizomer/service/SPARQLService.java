package net.rhizomik.rhizomer.service;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.update.UpdateExecutionFactory;
import com.hp.hpl.jena.update.UpdateFactory;
import com.hp.hpl.jena.update.UpdateProcessor;
import com.hp.hpl.jena.update.UpdateRequest;
import net.rhizomik.rhizomer.model.Pond;
import net.rhizomik.rhizomer.model.Queries;
import net.rhizomik.rhizomer.model.Server;
import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.StringWriter;
import java.util.List;

/**
 * Created by http://rhizomik.net/~roberto/
 */
@Service
public class SPARQLService {
    private static final Logger logger = LoggerFactory.getLogger(SPARQLService.class);

    public ResultSet querySelect(Server server, Query query) {
        return this.querySelect(server, query, null, null);
    }

    public ResultSet querySelect(Server server, Query query, List<String> graphs, List<String> ontologies) {
        graphs.forEach(query::addGraphURI);
        logger.debug("Sending to {} query: \n{}", server.getEndpoint(), query);
        QueryExecution q = QueryExecutionFactory.sparqlService(server.getEndpoint(), query, graphs, ontologies);
        return ResultSetFactory.copyResults(q.execSelect());
    }

    public Model queryConstruct(Server server, Query query, List<String> graphs) {
        graphs.forEach(query::addGraphURI);
        logger.debug("Sending to {} query: \n{}", server.getEndpoint(), query);
        QueryExecution q = QueryExecutionFactory.sparqlService(server.getEndpoint(), query, graphs, null);
        return q.execConstruct();
    }

    public void queryUpdate(Server server, UpdateRequest update) {
        logger.debug("Sending to {} query: \n{}", server.getEndpoint(), update.toString());
        UpdateProcessor processor = UpdateExecutionFactory.createRemote(update, server.getEndpoint());
        try {
            processor.execute();
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    public void loadOntology(Server server, String graph, String uri) {
        Model model = RDFDataMgr.loadModel(uri);
        loadModel(server, graph, model);
    }

    public void loadModel(Server server, String graph, Model model) {
        StringWriter out = new StringWriter();
        RDFDataMgr.write(out, model, Lang.NTRIPLES);
        String insertString = "INSERT DATA { GRAPH <" + graph + "> { " + out.toString() + " } } ";
        UpdateRequest update = UpdateFactory.create(insertString);
        logger.debug("Sending to {} query: \n{}", server.getEndpoint(), update.toString());
        UpdateProcessor processor = UpdateExecutionFactory.createRemote(update, server.getEndpoint());
        try {
            processor.execute();
        } catch (HttpException e) {
            logger.error(e.getMessage());
        }
    }

    public void inferTypes(Pond pond) {
        List<String> targetGraphs = pond.getPondGraphsStrings();
        targetGraphs.add(pond.getPondOntologiesGraph().toString());
        UpdateRequest createGraph = Queries.getCreateGraph(pond.getPondInferenceGraph().toString());
        queryUpdate(pond.getServer(), createGraph);
        UpdateRequest update = Queries.getUpdateInferTypes(targetGraphs, pond.getPondInferenceGraph().toString());
        queryUpdate(pond.getServer(), update);
    }

    public void inferTypesConstruct(Pond pond) {
        UpdateRequest createGraph = Queries.getCreateGraph(pond.getPondInferenceGraph().toString());
        queryUpdate(pond.getServer(), createGraph);
        List<String> targetGraphs = pond.getPondGraphsStrings();
        targetGraphs.add(pond.getPondOntologiesGraph().toString());
        Model inferredModel = queryConstruct(pond.getServer(), Queries.getQueryInferTypes(), targetGraphs);
        /*File inferenceOut = new File(pond.getId() + "-inference.ttl");
        try {
            RDFDataMgr.write(new FileOutputStream(inferenceOut), inferredModel, Lang.TURTLE);
        } catch (FileNotFoundException e) {
            logger.error(e.getMessage());
        }*/
        loadModel(pond.getServer(), pond.getPondInferenceGraph().toString(), inferredModel);
    }
}