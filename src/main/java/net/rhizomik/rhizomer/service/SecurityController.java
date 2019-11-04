package net.rhizomik.rhizomer.service;

import net.rhizomik.rhizomer.model.Admin;
import net.rhizomik.rhizomer.model.Dataset;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

/**
 * Created by http://rhizomik.net/~roberto/
 */
@Service
public class SecurityController {

    public void checkPublicOrOwner(Dataset dataset, Authentication auth)
        throws AuthorizationServiceException{
        if (dataset.isPublic())
            return;
        checkOwner(dataset, auth);
    }

    public void checkOwner(Dataset dataset, Authentication auth)
        throws AuthorizationServiceException {
        if (auth == null)
            throw new AuthorizationServiceException("Not authorized to access dataset "+ dataset.getId());
        if (auth.isAuthenticated() &&
            !(auth.getPrincipal() instanceof Admin) &&
            !dataset.getOwner().equals(auth.getName()))
            throw new AuthorizationServiceException("User " + auth.getName() +
                " not authorized to access dataset "+ dataset.getId());
    }
}
