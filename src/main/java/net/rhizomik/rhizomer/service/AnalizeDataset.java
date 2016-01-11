package net.rhizomik.rhizomer.service;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Resource;
import net.rhizomik.rhizomer.model.*;
import net.rhizomik.rhizomer.model.Class;
import net.rhizomik.rhizomer.repository.ClassRepository;
import net.rhizomik.rhizomer.repository.FacetRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;

/**
 * Created by http://rhizomik.net/~roberto/
 */
@Service
public class AnalizeDataset {
    final Logger logger = LoggerFactory.getLogger(AnalizeDataset.class);

    @Autowired private SPARQLService sparqlService;
    @Autowired private ClassRepository classRepository;
    @Autowired private FacetRepository facetRepository;

    public void detectPondClasses(Pond pond){
        if (pond.isInferenceEnabled())
            sparqlService.inferTypes(pond);

        ResultSet result = sparqlService.querySelect(pond.getSparqlEndPoint(), Queries.getQueryClasses(pond.getQueryType()),
                                              pond.getPondGraphs(), null);
        while (result.hasNext()) {
            QuerySolution soln = result.nextSolution();
            Resource r = soln.getResource("?class");
            int count = soln.getLiteral("?n").getInt();
            try {
                Class detectedClass = new Class(pond, new URI(r.getURI()), r.getLocalName(), count);
                pond.addClass(classRepository.save(detectedClass));
                logger.info("Added detected Class {} to Pond {}", detectedClass.getId().getClassCurie(), pond.getId());
            } catch (URISyntaxException e) {
                logger.error("URI syntax error: {}", r.getURI());
            }
        }
    }

    public void detectClassFacets(Class pondClass) {
        ResultSet result = sparqlService.querySelect(pondClass.getPond().getSparqlEndPoint(),
                Queries.getQueryClassFacets(pondClass.getUri().toString(), pondClass.getPond().getQueryType(),
                        pondClass.getPond().getSampleSize(), pondClass.getInstanceCount(), pondClass.getPond().getCoverage()),
                        pondClass.getPond().getPondGraphs(), null);

        while (result.hasNext()) {
            QuerySolution soln = result.nextSolution();
            if (soln.contains("?property")) {
                Resource r = soln.getResource("?property");
                int uses = soln.getLiteral("?uses").getInt();
                int values = soln.getLiteral("?values").getInt();
                String[] ranges = {};
                if (soln.contains("?ranges"))
                    ranges = Pattern.compile(",").splitAsStream(soln.getLiteral("?ranges").getString())
                                                    .map(Curie::uriStrToCurie).toArray(String[]::new);
                boolean allLiteralBoolean;
                Literal allLiteral = soln.getLiteral("?allLiteral");
                if(allLiteral.getDatatypeURI().equals("http://www.w3.org/2001/XMLSchema#integer"))
                    allLiteralBoolean = (allLiteral.getInt() != 0);
                else
                    allLiteralBoolean = allLiteral.getBoolean();
                try {
                    Facet detectedFacet = new Facet(pondClass, new URI(r.getURI()), r.getLocalName(), uses, values, ranges, allLiteralBoolean);
                    pondClass.addFacet(facetRepository.save(detectedFacet));
                    logger.info("Added detected Facet {} to Class {} in Pond",
                            detectedFacet.getId().getFacetCurie(), pondClass.getId().getClassCurie(), pondClass.getPond().getId());

                } catch (URISyntaxException e) {
                    logger.error("URI syntax error: {}", r.getURI());
                }
            }
        }
    }
}
