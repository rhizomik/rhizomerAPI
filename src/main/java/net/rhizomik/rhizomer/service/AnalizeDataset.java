package net.rhizomik.rhizomer.service;

import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.rhizomik.rhizomer.model.Class;
import net.rhizomik.rhizomer.model.Dataset;
import net.rhizomik.rhizomer.model.Facet;
import net.rhizomik.rhizomer.model.Range;
import net.rhizomik.rhizomer.model.Value;
import net.rhizomik.rhizomer.model.id.DatasetClassFacetId;
import net.rhizomik.rhizomer.repository.ClassRepository;
import net.rhizomik.rhizomer.repository.FacetRepository;
import net.rhizomik.rhizomer.repository.RangeRepository;
import net.rhizomik.rhizomer.service.Queries.QueryType;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RiotException;
import org.apache.jena.vocabulary.XSD;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

/**
 * Created by http://rhizomik.net/~roberto/
 */
@Service
public class AnalizeDataset {
    final Logger logger = LoggerFactory.getLogger(AnalizeDataset.class);

    @org.springframework.beans.factory.annotation.Value("${rhizomer.omit.properties}")
    String[] omittedProperties;
    @org.springframework.beans.factory.annotation.Value("${rhizomer.omit.classes}")
    String[] omittedClasses;

    @Autowired private PrefixCCMap prefixCCMap;
    @Autowired private SPARQLService sparqlService;
    @Autowired private OptimizedQueries optimizedQueries;
    @Autowired private DetailedQueries detailedQueries;
    @Autowired private ClassRepository classRepository;
    @Autowired private FacetRepository facetRepository;
    @Autowired private RangeRepository rangeRepository;

    private Queries queries(Queries.QueryType queryType) {
        if (queryType == QueryType.DETAILED)
            return detailedQueries;
        else
            return optimizedQueries;
    }

