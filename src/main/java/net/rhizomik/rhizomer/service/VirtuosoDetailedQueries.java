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
public class VirtuosoDetailedQueries extends DetailedQueries {

    @Override
    public Query getQueryClassFacets(String classUri, int sampleSize, int classCount, double coverage) {
        ParameterizedSparqlString pQuery = new ParameterizedSparqlString();
        if (sampleSize > 0 && coverage > 0.0) {
            pQuery.setCommandText(prefixes +
                "SELECT ?property ?range (COUNT(?instance) AS ?uses) (COUNT(DISTINCT ?object) AS ?values) " +
                "       (MIN(?isLiteral) as ?allLiteral) (SAMPLE(?labels) AS ?label) (SAMPLE(?rlabels) AS ?rlabel) \n" +
                "WHERE { \n" +
                "\t { { SELECT ?instance WHERE { ?instance a ?class } OFFSET 0 "+"LIMIT "+sampleSize+" } \n" +
                addSamples(classCount, sampleSize, coverage) + " } \n" +
                "\t ?instance ?property ?object \n" +
                "\t OPTIONAL { ?object a ?type }\n" +
                "\t BIND(if(bound(?type), ?type, if(isLiteral(?object), datatype(?object), rdfs:Resource)) AS ?range) \n" +
                "\t BIND(isLiteral(?object) AS ?isLiteral) \n" +
                "\t OPTIONAL { GRAPH ?g { ?property rdfs:label ?labels FILTER LANGMATCHES(LANG(?labels), \"en\") } } \n" +
                "\t OPTIONAL { GRAPH ?g { ?property rdfs:label ?labels } } \n" +
                "\t OPTIONAL { GRAPH ?g { ?range rdfs:label ?rlabels FILTER LANGMATCHES(LANG(?rlabels), \"en\") } } \n" +
                "\t OPTIONAL { GRAPH ?g { ?range rdfs:label ?rlabels } } \n" +
                "} GROUP BY ?property ?range");
        } else {
            pQuery.setCommandText(prefixes +
                "SELECT ?property ?range (COUNT(?instance) AS ?uses) (COUNT(DISTINCT ?object) AS ?values) " +
                "       (MIN(?isLiteral) as ?allLiteral) (SAMPLE(?labels) AS ?label) (SAMPLE(?rlabels) AS ?rlabel) \n" +
                "WHERE { \n" +
                "\t { SELECT ?instance WHERE { ?instance a ?class } " + ((sampleSize>0) ? "LIMIT "+sampleSize : "") + " } \n" +
                "\t ?instance ?property ?object \n" +
                "\t OPTIONAL { ?object a ?type }\n" +
                "\t BIND(if(bound(?type), ?type, if(isLiteral(?object), datatype(?object), rdfs:Resource)) AS ?range) \n" +
                "\t BIND(isLiteral(?object) AS ?isLiteral) \n" +
                "\t OPTIONAL { GRAPH ?g { ?property rdfs:label ?labels FILTER LANGMATCHES(LANG(?labels), \"en\") } } \n" +
                "\t OPTIONAL { GRAPH ?g { ?property rdfs:label ?labels } } \n" +
                "\t OPTIONAL { GRAPH ?g { ?range rdfs:label ?rlabels FILTER LANGMATCHES(LANG(?rlabels), \"en\") } } \n" +
                "\t OPTIONAL { GRAPH ?g { ?range rdfs:label ?rlabels } } \n" +
                "} GROUP BY ?property ?range");
        }
        pQuery.setIri("class", classUri);
        return pQuery.asQuery();
    }
}
