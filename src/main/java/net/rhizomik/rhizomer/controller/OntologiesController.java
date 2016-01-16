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
import java.net.URI;
import java.util.List;
import java.util.Set;

@RepositoryRestController
public class OntologiesController {
    final Logger logger = LoggerFactory.getLogger(OntologiesController.class);

    @Autowired private PondRepository pondRepository;
    @Autowired private SPARQLService sparqlService;

    @RequestMapping(value = "/ponds/{pondId}/ontologies", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    public @ResponseBody List<String> addPondOntology(@RequestBody List<String> ontologies, @PathVariable String pondId) throws Exception {
        Pond pond = pondRepository.findOne(pondId);
        Preconditions.checkNotNull(pond, "Pond with id {} not found", pondId);
        ontologies.forEach(ontology -> {
            sparqlService.loadData(pond.getSparqlEndPoint(), pond.getPondOntologiesGraph().toString(), ontology);
            pond.addPondOntology(ontology);
        });
        logger.info("Added ontologies {} to Pond {}", ontologies.toString(), pondId);
        return pondRepository.save(pond).getPondOntologies();
    }

    @RequestMapping(value = "/ponds/{pondId}/ontologies", method = RequestMethod.GET)
    public @ResponseBody List<String> retrievePondOntologies(@PathVariable String pondId) throws Exception {
        Pond pond = pondRepository.findOne(pondId);
        Preconditions.checkNotNull(pond, "Pond with id {} not found", pondId);
        logger.info("Retrieved ontologies for Pond {}", pondId);
        return pond.getPondOntologies();
    }

    @RequestMapping(value = "/ponds/{pondId}/ontologies", method = RequestMethod.PUT)
    public @ResponseBody List<String> updatePondOntologies(@Valid @RequestBody Set<String> updatedOntologies, @PathVariable String pondId) throws Exception {
        Pond pond = pondRepository.findOne(pondId);
        Preconditions.checkNotNull(pond, "Pond with id {} not found", pondId);
        pond.setPondOntologies(updatedOntologies);
        URI pondOntologiesGraph = pond.getPondOntologiesGraph();
        sparqlService.clearGraph(pond.getSparqlEndPoint(), pondOntologiesGraph);
        updatedOntologies.forEach(ontologyUriStr ->
                sparqlService.loadData(pond.getSparqlEndPoint(), pondOntologiesGraph.toString(), ontologyUriStr));
        logger.info("Updated Pond {} ontologies", pondId);
        return pondRepository.save(pond).getPondOntologies();
    }
}
