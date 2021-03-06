package net.rhizomik.rhizomer.service;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

import net.rhizomik.rhizomer.model.SPARQLEndPoint;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

/**
 * Created by http://rhizomik.net/~roberto/
 */
@Service
public interface Queries {
    String prefixes =
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n";

    enum QueryType { OPTIMIZED, DETAILED }

    Query getQueryClasses();

    Query getQueryClassInstancesCount(String classUri, MultiValueMap<String, String> filters);

    default Query getQueryClassInstances(String classUri,
        MultiValueMap<String, String> filters, int limit, int offset) {
        ParameterizedSparqlString pQuery = new ParameterizedSparqlString();
        pQuery.setCommandText(prefixes +
            "DESCRIBE ?instance \n" +
                "WHERE { \n" +
                "\t { SELECT DISTINCT ?instance \n" +
                "\t\t WHERE { \n" +
                "\t\t\t ?instance a ?class . \n" +
                "\t\t\t OPTIONAL { ?instance rdfs:label ?label } \n" +
                getFilterPatternsAnd(filters) +
                "\t\t } ORDER BY LCASE(?label) LIMIT " + limit + " OFFSET " + offset + " \n" +
                "\t } \n" +
                "}");
        pQuery.setIri("class", classUri);
        Query query = pQuery.asQuery();
        return query;
    }

    default Query getQueryClassInstancesLabels(String classUri,
        MultiValueMap<String, String> filters, int limit, int offset) {
        ParameterizedSparqlString pQuery = new ParameterizedSparqlString();
        pQuery.setCommandText(prefixes +
            "CONSTRUCT { ?resource rdfs:label ?label } \n" +
            "WHERE { \n" +
            "\t { SELECT DISTINCT ?instance \n" +
            "\t\t WHERE { \n" +
            "\t\t\t ?instance a ?class . \n" +
            "\t\t\t OPTIONAL { ?instance rdfs:label ?label } \n" +
            getFilterPatternsAnd(filters) +
            "\t\t } ORDER BY LCASE(?label) LIMIT " + limit + " OFFSET " + offset + " \n" +
            "\t } \n" +
            "\t { \n" +
            "\t\t ?instance ?property ?resource . \n" +
            "\t\t ?resource rdfs:label ?label . \n" +
            " } UNION { \n" +
            "\t\t ?instance ?propertyanon ?anon . FILTER(isBlank(?anon)) \n" +
            "\t\t ?anon ?property ?resource .\n" +
            "\t\t ?resource rdfs:label ?label . \n" +
            " } UNION { \n" +
            "\t\t ?instance ?resource ?object . \n" +
            "\t\t ?resource rdfs:label ?label . \n" +
            "} }");
        pQuery.setIri("class", classUri);
        Query query = pQuery.asQuery();
        return query;
    }

    Query getQueryClassFacets(String classUri, int sampleSize, int classCount, double coverage);

    Query getQueryFacetRangeValues(String classUri, String facetUri, String rangeUri,
        MultiValueMap<String, String> filters, boolean isLiteral, int limit, int offset,
        boolean ordered);

    Query getQueryFacetRangeValuesContaining(String classUri, String facetUri, String rangeUri,
           MultiValueMap<String, String> filters, boolean isLiteral, String containing, int top);

    default Query getQueryDescribeResource(URI resourceUri) {
        return QueryFactory.create("DESCRIBE <" + resourceUri + ">");
    }

    default Query getQueryDescribeResourceLabels(URI resourceUri) {
        ParameterizedSparqlString pQuery = new ParameterizedSparqlString();
        pQuery.setCommandText(prefixes +
            "CONSTRUCT { ?resource rdfs:label ?label } \n" +
            "WHERE { { \n" +
            "\t ?instance ?property ?resource . \n" +
            "\t ?resource rdfs:label ?label . \n" +
            "} UNION { \n" +
            "\t ?instance ?propertyanon ?anon . FILTER(isBlank(?anon)) \n" +
            "\t ?anon ?property ?resource .\n" +
            "\t ?resource rdfs:label ?label . \n" +
            " } UNION { \n" +
            "\t\t ?instance ?resource ?object . \n" +
            "\t\t ?resource rdfs:label ?label . \n" +
            "} }");
        pQuery.setIri("instance", resourceUri.toString());
        Query query = pQuery.asQuery();
        return query;
    }

