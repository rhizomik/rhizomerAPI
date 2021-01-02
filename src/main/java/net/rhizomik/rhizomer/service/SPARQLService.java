package net.rhizomik.rhizomer.service;

import net.rhizomik.rhizomer.model.Dataset;
import net.rhizomik.rhizomer.model.SPARQLEndPoint;
import net.rhizomik.rhizomer.repository.SPARQLEndPointRepository;
import org.apache.http.client.HttpClient;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;
import org.apache.jena.update.UpdateExecutionFactory;
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
        return this.querySelect(sparqlEndpoint, timeout, query, new ArrayList<>(), creds);
    }

    public ResultSet querySelect(URL sparqlEndpoint, String timeout, Query query, List<String> graphs, HttpClient creds) {
        graphs.forEach(query::addGraphURI);
        logger.info("Sending to {} query: \n{}", sparqlEndpoint, query);
        QueryExecution q = QueryExecutionFactory.sparqlService(sparqlEndpoint.toString(), query, graphs, new ArrayList<>(), creds);
        if (timeout != null)
            ((QueryEngineHTTP) q).addParam("timeout", timeout);
        ResultSet result = ResultSetFactory.copyResults(q.execSelect());
        q.close();
        return result;
    }

    public Model queryDescribe(SPARQLEndPoint endpoint, String timeout, Query query, List<String> graphs, HttpClient creds) {
        graphs.forEach(query::addGraphURI);
        String queryString = query.toString();
        if (endpoint.getType() == SPARQLEndPoint.ServerType.VIRTUOSO) {
            queryString = "DEFINE sql:describe-mode \"CBD\" \n" + queryString;
        } else if (endpoint.getType() == SPARQLEndPoint.ServerType.STARDOG) {
            queryString = "#pragma describe.strategy cbd \n" + queryString;
        }
        logger.info("Sending to {} query: \n{}", endpoint.getQueryEndPoint(), queryString);
        QueryExecution q = new QueryEngineHTTP(endpoint.getQueryEndPoint().toString(), queryString, creds);
        if (timeout != null)
            ((QueryEngineHTTP) q).addParam("timeout", timeout);
        else
            ((QueryEngineHTTP) q).setAcceptHeader("application/n-triples"); // Workaround for MarkLogic
        return q.execDescribe();
    }

    public Model queryConstruct(URL sparqlEndpoint, String timeout, Query query, List<String> graphs, HttpClient creds) {
        graphs.forEach(query::addGraphURI);
        logger.info("Sending to {} query: \n{}", sparqlEndpoint, query);
        QueryExecution q = QueryExecutionFactory.sparqlService(sparqlEndpoint.toString(), query, graphs, new ArrayList<>(), creds);
        if (timeout != null)
            ((QueryEngineHTTP) q).addParam("timeout", timeout);
        else
            ((QueryEngineHTTP) q).setAcceptHeader("application/n-triples"); // Workaround for MarkLogic
        return q.execConstruct();
    }

    public void queryUpdate(URL sparqlEndpoint, UpdateRequest update, HttpClient creds) {
        logger.info("Sending to {} query: \n{}", sparqlEndpoint, update.toString());
        UpdateProcessor processor = UpdateExecutionFactory.createRemoteForm(update, sparqlEndpoint.toString(), creds);
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

    public void inferTypes(Dataset dataset, SPARQLEndPoint endPoint, HttpClient creds) {
        if (endPoint.isWritable()) {
            List<String> targetGraphs = endPoint.getGraphs();
            targetGraphs.add(dataset.getDatasetOntologiesGraph().toString());
            UpdateRequest update = queries
                    .getUpdateInferTypes(targetGraphs, dataset.getDatasetInferenceGraph().toString());
            queryUpdate(endPoint.getUpdateEndPoint(), update, creds);
        }
    }

    public void inferTypesConstruct(Dataset dataset, String timeout, SPARQLEndPoint endPoint, HttpClient creds) {
        if(endPoint.isWritable()) {
            List<String> targetGraphs = endPoint.getGraphs();
            targetGraphs.add(dataset.getDatasetOntologiesGraph().toString());
            UpdateRequest createGraph = queries.getCreateGraph(dataset.getDatasetInferenceGraph().toString());
            queryUpdate(endPoint.getUpdateEndPoint(), createGraph, creds);
            Model inferredModel = queryConstruct(
                    endPoint.getUpdateEndPoint(), timeout, queries.getQueryInferTypes(), targetGraphs, creds);
            /*File inferenceOut = new File(dataset.getId() + "-inference.ttl");
            try {
                RDFDataMgr.write(new FileOutputStream(inferenceOut), inferredModel, Lang.TURTLE);
            } catch (FileNotFoundException e) {
                logger.error(e.getMessage());
            }*/
            loadModel(endPoint.getUpdateEndPoint(), endPoint.getType(), dataset.getDatasetInferenceGraph().toString(),
                    inferredModel, creds);
        }
    }
}
