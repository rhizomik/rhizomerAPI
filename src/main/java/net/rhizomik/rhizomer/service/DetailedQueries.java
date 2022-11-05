package net.rhizomik.rhizomer.service;

import net.rhizomik.rhizomer.model.SPARQLEndPoint;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.vocabulary.RDFS;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by http://rhizomik.net/~roberto/
 */
@Service
public class DetailedQueries implements Queries {

    @Override
    public Query getQueryClasses() {
        return QueryFactory.create(prefixes +
            "SELECT ?class ?n (GROUP_CONCAT(?langLabel; SEPARATOR = \" || \") AS ?label) \n" +
            "WHERE { \n" +
            "\t { SELECT ?class (COUNT(DISTINCT ?instance) as ?n) \n" +
            "\t\t WHERE { \n" +
            "\t\t\t { ?instance a ?class . FILTER ( !isBlank(?class) ) } \n" +
            "\t\t\t UNION \n" +
            "\t\t\t { ?instance ?p ?o . FILTER(NOT EXISTS {?instance a ?c} ) BIND(rdfs:Resource AS ?class) } \n" +
            "\t\t } GROUP BY ?class } \n" +
            "\t OPTIONAL { GRAPH ?g { ?class rdfs:label ?l } BIND (CONCAT(?l, IF(LANG(?l),\"@\",\"\"), LANG(?l)) AS ?langLabel) } \n" +
            "} GROUP BY ?class ?n");
    }

