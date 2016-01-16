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
import java.util.List;
import java.util.Set;

@RepositoryRestController
public class GraphController {
    final Logger logger = LoggerFactory.getLogger(GraphController.class);

    @Autowired private PondRepository pondRepository;
    @Autowired private SPARQLService sparqlService;

    @RequestMapping(value = "/ponds/{pondId}/graphs", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    public @ResponseBody List<String> addPondGraph(@RequestBody List<String> addGraphs, @PathVariable String pondId) throws Exception {
        Pond pond = pondRepository.findOne(pondId);
        Preconditions.checkNotNull(pond, "Pond with id {} not found", pondId);
        addGraphs.forEach(graph -> pond.addPondGraph(graph));
        logger.info("Added graphs {} to Pond {}", addGraphs.toString(), pondId);
        return pondRepository.save(pond).getPondGraphs();
    }

    @RequestMapping(value = "/ponds/{pondId}/graphs", method = RequestMethod.GET)
    public @ResponseBody List<String> retrievePondGraphs(@PathVariable String pondId) throws Exception {
        Pond pond = pondRepository.findOne(pondId);
        Preconditions.checkNotNull(pond, "Pond with id {} not found", pondId);
        logger.info("Retrieved graphs for Pond {}", pondId);
        return pond.getPondGraphs();
    }

    @RequestMapping(value = "/ponds/{pondId}/graphs", method = RequestMethod.PUT)
    public @ResponseBody List<String> updatePondGraphs(@Valid @RequestBody Set<String> updatedGraphs, @PathVariable String pondId) throws Exception {
        Pond pond = pondRepository.findOne(pondId);
        Preconditions.checkNotNull(pond, "Pond with id {} not found", pondId);
        pond.setPondGraphs(updatedGraphs);
        logger.info("Updated Pond {} graphs to {}", pondId, updatedGraphs.toString());
        return pondRepository.save(pond).getPondGraphs();
    }
}
