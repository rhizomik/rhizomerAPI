package net.rhizomik.rhizomer.controller;

/**
 * Created by http://rhizomik.net/~roberto/
 */
import com.google.common.base.Preconditions;
import net.rhizomik.rhizomer.model.Class;
import net.rhizomik.rhizomer.model.Curie;
import net.rhizomik.rhizomer.model.Pond;
import net.rhizomik.rhizomer.model.PondClassId;
import net.rhizomik.rhizomer.repository.ClassRepository;
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
public class ClassController {
    final Logger logger = LoggerFactory.getLogger(ClassController.class);

    @Autowired private PondRepository pondRepository;
    @Autowired private ClassRepository classRepository;
    @Autowired private AnalizeDataset analiseDataset;

    @RequestMapping(value = "/ponds/{pondId}/classes", method = RequestMethod.GET)
    public @ResponseBody List<Class> listPondClass(@PathVariable String pondId) throws Exception {
        Pond pond = pondRepository.findOne(pondId);
        Preconditions.checkNotNull(pond, "Pond with id %s not found", pondId);
        logger.info("Retrieving classes in Pond {}", pondId);
        if (pond.getClasses().isEmpty() && pond.getSparqlEndPoint()!=null )
            analiseDataset.detectPondClasses(pond);
        //Pond savedPond = pondRepository.save(pond);
        return pond.getClasses();
    }

    @RequestMapping(value = "/ponds/{pondId}/classes/{classCurie}", method = RequestMethod.GET)
    public @ResponseBody Class retrievePondClass(@PathVariable String pondId, @PathVariable String classCurie) throws Exception {
        Pond pond = pondRepository.findOne(pondId);
        Preconditions.checkNotNull(pond, "Pond with id %s not found", pondId);
        Class pondClass = classRepository.findOne(new PondClassId(pond, new Curie(classCurie)));
        Preconditions.checkNotNull(pondClass, "Class %s in Pond %s not found", classCurie, pondId);
        logger.info("Retrieved Class {} in Pond {}", classCurie, pondId);
        return pondClass;
    }

    @RequestMapping(value = "/ponds/{pondId}/classes", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public Class createPondClass(@Valid @RequestBody Class newClass, @PathVariable String pondId) throws Exception {
        Pond pond = pondRepository.findOne(pondId);
        Preconditions.checkNotNull(pond, "Pond with id %s not found", pondId);
        Preconditions.checkState(!classRepository.exists(new PondClassId(pond, new Curie(newClass.getUri()))),
                "Class with URI %s already exists in Pond %s", newClass.getUri(), pondId);
        newClass.setPond(pond);
        logger.info("Creating Class: {}", newClass.toString());
        return classRepository.save(newClass);
    }
}
