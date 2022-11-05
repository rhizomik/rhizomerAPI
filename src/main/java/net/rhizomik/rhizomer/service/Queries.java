package net.rhizomik.rhizomer.service;

import java.net.URI;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +
            "PREFIX foaf: <http://xmlns.com/foaf/0.1/> \n" +
            "PREFIX text: <http://jena.apache.org/text#> \n" +
            "PREFIX bif:  <http://www.openlinksw.com/schemas/bif#> \n";

    enum QueryType { OPTIMIZED, DETAILED }

    Query getQueryClasses();

    default
    Query getQueryClassInstancesCount(
            SPARQLEndPoint.ServerType serverType, String classUri, MultiValueMap<String, String> filters) {
        ParameterizedSparqlString pQuery = new ParameterizedSparqlString();
        pQuery.setCommandText(prefixes +
                "SELECT (COUNT(DISTINCT ?instance) AS ?n) \n" +
                        "WHERE { \n" +
                        "\t ?instance a ?class . \n" +
                        getFilterPatterns(serverType, filters) +
                        "}");
        pQuery.setIri("class", classUri);
        Query query = pQuery.asQuery();
        return query;
    }

    default
    Query getQueryClassDescriptions(SPARQLEndPoint.ServerType serverType, String classUri,
                                    MultiValueMap<String, String> filters, int limit, int offset) {
        ParameterizedSparqlString pQuery = new ParameterizedSparqlString();
        pQuery.setCommandText(prefixes +
            "DESCRIBE ?instance \n" +
                "WHERE { \n" +
                "\t { SELECT DISTINCT ?instance \n" +
                "\t\t WHERE { \n" +
                "\t\t\t ?instance a ?class . \n" +
                "\t\t\t OPTIONAL { ?instance rdfs:label ?label } \n" +
                getFilterPatterns(serverType, filters) +
                "\t\t } ORDER BY (!BOUND(?label)) ASC(LCASE(?label)) LIMIT " + limit + " OFFSET " + offset + " \n" +
                "\t } \n" +
                "}");
        pQuery.setIri("class", classUri);
        Query query = pQuery.asQuery();
        return query;
    }

    default
    Query getQueryClassInstances(SPARQLEndPoint.ServerType serverType, String classUri,
                                 MultiValueMap<String, String> filters, int limit, int offset) {
        ParameterizedSparqlString pQuery = new ParameterizedSparqlString();
        pQuery.setCommandText(prefixes +
                "CONSTRUCT { \n" +
                "\t ?instance a ?class; \n" +
                "\t\t rdfs:label ?label; \n" +
                "\t\t foaf:depiction ?depiction; \n" +
                "\t\t rdfs:comment ?comment . \n" +
                "\t ?class rdfs:label ?classLabel . \n" +
                "} WHERE { \n" +
                "\t { SELECT DISTINCT ?instance \n" +
                "\t\t WHERE { \n" +
                "\t\t\t ?instance a ?class . \n" +
                "\t\t\t OPTIONAL { ?instance rdfs:label ?label } \n" +
                getFilterPatterns(serverType, filters) +
                "\t\t } ORDER BY (!BOUND(?label)) ASC(LCASE(?label)) LIMIT " + limit + " OFFSET " + offset + " \n" +
                "} \n" +
                "\t\t OPTIONAL { ?instance rdfs:label ?label } \n" +
                "\t\t OPTIONAL { ?instance foaf:depiction ?depiction } \n" +
                "\t\t OPTIONAL { ?instance rdfs:comment ?comment } \n" +
                "\t\t OPTIONAL { GRAPH ?g { OPTIONAL { ?class rdfs:label ?classLabel } } } \n" +
                "}");
        pQuery.setIri("class", classUri);
        Query query = pQuery.asQuery();
        return query;
    }

    default
    Query getQuerySearchInstancesCount(SPARQLEndPoint.ServerType serverType, String text) {
        ParameterizedSparqlString pQuery = new ParameterizedSparqlString();
        pQuery.setCommandText(prefixes +
                "SELECT (COUNT(DISTINCT ?instance) AS ?n) \n" +
                "WHERE { \n" +
                containingText(serverType, text) +
                "}");
        Query query = pQuery.asQuery();
        return query;
    }

    default
    Query getQuerySearchInstances(SPARQLEndPoint.ServerType serverType, String text, int limit) {
        ParameterizedSparqlString pQuery = new ParameterizedSparqlString();
        pQuery.setCommandText(prefixes +
                "CONSTRUCT { \n" +
                "\t ?instance a ?class; \n" +
                "\t\t rdfs:label ?label; \n" +
                "\t\t foaf:depiction ?depiction; \n" +
                "\t\t ?property ?value . \n" +
                "\t ?class rdfs:label ?classLabel . \n" +
                "\t ?property rdfs:label ?propLabel . \n" +
                "\t ?value rdfs:label ?valueLabel . \n" +
                "} WHERE { { SELECT ?instance ?value WHERE { \n" +
                containingText(serverType, text) +
                "\t } LIMIT " + limit + " } \n" +
                "\t ?instance a ?class ; ?property ?value \n" +
                "\t OPTIONAL { ?instance rdfs:label ?label } \n" +
                "\t OPTIONAL { ?value rdfs:label ?valueLabel } \n" +
                "\t OPTIONAL { ?instance foaf:depiction ?depiction } \n" +
                "\t OPTIONAL { GRAPH ?g { OPTIONAL { ?class rdfs:label ?classLabel } } } \n" +
                "\t OPTIONAL { GRAPH ?g { OPTIONAL { ?property rdfs:label ?propLabel } } } \n" +
                "}");
        return pQuery.asQuery();
    }

    default
    Query getQuerySearchTypeFacet(SPARQLEndPoint.ServerType serverType, String text,
                                  int limit, int offset, boolean ordered) {
        ParameterizedSparqlString pQuery = new ParameterizedSparqlString();
        pQuery.setCommandText(prefixes +
                "SELECT ?class ?count (GROUP_CONCAT(?langLabel; SEPARATOR = \" || \") AS ?label) \n" +
                "WHERE { \n" +
                "\t { SELECT ?class (COUNT(DISTINCT ?instance) as ?count) \n" +
                "\t\t WHERE { \n" +
                containingText(serverType, text) +
                "\t\t } GROUP BY ?class } \n" +
                "\t OPTIONAL { GRAPH ?g { ?class rdfs:label ?l } BIND (CONCAT(?l, IF(LANG(?l),\"@\",\"\"), LANG(?l)) AS ?langLabel) } \n" +
                "} GROUP BY ?class ?count");
        Query query = pQuery.asQuery();
        if (limit > 0) query.setLimit(limit);
        if (offset > 0) query.setOffset(offset);
        if (ordered) query.addOrderBy("count", -1);
        return query;
    }

    default
    Query getQueryClassInstancesLabels(SPARQLEndPoint.ServerType serverType, String classUri,
                                       MultiValueMap<String, String> filters, int limit, int offset) {
        ParameterizedSparqlString pQuery = new ParameterizedSparqlString();
        pQuery.setCommandText(prefixes +
            "CONSTRUCT { ?resource rdfs:label ?label } \n" +
            "WHERE { \n" +
            "\t { SELECT DISTINCT ?instance \n" +
            "\t\t WHERE { \n" +
            "\t\t\t ?instance a ?class . \n" +
            "\t\t\t OPTIONAL { ?instance rdfs:label ?label } \n" +
            getFilterPatterns(serverType, filters) +
            "\t\t } ORDER BY (!BOUND(?label)) ASC(LCASE(?label)) LIMIT " + limit + " OFFSET " + offset + " \n" +
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
            "\t\t GRAPH ?g { ?resource rdfs:label ?label } \n" +
            " } UNION { \n" +
            "\t\t ?instance a ?resource . \n" +
            "\t\t GRAPH ?g { ?resource rdfs:label ?label } \n" +
            "} }");
        pQuery.setIri("class", classUri);
        Query query = pQuery.asQuery();
        return query;
    }

    Query getQueryClassFacets(String classUri);

    Query getQueryFacetRangeValues(
            SPARQLEndPoint.ServerType serverType, String classUri, String facetUri, String rangeUri,
            MultiValueMap<String, String> filters, boolean isLiteral, int limit, int offset, boolean ordered);

    Query getQueryFacetRangeValuesContaining(
            SPARQLEndPoint.ServerType serverType, String classUri, String facetUri, String rangeUri,
            MultiValueMap<String, String> filters, boolean isLiteral, String containing, int top, String lang);

    Query getQueryFacetRangeMinMax(
            SPARQLEndPoint.ServerType serverType, String classUri, String facetUri, String rangeUri,
            MultiValueMap<String, String> filters);

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
            " } UNION { \n" +
            "\t ?instance ?propertyanon ?anon . FILTER(isBlank(?anon)) \n" +
            "\t ?anon ?property ?resource .\n" +
            "\t ?resource rdfs:label ?label . \n" +
            " } UNION { \n" +
            "\t ?instance ?propertyanon ?anon . FILTER(isBlank(?anon)) \n" +
            "\t ?anon ?resource ?object .\n" +
            "\t GRAPH ?g { ?resource rdfs:label ?label } \n" +
            " } UNION { \n" +
            "\t ?instance ?propertyanon ?anon . FILTER(isBlank(?anon)) \n" +
            "\t ?anon a ?resource .\n" +
            "\t GRAPH ?g { ?resource rdfs:label ?label } \n" +
            " } UNION { \n" +
            "\t ?instance ?resource ?object . \n" +
            "\t GRAPH ?g { ?resource rdfs:label ?label } \n" +
            " } UNION { \n" +
            "\t ?instance a ?resource . \n" +
            "\t GRAPH ?g { ?resource rdfs:label ?label } \n" +
            "} }");
        pQuery.setIri("instance", resourceUri.toString());
        Query query = pQuery.asQuery();
        return query;
    }

    default Query getQueryResourceIncomingFacets(URI resourceUri) {
        ParameterizedSparqlString pQuery = new ParameterizedSparqlString();
        pQuery.setCommandText(prefixes +
            "SELECT (SAMPLE(?class) as ?range) ?prop ?uses ?domain (COUNT(DISTINCT ?s) AS ?count) " +
            "   (GROUP_CONCAT(DISTINCT(?pLabel) ; separator=' || ') AS ?proplabel) " +
            "   (GROUP_CONCAT(DISTINCT(?dLabel) ; separator=' || ') AS ?domainlabel) \n" +
            "WHERE { \n" +
            "\t { SELECT ?class ?prop (COUNT(DISTINCT ?s) AS ?uses) \n" +
            "\t\t WHERE { \n" +
            "\t\t\t ?s ?prop ?resource . \n" +
            "\t\t\t OPTIONAL { ?resource a ?class } \n" +
            "\t\t\t FILTER NOT EXISTS { ?subclass rdfs:subClassOf ?class . ?resource a ?subclass } \n" +
            "\t } GROUP BY ?class ?prop } \n" +
            "\t ?s ?prop ?resource ; a ?domain . \n" +
            "\t FILTER NOT EXISTS { ?subdomain rdfs:subClassOf ?domain . ?s a ?subdomain } \n" +
            "\t OPTIONAL { GRAPH ?g { ?prop  rdfs:label ?l } BIND (CONCAT(?l, IF(LANG(?l),\"@\",\"\"), LANG(?l)) AS ?pLabel) } \n" +
            "\t OPTIONAL { GRAPH ?g { ?domain rdfs:label ?dl } BIND (CONCAT(?dl, IF(LANG(?dl),\"@\",\"\"), LANG(?dl)) AS ?dLabel) } \n" +
            "} GROUP BY ?prop ?domain ?uses");
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
            "INSERT { GRAPH <" + datasetInferenceGraph + "> { ?i a ?class } } \n" +
            targetGraphs.stream().map(s -> String.format("USING <%s> \n", s)).collect(Collectors.joining()) +
            "WHERE { \n" +
            "\t { ?subclass rdfs:subClassOf* ?class . \n" +
            "\t\t ?i a ?subclass \n" +
            "\t\t FILTER NOT EXISTS {?i a ?class} \n" +
            "\t } \n" +
            "\t UNION \n" +
            "\t { ?p rdfs:domain ?type . \n" +
            "\t\t ?subp rdfs:subPropertyOf* ?p . \n" +
            "\t\t ?i ?subp ?o \n" +
            "\t\t FILTER NOT EXISTS {?i a ?class} \n" +
            "\t } \n" +
            "\t UNION \n" +
            "\t { ?p rdfs:range ?type . \n" +
            // "\t\t ?subp rdfs:subPropertyOf* ?p . \n" +
            "\t\t ?s ?p ?i \n" +
            "\t\t FILTER NOT EXISTS {?i a ?class} \n" +
            "\t } \n" +
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

    default String getFilterPatterns(SPARQLEndPoint.ServerType serverType, MultiValueMap<String, String> filters) {
        StringBuilder filtersPatterns = new StringBuilder();
        filters.forEach((property_range, values) -> {
            String property = property_range.split(" ")[0];
            String range = property_range.indexOf(" ") > 0 ? property_range.split(" ")[1] : null;
            if (values.size() > 1) {
                filtersPatterns.append(convertOrFilter(serverType, property, range, values));
            } else if (values.size() == 1 && values.get(0).startsWith("AND(")) {
                filtersPatterns.append(convertAndFilter(serverType, property, range, patternValuesToList(values.get(0))));
            } else if (values.size() == 1 && values.get(0).startsWith("OR(")) {
                filtersPatterns.append(convertOrFilter(serverType, property, range, patternValuesToList(values.get(0))));
            } else {
                filtersPatterns.append(convertAndFilter(serverType, property, range, patternValuesToList(values.get(0))));
            }
        });
        return filtersPatterns.toString();
    }

    default String convertAndFilter(SPARQLEndPoint.ServerType serverType, String property,
                                    String range, List<String> values) {
        StringBuilder pattern = new StringBuilder();
        values.forEach(value -> {
            String propertyValueVar = Integer.toUnsignedString(property.hashCode() + value.hashCode());
            if (property.equalsIgnoreCase("urn:rhz:contains")) {
                pattern.append(containingText(serverType, value, propertyValueVar));
            } else {
                pattern.append("\t ?instance <" + property + "> ?v" + propertyValueVar + " . \n");
                if (!value.equals("null")) {
                    String valueString = value;
                    if (valueString.startsWith("<") && valueString.endsWith(">")) {
                        valueString = valueString.substring(1, valueString.length() - 1);
                    }
                    pattern.append("FILTER ( STR(?v" + propertyValueVar + ") = " + valueString + " ) \n");
                }
            }
        });
        return pattern.toString();
    }

    default String convertOrFilter(SPARQLEndPoint.ServerType serverType, String property,
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
                pattern.append("FILTER ( STR(?v" + propertyVar + ") IN (" +
                        values.stream().map(value -> (value.startsWith("<") && value.endsWith(">")) ?
                                        "\"" + value.substring(1, value.length() - 1) + "\"" : value)
                                .collect(Collectors.joining(", "))
                        + ") ) \n");
            }
        }
        return pattern.toString();
    }

    default String containingText(SPARQLEndPoint.ServerType serverType, String text) {
        return containingText(serverType, text, "");
    }

    default String containingText(SPARQLEndPoint.ServerType serverType, String text, String propValueId) {
        if (serverType == SPARQLEndPoint.ServerType.FUSEKI_LUCENE) {
            String queryText = text.replaceAll("\"", "").toLowerCase();
            return  "\t { (?instance [] ?value"+propValueId+") text:query \"\\\"" + queryText + "\\\"\" } \n" +
                    "\t UNION \n" +
                    "\t { ?value"+propValueId+" text:query \"\\\"" + queryText + "\\\"\" } \n" +
                    "\t ?instance a ?class ; ?property"+propValueId+" ?value"+propValueId+" \n";
        } else if (serverType == SPARQLEndPoint.ServerType.VIRTUOSO) {
            String queryText = text.replaceAll("\"", Matcher.quoteReplacement("\\\"")).toLowerCase();
            return  "\t { ?instance a ?class ; ?property"+propValueId+" ?value"+propValueId+" \n" +
                    "\t\t FILTER(bif:contains(?value"+propValueId+", \"'" + queryText + "'\")) } \n" +
                    "\t UNION \n" +
                    "\t { ?instance a ?class ; ?property"+propValueId+" ?value"+propValueId+" . ?value"+propValueId+" rdfs:label ?valueLabel"+propValueId+" \n" +
                    "\t\t FILTER(bif:contains(?valueLabel"+propValueId+", \"'" + queryText + "'\")) } \n";
        } else {
            String queryText = text.replaceAll("\"", "").toLowerCase();
            return  "\t ?instance a ?class ; ?property"+propValueId+" ?value"+propValueId+" \n" +
                    "\t OPTIONAL { ?value"+propValueId+" rdfs:label ?valueLabel"+propValueId+" } \n" +
                    "\t FILTER ( ( ISLITERAL(?value"+propValueId+") && CONTAINS(LCASE(STR(?value"+propValueId+")), \""+ queryText + "\") ) || \n" +
                    "\t\t CONTAINS(LCASE(STR(?valueLabel"+propValueId+")), \"" + queryText + "\") ) \n";
        }
    }

    default List<String> patternValuesToList (String values) {
        if ((values.startsWith("AND(") || values.startsWith("OR(")) && values.endsWith(")")) {
            values = values.substring(values.indexOf("(") + 1, values.lastIndexOf(")"));
        }
        if (values.matches("^<[^>]+>(?: <[^>]+>)*$")) {
            Pattern p = Pattern.compile("<[^>]+>");
            Matcher m = p.matcher(values);
            return m.results().map(match -> match.group()).collect(Collectors.toList());
        } else if (values.matches("^\\\"[^\\\"]+\\\"(?: \\\"[^\\\"]+\\\")*$")) {
            Pattern p = Pattern.compile("\\\"[^\\\"]+\\\"");
            Matcher m = p.matcher(values);
            return m.results().map(match -> match.group()).collect(Collectors.toList());
        }
        return List.of(values);
    }
}
