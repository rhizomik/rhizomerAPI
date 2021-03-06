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
public class NeptuneDetailedQueries extends DetailedQueries {
    String prefixes =
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +
            "PREFIX hint: <http://aws.amazon.com/neptune/vocab/v01/QueryHints#> \n" ;

    @Override
    public Query getQueryClasses() {
        return QueryFactory.create(prefixes +
                "SELECT ?class ?label (COUNT(DISTINCT ?instance) as ?n) \n" +
                "WHERE { \n" +
                "\t hint:Query hint:joinOrder \"Ordered\" .\n" +
                "\t ?instance a ?class . FILTER ( !isBlank(?class) ) \n" +
                "\t OPTIONAL { ?class rdfs:label ?label \n" +
                "\t\t FILTER LANGMATCHES(LANG(?label), \"en\")  } \n" +
                "\t OPTIONAL { ?class rdfs:label ?label } \n" +
                "} GROUP BY ?class ?label");
    }

    @Override
    public Query getQueryClassFacets(String classUri, int sampleSize, int classCount, double coverage) {
        ParameterizedSparqlString pQuery = new ParameterizedSparqlString();
        if (sampleSize > 0 && coverage > 0.0) {
            pQuery.setCommandText(prefixes +
                "SELECT ?property ?range (COUNT(?instance) AS ?uses) (COUNT(DISTINCT ?object) AS ?values) " +
                "       (MIN(?isLiteral) as ?allLiteral) (SAMPLE(?labels) AS ?label) (SAMPLE(?rlabels) AS ?rlabel) \n" +
                "WHERE { \n" +
                "hint:Query hint:joinOrder \"Ordered\" .\n" +
                "\t { { SELECT ?instance WHERE { ?instance a ?class } OFFSET 0 "+"LIMIT "+sampleSize+" } \n" +
                addSamples(classCount, sampleSize, coverage) + " } \n" +
                "\t ?instance ?property ?object \n" +
                "\t OPTIONAL { ?object a ?type }\n" +
                "\t BIND(if(bound(?type), ?type, if(isLiteral(?object), datatype(?object), rdfs:Resource)) AS ?range) \n" +
                "\t BIND(isLiteral(?object) AS ?isLiteral) \n" +
                "\t OPTIONAL { ?property rdfs:label ?labels FILTER LANGMATCHES(LANG(?labels), \"en\")  } \n" +
                "\t OPTIONAL { ?property rdfs:label ?labels } \n" +
                "\t OPTIONAL { ?range rdfs:label ?rlabels FILTER LANGMATCHES(LANG(?rlabels), \"en\")  } \n" +
                "\t OPTIONAL { ?range rdfs:label ?rlabels } \n" +
                "} GROUP BY ?property ?range");
        } else {
            pQuery.setCommandText(prefixes +
                "SELECT ?property ?range (COUNT(?instance) AS ?uses) (COUNT(DISTINCT ?object) AS ?values) " +
                "       (MIN(?isLiteral) as ?allLiteral) (SAMPLE(?labels) AS ?label) (SAMPLE(?rlabels) AS ?rlabel) \n" +
                "WHERE { \n" +
                "hint:Query hint:joinOrder \"Ordered\" .\n" +
                "\t { SELECT ?instance WHERE { ?instance a ?class } " + ((sampleSize>0) ? "LIMIT "+sampleSize : "") + " } \n" +
                "\t ?instance ?property ?object \n" +
                "\t OPTIONAL { ?object a ?type }\n" +
                "\t BIND(if(bound(?type), ?type, if(isLiteral(?object), datatype(?object), rdfs:Resource)) AS ?range) \n" +
                "\t BIND(isLiteral(?object) AS ?isLiteral) \n" +
                "\t OPTIONAL { ?property rdfs:label ?labels FILTER LANGMATCHES(LANG(?labels), \"en\")  } \n" +
                "\t OPTIONAL { ?property rdfs:label ?labels } \n" +
                "\t OPTIONAL { ?range rdfs:label ?rlabels FILTER LANGMATCHES(LANG(?rlabels), \"en\")  } \n" +
                "\t OPTIONAL { ?range rdfs:label ?rlabels } \n" +
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
            "hint:Query hint:joinOrder \"Ordered\" .\n" +
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
            "\t BIND(?resource AS ?value)\n \n" +
            ( isLiteral ?
                "\t FILTER( ISLITERAL(?resource) && DATATYPE(?resource) = <" + rangeUri + "> )\n" :
                !rangeUri.equals(RDFS.Resource.getURI()) ?
                    "\t ?resource a <" + rangeUri + "> \n" :
                    "\t OPTIONAL { ?resource a ?type } FILTER( (!BOUND(?type) || ?type=rdfs:Resource ) && !ISLITERAL(?resource) ) \n" ) +
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
                "hint:Query hint:joinOrder \"Ordered\" .\n" +
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
                ( isLiteral ?
                    "\t FILTER( ISLITERAL(?resource) && DATATYPE(?resource) = <" + rangeUri + "> )\n" :
                    !rangeUri.equals(RDFS.Resource.getURI()) ?
                        "\t ?resource a <" + rangeUri + "> \n" :
                        "\t OPTIONAL { ?resource a ?type } FILTER( (!BOUND(?type) || ?type=rdfs:Resource ) && !ISLITERAL(?resource) ) \n" ) +
                "\t BIND(?resource AS ?value) \n" +
                "\t FILTER( CONTAINS(LCASE(STR(?resource)), LCASE(?containing)) || CONTAINS(LCASE(?label), LCASE(?containing)) ) \n" +
                "}");
        pQuery.setIri("class", classUri);
        pQuery.setIri("property", facetUri);
        pQuery.setLiteral("containing", containing);
        Query query = pQuery.asQuery();
        if (top > 0) query.setLimit(top);
        return query;
    }

    @Override
    public Query getQueryClassInstances(String classUri, MultiValueMap<String, String> filters, int limit, int offset) {
        ParameterizedSparqlString pQuery = new ParameterizedSparqlString();
        pQuery.setCommandText(prefixes +
                "DESCRIBE ?instance \n" +
                "WHERE { \n" +
                "\t { SELECT DISTINCT ?instance \n" +
                "\t\t WHERE { \n" +
                "\t\t\t hint:Query hint:joinOrder \"Ordered\" .\n" +
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

    @Override
    public Query getQueryClassInstancesLabels(String classUri,
        MultiValueMap<String, String> filters, int limit, int offset) {
        ParameterizedSparqlString pQuery = new ParameterizedSparqlString();
        pQuery.setCommandText(prefixes +
            "CONSTRUCT { ?resource rdfs:label ?label } \n" +
            "WHERE { \n" +
            "\t { SELECT DISTINCT ?instance \n" +
            "\t\t WHERE { \n" +
            "\t\t\t hint:Query hint:joinOrder \"Ordered\" .\n" +
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
}
