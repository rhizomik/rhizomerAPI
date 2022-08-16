package net.rhizomik.rhizomer.controller;

/**
 * Created by http://rhizomik.net/~roberto/
 */

import net.rhizomik.rhizomer.model.Class;
import net.rhizomik.rhizomer.model.Curie;
import net.rhizomik.rhizomer.model.Dataset;
import net.rhizomik.rhizomer.model.id.DatasetClassId;
import net.rhizomik.rhizomer.repository.ClassRepository;
import net.rhizomik.rhizomer.repository.DatasetRepository;
import net.rhizomik.rhizomer.repository.SPARQLEndPointRepository;
import net.rhizomik.rhizomer.service.AnalizeDataset;
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
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.validation.Valid;
import java.net.URI;
import java.util.List;

@RestController
public class ClassController {
    final Logger logger = LoggerFactory.getLogger(ClassController.class);

    @Autowired private DatasetRepository datasetRepository;
    @Autowired private SPARQLEndPointRepository endPointRepository;
    @Autowired private ClassRepository classRepository;
    @Autowired private AnalizeDataset analizeDataset;
    @Autowired private SecurityController securityController;

    @RequestMapping(value = "/datasets/{datasetId}/classes", method = RequestMethod.GET)
    public @ResponseBody List<Class> searchDatasetClass(@PathVariable String datasetId,
        @RequestParam(value = "containing", defaultValue = "") String containing,
        @RequestParam(value = "top", defaultValue = "-1") int top,
        @RequestParam(value = "lang", defaultValue = "en") String lang, Authentication auth) {
        Dataset dataset = getDataset(datasetId);
        securityController.checkPublicOrOwner(dataset, auth);
        logger.info("Retrieving top {} classes in Dataset {} containing '{}'", top, datasetId, containing);
        if (dataset.getClasses().isEmpty() && endPointRepository.existsByDataset(dataset))
            analizeDataset.detectDatasetClasses(dataset);
        return dataset.getClassesContaining(containing, top, lang);
    }

    @RequestMapping(value = "/datasets/{datasetId}/classByUri", method = RequestMethod.GET)
    public @ResponseBody Class searchDatasetClass(@PathVariable String datasetId,
            @RequestParam(value = "uri") URI classUri, Authentication auth) {
        Dataset dataset = getDataset(datasetId);
        securityController.checkPublicOrOwner(dataset, auth);
        logger.info("Retrieving dataset '{}' class from its URI '{}'", datasetId, classUri);
        return classRepository.findByDatasetAndUri(dataset, classUri.toString());
    }

    @RequestMapping(value = "/datasets/{datasetId}/classes/{classCurie}", method = RequestMethod.GET)
    public @ResponseBody Class retrieveDatasetClass(@PathVariable String datasetId,
        @PathVariable String classCurie, Authentication auth) {
        Dataset dataset = getDataset(datasetId);
        securityController.checkPublicOrOwner(dataset, auth);
        Class datasetClass = getClass(classCurie, dataset);
        logger.info("Retrieved Class {} in Dataset {}", classCurie, datasetId);
        return datasetClass;
    }

