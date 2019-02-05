package net.rhizomik.rhizomer.controller;

/**
 * Created by http://rhizomik.net/~roberto/
 */
import java.util.List;
import javax.validation.Valid;
import net.rhizomik.rhizomer.model.Class;
import net.rhizomik.rhizomer.model.Curie;
import net.rhizomik.rhizomer.model.Dataset;
import net.rhizomik.rhizomer.model.Facet;
import net.rhizomik.rhizomer.model.Range;
import net.rhizomik.rhizomer.model.Value;
import net.rhizomik.rhizomer.model.id.DatasetClassFacetId;
import net.rhizomik.rhizomer.model.id.DatasetClassFacetRangeId;
import net.rhizomik.rhizomer.model.id.DatasetClassId;
import net.rhizomik.rhizomer.repository.ClassRepository;
import net.rhizomik.rhizomer.repository.DatasetRepository;
import net.rhizomik.rhizomer.repository.FacetRepository;
import net.rhizomik.rhizomer.repository.RangeRepository;
import net.rhizomik.rhizomer.service.AnalizeDataset;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.http.HttpStatus;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@RepositoryRestController
public class RangeController {
    final Logger logger = LoggerFactory.getLogger(RangeController.class);

    @Autowired private DatasetRepository datasetRepository;
    @Autowired private ClassRepository classRepository;
    @Autowired private FacetRepository facetRepository;
    @Autowired private RangeRepository rangeRepository;
    @Autowired private AnalizeDataset analiseDataset;


    @RequestMapping(value = "/datasets/{datasetId}/classes/{classCurie}/facets/{facetCurie}/ranges", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public List<Range> getRanges(@PathVariable String datasetId, @PathVariable String classCurie, @PathVariable String facetCurie) {
        Dataset dataset = datasetRepository.findOne(datasetId);
        Validate.notNull(dataset, "Dataset with id '%s' not found", datasetId);
        DatasetClassId datasetClassId = new DatasetClassId(dataset, new Curie(classCurie));
        Class datasetClass = classRepository.findOne(datasetClassId);
        Validate.notNull(datasetClass, "Class with id '%s' not found", datasetClassId);
        DatasetClassFacetId datasetClassFacetId = new DatasetClassFacetId(datasetClassId, new Curie(facetCurie));
        Facet classFacet = facetRepository.findOne(datasetClassFacetId);
        Validate.notNull(classFacet, "Facet with id '%s' not found", datasetClassFacetId);
        return classFacet.getRanges();
    }

    @RequestMapping(value = "/datasets/{datasetId}/classes/{classCurie}/facets/{facetCurie}/ranges/{rangeCurie}/values", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public List<Value> getRangeValues(@PathVariable String datasetId, @PathVariable String classCurie,
        @PathVariable String facetCurie, @PathVariable String rangeCurie,
        @RequestParam MultiValueMap<String, String> filters,
        @RequestParam(value="page", defaultValue="0") int page,
        @RequestParam(value="size", defaultValue="10") int size) {
        Dataset dataset = datasetRepository.findOne(datasetId);
        Validate.notNull(dataset, "Dataset with id '%s' not found", datasetId);
        DatasetClassId datasetClassId = new DatasetClassId(dataset, new Curie(classCurie));
        Class datasetClass = classRepository.findOne(datasetClassId);
        Validate.notNull(datasetClass, "Class with id '%s' not found", datasetClassId);
        DatasetClassFacetId datasetClassFacetId = new DatasetClassFacetId(datasetClassId, new Curie(facetCurie));
        Facet classFacet = facetRepository.findOne(datasetClassFacetId);
        Validate.notNull(classFacet, "Facet with id '%s' not found", datasetClassFacetId);
        DatasetClassFacetRangeId datasetClassFacetRangeId = new DatasetClassFacetRangeId(datasetClassFacetId, new Curie(rangeCurie));
        Range facetRange = rangeRepository.findOne(datasetClassFacetRangeId);
        Validate.notNull(facetRange, "Range with id '%s' not found", datasetClassFacetRangeId);
        return analiseDataset.retrieveRangeValues(dataset, facetRange, filters, page, size);
    }

    @RequestMapping(value = "/datasets/{datasetId}/classes/{classCurie}/facets/{facetCurie}/ranges", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public Range addRange(@Valid @RequestBody Range newRange, @PathVariable String datasetId,
        @PathVariable String classCurie, @PathVariable String facetCurie) {
        Dataset dataset = datasetRepository.findOne(datasetId);
        Validate.notNull(dataset, "Dataset with id '%s' not found", datasetId);
        DatasetClassId datasetClassId = new DatasetClassId(dataset, new Curie(classCurie));
        Class datasetClass = classRepository.findOne(datasetClassId);
        Validate.notNull(datasetClass, "Class with id '%s' not found", datasetClassId);
        DatasetClassFacetId datasetClassFacetId = new DatasetClassFacetId(datasetClassId, new Curie(facetCurie));
        Facet classFacet = facetRepository.findOne(datasetClassFacetId);
        Validate.notNull(classFacet, "Facet with id '%s' not found", datasetClassFacetId);
        newRange.setFacet(classFacet);
        logger.info("Creating Range: {}", newRange.toString());
        return rangeRepository.save(newRange);
    }
}
