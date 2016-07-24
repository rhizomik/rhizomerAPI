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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.net.URI;
import java.util.List;

@RepositoryRestController
public class ServerController {
    final Logger logger = LoggerFactory.getLogger(ServerController.class);

    @Autowired private DatasetRepository datasetRepository;
    @Autowired private AnalizeDataset analizeDataset;

    @RequestMapping(value = "/datasets/{datasetId}/server/graphs", method = RequestMethod.GET)
    public @ResponseBody
    List<URI> retrieveServerGraphs(@PathVariable String datasetId) throws Exception {
        Dataset dataset = datasetRepository.findOne(datasetId);
        Preconditions.checkNotNull(dataset, "Dataset with id '%s' not found", datasetId);
        logger.info("Retrieve Dataset {} Server {} graphs", datasetId, dataset.getSparqlEndPoint().toString());
        return analizeDataset.listServerGraphs(dataset.getSparqlEndPoint());
    }
}
