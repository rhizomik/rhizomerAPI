package net.rhizomik.rhizomer.service;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.update.UpdateAction;
import com.hp.hpl.jena.update.UpdateRequest;
import net.rhizomik.rhizomer.model.Server;
import org.apache.jena.riot.RDFDataMgr;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

        when(mock.querySelect(isA(Server.class), isA(Query.class), anyList(), anyList()))
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

        when(mock.queryConstruct(isA(Server.class), isA(Query.class), anyList()))
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
        }).when(mock).queryUpdate(isA(Server.class), any(UpdateRequest.class));

        doAnswer(invocationOnMock -> {
            Server server = (Server) invocationOnMock.getArguments()[0];
            String graph = (String) invocationOnMock.getArguments()[1];
            String uri = (String) invocationOnMock.getArguments()[2];
            Model model = RDFDataMgr.loadModel(uri);
            mock.loadModel(server, graph, model);
            return null;
        }).when(mock).loadOntology(isA(Server.class), anyString(), anyString());

        doAnswer(invocationOnMock -> {
            String graph = (String) invocationOnMock.getArguments()[1];
            Model model = (Model) invocationOnMock.getArguments()[2];
            if (dataset.containsNamedModel(graph))
                model.add(dataset.getNamedModel(graph));
            dataset.addNamedModel(graph, model);
            return null;
        }).when(mock).loadModel(isA(Server.class), anyString(), any(Model.class));

        return mock;
    }
}
