package net.rhizomik.rhizomer.service;

import java.io.StringWriter;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import net.rhizomik.rhizomer.model.Dataset;
import net.rhizomik.rhizomer.model.Queries;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClients;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Created by http://rhizomik.net/~roberto/
 */
@Service
public class SPARQLService {
    private static final Logger logger = LoggerFactory.getLogger(SPARQLService.class);

    public ResultSet querySelect(URL sparqlEndpoint, Query query) {
        return this.querySelect(sparqlEndpoint, query, new ArrayList<>(), new ArrayList<>());
    }

    public ResultSet querySelect(URL sparqlEndpoint, Query query, List<String> graphs, List<String> ontologies) {
        graphs.forEach(query::addGraphURI);
        logger.info("Sending to {} query: \n{}", sparqlEndpoint, query);
        QueryExecution q = QueryExecutionFactory.sparqlService(sparqlEndpoint.toString(), query, graphs, ontologies);
        return ResultSetFactory.copyResults(q.execSelect());
    }

    public Model queryConstruct(URL sparqlEndpoint, Query query, List<String> graphs) {
        graphs.forEach(query::addGraphURI);
        logger.info("Sending to {} query: \n{}", sparqlEndpoint, query);
        QueryExecution q = QueryExecutionFactory.sparqlService(sparqlEndpoint.toString(), query, graphs, null);
        return q.execConstruct();
    }

    public void queryUpdate(URL sparqlEndpoint, UpdateRequest update) {
        logger.info("Sending to {} query: \n{}", sparqlEndpoint, update.toString());
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
        int count = 0;
        while (result.hasNext()) {
            QuerySolution soln = result.nextSolution();
            if (soln.contains("?n"))
                count = soln.getLiteral("?n").getInt();
        }
        return count;
    }

    public void loadURI(URL sparqlEndpoint, String graph, String uri, String username, String password) {
        Model model = RDFDataMgr.loadModel(uri);
        loadModel(sparqlEndpoint, graph, model, username, password);
    }

    public void loadModel(URL sparqlEndpoint, String graph, Model model, String username, String password) {
        StringWriter out = new StringWriter();
        RDFDataMgr.write(out, model, Lang.NTRIPLES);
        String insertString = "INSERT DATA { GRAPH <" + graph + "> { " + out.toString() + " } } ";
        UpdateRequest update = UpdateFactory.create(insertString);
        logger.debug("Sending to {} query: \n{}", sparqlEndpoint, update.toString());
        UpdateProcessor processor;
        if (username != null || !username.isEmpty())
            processor = UpdateExecutionFactory.createRemote(
                update, sparqlEndpoint.toString(), withCreds(username, password));
        else
            processor = UpdateExecutionFactory.createRemote(
                update, sparqlEndpoint.toString());
        processor.execute();
    }

    public void clearGraph(URL sparqlEndPoint, URI datasetOntologiesGraph) {
        UpdateRequest clearGraph = Queries.getClearGraph(datasetOntologiesGraph.toString());
        queryUpdate(sparqlEndPoint, clearGraph);
    }

    public void inferTypes(Dataset dataset) {
        List<String> targetGraphs = dataset.getDatasetGraphs();
        targetGraphs.add(dataset.getDatasetOntologiesGraph().toString());
        UpdateRequest createGraph = Queries.getCreateGraph(dataset.getDatasetInferenceGraph().toString());
        queryUpdate(dataset.getSparqlEndPoint(), createGraph);
        UpdateRequest update = Queries.getUpdateInferTypes(targetGraphs, dataset.getDatasetInferenceGraph().toString());
        queryUpdate(dataset.getSparqlEndPoint(), update);
    }

    public void inferTypesConstruct(net.rhizomik.rhizomer.model.Dataset dataset) {
        UpdateRequest createGraph = Queries.getCreateGraph(dataset.getDatasetInferenceGraph().toString());
        queryUpdate(dataset.getSparqlEndPoint(), createGraph);
        List<String> targetGraphs = dataset.getDatasetGraphs();
        targetGraphs.add(dataset.getDatasetOntologiesGraph().toString());
        Model inferredModel = queryConstruct(dataset.getSparqlEndPoint(), Queries.getQueryInferTypes(), targetGraphs);
        /*File inferenceOut = new File(dataset.getId() + "-inference.ttl");
        try {
            RDFDataMgr.write(new FileOutputStream(inferenceOut), inferredModel, Lang.TURTLE);
        } catch (FileNotFoundException e) {
            logger.error(e.getMessage());
        }*/
        loadModel(dataset.getSparqlEndPoint(), dataset.getDatasetInferenceGraph().toString(),
            inferredModel, dataset.getUsername(), dataset.getPassword());
    }

    private static HttpClient withCreds(String uname, String password) {
        BasicCredentialsProvider credsProv = new BasicCredentialsProvider();
        credsProv.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(uname, password));
        return HttpClients.custom().setDefaultCredentialsProvider(credsProv).build();
    }
}