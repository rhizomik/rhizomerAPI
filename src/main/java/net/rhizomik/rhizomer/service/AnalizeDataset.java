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

    public void detectDatasetClasses(Dataset dataset){
        if (dataset.isInferenceEnabled())
            sparqlService.inferTypes(dataset);

        ResultSet result = sparqlService.querySelect(dataset.getSparqlEndPoint(), Queries.getQueryClasses(dataset.getQueryType()),
                                              dataset.getDatasetGraphs(), null);
        while (result.hasNext()) {
            QuerySolution soln = result.nextSolution();
            Resource r = soln.getResource("?class");
            int count = soln.getLiteral("?n").getInt();
            try {
                Class detectedClass = new Class(dataset, new URI(r.getURI()), r.getLocalName(), count);
                dataset.addClass(classRepository.save(detectedClass));
                logger.info("Added detected Class {} to Dataset {}", detectedClass.getId().getClassCurie(), dataset.getId());
            } catch (URISyntaxException e) {
                logger.error("URI syntax error: {}", r.getURI());
            }
        }
    }

    public void detectClassFacets(Class datasetClass) {
        ResultSet result = sparqlService.querySelect(datasetClass.getDataset().getSparqlEndPoint(),
                Queries.getQueryClassFacets(datasetClass.getUri().toString(), datasetClass.getDataset().getQueryType(),
                        datasetClass.getDataset().getSampleSize(), datasetClass.getInstanceCount(), datasetClass.getDataset().getCoverage()),
                        datasetClass.getDataset().getDatasetGraphs(), null);

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
                    Facet detectedFacet = new Facet(datasetClass, new URI(r.getURI()), r.getLocalName(), uses, values, ranges, allLiteralBoolean);
                    datasetClass.addFacet(facetRepository.save(detectedFacet));
                    logger.info("Added detected Facet {} to Class {} in Dataset",
                            detectedFacet.getId().getFacetCurie(), datasetClass.getId().getClassCurie(), datasetClass.getDataset().getId());

                } catch (URISyntaxException e) {
                    logger.error("URI syntax error: {}", r.getURI());
                }
            }
        }
    }
}