    @RequestMapping(value = "/datasets/{datasetId}/classes/{classCurie}/instances", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StreamingResponseBody> retrieveClassFacetedInstances(
            @PathVariable String datasetId, @PathVariable String classCurie,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam MultiValueMap<String, String> filters, Authentication auth) {
        Dataset dataset = getDataset(datasetId);
        securityController.checkPublicOrOwner(dataset, auth);
        Class datasetClass = getClass(classCurie, dataset);
        logger.info("List instances for Class {} in Dataset {}", classCurie, datasetId);
        filters.remove("page");
        filters.remove("size");
        StreamingResponseBody stream = outputStream ->
                analizeDataset.retrieveClassInstances(outputStream,
                        dataset, datasetClass, filters, page, size, RDFFormat.JSONLD);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(stream);
    }

    @RequestMapping(value = "/datasets/{datasetId}/classes/{classCurie}/describe", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StreamingResponseBody> retrieveClassFacetedDescriptions(
            @PathVariable String datasetId, @PathVariable String classCurie,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam MultiValueMap<String, String> filters, Authentication auth) {
        Dataset dataset = getDataset(datasetId);
        securityController.checkPublicOrOwner(dataset, auth);
        Class datasetClass = getClass(classCurie, dataset);
        logger.info("Describe instances for Class {} in Dataset {}", classCurie, datasetId);
        filters.remove("page");
        filters.remove("size");
        StreamingResponseBody stream = outputStream ->
            analizeDataset.retrieveClassDescriptions(outputStream,
                dataset, datasetClass, filters, page, size, RDFFormat.JSONLD);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(stream);
    }

    @RequestMapping(value = "/datasets/{datasetId}/classes/{classCurie}/instancesLabels", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StreamingResponseBody> retrieveClassFacetedInstancesLabels(
            @PathVariable String datasetId, @PathVariable String classCurie,
            @RequestParam(value="page", defaultValue="0") int page,
            @RequestParam(value="size", defaultValue="10") int size,
            @RequestParam MultiValueMap<String, String> filters, Authentication auth) {
        Dataset dataset = getDataset(datasetId);
        securityController.checkPublicOrOwner(dataset, auth);
        Class datasetClass = getClass(classCurie, dataset);
        logger.info("Retrieve instances labels for Class {} in Dataset {}", classCurie, datasetId);
        filters.remove("page");
        filters.remove("size");
        StreamingResponseBody stream = outputStream ->
            analizeDataset.getLinkedResourcesLabels(outputStream,
                dataset, datasetClass, filters, page, size, RDFFormat.JSONLD);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(stream);
    }

    @RequestMapping(value = "/datasets/{datasetId}/classes/{classCurie}/count", method = RequestMethod.GET)
    public @ResponseBody int retrieveClassFacetedInstancesCount(
        @PathVariable String datasetId, @PathVariable String classCurie,
        @RequestParam MultiValueMap<String, String> filters, Authentication auth) {
        Dataset dataset = getDataset(datasetId);
        securityController.checkPublicOrOwner(dataset, auth);
        Class datasetClass = getClass(classCurie, dataset);
        logger.info("Retrieved instances count for Class {} in Dataset {}", classCurie, datasetId);
        return analizeDataset.retrieveClassInstancesCount(dataset, datasetClass, filters);
    }

    @RequestMapping(value = "/datasets/{datasetId}/classes", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public Class createDatasetClass(@Valid @RequestBody Class newClass,
        @PathVariable String datasetId, Authentication auth) {
        Dataset dataset = getDataset(datasetId);
        securityController.checkOwner(dataset, auth);
        Validate.validState(!classRepository.existsById(new DatasetClassId(dataset, new Curie(newClass.getUri()))),
                "Class with URI '%s' already exists in Dataset '%s'", newClass.getUri(), datasetId);
        newClass.setDataset(dataset);
        logger.info("Creating Class: {}", newClass.toString());
        return classRepository.save(newClass);
    }

    @RequestMapping(value = "/datasets/{datasetId}/classes", method = RequestMethod.PUT)
    public @ResponseBody List<Class> updateDatasetClasses(@Valid @RequestBody List<Class> newClasses,
        @PathVariable String datasetId, Authentication auth) {
        Dataset dataset = getDataset(datasetId);
        securityController.checkOwner(dataset, auth);
        dataset.getClasses().forEach(aClass -> {
            classRepository.delete(aClass);
            dataset.removeClass(aClass);
        });
        newClasses.forEach(newClass -> {
            newClass.setDataset(dataset);
            classRepository.save(newClass);
        });
        logger.info("Updated Dataset {} classes to {}", datasetId, newClasses.toString());
        return datasetRepository.save(dataset).getClasses();
    }

    private Dataset getDataset(String datasetId) {
        return datasetRepository
            .findById(datasetId)
            .orElseThrow(() ->
                new NullPointerException(String.format("Dataset with id '%s' not found", datasetId)));
    }

    private Class getClass(String classCurie, Dataset dataset) {
        return classRepository
            .findById(new DatasetClassId(dataset, new Curie(classCurie)))
            .orElseThrow(() ->
                new NullPointerException(
                    String.format("Class '%s' in Dataset '%s' not found", classCurie, dataset.getId())));
    }
}
