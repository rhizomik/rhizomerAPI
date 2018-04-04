package net.rhizomik.rhizomer.service;

import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.URL;
import java.util.List;
import net.rhizomik.rhizomer.model.Dataset;
import net.rhizomik.rhizomer.model.Queries;
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
        SPARQLService mock = Mockito.mock(SPARQLService.class);

        when(mock.querySelect(isA(URL.class), isA(Query.class)))
                .thenAnswer(invocationOnMock -> {
                    Query query = (Query) invocationOnMock.getArguments()[1];
                    logger.info("Sending to {} query: \n{}", "mockServer", query);
                    QueryExecution qexec = QueryExecutionFactory.create(query, dataset);
                    return qexec.execSelect();
                });

        when(mock.querySelect(isA(URL.class), isA(Query.class), anyList(), anyList()))
                .thenAnswer(invocationOnMock -> {
                    Query query = (Query) invocationOnMock.getArguments()[1];
                    List<String> graphs = (List<String>) invocationOnMock.getArguments()[2];
                    Model queryDataset = ModelFactory.createDefaultModel();
                    graphs.forEach(graph -> queryDataset.add(dataset.getNamedModel(graph)));
                    graphs.forEach(query::addGraphURI);
                    logger.info("Sending to {} query: \n{}", "mockServer", query);
                    QueryExecution qexec = QueryExecutionFactory.create(query, queryDataset);
                    return qexec.execSelect();
                });

        when(mock.queryConstruct(isA(URL.class), isA(Query.class), anyList()))
                .thenAnswer(invocationOnMock -> {
                    Query query = (Query) invocationOnMock.getArguments()[1];
                    List<String> graphs = (List<String>) invocationOnMock.getArguments()[2];
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
        }).when(mock).queryUpdate(isA(URL.class), any(UpdateRequest.class));

        when(mock.countGraphTriples(isA(URL.class), anyString()))
                .thenAnswer(invocationOnMock -> {
                    String graph = (String) invocationOnMock.getArguments()[1];
                    if (dataset.containsNamedModel(graph))
                        return dataset.getNamedModel(graph).size();
                    else
                        return 0;
                });

        doAnswer(invocationOnMock -> {
            URL sparqlEndPoint = (URL) invocationOnMock.getArguments()[0];
            String graph = (String) invocationOnMock.getArguments()[1];
            String uri = (String) invocationOnMock.getArguments()[2];
            Model model = RDFDataMgr.loadModel(uri);
            mock.loadModel(sparqlEndPoint, graph, model);
            return null;
        }).when(mock).loadData(isA(URL.class), anyString(), anyString());

        doAnswer(invocationOnMock -> {
            String graph = (String) invocationOnMock.getArguments()[1];
            Model model = (Model) invocationOnMock.getArguments()[2];
            if (dataset.containsNamedModel(graph))
                model.add(dataset.getNamedModel(graph));
            dataset.addNamedModel(graph, model);
            return null;
        }).when(mock).loadModel(isA(URL.class), anyString(), any(Model.class));

        doAnswer(invocationOnMock -> {
            URI graph = (URI) invocationOnMock.getArguments()[1];
            Model blankModel = ModelFactory.createDefaultModel();
            dataset.replaceNamedModel(graph.toString(), blankModel);
            return null;
        }).when(mock).clearGraph(isA(URL.class), isA(URI.class));

        doAnswer(invocationOnMock -> {
            Dataset dataset = (Dataset) invocationOnMock.getArguments()[0];
            List<String> targetGraphs = dataset.getDatasetGraphs();
            targetGraphs.add(dataset.getDatasetOntologiesGraph().toString());
            UpdateRequest createGraph = Queries.getCreateGraph(dataset.getDatasetInferenceGraph().toString());
            mock.queryUpdate(dataset.getSparqlEndPoint(), createGraph);
            UpdateRequest update = Queries.getUpdateInferTypes(targetGraphs, dataset.getDatasetInferenceGraph().toString());
            mock.queryUpdate(dataset.getSparqlEndPoint(), update);
            return null;
        }).when(mock).inferTypes(isA(Dataset.class));

        return mock;
    }
}
