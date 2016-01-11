package net.rhizomik.rhizomer.controller;

/**
 * Created by http://rhizomik.net/~roberto/
 */
import com.google.common.base.Preconditions;
import net.rhizomik.rhizomer.model.Class;
import net.rhizomik.rhizomer.model.*;
import net.rhizomik.rhizomer.repository.ClassRepository;
import net.rhizomik.rhizomer.repository.FacetRepository;
import net.rhizomik.rhizomer.repository.PondRepository;
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

    @Autowired private PondRepository pondRepository;
    @Autowired private ClassRepository classRepository;
    @Autowired private FacetRepository facetRepository;
    @Autowired private AnalizeDataset analiseDataset;

    @RequestMapping(value = "/ponds/{pondId}/classes/{classCurie}/facets", method = RequestMethod.GET)
    public @ResponseBody List<Facet> listClassFacets(@PathVariable String pondId, @PathVariable String classCurie) throws Exception {
        Pond pond = pondRepository.findOne(pondId);
        Preconditions.checkNotNull(pond, "Pond with id %s not found", pondId);
        PondClassId pondClassId = new PondClassId(pond, new Curie(classCurie));
        Class pondClass = classRepository.findOne(pondClassId);
        Preconditions.checkNotNull(pondClass, "Class with id %s not found", pondClassId);
        logger.info("Retrieving facets for Class {} in Pond {}", classCurie, pondId);
        if (pondClass.getFacets().isEmpty() && pond.getSparqlEndPoint()!=null )
            analiseDataset.detectClassFacets(pondClass);
        return pondClass.getFacets();
    }

    @RequestMapping(value = "/ponds/{pondId}/classes/{classCurie}/facets/{facetCurie}", method = RequestMethod.GET)
    public @ResponseBody Facet retrieveClassFacet(@PathVariable String pondId, @PathVariable String classCurie, @PathVariable String facetCurie) throws Exception {
        Pond pond = pondRepository.findOne(pondId);
        Preconditions.checkNotNull(pond, "Pond with id %s not found", pondId);
        PondClassId pondClassId = new PondClassId(pond, new Curie(classCurie));
        Class pondClass = classRepository.findOne(pondClassId);
        Preconditions.checkNotNull(pondClass, "Class with id %s not found", pondClassId);
        PondClassFacetId pondClassFacetId = new PondClassFacetId(pondClassId, new Curie(facetCurie));
        Facet classFacet = facetRepository.findOne(pondClassFacetId);
        Preconditions.checkNotNull(classFacet, "Facet with id %s not found", pondClassFacetId);
        logger.info("Retrieved Facet {} for Class {} in Pond {}", facetCurie, classCurie, pondId);
        return classFacet;
    }

    @RequestMapping(value = "/ponds/{pondId}/classes/{classCurie}/facets", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public Facet createClassFacet(@Valid @RequestBody Facet newFacet, @PathVariable String pondId, @PathVariable String classCurie) throws Exception {
        Pond pond = pondRepository.findOne(pondId);
        Preconditions.checkNotNull(pond, "Pond with id %s not found", pondId);
        PondClassId pondClassId = new PondClassId(pond, new Curie(classCurie));
        Class pondClass = classRepository.findOne(pondClassId);
        Preconditions.checkNotNull(pondClass, "Class with id %s not found", pondClassId);
        PondClassFacetId pondClassFacetId = new PondClassFacetId(pondClassId, new Curie(newFacet.getUri()));
        Preconditions.checkState(!facetRepository.exists(pondClassFacetId),
                "Facet with URI %s already exists for Class %s in Pond %s", newFacet.getUri(), classCurie, pondId);
        newFacet.setDomain(pondClass);
        logger.info("Creating Facet: {}", newFacet.toString());
        return facetRepository.save(newFacet);
    }
}
