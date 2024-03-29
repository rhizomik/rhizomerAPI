package net.rhizomik.rhizomer.service;

import net.rhizomik.rhizomer.model.SPARQLEndPoint;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

/**
 * Created by http://rhizomik.net/~roberto/
 */
@Primary
@Service
public class OptimizedQueries implements Queries {

    @Override
    public Query getQueryClasses() {
        return QueryFactory.create(prefixes +
            "SELECT ?class ?n (GROUP_CONCAT(?langLabel; SEPARATOR = \" || \") AS ?label) \n" +
            "WHERE { \n" +
            "\t { SELECT ?class (COUNT(DISTINCT ?instance) as ?n) \n" +
            "\t\t WHERE { \n" +
            "\t\t\t ?instance a ?class . FILTER ( !isBlank(?class) ) \n" +
            "\t\t } GROUP BY ?class } \n" +
            "\t OPTIONAL { GRAPH ?g { ?class rdfs:label ?l } BIND (CONCAT(?l, IF(LANG(?l),\"@\",\"\"), LANG(?l)) AS ?langLabel) } \n" +
            "} GROUP BY ?class ?n");
    }

    @Override
    public Query getQueryClassFacets(String classUri) {
        ParameterizedSparqlString pQuery = new ParameterizedSparqlString();
        pQuery.setCommandText(prefixes +
            "SELECT ?property ?uses ?values ?allLiteral ?allBlank " +
            "       (GROUP_CONCAT(DISTINCT(?langLabel) ; separator=' || ') AS ?label) \n" +
            "WHERE { \n" +
            "\t { SELECT ?property (COUNT(?instance) AS ?uses) (COUNT(DISTINCT ?object) AS ?values) " +
            "\t          (MIN(?isLiteral) AS ?allLiteral) (MIN(?isBlank) AS ?allBlank) \n" +
            "\t\t WHERE { \n" +
            "\t\t\t ?instance a ?class . \n"+
            "\t\t\t ?instance ?property ?object \n" +
            "\t\t\t BIND(isLiteral(?object) AS ?isLiteral) \n" +
            "\t\t\t BIND(isBlank(?object) AS ?isBlank) \n" +
            "\t\t } GROUP BY ?property \n" +
            "\t } \n" +
            "\t OPTIONAL { GRAPH ?g { ?property rdfs:label ?l } BIND (CONCAT(?l, IF(LANG(?l),\"@\",\"\"), LANG(?l)) AS ?langLabel) } \n" +
            "} GROUP BY ?property ?uses ?values ?allLiteral ?allBlank");
        pQuery.setIri("class", classUri);
        return pQuery.asQuery();
    }

    @Override
    public Query getQueryFacetRangeValues(
            SPARQLEndPoint.ServerType serverType, String classUri, String facetUri, String rangeUri,
            MultiValueMap<String, String> filters, boolean isLiteral, int limit, int offset, boolean ordered) {
        ParameterizedSparqlString pQuery = new ParameterizedSparqlString();
        pQuery.setCommandText(prefixes +
            "SELECT ?value ?count (GROUP_CONCAT(?langLabel; SEPARATOR = \" || \") AS ?label) \n" +
            "\t WHERE { \n" +
            "\t { SELECT ?resource (COUNT(?resource) AS ?count) \n" +
            "\t\t WHERE { \n" +
            "\t\t { SELECT DISTINCT ?instance " +
            "\t\t\t WHERE { \n" +
            "\t\t\t\t ?instance a ?class . \n" +
            getFilterPatterns(serverType, filters) +
            "\t\t\t } \n" +
            "\t\t } \n" +
            "\t\t ?instance ?property ?resource . \n" +
            "\t\t } GROUP BY ?resource } \n" +
            "\t BIND(str(?resource) AS ?value) \n" +
            "\t OPTIONAL { ?resource rdfs:label ?l BIND (CONCAT(?l, IF(LANG(?l),\"@\",\"\"), LANG(?l)) AS ?langLabel) } \n" +
            "\t OPTIONAL { GRAPH ?g { ?resource rdfs:label ?l } BIND (CONCAT(?l, IF(LANG(?l),\"@\",\"\"), LANG(?l)) AS ?langLabel) } \n" +
            "} GROUP BY ?value ?count");
        pQuery.setIri("class", classUri);
        pQuery.setIri("property", facetUri);
        Query query = pQuery.asQuery();
        if (limit > 0) query.setLimit(limit);
        if (offset > 0) query.setOffset(offset);
        if (ordered) query.addOrderBy("count", -1);
        return query;
    }

