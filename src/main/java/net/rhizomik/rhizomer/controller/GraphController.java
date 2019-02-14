package net.rhizomik.rhizomer.controller;

/**
 * Created by http://rhizomik.net/~roberto/
 */
import net.rhizomik.rhizomer.model.Dataset;
import net.rhizomik.rhizomer.repository.DatasetRepository;
import net.rhizomik.rhizomer.service.SecurityController;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Set;

@RepositoryRestController
public class GraphController {
    final Logger logger = LoggerFactory.getLogger(GraphController.class);

    @Autowired private DatasetRepository datasetRepository;
    @Autowired private SecurityController securityController;

    @RequestMapping(value = "/datasets/{datasetId}/graphs", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    public @ResponseBody List<String> addDatasetGraph(@RequestBody List<String> addGraphs,
        @PathVariable String datasetId, Authentication auth) {
        Dataset dataset = datasetRepository.findOne(datasetId);
        Validate.notNull(dataset, "Dataset with id '%s' not found", datasetId);
        securityController.checkOwner(dataset, auth);
        addGraphs.forEach(graph -> dataset.addDatasetGraph(graph));
        logger.info("Added graphs {} to Dataset {}", addGraphs.toString(), datasetId);
        return datasetRepository.save(dataset).getDatasetGraphs();
    }

    @RequestMapping(value = "/datasets/{datasetId}/graphs", method = RequestMethod.GET)
    public @ResponseBody List<String> retrieveDatasetGraphs(@PathVariable String datasetId,
        Authentication auth) {
        Dataset dataset = datasetRepository.findOne(datasetId);
        Validate.notNull(dataset, "Dataset with id '%s' not found", datasetId);
        securityController.checkPublicOrOwner(dataset, auth);
        logger.info("Retrieved graphs for Dataset {}", datasetId);
        return dataset.getDatasetGraphs();
    }

    @RequestMapping(value = "/datasets/{datasetId}/graphs", method = RequestMethod.PUT)
    public @ResponseBody List<String> updateDatasetGraphs(@Valid @RequestBody Set<String> updatedGraphs,
        @PathVariable String datasetId, Authentication auth) {
        Dataset dataset = datasetRepository.findOne(datasetId);
        Validate.notNull(dataset, "Dataset with id '%s' not found", datasetId);
        securityController.checkOwner(dataset, auth);
        dataset.setDatasetGraphs(updatedGraphs);
        logger.info("Updated Dataset {} graphs to {}", datasetId, updatedGraphs.toString());
        return datasetRepository.save(dataset).getDatasetGraphs();
    }
}