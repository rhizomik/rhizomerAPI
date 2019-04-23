package net.rhizomik.rhizomer.service;

import java.util.stream.Collectors;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

/**
 * Created by http://rhizomik.net/~roberto/
 */
@Service
public class OptimizedQueries implements Queries {

    @Override
    public Query getQueryClasses() {
        return QueryFactory.create(
            "SELECT ?class (COUNT(?instance) as ?n) \n" +
            "WHERE { \n" +
            "\t ?instance a ?class . FILTER ( !isBlank(?class) ) \n" +
            "} GROUP BY ?class");
    }

    @Override
    public Query getQueryClassInstancesCount(String classUri,
        MultiValueMap<String, String> filters) {
        ParameterizedSparqlString pQuery = new ParameterizedSparqlString();
        if (filters.isEmpty()) {
            pQuery.setCommandText(
                "SELECT (COUNT(?instance) AS ?n) \n" +
                "WHERE { \n" +
                "\t ?instance a ?class . \n" +
                "}");
        } else {
            pQuery.setCommandText(
                "SELECT (COUNT(DISTINCT ?instance) AS ?n) \n" +
                "WHERE { \n" +
                "\t ?instance a ?class . \n" +
                getFilterPatternsAnd(filters) +
                "}");
        }
        pQuery.setIri("class", classUri);
        Query query = pQuery.asQuery();
        return query;
    }

    @Override
    public Query getQueryClassInstances(String classUri,
        MultiValueMap<String, String> filters, int limit, int offset) {
        ParameterizedSparqlString pQuery = new ParameterizedSparqlString();
        pQuery.setCommandText(
            "DESCRIBE ?instance \n" +
            "WHERE { \n" +
            "\t { SELECT DISTINCT ?instance \n" +
            "\t\t WHERE { \n" +
            "\t\t\t ?instance a ?class . \n" +
            getFilterPatternsAnd(filters) +
            "\t\t } LIMIT " + limit + " OFFSET " + offset + "\n" +
            "\t } \n" +
            "}");
        pQuery.setIri("class", classUri);
        Query query = pQuery.asQuery();
        return query;
    }

    @Override
    public Query getQueryClassFacets(String classUri, int sampleSize, int classCount, double coverage) {
        ParameterizedSparqlString pQuery = new ParameterizedSparqlString();
        if (sampleSize > 0 && coverage > 0.0) {
            pQuery.setCommandText(
                "SELECT ?property (COUNT(?instance) AS ?uses) (COUNT(DISTINCT ?object) AS ?values) (MIN(?isLiteral) as ?allLiteral) \n" +
                "WHERE { \n" +
                "\t { { SELECT ?instance WHERE { ?instance a ?class } OFFSET 0 "+"LIMIT "+sampleSize+" } \n" +
                addSamples(classCount, sampleSize, coverage) + " } \n" +
                "\t ?instance ?property ?object \n" +
                "\t BIND(isLiteral(?object) AS ?isLiteral) \n" +
                "} GROUP BY ?property");
        } else {
            pQuery.setCommandText(
                "SELECT ?property (COUNT(?instance) AS ?uses) (COUNT(DISTINCT ?object) AS ?values) (MIN(?isLiteral) as ?allLiteral) \n" +
                "WHERE { \n" +
                "\t { SELECT ?instance WHERE { ?instance a ?class } " + ((sampleSize>0) ? "LIMIT "+sampleSize : "") + " } \n" +
                "\t ?instance ?property ?object \n" +
                "\t BIND(isLiteral(?object) AS ?isLiteral) \n" +
                "} GROUP BY ?property");
        }
        pQuery.setIri("class", classUri);
        return pQuery.asQuery();
    }

    @Override
    public Query getQueryFacetRangeValues(String classUri, String facetUri, String rangeUri,
        MultiValueMap<String, String> filters, boolean isLiteral, int limit, int offset, boolean ordered) {
        ParameterizedSparqlString pQuery = new ParameterizedSparqlString();
        pQuery.setCommandText(prefixes +
            "SELECT ?value ?label (COUNT(?value) AS ?count) \n" +
            "WHERE { \n" +
            "\t { SELECT DISTINCT ?instance " +
            "\t\t WHERE { \n" +
            "\t\t\t ?instance a ?class . \n" +
            getFilterPatternsAnd(filters) +
            "\t\t } \n" +
            "\t } \n" +
            "\t ?instance ?property ?resource . \n" +
            "\t OPTIONAL { ?resource rdfs:label ?label \n" +
            "\t\t FILTER LANGMATCHES(LANG(?label), \"en\")  } \n" +
            "\t OPTIONAL { ?resource rdfs:label ?label } \n" +
            "\t BIND( str(?resource) AS ?value)\n \n" +
            "} GROUP BY ?value ?label");
        pQuery.setIri("class", classUri);
        pQuery.setIri("property", facetUri);
        Query query = pQuery.asQuery();
        if (limit > 0) query.setLimit(limit);
        if (offset > 0) query.setOffset(offset);
        if (ordered) query.addOrderBy("count", -1);
        return query;
    }

    public static String getFilterPatternsOr(MultiValueMap<String, String> filters) {
        StringBuilder filtersPatterns = new StringBuilder();
        filters.forEach((property, values) -> {
            String propertyVar = Integer.toUnsignedString(property.hashCode());
            String pattern = "\t ?instance <" + property + "> ?v" + propertyVar + " . \n";
            values.removeIf(value -> value.equals("null"));
            if (!values.isEmpty()) {
                pattern += "\t FILTER( STR(?v" + propertyVar + ") IN (" +
                    values.stream().map(value -> "STR(" + value + ")").collect(Collectors.joining(", "))
                    + ")) . \n";
            }
            filtersPatterns.append(pattern);
        });
        return filtersPatterns.toString();
    }

    public static String getFilterPatternsAnd(MultiValueMap<String, String> filters) {
        StringBuilder filtersPatterns = new StringBuilder();
        filters.forEach((property, values) -> {
            values.removeIf(value -> value.equals("null"));
            values.forEach(value -> {
                String propertyValueVar = Integer.toUnsignedString(property.hashCode() + value.hashCode());
                String pattern = "\t ?instance <" + property + "> ?v" + propertyValueVar + " . " +
                    "FILTER ( STR(?v" + propertyValueVar + ") = STR(" + value + ") ) \n";
                filtersPatterns.append(pattern);
            });
        });
        return filtersPatterns.toString();
    }
}
