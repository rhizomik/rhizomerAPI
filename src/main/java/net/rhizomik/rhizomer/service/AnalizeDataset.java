package net.rhizomik.rhizomer.service;

import net.rhizomik.rhizomer.model.Class;
import net.rhizomik.rhizomer.model.*;
import net.rhizomik.rhizomer.repository.ClassRepository;
import net.rhizomik.rhizomer.repository.FacetRepository;
import net.rhizomik.rhizomer.repository.RangeRepository;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by http://rhizomik.net/~roberto/
 */
@Service
public class AnalizeDataset {
    final Logger logger = LoggerFactory.getLogger(AnalizeDataset.class);

    @Autowired private SPARQLService sparqlService;
    @Autowired private ClassRepository classRepository;
    @Autowired private FacetRepository facetRepository;
    @Autowired private RangeRepository rangeRepository;

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
                Resource property = soln.getResource("?property");
                Resource range = soln.getResource("?range");
                int uses = soln.getLiteral("?uses").getInt();
                int values = soln.getLiteral("?values").getInt();
                boolean allLiteralBoolean;
                Literal allLiteral = soln.getLiteral("?allLiteral");
                if(allLiteral.getDatatypeURI().equals("http://www.w3.org/2001/XMLSchema#integer"))
                    allLiteralBoolean = (allLiteral.getInt() != 0);
                else
                    allLiteralBoolean = allLiteral.getBoolean();
                try {
                    Facet detectedFacet;
                    URI propertyUri = new URI(property.getURI());
                    DatasetClassFacetId datasetClassFacetId = new DatasetClassFacetId(datasetClass.getId(), propertyUri);
                    if (facetRepository.exists(datasetClassFacetId))
                        detectedFacet = facetRepository.findOne(datasetClassFacetId);
                    else {
                        detectedFacet = facetRepository.save(new Facet(datasetClass, propertyUri, property.getLocalName()));
                        datasetClass.addFacet(detectedFacet);
                        logger.info("Added detected Facet {} to Class {} in Dataset",
                                detectedFacet.getId().getFacetCurie(), datasetClass.getId().getClassCurie(),
                                datasetClass.getDataset().getId());
                    }
                    URI rangeUri = new URI(range.getURI());
                    Range detectedRange = new Range(detectedFacet, rangeUri, range.getLocalName(), uses, values, allLiteralBoolean);
                    detectedFacet.addRange(rangeRepository.save(detectedRange));
                    facetRepository.save(detectedFacet);
                    logger.info("Added detected Range {} to Facet {} for Class {} in Dataset",
                            detectedRange.getId().getRangeCurie(), detectedFacet.getId().getFacetCurie(),
                            datasetClass.getId().getClassCurie(), datasetClass.getDataset().getId());
                } catch (URISyntaxException e) {
                    logger.error("URI syntax error: {}", property.getURI());
                }
            }
        }
    }

    public List<URI> listServerGraphs(URL sparqlEndPoint) {
        ResultSet result = sparqlService.querySelect(sparqlEndPoint, Queries.getQueryGraphs());
        List<URI> graphs = new ArrayList<>();
        while (result.hasNext()) {
            QuerySolution soln = result.nextSolution();
            if (soln.contains("?graph")) {
                Resource graph = soln.getResource("?graph");
                graphs.add(URI.create(graph.getURI()));
            }
        }
        return graphs;
    }
}
