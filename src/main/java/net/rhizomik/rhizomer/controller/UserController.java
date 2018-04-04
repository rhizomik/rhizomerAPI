package net.rhizomik.rhizomer.controller;

/**
 * Created by http://rhizomik.net/~roberto/
 */
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
public class UserController {
    final Logger logger = LoggerFactory.getLogger(UserController.class);

    @RequestMapping(value = "/user", method = RequestMethod.GET)
    public @ResponseBody
    Principal user(Principal principal) {
        logger.debug("Retrieve info about logged in user {}", principal.getName());
        return principal;
    }
}
