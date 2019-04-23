package net.rhizomik.rhizomer.controller;

/**
 * Created by http://rhizomik.net/~roberto/
 */
import javax.validation.Valid;
import net.rhizomik.rhizomer.model.Admin;
import net.rhizomik.rhizomer.repository.AdminRepository;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdminController {
    final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Autowired AdminRepository adminRepository;

    @RequestMapping(value = "/admins", method = RequestMethod.GET)
    public @ResponseBody
    Iterable<Admin> listAdmins() {
        return adminRepository.findAll();
    }

    @RequestMapping(value = "/admins", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    public @ResponseBody
    Admin createAdmin(@Valid @RequestBody Admin newAdmin) {
        Validate.isTrue(!adminRepository.existsById(newAdmin.getUsername()),
            "Admin with id '%s' already exists", newAdmin.getUsername());
        logger.info("Creating Admin: {}", newAdmin.getUsername());
        newAdmin.encodePassword();
        return adminRepository.save(newAdmin);
    }

    @RequestMapping(value = "/admins/{adminId}", method = RequestMethod.GET)
    public @ResponseBody
    Admin retrieveAdmin(@PathVariable String adminId) {
        Admin admin = adminRepository.findById(adminId).orElseThrow(() ->
            new NullPointerException(String.format("Admin with id '%s' not found", adminId)));
        logger.info("Retrieved Admin {}", adminId);
        return admin;
    }

    @RequestMapping(value = "/admins/{adminId}", method = RequestMethod.PUT)
    public @ResponseBody
    Admin updateAdmin(@Valid @RequestBody Admin updatedAdmin, @PathVariable String adminId) {
        Admin admin = adminRepository.findById(adminId).orElseThrow(() ->
            new NullPointerException(String.format("Admin with id '%s' not found", adminId)));
        logger.info("Updating Admin: {}", adminId);
        admin.setPassword(updatedAdmin.getPassword());
        admin.encodePassword();
        return adminRepository.save(admin);
    }

    @RequestMapping(value = "/admins/{adminId}", method = RequestMethod.DELETE)
    @ResponseBody
    public void deleteAdmin(@PathVariable String adminId) {
        Admin admin = adminRepository.findById(adminId).orElseThrow(() ->
            new NullPointerException(String.format("Admin with id '%s' not found", adminId)));
        logger.info("Deleting Admin {}", adminId);
        adminRepository.delete(admin);
    }
}
