package net.rhizomik.rhizomer.controller;

/**
 * Created by http://rhizomik.net/~roberto/
 */
import com.google.common.base.Preconditions;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RepositoryRestController
public class FacetController {
    final Logger logger = LoggerFactory.getLogger(FacetController.class);

    @Autowired private DatasetRepository datasetRepository;
    @Autowired private ClassRepository classRepository;
    @Autowired private FacetRepository facetRepository;
    @Autowired private AnalizeDataset analiseDataset;

    @RequestMapping(value = "/datasets/{datasetId}/classes/{classCurie}/facets", method = RequestMethod.GET)
    public @ResponseBody List<Facet> listClassFacets(@PathVariable String datasetId, @PathVariable String classCurie) throws Exception {
        Dataset dataset = datasetRepository.findOne(datasetId);
        Preconditions.checkNotNull(dataset, "Dataset with id %s not found", datasetId);
        DatasetClassId datasetClassId = new DatasetClassId(dataset, new Curie(classCurie));
        Class datasetClass = classRepository.findOne(datasetClassId);
        Preconditions.checkNotNull(datasetClass, "Class with id %s not found", datasetClassId);
        logger.info("Retrieving facets for Class {} in Dataset {}", classCurie, datasetId);
        if (datasetClass.getFacets().isEmpty() && dataset.getSparqlEndPoint()!=null )
            analiseDataset.detectClassFacets(datasetClass);
        return datasetClass.getFacets();
    }

    @RequestMapping(value = "/datasets/{datasetId}/classes/{classCurie}/facets/{facetCurie}", method = RequestMethod.GET)
    public @ResponseBody Facet retrieveClassFacet(@PathVariable String datasetId, @PathVariable String classCurie, @PathVariable String facetCurie) throws Exception {
        Dataset dataset = datasetRepository.findOne(datasetId);
        Preconditions.checkNotNull(dataset, "Dataset with id %s not found", datasetId);
        DatasetClassId datasetClassId = new DatasetClassId(dataset, new Curie(classCurie));
        Class datasetClass = classRepository.findOne(datasetClassId);
        Preconditions.checkNotNull(datasetClass, "Class with id %s not found", datasetClassId);
        DatasetClassFacetId datasetClassFacetId = new DatasetClassFacetId(datasetClassId, new Curie(facetCurie));
        Facet classFacet = facetRepository.findOne(datasetClassFacetId);
        Preconditions.checkNotNull(classFacet, "Facet with id %s not found", datasetClassFacetId);
        logger.info("Retrieved Facet {} for Class {} in Dataset {}", facetCurie, classCurie, datasetId);
        return classFacet;
    }

    @RequestMapping(value = "/datasets/{datasetId}/classes/{classCurie}/facets", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public Facet createClassFacet(@Valid @RequestBody Facet newFacet, @PathVariable String datasetId, @PathVariable String classCurie) throws Exception {
        Dataset dataset = datasetRepository.findOne(datasetId);
        Preconditions.checkNotNull(dataset, "Dataset with id %s not found", datasetId);
        DatasetClassId datasetClassId = new DatasetClassId(dataset, new Curie(classCurie));
        Class datasetClass = classRepository.findOne(datasetClassId);
        Preconditions.checkNotNull(datasetClass, "Class with id %s not found", datasetClassId);
        DatasetClassFacetId datasetClassFacetId = new DatasetClassFacetId(datasetClassId, new Curie(newFacet.getUri()));
        Preconditions.checkState(!facetRepository.exists(datasetClassFacetId),
                "Facet with URI %s already exists for Class %s in Dataset %s", newFacet.getUri(), classCurie, datasetId);
        newFacet.setDomain(datasetClass);
        logger.info("Creating Facet: {}", newFacet.toString());
        return facetRepository.save(newFacet);
    }
}
