package net.rhizomik.rhizomer.controller;

/**
 * Created by http://rhizomik.net/~roberto/
 */
import com.google.common.base.Preconditions;
import net.rhizomik.rhizomer.model.Pond;
import net.rhizomik.rhizomer.repository.PondRepository;
import net.rhizomik.rhizomer.service.SPARQLService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RepositoryRestController
public class PondController {
    final Logger logger = LoggerFactory.getLogger(PondController.class);

    @Autowired private PondRepository pondRepository;
    @Autowired private SPARQLService sparqlService;

    @RequestMapping(value = "/ponds", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    public @ResponseBody Pond createPond(@Valid @RequestBody Pond newPond) throws Exception {
        Preconditions.checkState(!pondRepository.exists(newPond.getId()), "Pond with id {} already exists", newPond.getId());
        logger.info("Creating Pond: {}", newPond.getId());
        return pondRepository.save(newPond);
    }

    @RequestMapping(value = "/ponds/{pondId}", method = RequestMethod.GET)
    public @ResponseBody Pond retrievePond(@PathVariable String pondId) throws Exception {
        Pond pond = pondRepository.findOne(pondId);
        Preconditions.checkNotNull(pond, "Pond with id {} not found", pondId);
        logger.info("Retrieved Pond {}", pondId);
        return pond;
    }

    @RequestMapping(value = "/ponds/{pondId}", method = RequestMethod.PUT)
    public @ResponseBody Pond updatePond(@Valid @RequestBody Pond updatedPond, @PathVariable String pondId) throws Exception {
        Pond pond = pondRepository.findOne(pondId);
        Preconditions.checkNotNull(pond, "Pond with id {} not found", pondId);
        logger.info("Updating Pond: {}", pondId);
        pond.setSparqlEndPoint(updatedPond.getSparqlEndPoint());
        pond.setQueryType(updatedPond.getQueryType());
        pond.setInferenceEnabled(updatedPond.isInferenceEnabled());
        pond.setSampleSize(updatedPond.getSampleSize());
        pond.setCoverage(updatedPond.getCoverage());
        return pondRepository.save(pond);
    }

    @RequestMapping(value = "/ponds/{pondId}", method = RequestMethod.DELETE)
    @ResponseBody
    public void deletePond(@PathVariable String pondId) throws Exception {
        Pond pond = pondRepository.findOne(pondId);
        Preconditions.checkNotNull(pond, "Pond with id {} not found", pondId);
        logger.info("Deleting Pond {}", pondId);
        pondRepository.delete(pond);
    }
}
