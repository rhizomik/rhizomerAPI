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
import net.rhizomik.rhizomer.repository.SPARQLEndPointRepository;
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
import org.springframework.beans.factory.annotation.Autowired;

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

        when(mock.querySelect(any(URL.class), any(Query.class), any()))
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

        when(mock.queryConstruct(any(URL.class), any(Query.class), anyList(), any()))
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
        }).when(mock).queryUpdate(any(URL.class), any(UpdateRequest.class), any());

        when(mock.countGraphTriples(any(URL.class), anyString(), any()))
                .thenAnswer(invocationOnMock -> {
                    String graph = invocationOnMock.getArgument(1);
                    if (dataset.containsNamedModel(graph))
                        return dataset.getNamedModel(graph).size();
                    else
                        return 0L;
                });

        doAnswer(invocationOnMock -> {
            URL sparqlEndPoint = invocationOnMock.getArgument(0);
            SPARQLEndPoint.ServerType type = invocationOnMock.getArgument(1);
            String graph = invocationOnMock.getArgument(2);
            String uri = invocationOnMock.getArgument(3);
            Model model = RDFDataMgr.loadModel(uri);
            mock.loadModel(sparqlEndPoint, type, graph, model, null);
            return null;
        }).when(mock).loadURI(any(URL.class), any(SPARQLEndPoint.ServerType.class), anyString(), anyString(), any());

        doAnswer(invocationOnMock -> {
            String graph = invocationOnMock.getArgument(2);
            Model model = invocationOnMock.getArgument(3);
            dataset.addNamedModel(graph, model);
            return null;
        }).when(mock).loadModel(any(URL.class), any(), anyString(), any(Model.class), any());

        doAnswer(invocationOnMock -> {
            String graph = invocationOnMock.getArgument(1);
            Model blankModel = ModelFactory.createDefaultModel();
            dataset.replaceNamedModel(graph, blankModel);
            return null;
        }).when(mock).clearGraph(any(URL.class), anyString(), any());

        doAnswer(invocationOnMock -> {
            Dataset dataset = invocationOnMock.getArgument(0);
            SPARQLEndPoint endPoint = invocationOnMock.getArgument(1);
            List<String> targetGraphs = endPoint.getGraphs();
            targetGraphs.add(dataset.getDatasetOntologiesGraph().toString());
            UpdateRequest update = queries.getUpdateInferTypes(targetGraphs, dataset.getDatasetInferenceGraph().toString());
            mock.queryUpdate(endPoint.getQueryEndPoint(), update, null);
            return null;
        }).when(mock).inferTypes(any(Dataset.class), any(SPARQLEndPoint.class), any());

        return mock;
    }
}
