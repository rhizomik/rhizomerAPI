package net.rhizomik.rhizomer.controller;

/**
 * Created by http://rhizomik.net/~roberto/
 */
import com.google.common.base.Preconditions;
import net.rhizomik.rhizomer.model.Class;
import net.rhizomik.rhizomer.model.*;
import net.rhizomik.rhizomer.model.id.DatasetClassFacetId;
import net.rhizomik.rhizomer.model.id.DatasetClassId;
import net.rhizomik.rhizomer.repository.ClassRepository;
import net.rhizomik.rhizomer.repository.DatasetRepository;
import net.rhizomik.rhizomer.repository.FacetRepository;
import net.rhizomik.rhizomer.repository.RangeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RepositoryRestController
public class RangeController {
    final Logger logger = LoggerFactory.getLogger(RangeController.class);

    @Autowired private DatasetRepository datasetRepository;
    @Autowired private ClassRepository classRepository;
    @Autowired private FacetRepository facetRepository;
    @Autowired private RangeRepository rangeRepository;

    @RequestMapping(value = "/datasets/{datasetId}/classes/{classCurie}/facets/{facetCurie}/ranges", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public Range addRange(@Valid @RequestBody Range newRange, @PathVariable String datasetId,
                                        @PathVariable String classCurie, @PathVariable String facetCurie) throws Exception {
        Dataset dataset = datasetRepository.findOne(datasetId);
        Preconditions.checkNotNull(dataset, "Dataset with id '%s' not found", datasetId);
        DatasetClassId datasetClassId = new DatasetClassId(dataset, new Curie(classCurie));
        Class datasetClass = classRepository.findOne(datasetClassId);
        Preconditions.checkNotNull(datasetClass, "Class with id '%s' not found", datasetClassId);
        DatasetClassFacetId datasetClassFacetId = new DatasetClassFacetId(datasetClassId, new Curie(facetCurie));
        Facet classFacet = facetRepository.findOne(datasetClassFacetId);
        Preconditions.checkNotNull(classFacet, "Facet with id '%s' not found", datasetClassFacetId);
        newRange.setFacet(classFacet);
        logger.info("Creating Range: {}", newRange.toString());
        return rangeRepository.save(newRange);
    }
}
