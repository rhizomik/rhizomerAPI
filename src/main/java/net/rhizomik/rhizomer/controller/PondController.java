package net.rhizomik.rhizomer.controller;

/**
 * Created by http://rhizomik.net/~roberto/
 */
import com.google.common.base.Preconditions;
import net.rhizomik.rhizomer.model.Pond;
import net.rhizomik.rhizomer.repository.PondRepository;
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

    @RequestMapping(value = "/ponds/{pondId}", method = RequestMethod.GET)
    public @ResponseBody Pond retrievePond(@PathVariable String pondId) throws Exception {
        Pond pond = pondRepository.findOne(pondId);
        Preconditions.checkNotNull(pond, "Pond with id {} not found", pondId);
        logger.info("Retrieved Pond {}", pondId);
        return pond;
    }

    @RequestMapping(value = "/ponds", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    public @ResponseBody Pond createPond(@Valid @RequestBody Pond newPond) throws Exception {
        Preconditions.checkState(!pondRepository.exists(newPond.getId()), "Pond with id {} already exists", newPond.getId());
        logger.info("Creating Pond: {}", newPond.toString());
        return pondRepository.save(newPond);
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