    default Query getQueryResourceIncomingFacets(URI resourceUri) {
        ParameterizedSparqlString pQuery = new ParameterizedSparqlString();
        pQuery.setCommandText(prefixes +
            "SELECT (SAMPLE(?class) as ?range) ?prop ?proplabel ?uses ?domain ?domainlabel (COUNT(DISTINCT ?s) AS ?count) \n" +
            "WHERE { \n" +
            "\t { SELECT ?class ?prop ?proplabel (COUNT(DISTINCT ?s) AS ?uses) \n" +
            "\t\t WHERE { \n" +
            "\t\t\t ?s ?prop ?resource . \n" +
            "\t\t\t ?resource a ?class . \n" +
            "\t\t\t FILTER NOT EXISTS { ?subclass rdfs:subClassOf ?class . ?resource a ?subclass } \n" +
            "\t\t\t OPTIONAL { ?prop rdfs:label ?proplabel FILTER LANGMATCHES(LANG(?proplabel), \"en\")  } \n" +
            "\t\t\t OPTIONAL { ?prop rdfs:label ?proplabel } \n" +
            "\t } GROUP BY ?class ?prop ?proplabel } \n" +
            "\t ?s ?prop ?resource ; a ?domain . \n" +
            "\t FILTER NOT EXISTS { ?subdomain rdfs:subClassOf ?domain . ?s a ?subdomain } \n" +
            "\t OPTIONAL { ?domain rdfs:label ?domainlabel FILTER LANGMATCHES(LANG(?domainlabel), \"en\")  } \n" +
            "\t OPTIONAL { ?domain rdfs:label ?domainlabel } \n" +
            "} GROUP BY ?prop ?proplabel ?domain ?domainlabel ?uses");
        pQuery.setIri("resource", resourceUri.toString());
        Query query = pQuery.asQuery();
        return query;
    }

    default Query getQueryInferTypes() {
        return QueryFactory.create(prefixes +
            "CONSTRUCT { ?i a ?type } \n" +
            "WHERE { \n" +
            "{\t ?p rdfs:domain ?type . \n" +
            " \t ?subp rdfs:subPropertyOf* ?p . \n" +
            " \t ?i ?subp ?o \n" +
            " \t FILTER NOT EXISTS {?i a ?class} \n" +
            "} \n" +
            "UNION \n" +
            "{\t ?p rdfs:range ?type . \n" +
            " \t ?subp rdfs:subPropertyOf* ?p . \n" +
            " \t ?s ?p ?i \n" +
            " \t FILTER NOT EXISTS {?i a ?class} \n" +
            "} \n" +
            "}");
    }

    default UpdateRequest getUpdateInferTypes(List<String> targetGraphs, String datasetInferenceGraph) {
        return UpdateFactory.create(prefixes +
            "INSERT { GRAPH <" + datasetInferenceGraph + "> { ?i a ?type } } \n" +
            targetGraphs.stream().map(s -> String.format("USING <%s> \n", s)).collect(Collectors.joining()) +
            "WHERE { \n" +
            "{\t ?p rdfs:domain ?type . \n" +
            " \t ?subp rdfs:subPropertyOf* ?p . \n" +
            " \t ?i ?subp ?o \n" +
            " \t FILTER NOT EXISTS {?i a ?class} \n" +
            "} \n" +
            "UNION \n" +
            "{\t ?p rdfs:range ?type . \n" +
            " \t ?subp rdfs:subPropertyOf* ?p . \n" +
            " \t ?s ?subp ?i \n" +
            " \t FILTER NOT EXISTS {?i a ?class} \n" +
            "} \n" +
            "}");
    }

    default UpdateRequest getUpdateResource(String oldResourceTriples, String newResourceTriples) {
        return UpdateFactory.create(prefixes +
                "DELETE { GRAPH ?g { \n" +
                oldResourceTriples.replaceAll("_:", "?") + "\n" +
                "} } \n" +
                "INSERT { GRAPH ?g { \n" +
                newResourceTriples + "\n" +
                "} } \n" +
                "WHERE { GRAPH ?g { \n" +
                oldResourceTriples.replaceAll("_:", "?") + "\n" +
                "} }");
    }

