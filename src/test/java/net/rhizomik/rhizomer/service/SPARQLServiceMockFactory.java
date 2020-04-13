package net.rhizomik.rhizomer.service;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import java.net.URL;
import java.util.List;
import net.rhizomik.rhizomer.model.Dataset;
import net.rhizomik.rhizomer.model.SPARQLEndPoint;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.update.UpdateAction;
import org.apache.jena.update.UpdateRequest;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by http://rhizomik.net/~roberto/
 */
public class SPARQLServiceMockFactory {
    private static final Logger logger = LoggerFactory.getLogger(SPARQLServiceMockFactory.class);

    private static org.apache.jena.query.Dataset dataset = DatasetFactory.create();

    public static void addData(String graph, String dataFile) {
        Model model = RDFDataMgr.loadModel(dataFile);
        dataset.addNamedModel(graph, model);
    }

    public static void clearDataset() {
        dataset = DatasetFactory.create();
    }

    public static SPARQLService build() {

        Queries queries = new OptimizedQueries();
        SPARQLService mock = Mockito.mock(SPARQLService.class);

        when(mock.querySelect(any(URL.class), any(Query.class)))
                .thenAnswer(invocationOnMock -> {
                    Query query = invocationOnMock.getArgument(1);
                    logger.info("Sending to {} query: \n{}", "mockServer", query);
                    QueryExecution qexec = QueryExecutionFactory.create(query, dataset);
                    return qexec.execSelect();
                });

        when(mock.querySelect(any(URL.class), any(Query.class), anyList(), any()))
                .thenAnswer(invocationOnMock -> {
                    Query query = invocationOnMock.getArgument(1);
                    List<String> graphs = invocationOnMock.getArgument(2);
                    graphs.forEach(query::addGraphURI);
                    logger.info("Sending to {} query: \n{}", "mockServer", query);
                    QueryExecution qexec = QueryExecutionFactory.create(query, dataset);
                    return qexec.execSelect();
                });

        when(mock.queryConstruct(any(URL.class), any(Query.class), anyList()))
                .thenAnswer(invocationOnMock -> {
                    Query query = invocationOnMock.getArgument(1);
                    List<String> graphs = invocationOnMock.getArgument(2);
                    Model queryDataset = ModelFactory.createDefaultModel();
                    graphs.forEach(graph -> queryDataset.add(dataset.getNamedModel(graph)));
                    graphs.forEach(query::addGraphURI);
                    logger.info("Sending to {} query: \n{}", "mockServer", query);
                    QueryExecution qexec = QueryExecutionFactory.create(query, queryDataset);
                    return qexec.execConstruct();
                });

        doAnswer(invocationOnMock -> {
            UpdateRequest update = (UpdateRequest) invocationOnMock.getArguments()[1];
            logger.debug("Sending to {} query: \n{}", "mockServer", update.toString());
            UpdateAction.execute(update, dataset);
            return null;
        }).when(mock).queryUpdate(any(URL.class), any(UpdateRequest.class), any(), any());

        when(mock.countGraphTriples(any(URL.class), anyString()))
                .thenAnswer(invocationOnMock -> {
                    String graph = invocationOnMock.getArgument(1);
                    if (dataset.containsNamedModel(graph))
                        return dataset.getNamedModel(graph).size();
                    else
                        return 0L;
                });

        doAnswer(invocationOnMock -> {
            URL sparqlEndPoint = invocationOnMock.getArgument(0);
            String graph = invocationOnMock.getArgument(1);
            String uri = invocationOnMock.getArgument(2);
            String username = invocationOnMock.getArgument(3);
            String password = invocationOnMock.getArgument(4);
            Model model = RDFDataMgr.loadModel(uri);
            mock.loadModel(sparqlEndPoint, graph, model, username, password);
            return null;
        }).when(mock).loadURI(any(URL.class), anyString(), anyString(), any(), any());

        doAnswer(invocationOnMock -> {
            String graph = invocationOnMock.getArgument(1);
            Model model = invocationOnMock.getArgument(2);
            dataset.addNamedModel(graph, model);
            return null;
        }).when(mock).loadModel(any(URL.class), anyString(), any(Model.class), any(), any());

        doAnswer(invocationOnMock -> {
            String graph = invocationOnMock.getArgument(1);
            Model blankModel = ModelFactory.createDefaultModel();
            dataset.replaceNamedModel(graph, blankModel);
            return null;
        }).when(mock).clearGraph(any(URL.class), anyString(), any(), any());

        doAnswer(invocationOnMock -> {
            Dataset dataset = invocationOnMock.getArgument(0);
            SPARQLEndPoint endPoint = dataset.getEndPoints().get(0); // Just get the first one
            List<String> targetGraphs = endPoint.getGraphs();
            targetGraphs.add(dataset.getDatasetOntologiesGraph().toString());
            UpdateRequest createGraph = queries.getCreateGraph(dataset.getDatasetInferenceGraph().toString());
            mock.queryUpdate(endPoint.getQueryEndPoint(), createGraph, null, null);
            UpdateRequest update = queries.getUpdateInferTypes(targetGraphs, dataset.getDatasetInferenceGraph().toString());
            mock.queryUpdate(endPoint.getQueryEndPoint(), update, null, null);
            return null;
        }).when(mock).inferTypes(any(Dataset.class));

        return mock;
    }
}
