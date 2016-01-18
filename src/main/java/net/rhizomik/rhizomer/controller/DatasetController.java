package net.rhizomik.rhizomer.controller;

/**
 * Created by http://rhizomik.net/~roberto/
 */
import com.google.common.base.Preconditions;
import net.rhizomik.rhizomer.model.Dataset;
import net.rhizomik.rhizomer.repository.DatasetRepository;
import net.rhizomik.rhizomer.service.SPARQLService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RepositoryRestController
public class DatasetController {
    final Logger logger = LoggerFactory.getLogger(DatasetController.class);

    @Autowired private DatasetRepository datasetRepository;
    @Autowired private SPARQLService sparqlService;

    @RequestMapping(value = "/datasets", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    public @ResponseBody
    Dataset createDataset(@Valid @RequestBody Dataset newDataset) throws Exception {
        Preconditions.checkState(!datasetRepository.exists(newDataset.getId()), "Dataset with id {} already exists", newDataset.getId());
        logger.info("Creating Dataset: {}", newDataset.getId());
        return datasetRepository.save(newDataset);
    }

    @RequestMapping(value = "/datasets/{datasetId}", method = RequestMethod.GET)
    public @ResponseBody
    Dataset retrieveDataset(@PathVariable String datasetId) throws Exception {
        Dataset dataset = datasetRepository.findOne(datasetId);
        Preconditions.checkNotNull(dataset, "Dataset with id {} not found", datasetId);
        logger.info("Retrieved Dataset {}", datasetId);
        return dataset;
    }

    @RequestMapping(value = "/datasets/{datasetId}", method = RequestMethod.PUT)
    public @ResponseBody
    Dataset updateDataset(@Valid @RequestBody Dataset updatedDataset, @PathVariable String datasetId) throws Exception {
        Dataset dataset = datasetRepository.findOne(datasetId);
        Preconditions.checkNotNull(dataset, "Dataset with id {} not found", datasetId);
        logger.info("Updating Dataset: {}", datasetId);
        dataset.setSparqlEndPoint(updatedDataset.getSparqlEndPoint());
        dataset.setQueryType(updatedDataset.getQueryType());
        dataset.setInferenceEnabled(updatedDataset.isInferenceEnabled());
        dataset.setSampleSize(updatedDataset.getSampleSize());
        dataset.setCoverage(updatedDataset.getCoverage());
        return datasetRepository.save(dataset);
    }

    @RequestMapping(value = "/datasets/{datasetId}", method = RequestMethod.DELETE)
    @ResponseBody
    public void deleteDataset(@PathVariable String datasetId) throws Exception {
        Dataset dataset = datasetRepository.findOne(datasetId);
        Preconditions.checkNotNull(dataset, "Dataset with id {} not found", datasetId);
        logger.info("Deleting Dataset {}", datasetId);
        datasetRepository.delete(dataset);
    }
}
