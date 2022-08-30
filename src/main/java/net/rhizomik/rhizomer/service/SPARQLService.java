package net.rhizomik.rhizomer.service;

import net.rhizomik.rhizomer.model.Dataset;
import net.rhizomik.rhizomer.model.SPARQLEndPoint;
import net.rhizomik.rhizomer.repository.SPARQLEndPointRepository;

import java.net.URI;
import java.net.http.HttpClient;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.exec.http.QueryExecutionHTTP;
import org.apache.jena.sparql.exec.http.QueryExecutionHTTPBuilder;
import org.apache.jena.sparql.exec.http.UpdateExecutionHTTPBuilder;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by http://rhizomik.net/~roberto/
 */
@Service
public class SPARQLService {
    private static final Logger logger = LoggerFactory.getLogger(SPARQLService.class);

    @Autowired SPARQLEndPointRepository endPointRepository;
    @Autowired Queries queries;

    public ResultSet querySelect(URL sparqlEndpoint, String timeout, Query query, HttpClient creds) {
        return this.querySelect(sparqlEndpoint, timeout, query, new ArrayList<>(), new ArrayList<>(), creds);
    }

    public ResultSet querySelect(URL sparqlEndpoint, String timeout, Query query, List<String> graphs, HttpClient creds) {
        return this.querySelect(sparqlEndpoint, timeout, query, graphs, new ArrayList<>(), creds);
    }

    public ResultSet querySelect(URL sparqlEndpoint, String timeout, Query query, List<String> graphs,
                                 List<String> namedGraphs, HttpClient creds) {
        graphs.forEach(query::addGraphURI);
        namedGraphs.forEach(query::addNamedGraphURI);
        logger.info("Sending to {} query: \n{}", sparqlEndpoint, query);
        QueryExecutionHTTPBuilder qBuilder = QueryExecutionHTTPBuilder.create();
        qBuilder.query(query).endpoint(sparqlEndpoint.toString()).httpClient(creds);
        if (timeout != null)
            qBuilder.param("timeout", timeout);
        QueryExecutionHTTP qExec = qBuilder.build();
        ResultSet result = ResultSetFactory.copyResults(qExec.execSelect());
        qExec.close();
        return result;
    }

    public Model queryDescribe(SPARQLEndPoint endpoint, String timeout, Query query, List<String> graphs,
                               HttpClient creds) {
        graphs.forEach(query::addGraphURI);
        String queryString = query.toString();
        if (endpoint.getType() == SPARQLEndPoint.ServerType.VIRTUOSO) {
            queryString = "DEFINE sql:describe-mode \"CBD\" \n" + queryString;
        } else if (endpoint.getType() == SPARQLEndPoint.ServerType.STARDOG) {
            queryString = "#pragma describe.strategy cbd \n" + queryString;
        }
        logger.info("Sending to {} query: \n{}", endpoint.getQueryEndPoint(), queryString);
        QueryExecutionHTTPBuilder qBuilder = QueryExecutionHTTPBuilder.create();
        qBuilder.query(query).endpoint(endpoint.getQueryEndPoint().toString()).httpClient(creds);
        if (timeout != null)
            qBuilder.param("timeout", timeout);
        if (endpoint.getType() == SPARQLEndPoint.ServerType.MARKLOGIC)
            qBuilder.acceptHeader("application/n-triples"); // Workaround for MarkLogic
        return qBuilder.build().execDescribe();
    }

    public Model queryConstruct(SPARQLEndPoint endpoint, String timeout, Query query, List<String> graphs,
                                List<String> namedGraphs, HttpClient creds) {
        graphs.forEach(query::addGraphURI);
        namedGraphs.forEach(query::addNamedGraphURI);
        logger.info("Sending to {} query: \n{}", endpoint.getQueryEndPoint(), query);
        QueryExecutionHTTPBuilder qBuilder = QueryExecutionHTTPBuilder.create();
        qBuilder.query(query).endpoint(endpoint.getQueryEndPoint().toString()).httpClient(creds);
        if (timeout != null)
            qBuilder.param("timeout", timeout);
        if (endpoint.getType() == SPARQLEndPoint.ServerType.MARKLOGIC)
            qBuilder.acceptHeader("application/n-triples"); // Workaround for MarkLogic
        return qBuilder.build().execConstruct();
    }

    public void queryUpdate(URL sparqlEndpoint, UpdateRequest update, HttpClient creds) {
        logger.info("Sending to {} query: \n{}", sparqlEndpoint, update.toString());
        UpdateExecutionHTTPBuilder uBuilder = UpdateExecutionHTTPBuilder.create();
        uBuilder.update(update).endpoint(sparqlEndpoint.toString()).httpClient(creds);
        UpdateProcessor processor = uBuilder.build();
        processor.execute();
    }

    public long countGraphTriples(URL sparqlEndPoint, String timeout, String graph, HttpClient creds) {
        Query countTriples = queries.getQueryCountTriples();
        countTriples.addGraphURI(graph);
        ResultSet result = querySelect(sparqlEndPoint, timeout, countTriples, creds);
        long count = 0;
        while (result.hasNext()) {
            QuerySolution soln = result.nextSolution();
            if (soln.contains("?n"))
                count = soln.getLiteral("?n").getLong();
        }
        return count;
    }

    public void loadURI(URL sparqlEndpoint, SPARQLEndPoint.ServerType endPointType, String graph, String uri, HttpClient creds) {
        Model model = RDFDataMgr.loadModel(uri);
        loadModel(sparqlEndpoint, endPointType, graph, model, creds);
    }

    public void loadModel(URL sparqlEndPoint, SPARQLEndPoint.ServerType endPointType, String graph, Model model, HttpClient creds) {
        StringWriter out = new StringWriter();
        RDFDataMgr.write(out, model, Lang.NTRIPLES);
        queryUpdate(sparqlEndPoint, queries.getInsertData(endPointType, graph, out.toString()), creds);
    }

    public void clearGraph(URL sparqlEndPoint, String graph, HttpClient creds) {
        UpdateRequest clearGraph = queries.getClearGraph(graph);
        queryUpdate(sparqlEndPoint, clearGraph, creds);
    }

    public void dropGraph(URL sparqlEndPoint, String graph, HttpClient creds) {
        UpdateRequest clearGraph = queries.getDropGraph(graph);
        queryUpdate(sparqlEndPoint, clearGraph, creds);
    }

    public void inferTypes(String targetGraph, SPARQLEndPoint endPoint, HttpClient creds) {
        List<String> sourceGraphs = endPoint.getGraphs();
        sourceGraphs.addAll(endPoint.getOntologyGraphs());
        UpdateRequest update = queries.getUpdateInferTypes(sourceGraphs, targetGraph);
        queryUpdate(endPoint.getUpdateEndPoint(), update, creds);
    }
}
