package net.rhizomik.rhizomer.controller;

/**
 * Created by http://rhizomik.net/~roberto/
 */
import com.google.common.base.Preconditions;
import net.rhizomik.rhizomer.model.Dataset;
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
import java.util.Set;

@RepositoryRestController
public class GraphController {
    final Logger logger = LoggerFactory.getLogger(GraphController.class);

    @Autowired private DatasetRepository datasetRepository;
    @Autowired private AnalizeDataset analizeDataset;


    @RequestMapping(value = "/datasets/{datasetId}/graphs", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    public @ResponseBody List<String> addDatasetGraph(@RequestBody List<String> addGraphs, @PathVariable String datasetId) throws Exception {
        Dataset dataset = datasetRepository.findOne(datasetId);
        Preconditions.checkNotNull(dataset, "Dataset with id {} not found", datasetId);
        addGraphs.forEach(graph -> dataset.addDatasetGraph(graph));
        logger.info("Added graphs {} to Dataset {}, recomputing classes...", addGraphs.toString(), datasetId);
        analizeDataset.recomputeDatasetClasses(dataset); //TODO: consider just recomputing classes for the new graphs
        return datasetRepository.save(dataset).getDatasetGraphs();
    }

    @RequestMapping(value = "/datasets/{datasetId}/graphs", method = RequestMethod.GET)
    public @ResponseBody List<String> retrieveDatasetGraphs(@PathVariable String datasetId) throws Exception {
        Dataset dataset = datasetRepository.findOne(datasetId);
        Preconditions.checkNotNull(dataset, "Dataset with id {} not found", datasetId);
        logger.info("Retrieved graphs for Dataset {}", datasetId);
        return dataset.getDatasetGraphs();
    }

    @RequestMapping(value = "/datasets/{datasetId}/graphs", method = RequestMethod.PUT)
    public @ResponseBody List<String> updateDatasetGraphs(@Valid @RequestBody Set<String> updatedGraphs, @PathVariable String datasetId) throws Exception {
        Dataset dataset = datasetRepository.findOne(datasetId);
        Preconditions.checkNotNull(dataset, "Dataset with id {} not found", datasetId);
        dataset.setDatasetGraphs(updatedGraphs);
        logger.info("Updated Dataset {} graphs to {}, recomputing classes", datasetId, updatedGraphs.toString());
        analizeDataset.recomputeDatasetClasses(dataset);
        return datasetRepository.save(dataset).getDatasetGraphs();
    }
}
