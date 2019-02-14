package net.rhizomik.rhizomer.controller;

/**
 * Created by http://rhizomik.net/~roberto/
 */
import java.io.IOException;
import java.net.URI;
import java.util.List;
import net.rhizomik.rhizomer.model.Dataset;
import net.rhizomik.rhizomer.repository.DatasetRepository;
import net.rhizomik.rhizomer.service.AnalizeDataset;
import net.rhizomik.rhizomer.service.SPARQLService;
import net.rhizomik.rhizomer.service.SecurityController;
import org.apache.commons.lang3.Validate;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

@RepositoryRestController
public class ServerController {
    final Logger logger = LoggerFactory.getLogger(ServerController.class);

    @Autowired private DatasetRepository datasetRepository;
    @Autowired private AnalizeDataset analizeDataset;
    @Autowired private SPARQLService sparqlService;
    @Autowired private SecurityController securityController;

    @RequestMapping(value = "/datasets/{datasetId}/server/graphs", method = RequestMethod.GET)
    public @ResponseBody List<URI> retrieveServerGraphs(@PathVariable String datasetId,
        Authentication auth) {
        Dataset dataset = datasetRepository.findOne(datasetId);
        Validate.notNull(dataset, "Dataset with id '%s' not found", datasetId);
        securityController.checkOwner(dataset, auth);
        logger.info("Retrieve Dataset {} Server {} graphs", datasetId, dataset.getSparqlEndPoint().toString());
        return analizeDataset.listServerGraphs(dataset.getSparqlEndPoint());
    }

    @RequestMapping(value = "/datasets/{datasetId}/server", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody long storeData(ServletServerHttpRequest request, Authentication auth)
        throws IOException {
        UriComponents uriComponents = UriComponentsBuilder.fromHttpRequest(request).build();
        String datasetId = uriComponents.getPathSegments().get(1);
        Dataset dataset = datasetRepository.findOne(datasetId);
        Validate.notNull(dataset, "Dataset with id '%s' not found", datasetId);
        securityController.checkOwner(dataset, auth);
        String graph = uriComponents.getQueryParams().getFirst("graph");
        Validate.notEmpty(graph,
            "A destination graph should be defined in the request URL: /datasets/{datasetId}/server?graph={graphURI}");
        String contentType = request.getServletRequest().getContentType();
        Validate.notEmpty(contentType, "A Content-Type header should be defined corresponding to the input RDF data syntax");

        Model model = ModelFactory.createDefaultModel();
        RDFDataMgr.read(model, request.getBody(), graph, RDFLanguages.contentTypeToLang(contentType));
        logger.info("Loading {} triples into graph {} at server {}",
            model.size(), graph, dataset.getUpdateEndPoint());
        sparqlService.loadModel(
            dataset.getUpdateEndPoint(), graph, model, dataset.getUsername(), dataset.getPassword());
        dataset.addDatasetGraph(graph);
        datasetRepository.save(dataset);
        logger.info("By default added graph {} to dataset {}", graph, dataset.getId());
        return model.size();
    }
}