    @Override
    public Query getFacetRangeValueLabelAndCount(
            SPARQLEndPoint.ServerType serverType, String classUri, String facetUri, String rangeUri, String value,
            MultiValueMap<String, String> filters, boolean isLiteral) {
        ParameterizedSparqlString pQuery = new ParameterizedSparqlString();
        pQuery.setCommandText(prefixes +
                "SELECT ?value ?count (GROUP_CONCAT(?langLabel; SEPARATOR = \" || \") AS ?label) \n" +
                "\t WHERE { \n" +
                "\t BIND(" + value + " AS ?value) \n" +
                "\t { SELECT (COUNT(?resource) AS ?count) \n" +
                "\t\t WHERE { \n" +
                "\t\t { SELECT DISTINCT ?instance " +
                "\t\t\t WHERE { \n" +
                "\t\t\t\t ?instance a ?class . \n" +
                getFilterPatterns(serverType, filters) +
                "\t\t\t } \n" +
                "\t\t } \n" +
                "\t\t ?instance ?property ?resource . FILTER (STR(?resource) = " + value + ") \n" +
                "\t\t } } \n" +
                "\t OPTIONAL { ?resource rdfs:label ?l . FILTER (STR(?resource) = " + value + ") \n" +
                "\t\t BIND (CONCAT(?l, IF(LANG(?l),\"@\",\"\"), LANG(?l)) AS ?langLabel) } \n" +
                "\t OPTIONAL { GRAPH ?g { ?resource rdfs:label ?l . FILTER (STR(?resource) = " + value + ") } \n" +
                "\t\t BIND (CONCAT(?l, IF(LANG(?l),\"@\",\"\"), LANG(?l)) AS ?langLabel) } \n" +
                "} GROUP BY ?value ?count");
        pQuery.setIri("class", classUri);
        pQuery.setIri("property", facetUri);
        Query query = pQuery.asQuery();
        return query;
    }

    @Override
    public Query getQueryFacetRangeValuesContaining(
            SPARQLEndPoint.ServerType serverType, String classUri, String facetUri, String rangeUri,
            MultiValueMap<String, String> filters, boolean isLiteral, String containing, int top, String lang) {
        ParameterizedSparqlString pQuery = new ParameterizedSparqlString();
        pQuery.setCommandText(prefixes +
                "SELECT DISTINCT ?value ?label \n" +
                "WHERE { \n" +
                "\t { SELECT DISTINCT ?instance " +
                "\t\t WHERE { \n" +
                "\t\t\t ?instance a ?class . \n" +
                getFilterPatterns(serverType, filters) +
                "\t\t } \n" +
                "\t } \n" +
                "\t ?instance ?property ?resource . \n" +
                "\t OPTIONAL { ?resource rdfs:label ?label \n" +
                "\t\t FILTER LANGMATCHES(LANG(?label), \"" + lang + "\")  } \n" +
                "\t OPTIONAL { ?resource rdfs:label ?label } \n" +
                "\t BIND( str(?resource) AS ?value) \n" +
                "\t FILTER( CONTAINS(LCASE(?value), LCASE(?containing)) || CONTAINS(LCASE(?label), LCASE(?containing)) ) \n" +
                "}");
        pQuery.setIri("class", classUri);
        pQuery.setIri("property", facetUri);
        pQuery.setLiteral("containing", containing);
        Query query = pQuery.asQuery();
        if (top > 0) query.setLimit(top);
        return query;
    }

    @Override
    public Query getQueryFacetRangeMinMax(
            SPARQLEndPoint.ServerType serverType, String classUri, String facetUri, String rangeUri,
            MultiValueMap<String, String> filters) {
        throw new UnsupportedOperationException("Optimized query for facet MIN and MAX not available");
    }
}