    @Override
    public Query getQueryClassFacets(String classUri) {
        ParameterizedSparqlString pQuery = new ParameterizedSparqlString();
        pQuery.setCommandText(prefixes +
            "SELECT ?property ?range ?uses ?values ?allLiteral ?allBlank " +
            "       (GROUP_CONCAT(DISTINCT(?langLabel) ; separator=' || ') AS ?label) " +
            "       (GROUP_CONCAT(DISTINCT(?rlangLabel) ; separator=' || ') AS ?rlabel) \n" +
            "WHERE { \n" +
            "\t { SELECT ?property ?range (COUNT(?instance) AS ?uses) (COUNT(DISTINCT ?object) AS ?values) \n" +
            "\t          (MIN(?isLiteral) AS ?allLiteral) (MIN(?isBlank) AS ?allBlank) \n" +
            "\t\t WHERE { \n" +
            "\t\t\t ?instance a ?class . \n"+
            "\t\t\t ?instance ?property ?object \n" +
            "\t\t\t OPTIONAL { ?object a ?type } \n" +
            "\t\t\t BIND(if(bound(?type), ?type, if(isLiteral(?object), datatype(?object), rdfs:Resource)) AS ?range) \n" +
            "\t\t\t BIND(isLiteral(?object) AS ?isLiteral) \n" +
            "\t\t\t BIND(isBlank(?object) AS ?isBlank) \n" +
            "\t\t } GROUP BY ?property ?range \n" +
            "\t } \n" +
            "\t OPTIONAL { GRAPH ?g { ?property rdfs:label ?l } BIND (CONCAT(?l, IF(LANG(?l),\"@\",\"\"), LANG(?l)) AS ?langLabel) } \n" +
            "\t OPTIONAL { GRAPH ?g { ?range rdfs:label ?rl } BIND (CONCAT(?rl, IF(LANG(?rl),\"@\",\"\"), LANG(?rl)) AS ?rlangLabel) } \n" +
            "} GROUP BY ?property ?range ?uses ?values ?allLiteral ?allBlank");
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
            "\t { SELECT ?value (COUNT(?value) AS ?count) \n" +
            "\t\t WHERE { \n" +
            "\t\t { SELECT DISTINCT ?instance " +
            "\t\t\t WHERE { \n" +
            "\t\t\t\t ?instance a ?class . \n" +
            getFilterPatterns(serverType, filters) +
            "\t\t\t } \n" +
            "\t\t } \n" +
            "\t\t ?instance ?property ?resource . \n" +
            "\t\t BIND(?resource AS ?value)\n \n" +
            ( isLiteral ?
                "\t\t FILTER( ISLITERAL(?resource) && DATATYPE(?resource) = <" + rangeUri + "> )\n" :
                !rangeUri.equals(RDFS.Resource.getURI()) ?
                    "\t\t ?resource a <" + rangeUri + "> \n" :
                    "\t\t OPTIONAL { ?resource a ?type } FILTER( (!BOUND(?type) || ?type=rdfs:Resource ) && !ISLITERAL(?resource) ) \n" ) +
            "\t\t } GROUP BY ?value } \n" +
            "\t OPTIONAL { ?value rdfs:label ?l BIND (CONCAT(?l, IF(LANG(?l),\"@\",\"\"), LANG(?l)) AS ?langLabel) } \n" +
            "\t OPTIONAL { GRAPH ?g { ?value rdfs:label ?l } BIND (CONCAT(?l, IF(LANG(?l),\"@\",\"\"), LANG(?l)) AS ?langLabel) } \n" +
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
    public Query getQueryFacetRangeMinMax(
            SPARQLEndPoint.ServerType serverType, String classUri, String facetUri, String rangeUri,
            MultiValueMap<String, String> filters) {
        ParameterizedSparqlString pQuery = new ParameterizedSparqlString();
        pQuery.setCommandText(prefixes +
                "SELECT (MIN(?num) AS ?min) (MAX(?num) AS ?max) \n" +
                "WHERE { \n" +
                "\t ?instance a ?class . \n" +
                getFilterPatterns(serverType, filters) +
                "\t ?instance ?property ?num . \n" +
                "\t FILTER( ISLITERAL(?num) && DATATYPE(?num) = <" + rangeUri + "> )\n" +
                "} ");
        pQuery.setIri("class", classUri);
        pQuery.setIri("property", facetUri);
        Query query = pQuery.asQuery();
        return query;
    }

    @Override
    public String convertAndFilter(SPARQLEndPoint.ServerType serverType, String property,
                                    String range, List<String> values) {
        StringBuilder pattern = new StringBuilder();
        values.forEach(value -> {
            String propertyValueVar = Integer.toUnsignedString(property.hashCode() + value.hashCode());
            if (property.equalsIgnoreCase("urn:rhz:contains")) {
                pattern.append(containingText(serverType, value, propertyValueVar));
            } else {
                pattern.append("\t ?instance <" + property + "> ?v" + propertyValueVar + " . \n");
                if (!value.equals("null")) {
                    if (value.startsWith("\"≧") || value.startsWith("\"≦")) {
                        convertRangeFilterToSparqlPattern(value, range, propertyValueVar, pattern);
                    }
                    else {
                        pattern.append(
                                (value.startsWith("<") && value.endsWith(">") ?
                                        "\t FILTER( ?v" + propertyValueVar + " = " + value + " )\n" :
                                        "\t FILTER( STR(?v" + propertyValueVar + ") = " + value +
                                                (range != null ?
                                                        " && DATATYPE(?v" + propertyValueVar + ") = <" + range + ">" : "") +
                                                " )\n"));
                    }
                }
            }
        });
        return pattern.toString();
    }

    public  String convertOrFilter(SPARQLEndPoint.ServerType serverType, String property,
                                   String range, List<String> values) {
        StringBuilder pattern = new StringBuilder();
        if (property.equalsIgnoreCase("urn:rhz:contains")) {
            values.forEach(value -> {
                String propertyValueVar = Integer.toUnsignedString(property.hashCode() + value.hashCode());
                pattern.append(containingText(serverType, value, propertyValueVar));
            });
        } else {
            String propertyVar = Integer.toUnsignedString(property.hashCode());
            pattern.append("\t ?instance <" + property + "> ?v" + propertyVar + " . \n");
            if (values.size() > 0 && !values.get(0).equals("null")) {
                if (values.get(0).startsWith("≧") || values.get(0).startsWith("≦")) {
                    convertRangeFilterToSparqlPattern(values.get(0), range, propertyVar, pattern);
                } else {
                    pattern.append("FILTER ( ?v" + propertyVar + " IN (" +
                            values.stream().collect(Collectors.joining(", "))
                            + ") ) \n");
                }
            }
        }
        return pattern.toString();
    }

    public void convertRangeFilterToSparqlPattern(String value, String range, String propertyValueVar,
                                                  StringBuilder pattern){
        String operator = value.startsWith("\"≧") ? ">=" : "<=";
        value = value.substring(2, value.lastIndexOf('"'));
        pattern.append("\t FILTER( ?v" + propertyValueVar + " " + operator + " \"" + value + "\"" +
                (range != null ? "^^<" + range + "> && DATATYPE(?v" + propertyValueVar + ") = <" + range + ">" : "") +
                " )\n");
    }

}
