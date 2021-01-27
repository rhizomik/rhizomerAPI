package net.rhizomik.rhizomer.controller;

/**
 * Created by http://rhizomik.net/~roberto/
 */
import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

import net.rhizomik.rhizomer.model.Dataset;
import net.rhizomik.rhizomer.model.SPARQLEndPoint;
import net.rhizomik.rhizomer.repository.DatasetRepository;
import net.rhizomik.rhizomer.repository.SPARQLEndPointRepository;
import net.rhizomik.rhizomer.service.AnalizeDataset;
import net.rhizomik.rhizomer.service.SecurityController;
import org.apache.commons.lang3.Validate;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.validation.Valid;

@RepositoryRestController
public class EndPointController {
    final Logger logger = LoggerFactory.getLogger(EndPointController.class);

    @Value("${rhizomer.sparql-timeout:300000}")
    private String TIMEOUT;

    @Autowired private DatasetRepository datasetRepository;
    @Autowired private SPARQLEndPointRepository endPointRepository;
    @Autowired private AnalizeDataset analizeDataset;
    @Autowired private SecurityController securityController;


    @RequestMapping(value = "/datasets/{datasetId}/endpoints", method = RequestMethod.GET)
    public @ResponseBody List<SPARQLEndPoint> retrieveEndPoints(@PathVariable String datasetId, Authentication auth) {
        Dataset dataset = getDataset(datasetId);
        securityController.checkOwner(dataset, auth);
        logger.info("List Dataset {} endpoints", datasetId);
        return endPointRepository.findByDataset(dataset);
    }

