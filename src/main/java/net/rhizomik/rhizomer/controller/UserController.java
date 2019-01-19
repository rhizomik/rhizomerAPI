package net.rhizomik.rhizomer.controller;

/**
 * Created by http://rhizomik.net/~roberto/
 */
import javax.validation.Valid;
import net.rhizomik.rhizomer.model.User;
import net.rhizomik.rhizomer.repository.UserRepository;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {
    final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired UserRepository userRepository;

    @RequestMapping(value = "/user", method = RequestMethod.GET)
    public @ResponseBody
    User user() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        logger.debug("Retrieve info about logged in user {}", user.getUsername());
        return user;
    }

    @RequestMapping(value = "/users", method = RequestMethod.GET)
    public @ResponseBody
    Iterable<User> listUsers() {
        return userRepository.findAll();
    }

    @RequestMapping(value = "/users/search", method = RequestMethod.GET)
    public @ResponseBody
    Iterable<User> searchUsers(@RequestParam("text") String text) {
        return userRepository.findByUsernameContaining(text);
    }

    @RequestMapping(value = "/users", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    public @ResponseBody
    User createUser(@Valid @RequestBody User newUser) {
        Validate.isTrue(!userRepository.exists(newUser.getUsername()),
            "User with id '%s' already exists", newUser.getUsername());
        logger.info("Creating User: {}", newUser.getUsername());
        newUser.encodePassword();
        return userRepository.save(newUser);
    }

    @RequestMapping(value = "/users/{userId}", method = RequestMethod.GET)
    public @ResponseBody
    User retrieveUser(@PathVariable String userId) {
        User user = userRepository.findOne(userId);
        Validate.notNull(user, "User with id '%s' not found", userId);
        logger.info("Retrieved User {}", userId);
        return user;
    }

    @RequestMapping(value = "/users/{userId}", method = RequestMethod.PUT)
    public @ResponseBody
    User updateUser(@Valid @RequestBody User updatedUser, @PathVariable String userId) {
        User user = userRepository.findOne(userId);
        Validate.notNull(user, "User with id '%s' not found", userId);
        logger.info("Updating User: {}", userId);
        user.setPassword(updatedUser.getPassword());
        user.encodePassword();
        return userRepository.save(user);
    }

    @RequestMapping(value = "/users/{userId}", method = RequestMethod.DELETE)
    @ResponseBody
    public void deleteUser(@PathVariable String userId) {
        User user = userRepository.findOne(userId);
        Validate.notNull(user, "User with id '%s' not found", userId);
        logger.info("Deleting User {}", userId);
        userRepository.delete(user);
    }
}
