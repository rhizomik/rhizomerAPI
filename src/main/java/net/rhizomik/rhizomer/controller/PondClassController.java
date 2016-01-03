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
public class PondClassController {
    final Logger logger = LoggerFactory.getLogger(PondClassController.class);

    @Autowired private PondRepository pondRepository;
    @Autowired private ClassRepository classRepository;
    @Autowired private AnalizeDataset analiseDataset;

    @RequestMapping(value = "/ponds/{pondId}/classes", method = RequestMethod.GET)
    public @ResponseBody List<Class> listPondClass(@PathVariable String pondId) throws Exception {
        Pond pond = pondRepository.findOne(pondId);
        Preconditions.checkNotNull(pond, "Pond with id %s not found", pondId);
        logger.info("Retrieving classes in Pond %s", pondId);
        if (pond.getClasses().isEmpty() && pond.getServer()!=null )
            analiseDataset.detectPondClasses(pond);
        //Pond savedPond = pondRepository.save(pond);
        return pond.getClasses();
    }

    @RequestMapping(value = "/ponds/{pondId}/classes/{classId}", method = RequestMethod.GET)
    public @ResponseBody Class retrievePondClass(@PathVariable String pondId, @PathVariable String classId) throws Exception {
        Pond pond = pondRepository.findOne(pondId);
        Preconditions.checkNotNull(pond, "Pond with id %s not found", pondId);
        Class pondClass = classRepository.findOne(new PondClassId(pond, new Curie(classId)));
        Preconditions.checkNotNull(pondClass, "Class %s in Pond %s not found", classId, pondId);
        logger.info("Retrieved Class %s in Pond %s", classId, pondId);
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
        logger.info("Creating Class: %s", newClass.toString());
        newClass.setPond(pond);
        return classRepository.save(newClass);
    }

    /*@RequestMapping(value = "/meetingProposals/{id}", method = RequestMethod.PUT)
    @ResponseBody
    public HttpEntity<MeetingProposal> updateMeeting(@PathVariable String id, @Valid @RequestBody MeetingProposal meetingProposal, @RequestParam(value = "key", required = false) String key) throws Exception {
        MeetingProposal oldMeeting = repository.findOne(UUID.fromString(id));
        if (oldMeeting != null) {
            if (!oldMeeting.isAdmin(key))
                throw new InvalidKeyException();
            oldMeeting.setTitle(meetingProposal.getTitle());
            oldMeeting.setDescription(meetingProposal.getDescription());
            oldMeeting.setOrganizer(meetingProposal.getOrganizer());
            oldMeeting.setSlotDuration(meetingProposal.getSlotDuration());
            oldMeeting.setIsOpen(meetingProposal.getIsOpen());
            repository.save(oldMeeting);
            return ResponseEntity.ok().body(oldMeeting);
        } else {
            throw new NullPointerException();
        }
    }

    @RequestMapping(value = "/meetingProposals/{id}", method = RequestMethod.DELETE)
    @ResponseBody
    public HttpEntity<Void> deleteMeeting(@PathVariable String id, @RequestParam(value = "key", required = false) String key) throws Exception {
        MeetingProposal meetingProposal = repository.findOne(UUID.fromString(id));
        if (meetingProposal != null) {
            if (!meetingProposal.isAdmin(key))
                throw new InvalidKeyException();
            repository.delete(UUID.fromString(id));
            return ResponseEntity.ok().build();
        } else {
            throw new NullPointerException();
        }
    }*/
}
