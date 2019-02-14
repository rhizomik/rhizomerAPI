package net.rhizomik.rhizomer.controller;

/**
 * Created by http://rhizomik.net/~roberto/
 */
import net.rhizomik.rhizomer.model.Class;
import net.rhizomik.rhizomer.model.Curie;
import net.rhizomik.rhizomer.model.Dataset;
import net.rhizomik.rhizomer.model.Facet;
import net.rhizomik.rhizomer.model.id.DatasetClassFacetId;
import net.rhizomik.rhizomer.model.id.DatasetClassId;
import net.rhizomik.rhizomer.repository.ClassRepository;
import net.rhizomik.rhizomer.repository.DatasetRepository;
import net.rhizomik.rhizomer.repository.FacetRepository;
import net.rhizomik.rhizomer.service.AnalizeDataset;
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
import java.util.Collections;
import java.util.List;

@RepositoryRestController
public class FacetController {
    final Logger logger = LoggerFactory.getLogger(FacetController.class);

    @Autowired private DatasetRepository datasetRepository;
    @Autowired private ClassRepository classRepository;
    @Autowired private FacetRepository facetRepository;
    @Autowired private AnalizeDataset analiseDataset;
    @Autowired private SecurityController securityController;

    @RequestMapping(value = "/datasets/{datasetId}/classes/{classCurie}/facets", method = RequestMethod.GET)
    public @ResponseBody List<Facet> listClassFacets(Authentication auth,
        @PathVariable String datasetId,
        @PathVariable String classCurie,
        @RequestParam(value="relevance", defaultValue="0") float relevance) {
        Dataset dataset = datasetRepository.findOne(datasetId);
        Validate.notNull(dataset, "Dataset with id '%s' not found", datasetId);
        securityController.checkPublicOrOwner(dataset, auth);
        DatasetClassId datasetClassId = new DatasetClassId(dataset, new Curie(classCurie));
        Class datasetClass = classRepository.findOne(datasetClassId);
        Validate.notNull(datasetClass, "Class with id '%s' not found", datasetClassId);
        logger.info("Retrieving facets for Class {} in Dataset {}", classCurie, datasetId);
        if (datasetClass.getFacets().isEmpty() && dataset.getSparqlEndPoint()!=null )
            analiseDataset.detectClassFacets(datasetClass);
        return datasetClass.getFacets(relevance);
    }

    @RequestMapping(value = "/datasets/{datasetId}/classes/{classCurie}/facets/{facetCurie}", method = RequestMethod.GET)
    public @ResponseBody Facet retrieveClassFacet(@PathVariable String datasetId,
        @PathVariable String classCurie, @PathVariable String facetCurie, Authentication auth) {
        Dataset dataset = datasetRepository.findOne(datasetId);
        Validate.notNull(dataset, "Dataset with id '%s' not found", datasetId);
        securityController.checkPublicOrOwner(dataset, auth);
        DatasetClassId datasetClassId = new DatasetClassId(dataset, new Curie(classCurie));
        Class datasetClass = classRepository.findOne(datasetClassId);
        Validate.notNull(datasetClass, "Class with id '%s' not found", datasetClassId);
        DatasetClassFacetId datasetClassFacetId = new DatasetClassFacetId(datasetClassId, new Curie(facetCurie));
        Facet classFacet = facetRepository.findOne(datasetClassFacetId);
        Validate.notNull(classFacet, "Facet with id '%s' not found", datasetClassFacetId);
        logger.info("Retrieved Facet {} for Class {} in Dataset {}", facetCurie, classCurie, datasetId);
        return classFacet;
    }

    @RequestMapping(value = "/datasets/{datasetId}/classes/{classCurie}/facets", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public Facet createClassFacet(@Valid @RequestBody Facet newFacet, @PathVariable String datasetId,
        @PathVariable String classCurie, Authentication auth) {
        Dataset dataset = datasetRepository.findOne(datasetId);
        Validate.notNull(dataset, "Dataset with id '%s' not found", datasetId);
        securityController.checkOwner(dataset, auth);
        DatasetClassId datasetClassId = new DatasetClassId(dataset, new Curie(classCurie));
        Class datasetClass = classRepository.findOne(datasetClassId);
        Validate.notNull(datasetClass, "Class with id '%s' not found", datasetClassId);
        DatasetClassFacetId datasetClassFacetId = new DatasetClassFacetId(datasetClassId, new Curie(newFacet.getUri()));
        Validate.isTrue(!facetRepository.exists(datasetClassFacetId),
                "Facet with URI '%s' already exists for Class '%s' in Dataset '%s'", newFacet.getUri(), classCurie, datasetId);
        newFacet.setDomain(datasetClass);
        logger.info("Creating Facet: {}", newFacet.toString());
        return facetRepository.save(newFacet);
    }

    @RequestMapping(value = "/datasets/{datasetId}/classes/{classCurie}/facets", method = RequestMethod.PUT)
    public @ResponseBody List<Facet> updateClassFacets(@Valid @RequestBody List<Facet> newFacets,
        @PathVariable String datasetId, @PathVariable String classCurie, Authentication auth) {
        Dataset dataset = datasetRepository.findOne(datasetId);
        Validate.notNull(dataset, "Dataset with id '%s' not found", datasetId);
        securityController.checkOwner(dataset, auth);
        DatasetClassId datasetClassId = new DatasetClassId(dataset, new Curie(classCurie));
        Class datasetClass = classRepository.findOne(datasetClassId);
        Validate.notNull(datasetClass, "Class with id '%s' not found", datasetClassId);
        datasetClass.getFacets().forEach(aFacet -> facetRepository.delete(aFacet));
        datasetClass.setFacets(Collections.emptyList());

        newFacets.forEach(newFacet -> {
            newFacet.setDomain(datasetClass);
            datasetClass.addFacet(facetRepository.save(newFacet));
        });

        logger.info("Updated Dataset {} class {} facets to {}",
                datasetClass.getDataset().getId(), datasetClass.getCurie(), newFacets.toString());
        return classRepository.save(datasetClass).getFacets();
    }
}
