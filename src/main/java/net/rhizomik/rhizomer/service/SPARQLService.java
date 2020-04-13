package net.rhizomik.rhizomer.service;

import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import net.rhizomik.rhizomer.model.Dataset;
import net.rhizomik.rhizomer.model.SPARQLEndPoint;
import net.rhizomik.rhizomer.repository.SPARQLEndPointRepository;
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
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Created by http://rhizomik.net/~roberto/
 */
@Service
public class SPARQLService {
    private static final Logger logger = LoggerFactory.getLogger(SPARQLService.class);

    @Value("${rhizomer.sparql-timeout:0}")
    private String TIMEOUT;

    @Autowired SPARQLEndPointRepository endPointRepository;
    @Autowired OptimizedQueries queries;

    public ResultSet querySelect(URL sparqlEndpoint, Query query) {
        return this.querySelect(sparqlEndpoint, query, new ArrayList<>(), new ArrayList<>());
    }

    public ResultSet querySelect(URL sparqlEndpoint, Query query, List<String> graphs, List<String> ontologies) {
        graphs.forEach(query::addGraphURI);
        logger.info("Sending to {} query: \n{}", sparqlEndpoint, query);
        QueryExecution q = QueryExecutionFactory.sparqlService(sparqlEndpoint.toString(), query, graphs, ontologies);
        ((QueryEngineHTTP) q).addParam("timeout", TIMEOUT);
        ResultSet result = ResultSetFactory.copyResults(q.execSelect());
        q.close();
        return result;
    }

    public Model queryDescribe(URL sparqlEndpoint, Query query, List<String> graphs) {
        graphs.forEach(query::addGraphURI);
        logger.info("Sending to {} query: \n{}", sparqlEndpoint, query);
        QueryExecution q = QueryExecutionFactory.sparqlService(sparqlEndpoint.toString(), query, graphs, null);
        ((QueryEngineHTTP) q).addParam("timeout", TIMEOUT);
        return q.execDescribe();
    }

    public Model queryConstruct(URL sparqlEndpoint, Query query, List<String> graphs) {
        graphs.forEach(query::addGraphURI);
        logger.info("Sending to {} query: \n{}", sparqlEndpoint, query);
        QueryExecution q = QueryExecutionFactory.sparqlService(sparqlEndpoint.toString(), query, graphs, null);
        ((QueryEngineHTTP) q).addParam("timeout", TIMEOUT);
        return q.execConstruct();
    }

    public void queryUpdate(URL sparqlEndpoint, UpdateRequest update, String username, String password) {
        logger.info("Sending to {} query: \n{}", sparqlEndpoint, update.toString());
        UpdateProcessor processor;
        if (username != null && !username.isEmpty())
            processor = UpdateExecutionFactory.createRemote(update, sparqlEndpoint.toString(), withCreds(username, password));
        else
            processor = UpdateExecutionFactory.createRemote(update, sparqlEndpoint.toString());
        processor.execute();
    }

    public long countGraphTriples(URL sparqlEndPoint, String graph) {
        Query countTriples = queries.getQueryCountTriples();
        countTriples.addGraphURI(graph);
        ResultSet result = querySelect(sparqlEndPoint, countTriples);
        long count = 0;
        while (result.hasNext()) {
            QuerySolution soln = result.nextSolution();
            if (soln.contains("?n"))
                count = soln.getLiteral("?n").getLong();
        }
        return count;
    }

    public void loadURI(URL sparqlEndpoint, String graph, String uri, String username, String password) {
        Model model = RDFDataMgr.loadModel(uri);
        loadModel(sparqlEndpoint, graph, model, username, password);
    }

    public void loadModel(URL sparqlEndPoint, String graph, Model model, String username, String password) {
        StringWriter out = new StringWriter();
        RDFDataMgr.write(out, model, Lang.NTRIPLES);
        // Original, changed to fix Virtuoso bug: https://github.com/openlink/virtuoso-opensource/issues/126
        // String insertString = "INSERT DATA { GRAPH <" + graph + "> { " + out.toString() + " } } ";
        String insertString = "INSERT { GRAPH <" + graph + "> { " + out.toString() + " } } WHERE { SELECT * {OPTIONAL {?s ?p ?o} } LIMIT 1 }";
        UpdateRequest update = UpdateFactory.create(insertString);
        queryUpdate(sparqlEndPoint, update, username, password);
    }

    public void clearGraph(URL sparqlEndPoint, String graph, String username, String password) {
        UpdateRequest clearGraph = queries.getClearGraph(graph);
        queryUpdate(sparqlEndPoint, clearGraph, username, password);
    }

    public void inferTypes(Dataset dataset) {
        endPointRepository.findByDataset(dataset).stream().filter(SPARQLEndPoint::isWritable).forEach(endPoint -> {
            List<String> targetGraphs = endPoint.getGraphs();
            targetGraphs.add(dataset.getDatasetOntologiesGraph().toString());
            UpdateRequest update = queries.getUpdateInferTypes(targetGraphs, dataset.getDatasetInferenceGraph().toString());
            queryUpdate(endPoint.getUpdateEndPoint(), update, endPoint.getUpdateUsername(), endPoint.getUpdatePassword());
        });
    }

    public void inferTypesConstruct(Dataset dataset) {
        endPointRepository.findByDataset(dataset).stream().filter(SPARQLEndPoint::isWritable).forEach(endPoint -> {
            List<String> targetGraphs = endPoint.getGraphs();
            targetGraphs.add(dataset.getDatasetOntologiesGraph().toString());
            UpdateRequest createGraph = queries.getCreateGraph(dataset.getDatasetInferenceGraph().toString());
            queryUpdate(endPoint.getUpdateEndPoint(), createGraph, endPoint.getUpdateUsername(), endPoint.getUpdatePassword());
            Model inferredModel = queryConstruct(endPoint.getUpdateEndPoint(), queries.getQueryInferTypes(), targetGraphs);
            /*File inferenceOut = new File(dataset.getId() + "-inference.ttl");
            try {
                RDFDataMgr.write(new FileOutputStream(inferenceOut), inferredModel, Lang.TURTLE);
            } catch (FileNotFoundException e) {
                logger.error(e.getMessage());
            }*/
            loadModel(endPoint.getUpdateEndPoint(), dataset.getDatasetInferenceGraph().toString(),
                    inferredModel, endPoint.getUpdateUsername(), endPoint.getUpdatePassword());
        });
    }

    private static HttpClient withCreds(String uname, String password) {
        BasicCredentialsProvider credsProv = new BasicCredentialsProvider();
        credsProv.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(uname, password));
        return HttpClients.custom().setDefaultCredentialsProvider(credsProv).build();
    }
}
