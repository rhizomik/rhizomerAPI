package net.rhizomik.rhizomer.model;

import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;

/**
 * Created by http://rhizomik.net/~roberto/
 */
public class Queries {
    private static final String prefixes =
            "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +
            "PREFIX owl: <http://www.w3.org/2002/07/owl#> \n" +
            "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> \n";

    public enum QueryType { SIMPLE, FULL }

    public static Query getQueryClasses(QueryType queryType){
        switch (queryType) {
            case SIMPLE: return Queries.classesFull();
            case FULL: return Queries.classesFull();
            default: return Queries.classesSimple();
        }
    }

    public static Query getQueryClassFacets(String classUri, QueryType queryType, int sampleSize, int classCount, double coverage) {
        switch (queryType) {
            case SIMPLE:
                if (sampleSize > 0 && coverage > 0.0)
                    return Queries.classFacetsSimplified(classUri,sampleSize, classCount, coverage);
                else
                return Queries.classFacetsSimplified(classUri, sampleSize);
            case FULL:
                if (sampleSize > 0 && coverage > 0.0)
                    return Queries.classFacetsComplete(classUri,sampleSize, classCount, coverage);
                else
                    return Queries.classFacetsComplete(classUri, sampleSize);
            default: return Queries.classFacetsSimplified(classUri, sampleSize);
        }
    }

