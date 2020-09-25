package net.rhizomik.rhizomer.controller;

/**
 * Created by http://rhizomik.net/~roberto/
 */

import net.rhizomik.rhizomer.model.Admin;
import net.rhizomik.rhizomer.model.Dataset;
import net.rhizomik.rhizomer.model.SPARQLEndPoint;
import net.rhizomik.rhizomer.repository.DatasetRepository;
import net.rhizomik.rhizomer.repository.SPARQLEndPointRepository;
import net.rhizomik.rhizomer.service.AnalizeDataset;
import net.rhizomik.rhizomer.service.HttpClient;
import net.rhizomik.rhizomer.service.SecurityController;
import org.apache.commons.lang3.Validate;
import org.apache.jena.riot.RDFFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.transaction.Transactional;
import javax.validation.Valid;
import java.net.*;

@RepositoryRestController
public class DatasetController {
    final Logger logger = LoggerFactory.getLogger(DatasetController.class);

    @Autowired private DatasetRepository datasetRepository;
    @Autowired private SPARQLEndPointRepository endPointRepository;
    @Autowired private SecurityController securityController;
    @Autowired private AnalizeDataset analizeDataset;
    @Autowired private HttpClient httpClient;

    @RequestMapping(value = "/datasets", method = RequestMethod.GET)
    public @ResponseBody
    Iterable<Dataset> listDatasets(Authentication auth) throws Exception {
        if (auth.getPrincipal() instanceof Admin)
            return datasetRepository.findAll();
        else {
            return datasetRepository.findByOwner(auth.getName());
        }
    }

    @RequestMapping(value = "/datasets", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    public @ResponseBody
    Dataset createDataset(@Valid @RequestBody Dataset newDataset, Authentication auth) {
        Validate.isTrue(!datasetRepository.existsById(newDataset.getId()),
                "Dataset with id '%s' already exists", newDataset.getId());
        logger.info("Creating Dataset: {}", newDataset.getId());
        newDataset.setOwner(auth.getName());
        return datasetRepository.save(newDataset);
    }

    @RequestMapping(value = "/datasets/{datasetId}", method = RequestMethod.GET)
    public @ResponseBody
    Dataset retrieveDataset(@PathVariable String datasetId, Authentication auth) {
        Dataset dataset = getDataset(datasetId);
        securityController.checkPublicOrOwner(dataset, auth);
        logger.info("Retrieved Dataset {}", datasetId);
        return dataset;
    }

    @RequestMapping(value = "/datasets/{datasetId}", method = RequestMethod.PUT)
    public @ResponseBody
    Dataset updateDataset(@Valid @RequestBody Dataset updatedDataset,
        @PathVariable String datasetId, Authentication auth) {
        Dataset dataset = getDataset(datasetId);
        securityController.checkOwner(dataset, auth);
        logger.info("Updating Dataset: {}", datasetId);
        return datasetRepository.save(updatedDataset);
    }

    @RequestMapping(value = "/datasets/{datasetId}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    public void deleteDataset(@PathVariable String datasetId, Authentication auth) {
        Dataset dataset = datasetRepository.findById(datasetId).orElseThrow(() ->
            new NullPointerException(String.format("Dataset with id '%s' not found", datasetId)));
        securityController.checkOwner(dataset, auth);
        logger.info("Deleting dataset {} and its endpoints", datasetId);
        if (endPointRepository.existsByDataset(dataset)) {
            logger.info("Deleting endpoints for dataset {}", datasetId);
            SPARQLEndPoint defaultEndPoint = endPointRepository.findByDataset(dataset).get(0);
            analizeDataset.dropGraph(defaultEndPoint, dataset.getDatasetOntologiesGraph().toString());
            analizeDataset.dropGraph(defaultEndPoint, dataset.getDatasetInferenceGraph().toString());
            endPointRepository.deleteByDataset(dataset);
        }
        datasetRepository.delete(dataset);
    }

    @RequestMapping(value = "/datasets/{datasetId}/describe", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StreamingResponseBody> describeDatasetResource(
        @PathVariable String datasetId,
        @RequestParam(value = "uri") URI resourceUri, Authentication auth) {
        Dataset dataset = datasetRepository.findById(datasetId).orElseThrow(() ->
            new NullPointerException(String.format("Dataset with id '%s' not found", datasetId)));
        securityController.checkPublicOrOwner(dataset, auth);
        logger.info("Retrieved description for {}", resourceUri);
        StreamingResponseBody stream = outputStream ->
            analizeDataset.describeDatasetResource(outputStream,
                dataset, resourceUri, RDFFormat.JSONLD);
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(stream);
    }

    @RequestMapping(value = "/datasets/{datasetId}/browseData", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StreamingResponseBody> browseUriData(
        @PathVariable String datasetId,
        @RequestParam(value = "uri") URI resourceUri, Authentication auth) {
        Dataset dataset = datasetRepository.findById(datasetId).orElseThrow(() ->
            new NullPointerException(String.format("Dataset with id '%s' not found", datasetId)));
        securityController.checkPublicOrOwner(dataset, auth);
        logger.info("Browsing available data at {}", resourceUri);
        StreamingResponseBody stream = outputStream ->
            analizeDataset.browseUri(outputStream, resourceUri, RDFFormat.JSONLD);
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(stream);
    }

    @RequestMapping(value = "/datasets/{datasetId}/browse", method = RequestMethod.GET)
    public ResponseEntity<StreamingResponseBody> browseUriContent(
            @PathVariable String datasetId,
            @RequestParam(value = "uri") URI resourceUri, Authentication auth) throws Exception {
        Dataset dataset = datasetRepository.findById(datasetId).orElseThrow(() ->
                new NullPointerException(String.format("Dataset with id '%s' not found", datasetId)));
        securityController.checkPublicOrOwner(dataset, auth);
        logger.info("Browsing available content at {}", resourceUri);
        StreamingResponseBody stream = outputStream ->
                httpClient.loadUrl(resourceUri.toURL(), outputStream);
        return ResponseEntity.ok()
                .body(stream);
    }

    private Dataset getDataset(String datasetId) {
        return datasetRepository
                .findById(datasetId)
                .orElseThrow(() ->
                        new NullPointerException(String.format("Dataset with id '%s' not found", datasetId)));
    }
}
