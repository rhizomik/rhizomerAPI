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
import net.rhizomik.rhizomer.service.SecurityController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
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
    @Autowired private SecurityController securityController;

    @RequestMapping(value = "/datasets/{datasetId}/classes/{classCurie}/facets/{facetCurie}/ranges",
        method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody List<Range> getRanges(@PathVariable String datasetId,
        @PathVariable String classCurie, @PathVariable String facetCurie, Authentication auth) {
        Dataset dataset = getDataset(datasetId);
        securityController.checkPublicOrOwner(dataset, auth);
        Class datasetClass = getClass(classCurie, dataset);
        Facet classFacet = getFacet(facetCurie, datasetClass.getId());
        return classFacet.getRanges();
    }

    @RequestMapping(method = RequestMethod.GET,
        value = "/datasets/{datasetId}/classes/{classCurie}/facets/{facetCurie}/ranges/{rangeCurie}/values")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody List<Value> getRangeValues(@PathVariable String datasetId,
        @PathVariable String classCurie,
        @PathVariable String facetCurie, @PathVariable String rangeCurie,
        @RequestParam MultiValueMap<String, String> filters, Authentication auth,
        @RequestParam(value="page", defaultValue="0") int page,
        @RequestParam(value="size", defaultValue="10") int size) {
        Dataset dataset = getDataset(datasetId);
        securityController.checkPublicOrOwner(dataset, auth);
        Class datasetClass = getClass(classCurie, dataset);
        Facet classFacet = getFacet(facetCurie, datasetClass.getId());
        Range facetRange = getRange(rangeCurie, classFacet);
        return analiseDataset.retrieveRangeValues(dataset, facetRange, filters, page, size);
    }

    @RequestMapping(value = "/datasets/{datasetId}/classes/{classCurie}/facets/{facetCurie}/ranges",
        method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    public @ResponseBody Range addRange(@Valid @RequestBody Range newRange,
        @PathVariable String datasetId, @PathVariable String classCurie,
        @PathVariable String facetCurie, Authentication auth) {
        Dataset dataset = getDataset(datasetId);
        securityController.checkOwner(dataset, auth);
        Class datasetClass = getClass(classCurie, dataset);
        Facet classFacet = getFacet(facetCurie, datasetClass.getId());
        newRange.setFacet(classFacet);
        logger.info("Creating Range: {}", newRange.toString());
        return rangeRepository.save(newRange);
    }

    private Dataset getDataset(String datasetId) {
        return datasetRepository
            .findById(datasetId)
            .orElseThrow(() ->
                new NullPointerException(String.format("Dataset with id '%s' not found", datasetId)));
    }

    private Class getClass(String classCurie, Dataset dataset) {
        return classRepository
            .findById(new DatasetClassId(dataset, new Curie(classCurie)))
            .orElseThrow(() ->
                new NullPointerException(
                    String.format("Class '%s' in Dataset '%s' not found", classCurie, dataset.getId())));
    }

    private Facet getFacet(String facetCurie, DatasetClassId datasetClassId) {
        DatasetClassFacetId datasetClassFacetId = new DatasetClassFacetId(datasetClassId, new Curie(facetCurie));
        return facetRepository.findById(datasetClassFacetId).orElseThrow(() ->
            new NullPointerException(
                String.format("Facet with id '%s' not found", datasetClassFacetId)));
    }

    private Range getRange(@PathVariable String rangeCurie,
        Facet classFacet) {
        DatasetClassFacetRangeId datasetClassFacetRangeId =
            new DatasetClassFacetRangeId(classFacet.getId(), new Curie(rangeCurie));
        return rangeRepository.findById(datasetClassFacetRangeId).orElseThrow(() ->
            new NullPointerException(String.format("Range with id '%s' not found", datasetClassFacetRangeId)));
    }
}