    public void detectDatasetClasses(Dataset dataset){
        if (dataset.isInferenceEnabled())
            sparqlService.inferTypes(dataset);

        ResultSet result = sparqlService.querySelect(dataset.getSparqlEndPoint(),
            queries(dataset.getQueryType()).getQueryClasses(), dataset.getDatasetGraphs(), null);
        while (result.hasNext()) {
            QuerySolution soln = result.nextSolution();
            if (!soln.contains("?class")) continue;
            Resource r = soln.getResource("?class");
            if (isOmittedClass(r.getURI())) continue;
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

    private boolean isOmittedClass(String uri) {
        return Arrays.stream(omittedClasses).anyMatch(uri::contains);
    }

    public void detectClassFacets(Class datasetClass) {
        ResultSet result = sparqlService.querySelect(datasetClass.getDataset().getSparqlEndPoint(),
            queries(datasetClass.getDataset().getQueryType()).getQueryClassFacets(
                datasetClass.getUri().toString(), datasetClass.getDataset().getSampleSize(),
                datasetClass.getInstanceCount(), datasetClass.getDataset().getCoverage()),
            datasetClass.getDataset().getDatasetGraphs(), null);

        while (result.hasNext()) {
            QuerySolution soln = result.nextSolution();
            if (!soln.contains("?property")) continue;
            Resource property = soln.getResource("?property");
            if (isOmittedProperty(property.getURI())) continue;
            String range = XSD.xstring.toString();
            if (soln.contains("?range"))
                range = soln.get("?range").toString();
            int uses = soln.getLiteral("?uses").getInt();
            int values = soln.getLiteral("?values").getInt();
            boolean allLiteralBoolean = false;
            if (soln.contains("?allLiteral")) {
                Literal allLiteral = soln.getLiteral("?allLiteral");
                if (allLiteral.getDatatype().equals(XSDDatatype.XSDboolean))
                    allLiteralBoolean = allLiteral.getBoolean();
                else
                    allLiteralBoolean = (allLiteral.getInt() != 0);
            }
            try {
                Facet detectedFacet;
                URI propertyUri = new URI(property.getURI());
                DatasetClassFacetId datasetClassFacetId = new DatasetClassFacetId(datasetClass.getId(), propertyUri);
                if (facetRepository.existsById(datasetClassFacetId))
                    detectedFacet = facetRepository.findById(datasetClassFacetId).get();
                else {
                    detectedFacet = facetRepository.save(new Facet(datasetClass, propertyUri, property.getLocalName()));
                    datasetClass.addFacet(detectedFacet);
                    logger.info("Added detected Facet {} to Class {} in Dataset",
                            detectedFacet.getId().getFacetCurie(), datasetClass.getId().getClassCurie(),
                            datasetClass.getDataset().getId());
                }
                URI rangeUri = new URI(range);
                String rangeLabel = prefixCCMap.localName(range);
                Range detectedRange = new Range(detectedFacet, rangeUri, rangeLabel, uses, values, allLiteralBoolean);
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

    private boolean isOmittedProperty(String uri) {
        return Arrays.stream(omittedProperties).anyMatch(uri::contains);
    }

    public List<Value> retrieveRangeValues(Dataset dataset, Range facetRange,
            MultiValueMap<String, String> filters, int page, int size) {
        URI classUri = facetRange.getFacet().getDomain().getUri();
        URI facetUri = facetRange.getFacet().getUri();
        ResultSet result = sparqlService.querySelect(dataset.getSparqlEndPoint(),
            queries(dataset.getQueryType()).getQueryFacetRangeValues(classUri.toString(), facetUri.toString(),
                facetRange.getUri().toString(), filters, facetRange.getAllLiteral(),
                size, size * page, true),
            dataset.getDatasetGraphs(), null);

        List<Value> rangeValues = new ArrayList<>();
        while (result.hasNext()) {
            QuerySolution soln = result.nextSolution();
            if (soln.contains("?value")) {
                RDFNode value = soln.get("?value");
                int count = soln.getLiteral("?count").getInt();
                String label = null;
                if (soln.contains("?label"))
                    label = soln.getLiteral("?label").getString();
                String uri = null;
                if (value.isResource())
                    uri = value.asResource().getURI();
                String curie = null;
                if (uri != null)
                    try {
                        curie = prefixCCMap.abbreviate(new URL(uri).toString());
                    } catch (Exception e) {}
                rangeValues.add(new Value(value.toString(), count, uri, curie, label));
            }
        }
        return rangeValues;
    }

    public List<Value> retrieveRangeValuesContaining(Dataset dataset, Range facetRange,
           MultiValueMap<String, String> filters, String containing, int top) {
        URI classUri = facetRange.getFacet().getDomain().getUri();
        URI facetUri = facetRange.getFacet().getUri();
        ResultSet result = sparqlService.querySelect(dataset.getSparqlEndPoint(),
                queries(dataset.getQueryType()).getQueryFacetRangeValuesContaining(classUri.toString(), facetUri.toString(),
                        facetRange.getUri().toString(), filters, facetRange.getAllLiteral(), containing, top),
                dataset.getDatasetGraphs(), null);

        List<Value> rangeValues = new ArrayList<>();
        while (result.hasNext()) {
            QuerySolution soln = result.nextSolution();
            if (soln.contains("?value")) {
                RDFNode value = soln.get("?value");
                // int count = soln.getLiteral("?count").getInt();
                String label = null;
                if (soln.contains("?label"))
                    label = soln.getLiteral("?label").getString();
                String uri = null;
                if (value.isResource())
                    uri = value.asResource().getURI();
                String curie = null;
                if (uri != null)
                    try {
                        curie = prefixCCMap.abbreviate(new URL(uri).toString());
                    } catch (Exception e) {}
                rangeValues.add(new Value(value.toString(), 0, uri, curie, label));
            }
        }
        return rangeValues;
    }

    public List<URI> listServerGraphs(Dataset dataset) {
        ResultSet result = sparqlService.querySelect(dataset.getSparqlEndPoint(),
            queries(dataset.getQueryType()).getQueryGraphs());
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

    public void retrieveClassInstances(OutputStream out, Dataset dataset, Class datasetClass,
        MultiValueMap<String, String> filters, int page, int size, RDFFormat format) {
        URI classUri = datasetClass.getUri();
        Model model = sparqlService.queryDescribe(dataset.getSparqlEndPoint(),
            queries(dataset.getQueryType()).getQueryClassInstances(classUri.toString(), filters, size,
                    size * page), dataset.getDatasetGraphs());
        RDFDataMgr.write(out, model, format);
    }

    public void getLinkedResourcesLabels(OutputStream out, Dataset dataset, Class datasetClass,
        MultiValueMap<String, String> filters, int page, int size, RDFFormat format) {
        URI classUri = datasetClass.getUri();
        Model model = sparqlService.queryDescribe(dataset.getSparqlEndPoint(),
            queries(dataset.getQueryType()).getQueryClassInstancesLabels(classUri.toString(), filters, size,
                    size * page), dataset.getDatasetGraphs());
        RDFDataMgr.write(out, model, format);
    }

    public int retrieveClassInstancesCount(Dataset dataset, Class datasetClass,
        MultiValueMap<String, String> filters) {
        URI classUri = datasetClass.getUri();
        ResultSet result = sparqlService.querySelect(dataset.getSparqlEndPoint(),
            queries(dataset.getQueryType()).getQueryClassInstancesCount(classUri.toString(), filters),
            dataset.getDatasetGraphs(), null);
        int count = 0;
        while (result.hasNext()) {
            QuerySolution soln = result.nextSolution();
            if (soln.contains("?n"))
                count = soln.getLiteral("?n").getInt();
        }
        return count;
    }

    public void describeDatasetResource(OutputStream out, Dataset dataset, URI resourceUri, RDFFormat format) {
        Model model = sparqlService.queryDescribe(dataset.getSparqlEndPoint(),
            queries(dataset.getQueryType()).getQueryDescribeResource(resourceUri), dataset.getDatasetGraphs());
        model.add(sparqlService.queryDescribe(dataset.getSparqlEndPoint(),
            queries(dataset.getQueryType()).getQueryDescribeResourceLabels(resourceUri), dataset.getDatasetGraphs()));
        RDFDataMgr.write(out, model, format);
    }

    public void browseUri(OutputStream out, URI resourceUri, RDFFormat format) {
        Model model = ModelFactory.createDefaultModel();
        try {
            RDFDataMgr.read(model, resourceUri.toString());
        } catch (RiotException e) {
            logger.info("Unable to retrieve RDF from {}: {}", resourceUri, e.getMessage());
        }
        RDFDataMgr.write(out, model, format);
    }
}
