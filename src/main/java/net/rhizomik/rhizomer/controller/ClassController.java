package net.rhizomik.rhizomer.controller;

/**
 * Created by http://rhizomik.net/~roberto/
 */
import java.util.List;
import javax.validation.Valid;
import net.rhizomik.rhizomer.model.Class;
import net.rhizomik.rhizomer.model.Curie;
import net.rhizomik.rhizomer.model.Dataset;
import net.rhizomik.rhizomer.model.id.DatasetClassId;
import net.rhizomik.rhizomer.repository.ClassRepository;
import net.rhizomik.rhizomer.repository.DatasetRepository;
import net.rhizomik.rhizomer.service.AnalizeDataset;
import org.apache.commons.lang3.Validate;
import org.apache.jena.riot.Lang;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@RepositoryRestController
public class ClassController {
    final Logger logger = LoggerFactory.getLogger(ClassController.class);

    @Autowired private DatasetRepository datasetRepository;
    @Autowired private ClassRepository classRepository;
    @Autowired private AnalizeDataset analiseDataset;

    @RequestMapping(value = "/datasets/{datasetId}/classes", method = RequestMethod.GET)
    public @ResponseBody List<Class> listDatasetClass(@PathVariable String datasetId) {
        Dataset dataset = datasetRepository.findOne(datasetId);
        Validate.notNull(dataset, "Dataset with id '%s' not found", datasetId);
        logger.info("Retrieving classes in Dataset {}", datasetId);
        if (dataset.getClasses().isEmpty() && dataset.getSparqlEndPoint()!=null )
            analiseDataset.detectDatasetClasses(dataset);
        return dataset.getClasses();
    }

    @RequestMapping(value = "/datasets/{datasetId}/classes/{classCurie}", method = RequestMethod.GET)
    public @ResponseBody Class retrieveDatasetClass(@PathVariable String datasetId,
        @PathVariable String classCurie) {
        Dataset dataset = datasetRepository.findOne(datasetId);
        Validate.notNull(dataset, "Dataset with id '%s' not found", datasetId);
        Class datasetClass = classRepository.findOne(new DatasetClassId(dataset, new Curie(classCurie)));
        Validate.notNull(datasetClass, "Class '%s' in Dataset '%s' not found", classCurie, datasetId);
        logger.info("Retrieved Class {} in Dataset {}", classCurie, datasetId);
        return datasetClass;
    }

    @RequestMapping(value = "/datasets/{datasetId}/classes/{classCurie}/instances", method = RequestMethod.GET,
    produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StreamingResponseBody> retrieveDatasetClass(@PathVariable String datasetId,
        @PathVariable String classCurie, @RequestParam(value="page", defaultValue="0") int page,
        @RequestParam(value="size", defaultValue="10") int size) {
        Dataset dataset = datasetRepository.findOne(datasetId);
        Validate.notNull(dataset, "Dataset with id '%s' not found", datasetId);
        Class datasetClass = classRepository.findOne(new DatasetClassId(dataset, new Curie(classCurie)));
        Validate.notNull(datasetClass, "Class '%s' in Dataset '%s' not found", classCurie, datasetId);
        logger.info("Retrieved Class {} in Dataset {}", classCurie, datasetId);
        StreamingResponseBody stream = outputStream ->
            analiseDataset.retrieveClassInstances(outputStream,
                dataset, datasetClass, page, size, Lang.JSONLD);
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(stream);
    }

    @RequestMapping(value = "/datasets/{datasetId}/classes", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public Class createDatasetClass(@Valid @RequestBody Class newClass, @PathVariable String datasetId) {
        Dataset dataset = datasetRepository.findOne(datasetId);
        Validate.notNull(dataset, "Dataset with id '%s' not found", datasetId);
        Validate.validState(!classRepository.exists(new DatasetClassId(dataset, new Curie(newClass.getUri()))),
                "Class with URI '%s' already exists in Dataset '%s'", newClass.getUri(), datasetId);
        newClass.setDataset(dataset);
        logger.info("Creating Class: {}", newClass.toString());
        return classRepository.save(newClass);
    }

    @RequestMapping(value = "/datasets/{datasetId}/classes", method = RequestMethod.PUT)
    public @ResponseBody List<Class> updateDatasetClasses(@Valid @RequestBody List<Class> newClasses,
        @PathVariable String datasetId) {
        Dataset dataset = datasetRepository.findOne(datasetId);
        Validate.notNull(dataset, "Dataset with id '%s' not found", datasetId);
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
}