    default UpdateRequest getInsertData(SPARQLEndPoint.ServerType serverType, String graph, String data) {
        if (serverType == SPARQLEndPoint.ServerType.VIRTUOSO)
            // Fix Virtuoso bug: https://github.com/openlink/virtuoso-opensource/issues/126
            return UpdateFactory.create("INSERT { GRAPH <" + graph + "> { " + data + " } } WHERE { SELECT * {OPTIONAL {?s ?p ?o} } LIMIT 1 }");
        else
            return UpdateFactory.create("INSERT DATA { GRAPH <" + graph + "> { " + data + " } } ");
    }

    default UpdateRequest getCreateGraph(String graph) {
        return UpdateFactory.create(
            "CREATE GRAPH <" + graph + ">");
    }

    default UpdateRequest getClearGraph(String graph) {
        return UpdateFactory.create(
            "CLEAR SILENT GRAPH <" + graph + ">");
    }

    default UpdateRequest getDropGraph(String graph) {
        return UpdateFactory.create(
            "DROP SILENT GRAPH <" + graph + ">");
    }

    default Query getQueryCountUntyped() {
        return QueryFactory.create(prefixes +
            "SELECT (COUNT(DISTINCT(?i)) AS ?n) \n" +
            "WHERE { \n" +
            "\t { ?i ?p ?o FILTER NOT EXISTS { ?i a ?class } } \n" +
            "\t UNION \n" +
            "\t { ?s ?p ?i FILTER NOT EXISTS { ?i a ?class } } \n" +
            "}");
    }

    default Query getQueryCountTriples() {
        return QueryFactory.create(prefixes +
            "SELECT (COUNT(?s)) AS ?n) \n" +
            "WHERE { ?s ?p ?o }");
    }

    default Query getQueryGraphs() {
        return QueryFactory.create(
            "SELECT DISTINCT ?graph \n" +
            "WHERE { GRAPH ?graph { ?s a ?o } }");
    }

    default Query getQueryCountType(String type) {
        ParameterizedSparqlString pQuery = new ParameterizedSparqlString();
        pQuery.setCommandText(prefixes +
            "SELECT (COUNT(DISTINCT(?s)) AS ?n) \n" +
            "WHERE { ?s a ?type }");
        pQuery.setIri("type", type);
        return pQuery.asQuery();
    }

    default String addSamples(int classCount, int sampleSize, double coverage) {
        String selectsUnion = "";
        int samplesCount = (int)Math.floor((classCount * coverage)/sampleSize);
        int offset = (int) Math.ceil((double) classCount/samplesCount);

        for(int sample = 1; sample < samplesCount; sample++) {
            selectsUnion += "\t\t UNION { SELECT ?instance WHERE { ?instance a ?class } OFFSET "+ sample * offset +" LIMIT "+sampleSize+" } \n";
        }

        return selectsUnion;
    }

    default String getFilterPatternsOr(MultiValueMap<String, String> filters) {
        StringBuilder filtersPatterns = new StringBuilder();
        filters.forEach((property_range, values) -> {
            String property = property_range.split(" ")[0];
            String range = property_range.split(" ")[1];
            String propertyVar = Integer.toUnsignedString(property.hashCode());
            String pattern = "\t ?instance <" + property + "> ?v" + propertyVar + " . \n";
            values.removeIf(value -> value.equals("null"));
            if (!values.isEmpty()) {
                pattern += "\t FILTER( STR(?v" + propertyVar + ") IN (" +
                    values.stream().map(value -> "STR(" + value.replaceAll("[<>]", "\"") + ")")
                            .collect(Collectors.joining(", "))
                    + ")) . \n";
            }
            filtersPatterns.append(pattern);
        });
        return filtersPatterns.toString();
    }

    default String getFilterPatternsAnd(MultiValueMap<String, String> filters) {
        StringBuilder filtersPatterns = new StringBuilder();
        filters.forEach((property_range, values) -> {
            values.forEach(value -> {
                String property = property_range.split(" ")[0];
                String range = property_range.indexOf(" ") > 0 ? property_range.split(" ")[1] : null;
                String propertyValueVar = Integer.toUnsignedString(property.hashCode() + value.hashCode());
                String pattern = "\t ?instance <" + property + "> ?v" + propertyValueVar + " . \n";
                if (!value.equals("null")) {
                    pattern += "FILTER ( STR(?v" + propertyValueVar + ") = " +
                            value.replaceAll("[<>]", "\"") + " ) \n";
                }
                filtersPatterns.append(pattern);
            });
        });
        return filtersPatterns.toString();
    }
}
