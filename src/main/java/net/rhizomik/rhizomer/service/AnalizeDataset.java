package net.rhizomik.rhizomer.service;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Resource;
import net.rhizomik.rhizomer.model.Class;
import net.rhizomik.rhizomer.model.Pond;
import net.rhizomik.rhizomer.model.Queries;
import net.rhizomik.rhizomer.repository.ClassRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by http://rhizomik.net/~roberto/
 */
@Service
public class AnalizeDataset {
    final Logger logger = LoggerFactory.getLogger(AnalizeDataset.class);

    @Autowired private SPARQLService sparqlService;
    @Autowired private ClassRepository classRepository;

    public void detectPondClasses(Pond pond){
        if (pond.isInferenceEnabled())
            sparqlService.inferTypes(pond);

        ResultSet result = sparqlService.querySelect(pond.getServer(), Queries.getQueryClasses(pond.getQueryType()),
                                              pond.getPondGraphsStrings(), null);
        while (result.hasNext()) {
            QuerySolution soln = result.nextSolution();
            Resource r = soln.getResource("?class");
            int count = soln.getLiteral("?n").getInt();
            try {
                Class detectedClass = new Class(pond, new URI(r.getURI()), r.getLocalName(), count);
                pond.addClass(classRepository.save(detectedClass));
                logger.info("Added detected class {} to pond {}", detectedClass.getId().getClassCurie(), pond.getId());
            } catch (URISyntaxException e) {
                logger.error("URI syntax error: {}", r.getURI());
            }
        }
    }
}