    @RequestMapping(value = "/datasets/{datasetId}/endpoints", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    public @ResponseBody
    SPARQLEndPoint createEndPoint(@Valid @RequestBody SPARQLEndPoint endPoint, @PathVariable String datasetId,
                         Authentication auth) {
        Dataset dataset = getDataset(datasetId);
        securityController.checkOwner(dataset, auth);
        logger.info("Adding endpoint to dataset: {}", datasetId);
        endPoint.setDataset(dataset);
        if (endPoint.getTimeout() == null)
            endPoint.setTimeout(TIMEOUT);
        return endPointRepository.save(endPoint);
    }

    @RequestMapping(value = "/datasets/{datasetId}/endpoints/{endPointId}", method = RequestMethod.GET)
    public @ResponseBody SPARQLEndPoint retrieveEndPoint(@PathVariable String datasetId,
                                                         @PathVariable Integer endPointId, Authentication auth) {
        Dataset dataset = getDataset(datasetId);
        securityController.checkOwner(dataset, auth);
        SPARQLEndPoint endPoint = getServer(endPointId);
        logger.info("Retrieve Dataset {} endpoint {}", datasetId, endPoint.getQueryEndPoint());
        return endPoint;
    }

    @RequestMapping(value = "/datasets/{datasetId}/endpoints/{endPointId}", method = RequestMethod.PUT)
    public @ResponseBody SPARQLEndPoint updateEndPoint(@Valid @RequestBody SPARQLEndPoint updatedEndPoint,
                              @PathVariable Integer endPointId, @PathVariable String datasetId, Authentication auth) {
        Dataset dataset = getDataset(datasetId);
        securityController.checkOwner(dataset, auth);
        logger.info("Updating endpoint: {}", updatedEndPoint.getQueryEndPoint());
        updatedEndPoint.setDataset(dataset);
        if (updatedEndPoint.getTimeout() == null)
            updatedEndPoint.setTimeout(TIMEOUT);
        return endPointRepository.save(updatedEndPoint);
    }

    @RequestMapping(value = "/datasets/{datasetId}/endpoints/{endPointId}", method = RequestMethod.DELETE)
    public void deleteEndPoint(@PathVariable Integer endPointId, @PathVariable String datasetId, Authentication auth) {
        Dataset dataset = getDataset(datasetId);
        securityController.checkOwner(dataset, auth);
        SPARQLEndPoint endPoint = getServer(endPointId);
        logger.info("Deleting endpoint: {}", endPoint.getQueryEndPoint());
        endPointRepository.delete(endPoint);
    }

    @RequestMapping(value = "/datasets/{datasetId}/endpoints/{endPointId}/graphs",method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    public @ResponseBody List<String> addGraph(@RequestBody List<String> addGraphs, @PathVariable Integer endPointId,
                                              @PathVariable String datasetId, Authentication auth) {
        Dataset dataset = getDataset(datasetId);
        securityController.checkOwner(dataset, auth);
        SPARQLEndPoint endPoint = getServer(endPointId);
        logger.info("Add graphs {} to endpoint {}", addGraphs.toString(), endPoint.getQueryEndPoint());
        addGraphs.forEach(endPoint::addGraph);
        return endPointRepository.save(endPoint).getGraphs();
    }

    @RequestMapping(value = "/datasets/{datasetId}/endpoints/{endPointId}/graphs", method = RequestMethod.GET)
    public @ResponseBody List<String> listGraphs(@PathVariable String datasetId, @PathVariable Integer endPointId,
                                                 Authentication auth) {
        Dataset dataset = getDataset(datasetId);
        securityController.checkPublicOrOwner(dataset, auth);
        SPARQLEndPoint endPoint = getServer(endPointId);
        logger.info("Retrieved graphs for Dataset {} endpoint {}", datasetId, endPoint.getQueryEndPoint());
        return endPoint.getGraphs();
    }

    @RequestMapping(value = "/datasets/{datasetId}/endpoints/{endPointId}/graphs", method = RequestMethod.PUT)
    public @ResponseBody List<String> updateDGraphs(@Valid @RequestBody Set<String> updatedGraphs,
                            @PathVariable String datasetId, @PathVariable Integer endPointId, Authentication auth) {
        Dataset dataset = getDataset(datasetId);
        securityController.checkOwner(dataset, auth);
        SPARQLEndPoint endPoint = getServer(endPointId);
        logger.info("Updated endpoint {} graphs to {}", endPoint.getQueryEndPoint(), updatedGraphs.toString());
        endPoint.setGraphs(updatedGraphs);
        return endPointRepository.save(endPoint).getGraphs();
    }

    @RequestMapping(value = "/datasets/{datasetId}/endpoints/{endpointId}/server/graphs", method = RequestMethod.GET)
    public @ResponseBody List<URI> retrieveServerGraphs(@PathVariable String datasetId, @PathVariable Integer endpointId,
                                                        Authentication auth) {
        Dataset dataset = getDataset(datasetId);
        securityController.checkOwner(dataset, auth);
        SPARQLEndPoint endPoint = getServer(endpointId);
        logger.info("Retrieve Dataset {} endpoint {} graphs", datasetId, endPoint.getQueryEndPoint().toString());
        return analizeDataset.listServerGraphs(dataset, endPoint);
    }

    @RequestMapping(value = "/datasets/{datasetId}/endpoints/{endpointId}/server",
            method = { RequestMethod.POST, RequestMethod.PUT })
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody long storeData(ServletServerHttpRequest request, @PathVariable Integer endpointId,
                                        Authentication auth) throws IOException {
        UriComponents uriComponents = UriComponentsBuilder.fromHttpRequest(request).build();
        String datasetId = URLDecoder.decode(uriComponents.getPathSegments().get(1), StandardCharsets.UTF_8.toString());
        Dataset dataset = getDataset(datasetId);
        securityController.checkOwner(dataset, auth);
        SPARQLEndPoint endPoint = getServer(endpointId);
        Validate.isTrue(endPoint.isWritable(),"EndPoint '%s' is not writable", endPoint.getUpdateEndPoint());
        String graph = uriComponents.getQueryParams().getFirst("graph");
        Validate.notEmpty(graph,
            "A destination graph should be defined in the request URL: /datasets/{datasetId}/server?graph={graphURI}");
        String contentType = request.getServletRequest().getContentType();
        Validate.notEmpty(contentType, "A Content-Type header should be defined corresponding to the input RDF data syntax");

        if (request.getMethod().matches("PUT")) {
            logger.info("Clearing graph {} at server {}", graph, endPoint.getUpdateEndPoint());
            analizeDataset.clearGraph(endPoint, graph);
        }

        Model model = ModelFactory.createDefaultModel();
        RDFDataMgr.read(model, request.getBody(), graph, RDFLanguages.contentTypeToLang(contentType));
        logger.info("Loading {} triples into graph {} at server {}",
            model.size(), graph, endPoint.getUpdateEndPoint());
        analizeDataset.loadModel(endPoint, graph, model);
        endPointRepository.save(endPoint);
        logger.info("By default added graph {} to endpoint {}", graph, endPoint.getUpdateEndPoint());
        return model.size();
    }

    private Dataset getDataset(String datasetId) {
        return datasetRepository
            .findById(datasetId)
            .orElseThrow(() ->
                new NullPointerException(String.format("Dataset with id '%s' not found", datasetId)));
    }

    private SPARQLEndPoint getServer(Integer endPointId) {
        return endPointRepository
                .findById(endPointId)
                .orElseThrow(() ->
                        new NullPointerException(String.format("EndPoint with id '%s' not found", endPointId)));
    }
}
