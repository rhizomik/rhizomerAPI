package net.rhizomik.rhizomer.service;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.update.UpdateAction;
import com.hp.hpl.jena.update.UpdateRequest;
import net.rhizomik.rhizomer.model.Pond;
import net.rhizomik.rhizomer.model.Queries;
import org.apache.jena.riot.RDFDataMgr;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URL;
import java.util.List;

import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.*;


/**
 * Created by http://rhizomik.net/~roberto/
 */
public class SPARQLServiceMockFactory {
    private static final Logger logger = LoggerFactory.getLogger(SPARQLServiceMockFactory.class);

    private static Dataset dataset = DatasetFactory.createMem();

    public static void addData(String graph, String dataFile) {
        Model model = RDFDataMgr.loadModel(dataFile);
        dataset.addNamedModel(graph, model);
    }

    public static SPARQLService build() {
        SPARQLService mock = Mockito.mock(SPARQLService.class);

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
                        return -1;
                });

        doAnswer(invocationOnMock -> {
            URL sparqlEndPoint = (URL) invocationOnMock.getArguments()[0];
            String graph = (String) invocationOnMock.getArguments()[1];
            String uri = (String) invocationOnMock.getArguments()[2];
            Model model = RDFDataMgr.loadModel(uri);
            mock.loadModel(sparqlEndPoint, graph, model);
            return null;
        }).when(mock).loadOntology(isA(URL.class), anyString(), anyString());

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
            dataset.addNamedModel(graph.toString(), blankModel);
            return null;
        }).when(mock).clearGraph(isA(URL.class), isA(URI.class));

        doAnswer(invocationOnMock -> {
            Pond pond = (Pond) invocationOnMock.getArguments()[0];
            List<String> targetGraphs = pond.getPondGraphs();
            targetGraphs.add(pond.getPondOntologiesGraph().toString());
            UpdateRequest createGraph = Queries.getCreateGraph(pond.getPondInferenceGraph().toString());
            mock.queryUpdate(pond.getSparqlEndPoint(), createGraph);
            UpdateRequest update = Queries.getUpdateInferTypes(targetGraphs, pond.getPondInferenceGraph().toString());
            mock.queryUpdate(pond.getSparqlEndPoint(), update);
            return null;
        }).when(mock).inferTypes(isA(Pond.class));

        return mock;
    }
}
