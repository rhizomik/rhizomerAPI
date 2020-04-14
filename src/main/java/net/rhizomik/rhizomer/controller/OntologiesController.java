package net.rhizomik.rhizomer.controller;

/**
 * Created by http://rhizomik.net/~roberto/
 */
import java.util.List;
import java.util.Set;
import javax.validation.Valid;
import net.rhizomik.rhizomer.model.Dataset;
import net.rhizomik.rhizomer.model.SPARQLEndPoint;
import net.rhizomik.rhizomer.repository.DatasetRepository;
import net.rhizomik.rhizomer.repository.SPARQLEndPointRepository;
import net.rhizomik.rhizomer.service.AnalizeDataset;
import net.rhizomik.rhizomer.service.SPARQLService;
import net.rhizomik.rhizomer.service.SecurityController;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@RepositoryRestController
public class OntologiesController {
    final Logger logger = LoggerFactory.getLogger(OntologiesController.class);

    @Autowired private DatasetRepository datasetRepository;
    @Autowired private SPARQLEndPointRepository endPointRepository;
    @Autowired private AnalizeDataset analizeDataset;
    @Autowired private SecurityController securityController;

    @RequestMapping(value = "/datasets/{datasetId}/ontologies", method = RequestMethod.GET)
    public @ResponseBody List<String> retrieveDatasetOntologies(@PathVariable String datasetId,
        Authentication auth) {
        Dataset dataset = getDataset(datasetId);
        securityController.checkPublicOrOwner(dataset, auth);
        logger.info("Retrieved ontologies for Dataset {}", datasetId);
        return dataset.getDatasetOntologies();
    }

    @RequestMapping(value = "/datasets/{datasetId}/ontologies", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    public @ResponseBody List<String> addDatasetOntology(@RequestBody Set<String> ontologies,
        @PathVariable String datasetId, Authentication auth) {
        Dataset dataset = getDataset(datasetId);
        securityController.checkOwner(dataset, auth);
        Validate.isTrue(endPointRepository.existsByDataset(dataset),
                "Dataset '%s' does not have at least one endpoint", datasetId);
        SPARQLEndPoint defaultEndPoint = endPointRepository.findByDataset(dataset).get(0);
        Validate.isTrue(defaultEndPoint.isWritable(),
                "EndPoint '%s' is not writable", defaultEndPoint.getUpdateEndPoint());
        analizeDataset.loadOntologies(dataset, defaultEndPoint, ontologies);
        datasetRepository.save(dataset);
        logger.info("Added ontologies {} to Dataset {}", ontologies.toString(), datasetId);
        return datasetRepository.save(dataset).getDatasetOntologies();
    }

    @RequestMapping(value = "/datasets/{datasetId}/ontologies", method = RequestMethod.PUT)
    public @ResponseBody List<String> updateDatasetOntologies(Authentication auth,
        @Valid @RequestBody Set<String> updatedOntologies, @PathVariable String datasetId) {
        Dataset dataset = getDataset(datasetId);
        securityController.checkOwner(dataset, auth);
        Validate.isTrue(endPointRepository.existsByDataset(dataset),
                "Dataset '%s' does not have at least one endpoint", datasetId);
        SPARQLEndPoint defaultEndPoint = endPointRepository.findByDataset(dataset).get(0);
        Validate.isTrue(defaultEndPoint.isWritable(),"EndPoint '%s' is not writable", defaultEndPoint.getUpdateEndPoint());
        dataset.setDatasetOntologies(updatedOntologies);
        analizeDataset.clearOntologies(dataset, defaultEndPoint);
        analizeDataset.loadOntologies(dataset, defaultEndPoint, updatedOntologies);
        datasetRepository.save(dataset);
        logger.info("Updated Dataset {} ontologies with {}", datasetId, updatedOntologies.toString());
        return datasetRepository.save(dataset).getDatasetOntologies();
    }

    private Dataset getDataset(String datasetId) {
        return datasetRepository
                .findById(datasetId)
                .orElseThrow(() ->
                        new NullPointerException(String.format("Dataset with id '%s' not found", datasetId)));
    }
}
