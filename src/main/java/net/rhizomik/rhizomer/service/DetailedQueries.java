package net.rhizomik.rhizomer.service;

import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.vocabulary.RDFS;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

/**
 * Created by http://rhizomik.net/~roberto/
 */
@Service
public class DetailedQueries implements Queries {

    @Override
    public Query getQueryClasses() {
        return QueryFactory.create(prefixes +
            "SELECT ?class (COUNT(DISTINCT ?instance) as ?n) \n" +
            "WHERE { \n" +
            "\t { ?instance a ?class . FILTER ( !isBlank(?class) ) } UNION \n" +
            "\t { ?instance ?p ?o . FILTER(NOT EXISTS {?instance a ?c} ) BIND(rdfs:Resource AS ?class) } \n" +
            "} GROUP BY ?class");
    }

    @Override
    public Query getQueryClassInstancesCount(String classUri,
        MultiValueMap<String, String> filters) {
        ParameterizedSparqlString pQuery = new ParameterizedSparqlString();
        pQuery.setCommandText(
            "SELECT (COUNT(DISTINCT ?instance) AS ?n) \n" +
            "WHERE { \n" +
            "\t ?instance a ?class . \n" +
            getFilterPatternsAnd(filters) +
            "}");
        pQuery.setIri("class", classUri);
        Query query = pQuery.asQuery();
        return query;
    }

    @Override
    public Query getQueryClassFacets(String classUri, int sampleSize, int classCount, double coverage) {
        ParameterizedSparqlString pQuery = new ParameterizedSparqlString();
        if (sampleSize > 0 && coverage > 0.0) {
            pQuery.setCommandText(prefixes +
                "SELECT ?property ?range (COUNT(?instance) AS ?uses) (COUNT(DISTINCT ?object) AS ?values) (MIN(?isLiteral) as ?allLiteral) \n" +
                "WHERE { \n" +
                "\t { { SELECT ?instance WHERE { ?instance a ?class } OFFSET 0 "+"LIMIT "+sampleSize+" } \n" +
                addSamples(classCount, sampleSize, coverage) + " } \n" +
                "\t ?instance ?property ?object \n" +
                "\t OPTIONAL { ?object a ?type }\n" +
                "\t BIND(if(bound(?type), ?type, if(isLiteral(?object), datatype(?object), rdfs:Resource)) AS ?range) \n" +
                "\t BIND(if(isLiteral(?object), 1, 0) AS ?isLiteral) \n" +
                "} GROUP BY ?property ?range");
        } else {
            pQuery.setCommandText(prefixes +
                "SELECT ?property ?range (COUNT(?instance) AS ?uses) (COUNT(DISTINCT ?object) AS ?values) (MIN(?isLiteral) as ?allLiteral) \n" +
                "WHERE { \n" +
                "\t { SELECT ?instance WHERE { ?instance a ?class } " + ((sampleSize>0) ? "LIMIT "+sampleSize : "") + " } \n" +
                "\t ?instance ?property ?object \n" +
                "\t OPTIONAL { ?object a ?type }\n" +
                "\t BIND(if(bound(?type), ?type, if(isLiteral(?object), datatype(?object), rdfs:Resource)) AS ?range) \n" +
                "\t BIND(if(isLiteral(?object), 1, 0) AS ?isLiteral) \n" +
                "} GROUP BY ?property ?range");
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
            "\t ?instance ?property ?value . \n" +
            "\t OPTIONAL { ?value rdfs:label ?label \n" +
            "\t\t FILTER LANGMATCHES(LANG(?label), \"en\")  } \n" +
            "\t OPTIONAL { ?value rdfs:label ?label } \n" +
            ( isLiteral ?
                "\t FILTER( ISLITERAL(?value) && STR(DATATYPE(?value))=\"" + rangeUri + "\" )\n" :
                !rangeUri.equals(RDFS.Resource.getURI()) ?
                    "\t ?value a <" + rangeUri + "> \n" :
                    "\t OPTIONAL { ?value a ?type } FILTER( (!BOUND(?type) || ?type=rdfs:Resource ) && !ISLITERAL(?value) ) \n" ) +
            "} GROUP BY ?value ?label");
        pQuery.setIri("class", classUri);
        pQuery.setIri("property", facetUri);
        Query query = pQuery.asQuery();
        if (limit > 0) query.setLimit(limit);
        if (offset > 0) query.setOffset(offset);
        if (ordered) query.addOrderBy("count", -1);
        return query;
    }

    @Override
    public Query getQueryFacetRangeValuesContaining(String classUri, String facetUri, String rangeUri,
                                                    MultiValueMap<String, String> filters, boolean isLiteral, String containing, int top) {
        ParameterizedSparqlString pQuery = new ParameterizedSparqlString();
        pQuery.setCommandText(prefixes +
                "SELECT DISTINCT ?value ?label \n" +
                "WHERE { \n" +
                "\t { SELECT DISTINCT ?instance " +
                "\t\t WHERE { \n" +
                "\t\t\t ?instance a ?class . \n" +
                getFilterPatternsAnd(filters) +
                "\t\t } \n" +
                "\t } \n" +
                "\t ?instance ?property ?value . \n" +
                "\t OPTIONAL { ?value rdfs:label ?label \n" +
                "\t\t FILTER LANGMATCHES(LANG(?label), \"en\")  } \n" +
                "\t OPTIONAL { ?value rdfs:label ?label } \n" +
                ( isLiteral ?
                    "\t FILTER( ISLITERAL(?value) && STR(DATATYPE(?value))=\"" + rangeUri + "\" )\n" :
                    !rangeUri.equals(RDFS.Resource.getURI()) ?
                        "\t ?value a <" + rangeUri + "> \n" :
                        "\t OPTIONAL { ?value a ?type } FILTER( (!BOUND(?type) || ?type=rdfs:Resource ) && !ISLITERAL(?value) ) \n" ) +
                "\t FILTER( CONTAINS(LCASE(STR(?value)), LCASE(?containing)) || CONTAINS(LCASE(?label), LCASE(?containing)) ) \n" +
                "}");
        pQuery.setIri("class", classUri);
        pQuery.setIri("property", facetUri);
        pQuery.setLiteral("containing", containing);
        Query query = pQuery.asQuery();
        if (top > 0) query.setLimit(top);
        return query;
    }
}