    public static Query getQueryInferTypes() {
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

    public static UpdateRequest getUpdateInferTypes(List<String> targetGraphs, String datasetInferenceGraph) {
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


    public static UpdateRequest getCreateGraph(String graph) {
        return UpdateFactory.create(
            "CREATE GRAPH <" + graph + ">");
    }

    public static UpdateRequest getClearGraph(String graph) {
        return UpdateFactory.create(
            "CLEAR GRAPH <" + graph + ">");
    }

    public static Query getQueryCountUntyped() {
        return QueryFactory.create(prefixes +
            "SELECT (COUNT(DISTINCT(?i)) AS ?n) \n" +
            "WHERE { \n" +
            "\t { ?i ?p ?o FILTER NOT EXISTS { ?i a ?class } } \n" +
            "\t UNION \n" +
            "\t { ?s ?p ?i FILTER NOT EXISTS { ?i a ?class } } \n" +
            "}");
    }

    public static Query getQueryCountTriples() {
        return QueryFactory.create(prefixes +
                "SELECT (COUNT(?s)) AS ?n) \n" +
                "WHERE { ?s ?p ?o }");
    }

    public static Query getQueryGraphs() {
        return QueryFactory.create(
                "SELECT DISTINCT ?graph \n" +
                "WHERE { GRAPH ?graph { ?s ?p ?o } }");
    }

    public static Query getQueryCountType(String type) {
        ParameterizedSparqlString pQuery = new ParameterizedSparqlString();
        pQuery.setCommandText(prefixes +
            "SELECT (COUNT(DISTINCT(?s)) AS ?n) \n" +
            "WHERE { ?s a ?type }");
        pQuery.setIri("type", type);
        return pQuery.asQuery();
    }

    // Does not bother about class count or untyped resources
    public static final Query classesSimple() {
        return QueryFactory.create(prefixes +
                "SELECT DISTINCT ?class (0 AS ?n) \n" +
                "WHERE { \n" +
                "\t ?instance a ?class . FILTER ( !isBlank(?class) ) \n" +
                "}");
    }

    public static final Query classesFull() {
        return QueryFactory.create(prefixes +
                "SELECT ?class (COUNT(DISTINCT ?instance) as ?n) \n" +
                "WHERE { \n" +
                "\t { ?instance a ?class . FILTER ( !isBlank(?class) ) } UNION \n" +
                "\t { ?instance ?p ?o . FILTER(NOT EXISTS {?instance a ?c} ) BIND(rdfs:Resource AS ?class) } \n" +
                "} GROUP BY ?class");
    }

    // Does not collect all potential ranges or checking all are literal to decide if allLiteral, just picks one of each
    public static Query classFacetsSimplified(String classUri, int sampleSize) {
        ParameterizedSparqlString pQuery = new ParameterizedSparqlString();
        pQuery.setCommandText(prefixes +
            "SELECT ?property (0 AS ?uses) (0 AS ?values) (SAMPLE(?range) AS ?ranges) (SAMPLE(?isLiteral) as ?allLiteral) \n" +
            "WHERE { \n" +
            "\t { SELECT ?instance WHERE { ?instance a ?class } " + ((sampleSize>0) ? "LIMIT "+sampleSize : "") + " } \n" +
            "\t ?instance ?property ?object \n" +
            "\t OPTIONAL { ?object a ?type }\n" +
            "\t BIND(STR( IF(bound(?type), ?type, IF(isLiteral(?object), datatype(?object), rdfs:Resource)) ) AS ?range) \n" +
            "\t BIND(isLiteral(?object) AS ?isLiteral) \n" +
            "} GROUP BY ?property");
        pQuery.setIri("class", classUri);
        return pQuery.asQuery();
    }

    public static Query classFacetsSimplified(String classUri, int sampleSize, int classCount, double coverage) {
        ParameterizedSparqlString pQuery = new ParameterizedSparqlString();
        pQuery.setCommandText(prefixes +
            "SELECT ?property (0 AS ?uses) (0 AS ?values) (SAMPLE(?range) AS ?ranges) (SAMPLE(?isLiteral) as ?allLiteral) \n" +
            "WHERE { \n" +
            "\t { { SELECT ?instance WHERE { ?instance a ?class } OFFSET 0 " + "LIMIT " + sampleSize + " } \n" +
            addSamples(classCount, sampleSize, coverage) + " } \n" +
            "\t ?instance ?property ?object \n" +
            "\t OPTIONAL { ?object a ?type }\n" +
            "\t BIND(STR( IF(bound(?type), ?type, IF(isLiteral(?object), datatype(?object), rdfs:Resource)) ) AS ?range) \n" +
            "\t BIND(isLiteral(?object) AS ?isLiteral) \n" +
            "} GROUP BY ?property");
        pQuery.setIri("class", classUri);
        return pQuery.asQuery();
    }

    public static Query classFacetsComplete(String classUri, int sampleSize) {  // TODO: consider also subclasses?
        ParameterizedSparqlString pQuery = new ParameterizedSparqlString();
        pQuery.setCommandText(prefixes +
                "SELECT ?property ?range (COUNT(?instance) AS ?uses) (COUNT(DISTINCT ?object) AS ?values) (MIN(?isLiteral) as ?allLiteral) \n" +
                "WHERE { \n" +
                "\t { SELECT ?instance WHERE { ?instance a ?class } " + ((sampleSize>0) ? "LIMIT "+sampleSize : "") + " } \n" +
                "\t ?instance ?property ?object \n" +
                "\t OPTIONAL { ?object a ?type }\n" +
                "\t BIND(if(bound(?type), ?type, if(isLiteral(?object), if(datatype(?object)=\"\", datatype(?object), xsd:string), rdfs:Resource)) AS ?range) \n" +
                "\t BIND(if(isLiteral(?object), 1, 0) AS ?isLiteral) \n" +
                "} GROUP BY ?property ?range");
        pQuery.setIri("class", classUri);
        return pQuery.asQuery();
    }

    public static Query classFacetsComplete(String classUri, int sampleSize, int classCount, double coverage) {  // TODO: consider also subclasses?
        ParameterizedSparqlString pQuery = new ParameterizedSparqlString();
        pQuery.setCommandText(prefixes +
                "SELECT ?property ?range (COUNT(?instance) AS ?uses) (COUNT(DISTINCT ?object) AS ?values) (MIN(?isLiteral) as ?allLiteral) \n" +
                "WHERE { \n" +
                "\t { { SELECT ?instance WHERE { ?instance a ?class } OFFSET 0 "+"LIMIT "+sampleSize+" } \n" +
                addSamples(classCount, sampleSize, coverage) + " } \n" +
                "\t ?instance ?property ?object \n" +
                "\t OPTIONAL { ?object a ?type }\n" +
                "\t BIND(if(bound(?type), ?type, if(isLiteral(?object), if(datatype(?object)=\"\", xsd:string, datatype(?object)), rdfs:Resource)) AS ?range) \n" +
                "\t BIND(if(isLiteral(?object), 1, 0) AS ?isLiteral) \n" +
                "} GROUP BY ?property ?range");
        pQuery.setIri("class", classUri);
        return pQuery.asQuery();
    }

    public static Query getQueryFacetRageValues(String classUri, String facetUri, String rangeUri, int limit, int offset, boolean ordered) {
        ParameterizedSparqlString pQuery = new ParameterizedSparqlString();
        pQuery.setCommandText(prefixes +
            "SELECT ?value (COUNT(?value) AS ?count) \n" +
            "WHERE { \n" +
            "\t ?instance a ?class . \n" +
            "\t ?instance ?property ?value . \n" +
            ( rangeUri.equals(XSD.xstring.getURI()) ?
                "\t FILTER( isLiteral(?value) && ( datatype(?value)=\"\" || datatype(?value)=xsd:string ) )\n" :
                ( rangeUri.startsWith(XSD.getURI()) ?
                    "\t FILTER( isLiteral(?value) && datatype(?value)=<" + rangeUri + "> )\n" :
                    !rangeUri.equals(RDFS.Resource.getURI()) ?
                        "\t ?value a <" + rangeUri + "> \n" :
                        "\t OPTIONAL { ?value a ?type } FILTER( !BOUND(?type) && !isLiteral(?value) ) \n" ) ) +
            "} GROUP BY ?value");
        pQuery.setIri("class", classUri);
        pQuery.setIri("property", facetUri);
        Query query = pQuery.asQuery();
        if (limit > 0) query.setLimit(limit);
        if (offset > 0) query.setOffset(offset);
        if (ordered) query.addOrderBy("count", -1);
        return query;
    }

    public static String addSamples(int classCount, int sampleSize, double coverage) {
        String selectsUnion = "";
        int samplesCount = (int)Math.floor((classCount * coverage)/sampleSize);
        int offset = (int) Math.ceil((double) classCount/samplesCount);

        for(int sample = 1; sample < samplesCount; sample++) {
            selectsUnion += "\t\t UNION { SELECT ?instance WHERE { ?instance a ?class } OFFSET "+ sample * offset +" LIMIT "+sampleSize+" } \n";
        }

        return selectsUnion;
    }

    // TODO: consider omitting the following classes, properties and namespaces

    private static String[] omitPropertiesArray = {
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type"
    };

    private static String[] omitClassesArray = {
            "http://www.w3.org/2002/07/owl#",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#",
            "http://www.w3.org/2000/01/rdf-schema#",
            "http://www.w3.org/2001/XMLSchema#"
    };

    public static Set<String> getOmitProperties() {
        return new HashSet<>(Arrays.asList(omitPropertiesArray));
    }

    public static Set<String> getOmitClasses() {
        return new HashSet<>(Arrays.asList(omitClassesArray));
    }
}
