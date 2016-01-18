package net.rhizomik.rhizomer.controller;

/**
 * Created by http://rhizomik.net/~roberto/
 */
import com.google.common.base.Preconditions;
import net.rhizomik.rhizomer.model.Class;
import net.rhizomik.rhizomer.model.Curie;
import net.rhizomik.rhizomer.model.Dataset;
import net.rhizomik.rhizomer.model.DatasetClassId;
import net.rhizomik.rhizomer.repository.ClassRepository;
import net.rhizomik.rhizomer.repository.DatasetRepository;
import net.rhizomik.rhizomer.service.AnalizeDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RepositoryRestController
public class ClassController {
    final Logger logger = LoggerFactory.getLogger(ClassController.class);

    @Autowired private DatasetRepository datasetRepository;
    @Autowired private ClassRepository classRepository;
    @Autowired private AnalizeDataset analiseDataset;

    @RequestMapping(value = "/datasets/{datasetId}/classes", method = RequestMethod.GET)
    public @ResponseBody List<Class> listDatasetClass(@PathVariable String datasetId) throws Exception {
        Dataset dataset = datasetRepository.findOne(datasetId);
        Preconditions.checkNotNull(dataset, "Dataset with id %s not found", datasetId);
        logger.info("Retrieving classes in Dataset {}", datasetId);
        if (dataset.getClasses().isEmpty() && dataset.getSparqlEndPoint()!=null )
            analiseDataset.detectDatasetClasses(dataset);
        return dataset.getClasses();
    }

    @RequestMapping(value = "/datasets/{datasetId}/classes/{classCurie}", method = RequestMethod.GET)
    public @ResponseBody Class retrieveDatasetClass(@PathVariable String datasetId, @PathVariable String classCurie) throws Exception {
        Dataset dataset = datasetRepository.findOne(datasetId);
        Preconditions.checkNotNull(dataset, "Dataset with id %s not found", datasetId);
        Class datasetClass = classRepository.findOne(new DatasetClassId(dataset, new Curie(classCurie)));
        Preconditions.checkNotNull(datasetClass, "Class %s in Dataset %s not found", classCurie, datasetId);
        logger.info("Retrieved Class {} in Dataset {}", classCurie, datasetId);
        return datasetClass;
    }

    @RequestMapping(value = "/datasets/{datasetId}/classes", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public Class createDatasetClass(@Valid @RequestBody Class newClass, @PathVariable String datasetId) throws Exception {
        Dataset dataset = datasetRepository.findOne(datasetId);
        Preconditions.checkNotNull(dataset, "Dataset with id %s not found", datasetId);
        Preconditions.checkState(!classRepository.exists(new DatasetClassId(dataset, new Curie(newClass.getUri()))),
                "Class with URI %s already exists in Dataset %s", newClass.getUri(), datasetId);
        newClass.setDataset(dataset);
        logger.info("Creating Class: {}", newClass.toString());
        return classRepository.save(newClass);
    }
}
