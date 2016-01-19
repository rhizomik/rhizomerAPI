package net.rhizomik.rhizomer.service;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("ConstantConditions")
public class FacetGenerator {

    private String NL = System.getProperty("line.separator");
    private static final Logger log = Logger.getLogger(FacetGenerator.class.getName());


    private static String[] omitClasses = {
            "http://www.w3.org/2002/07/owl#Nothing",
            "http://www.w3.org/2002/07/owl#Thing"};
    private static String[] omitNamespaces = {
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#",
            "http://www.w3.org/2002/07/owl#",
            "http://www.w3.org/2000/01/rdf-schema#",
            "http://www.w3.org/2001/XMLSchema#"};

    private Connection conn;

    private String queryForClasses =
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"+NL+
                    "PREFIX owl: <http://www.w3.org/2002/07/owl#>"+NL+
                    "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"+NL+
                    "SELECT DISTINCT ?c"+NL+
                    "WHERE {"+NL+
                    "   { ?i rdf:type ?c } UNION { ?subc rdfs:subClassOf ?c }"+NL+
                    "   FILTER (!isBlank(?c) && isURI(?c) )"+NL+
                    "} GROUP BY ?c";

    private String queryForSubClasses =
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"+NL+
                    "PREFIX owl: <http://www.w3.org/2002/07/owl#>"+NL+
                    "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"+NL+
                    "SELECT DISTINCT ?subclass"+NL+
                    "WHERE {"+NL+
                    "   ?subclass rdfs:subClassOf <%1$s>"+NL+
                    "} GROUP BY ?subclass";

    private String queryForCountInstancesProperty =
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"+NL+
                    "PREFIX owl: <http://www.w3.org/2002/07/owl#>"+NL+
                    "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"+NL+
                    "SELECT (COUNT(?x) AS ?n)"+NL+
                    "WHERE {"+NL+
                    "   ?x a <%1$s> ; <%2$s> ?o"+NL+
                    "}";

    private String queryForCountInstancesInverseProperty =
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"+NL+
                    "PREFIX owl: <http://www.w3.org/2002/07/owl#>"+NL+
                    "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"+NL+
                    "SELECT (COUNT(?o) AS ?n)"+NL+
                    "WHERE {"+NL+
                    "   ?o a <%1$s>. ?x <%2$s> ?o."+NL+
                    "}";

    private String queryForInverseProperties =
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"+NL+
                    "PREFIX owl: <http://www.w3.org/2002/07/owl#>"+NL+
                    "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"+NL+
                    "SELECT DISTINCT ?p ?r"+NL+
                    "WHERE {"+NL+
                    "   ?o a ?type. ?x ?p ?o"+NL+
                    "   OPTIONAL { ?p rdfs:range ?r }"+NL+
                    "   FILTER (?p!=owl:differentFrom && ?p!=owl:sameAs)"+NL+
                    "   FILTER (%1$s)"+NL+  // Generate filter to get inverse properties for class and also all subclasses
                    "}";

    private String queryForCountTotalInstances =
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"+NL+
                    "PREFIX owl: <http://www.w3.org/2002/07/owl#>"+NL+
                    "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"+NL+
                    "SELECT (COUNT(?x) AS ?n)"+NL+
                    "WHERE {"+NL+
                    "   ?x a <%1$s>"+NL+
                    "}";

    private String queryForProperties =
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"+NL+
                    "PREFIX owl: <http://www.w3.org/2002/07/owl#>"+NL+
                    "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"+NL+
                    "SELECT DISTINCT ?p ?r"+NL+
                    "WHERE {"+NL+
                    "   ?x a ?type ; ?p ?o"+NL+
                    "   OPTIONAL { ?p rdfs:range ?r }"+NL+
                    "   FILTER (?o != \"\")"+NL+
                    "   FILTER (?p!=owl:differentFrom && ?p!=owl:sameAs)"+NL+
                    "   FILTER (%1$s)"+NL+   // Generate filter to get properties for class and also all subclasses
                    "}";

    private String queryForEntropy =
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"+NL+
                    "PREFIX owl: <http://www.w3.org/2002/07/owl#>"+NL+
                    "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"+NL+
                    "SELECT (COUNT(?o) AS ?n)"+NL+
                    "WHERE {"+NL+
                    "   ?x a <%1$s> ; <%2$s> ?o"+NL+
                    "}";

    private String queryForCountValues =
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"+NL+
                    "PREFIX owl: <http://www.w3.org/2002/07/owl#>"+NL+
                    "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"+NL+

                    "SELECT (COUNT(distinct(?o)) AS ?n)"+NL+
                    "WHERE {"+NL+
                    "   ?x a <%1$s> ; <%2$s> ?o ."+NL+
                    "   FILTER (?o!=\"\")"+NL+
                    "}";

    private String queryForCountInversePropertyValues =
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"+NL+
                    "PREFIX owl: <http://www.w3.org/2002/07/owl#>"+NL+
                    "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"+NL+
                    "SELECT (COUNT(distinct(?x)) AS ?n)"+NL+
                    "WHERE {"+NL+
                    "   ?o a <%1$s>. ?x <%2$s> ?o ."+NL+
                    "   FILTER (?o!=\"\")"+NL+
                    "}";

    private String queryIsNotInverseFunctional =
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"+NL+
                    "PREFIX owl: <http://www.w3.org/2002/07/owl#>"+NL+
                    "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"+NL+
                    "ASK"+NL+
                    "WHERE {"+NL+
                    "   ?x a <%1$s> ; <%2$s> ?o ."+NL+
                    "   ?y a <%1$s> ; <%2$s> ?o ."+NL+
                    "   FILTER (?x!=?y)"+NL+
                    "}";
    private String queryForMaxCardinality =
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"+NL+
                    "PREFIX owl: <http://www.w3.org/2002/07/owl#>"+NL+
                    "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"+NL+
                    "SELECT (COUNT(?o) AS ?n)"+NL+
                    "WHERE {"+NL+
                    "   ?x a <%1$s> ; <%2$s> ?o ."+NL+
                    "   FILTER (?o!=\"\")"+NL+
                    "}"+NL+
                    "GROUP BY ?o ORDER BY DESC(?n) LIMIT 1";

    private String queryForMaxCardinalityForInverseProperty =
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"+NL+
                    "PREFIX owl: <http://www.w3.org/2002/07/owl#>"+NL+
                    "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"+NL+
                    "SELECT (COUNT(?x) AS ?n)"+NL+
                    "WHERE {"+NL+
                    "   ?o a <%1$s>. ?x <%2$s> ?o ."+NL+
                    "   FILTER (?o!=\"\")"+NL+
                    "}"+NL+
                    "GROUP BY ?x ORDER BY DESC(?n) LIMIT 1";

    public FacetGenerator() {}

    private void createDB(String path, int facetHash) throws ClassNotFoundException, SQLException {
        String filePath = path+="facets-"+facetHash+".db";

        if(!fileExists(filePath)) {
            Class.forName("org.sqlite.JDBC"); // TODO: get db class from web.xml
            conn = DriverManager.getConnection("jdbc:sqlite:" + filePath);
            System.out.println("File \""+filePath+"\" created, generating facets");
            Statement stat = conn.createStatement();
            stat.execute("CREATE TABLE if not exists class_summary(class varchar(255), num_instances int, " +
                    "primary key(class))");
            /*stat.execute("CREATE TABLE if not exists property_summary(id int auto_increment, " +
                    "class varchar(255), property varchar(255), num_instances int, different_values int, " +
                    "entropy float, value_range varchar(255), value_type varchar(255), primary key(id))");
            */
            stat.execute("CREATE TABLE if not exists property_summary(id int auto_increment, " +
                    "class varchar(255), property varchar(255), num_instances int, different_values int, " +
                    "max_value int, max_cardinality int, value_range varchar(255), value_type varchar(255), is_inverse boolean, primary key(id))");
            stat.close();
            generateFacets();
        }
        else
            System.out.println("File \""+filePath+"\" already exists");
    }

    private void generateFacets() throws SQLException{
        ArrayList<String> classes = getClasses();

        for(String classUri : classes){
            if(!isInOmitClasses(classUri) && !isInOmitNamespaces(classUri)){
                System.out.println("Generating facets for "+classUri);
                generateFacetsForClass(classUri);
            }
            else
                System.out.println("Omiting facets for "+classUri);
        }
    }

    private boolean fileExists(String filename){
        File f = new File(filename);
        return f.exists();
    }

    boolean isInOmitClasses(String classUri){
        for(String omittedClass: omitClasses){
            if (classUri.equals(omittedClass))
                return true;
        }
        return false;
    }

    boolean isInOmitNamespaces(String classUri){
        for(String namespace: omitNamespaces){
            if (classUri.startsWith(namespace))
                return true;
        }
        return false;
    }

    private ArrayList<String> getClasses(){
        ArrayList<String> classes = new ArrayList<String>();
        ResultSet results = RhizomerRDF.instance().querySelect(queryForClasses, MetadataStore.REASONING);
        while(results.hasNext()){
            QuerySolution row = results.next();
            classes.add(row.get("c").toString());
        }
        return classes;
    }

    private ArrayList<String> getSubClasses(String uri){
        StringBuilder queryString = new StringBuilder();
        Formatter f = new Formatter(queryString);
        Object[] vars = {uri};
        f.format(queryForSubClasses, vars);
        ResultSet results = RhizomerRDF.instance().querySelect(queryString.toString(), MetadataStore.REASONING);
        ArrayList<String> subclasses = new ArrayList<String>();
        try {
            while(results.hasNext()){
                QuerySolution row = results.next();
                subclasses.add(row.get("subclass").toString());
            }
        } catch (Exception e) {}
        return subclasses;
    }

    private int countInstancesForProperty(String uri, String property){
        StringBuilder queryString = new StringBuilder();
        Formatter f = new Formatter(queryString);
        Object[] vars = {uri, property};
        f.format(queryForCountInstancesProperty, vars);
        ResultSet results = RhizomerRDF.instance().querySelect(queryString.toString(), MetadataStore.REASONING);
        int count = 0;
        if (results.hasNext())
        {
            QuerySolution row = results.next();
            // The first var is the count value
            String countVar = results.getResultVars().get(0);
            count = row.getLiteral(countVar).getInt();
        }
        return count;
    }

    private int countInstancesForInverseProperty(String uri, String property) {
        StringBuilder queryString = new StringBuilder();
        Formatter f = new Formatter(queryString);
        Object[] vars = {uri, property};
        f.format(queryForCountInstancesInverseProperty, vars);
        ResultSet results = RhizomerRDF.instance().querySelect(queryString.toString(), MetadataStore.REASONING);
        int count = 0;
        if (results.hasNext())
        {
            QuerySolution row = results.next();
            // The first var is the count value
            String countVar = results.getResultVars().get(0);
            count = row.getLiteral(countVar).getInt();
        }
        return count;
    }

    private int countMaxCardinalityForProperty(String uri, String property){
        StringBuilder queryString = new StringBuilder();
        Formatter f = new Formatter(queryString);
        Object[] vars = {uri, property};
        f.format(queryForMaxCardinality, vars);
        ResultSet results = RhizomerRDF.instance().querySelect(queryString.toString(), MetadataStore.REASONING);
        int count = 0;
        if (results.hasNext())
        {
            QuerySolution row = results.next();
            // The first var is the count value
            String countVar = results.getResultVars().get(0);
            count = row.getLiteral(countVar).getInt();
        }
        return count;
    }

    private int countMaxCardinalityForInverseProperty(String uri, String property) {
        StringBuilder queryString = new StringBuilder();
        Formatter f = new Formatter(queryString);
        Object[] vars = {uri, property};
        f.format(queryForMaxCardinalityForInverseProperty, vars);
        ResultSet results = RhizomerRDF.instance().querySelect(queryString.toString(), MetadataStore.REASONING);
        int count = 0;
        if (results.hasNext())
        {
            QuerySolution row = results.next();
            // The first var is the count value
            String countVar = results.getResultVars().get(0);
            count = row.getLiteral(countVar).getInt();
        }
        return count;
    }

    private boolean isInverseFunctionalForValues(String uri, String property){
        StringBuilder queryString = new StringBuilder();
        Formatter f = new Formatter(queryString);
        Object[] vars = {uri, property};
        f.format(queryIsNotInverseFunctional, vars);
        return !RhizomerRDF.instance().queryAsk(queryString.toString());
    }

    private int countTotalInstances(String uri){
        StringBuilder queryString = new StringBuilder();
        Formatter f = new Formatter(queryString);
        Object[] vars = {uri};
        f.format(queryForCountTotalInstances, vars);
        ResultSet results = RhizomerRDF.instance().querySelect(queryString.toString(), MetadataStore.REASONING);
        int count = 0;
        if (results!=null && results.hasNext())
        {
            QuerySolution row = results.next();
            // The first var is the count value
            String countVar = results.getResultVars().get(0);
            count = row.getLiteral(countVar).getInt();
        }
        return count;
    }

    public HashMap<String,String> getProperties(String uri) {
        StringBuilder queryString = new StringBuilder();
        Formatter f = new Formatter(queryString);
        Object[] vars = {makeTypesFilter(uri)};
        f.format(queryForProperties, vars);
        ResultSet results = RhizomerRDF.instance().querySelect(queryString.toString(), MetadataStore.REASONING);
        HashMap<String, String> properties = new HashMap<String, String>();
        while(results!=null && results.hasNext()){
            QuerySolution row = results.next();
            String property = row.get("p").toString();
            String range;
            if(row.get("r")!=null)
                range = row.get("r").toString();
            else
                range = null;
            properties.put(property, range);
        }
        return properties;
    }

    public HashMap<String,String> getInverseProperties(String uri){
        StringBuilder queryString = new StringBuilder();
        Formatter f = new Formatter(queryString);
        Object[] vars = {makeTypesFilter(uri)};
        f.format(queryForInverseProperties, vars);
        ResultSet results = RhizomerRDF.instance().querySelect(queryString.toString(), MetadataStore.REASONING);
        HashMap<String, String> properties = new HashMap<String, String>();
        while(results!=null && results.hasNext()){
            QuerySolution row = results.next();
            String property = row.get("p").toString();
            String range;
            if(row.get("r")!=null)
                range = row.get("r").toString();
            else
                range = null;
            properties.put(property, range);
        }
        return properties;
    }

    private double calculateEntropy(String uri, String property) throws SQLException{
        StringBuilder queryString = new StringBuilder();
        Formatter f = new Formatter(queryString);
        Object[] vars = {uri, property};
        f.format(queryForEntropy, vars);
        ResultSet results = RhizomerRDF.instance().querySelect(queryString.toString(), MetadataStore.REASONING);
        int total = 0;
        ArrayList<Integer> values = new ArrayList<Integer>();
        int numRows = 0;
        while(results.hasNext()){
            QuerySolution row = results.next();
            // The first var is the count value
            String countVar = results.getResultVars().get(0);
            int count = row.getLiteral(countVar).getInt();
            total += count;
            numRows++;
            values.add(count);
        }
        double entropy = 0;
        for(int i : values){
            double prob = (double)i/(double)total;
            double log = prob*log(prob,numRows);
            entropy += log;
        }
        entropy*=-1;
        if(Double.isNaN(entropy))
            entropy = 0;
        return entropy;
    }

    private int countValues(String uri, String property){
        int count = 0;
        StringBuilder queryString = new StringBuilder();
        Formatter f = new Formatter(queryString);
        Object[] vars = {uri, property};
        f.format(queryForCountValues, vars);
        try
        {
            ResultSet results = RhizomerRDF.instance().querySelect(queryString.toString(), MetadataStore.REASONING);
            QuerySolution row = results.next();
            // The first var is the count value
            String countVar = results.getResultVars().get(0);
            count = row.getLiteral(countVar).getInt();
        }
        catch (Exception e)
        {
            log.log(Level.SEVERE, "Exception in SPARQL query: "+queryString.toString());
        }
        return count;
    }

    private int countInversePropertyValues(String uri, String property) {
        int count = 0;
        StringBuilder queryString = new StringBuilder();
        Formatter f = new Formatter(queryString);
        Object[] vars = {uri, property};
        f.format(queryForCountInversePropertyValues, vars);
        try
        {
            ResultSet results = RhizomerRDF.instance().querySelect(queryString.toString(), MetadataStore.REASONING);
            QuerySolution row = results.next();
            // The first var is the count value
            String countVar = results.getResultVars().get(0);
            count = row.getLiteral(countVar).getInt();
        }
        catch (Exception e)
        {
            log.log(Level.SEVERE, "Exception in SPARQL query: "+queryString.toString());
        }
        return count;
    }

    private double log(double number, double base){
        return Math.log(number)/Math.log(base);
    }

    private void generateFacetsForClass(String classUri) throws SQLException{
        int total = this.countTotalInstances(classUri);

        PreparedStatement st = conn.prepareStatement("INSERT INTO class_summary VALUES(?,?)");
        st.setString(1, classUri);
        st.setInt(2, total);
        try { st.executeUpdate(); }
        catch (Exception e) {log.log(Level.SEVERE, "Error generating facets for class "+classUri+"\n"+e.toString()); }


        st = conn.prepareStatement("INSERT INTO property_summary VALUES(NULL,?,?,?,?,?,?,?,?,?)");
        //PreparedStatement st = conn.prepareStatement("UPDATE property_summary SET num_instances = ? where class = ? and property = ?");

        HashMap<String, String>  properties = this.getProperties(classUri);
        for(String property : properties.keySet())
            try { generateFacet(st, classUri, property, properties.get(property)); }
            catch (Exception e) {log.log(Level.SEVERE, "Error generating facet "+property+" for class"+classUri+"\n"+e.toString()); }

        HashMap<String, String>  invProperties = this.getInverseProperties(classUri);
        for(String property : invProperties.keySet())
            try { generateInverseFacet(st, classUri, property, properties.get(property)); }
            catch (Exception e) {log.log(Level.SEVERE, "Error generating inverse facet "+property+" for class"+classUri+"\n"+e.toString()); }

        st.close();
    }

    private void generateInverseFacet(PreparedStatement st, String classUri, String property, String range) throws Exception {
        //double entropy = calculateEntropy(classUri, property);
        int instances = 0;
        int values = 0;
        int maxValue = 2;
        int maxCardinality = 0;

        System.out.println("COUNT INSTANCES: "+ classUri + " - "+ property);
        instances = this.countInstancesForInverseProperty(classUri, property);
        System.out.println("COUNT VALUES: " + classUri + " - " + property);
        values = this.countInversePropertyValues(classUri, property);

        System.out.println("COUNT MAX CARDINALITY: "+ classUri + " - "+ property);
        maxCardinality = this.countMaxCardinalityForInverseProperty(classUri, property);

        /*if(isInverseFunctionalForValues(classUri, property))
        {
            System.out.println("Inverse functional property for "+ property +" for class "+ classUri);
            maxValue = 1;
        }*/

        boolean isInverse = true;
        insertFacetData(st, classUri, property, range, instances, values, maxValue, maxCardinality, isInverse);
    }

    private void generateFacet(PreparedStatement st, String classUri, String property, String range) throws SQLException {
        //double entropy = calculateEntropy(classUri, property);
        int instances = 0;
        int values = 0;
        int maxValue = 2;
        int maxCardinality = 0;

        System.out.println("COUNT INSTANCES: " + classUri + " - " + property);
        instances = this.countInstancesForProperty(classUri, property);
        System.out.println("COUNT VALUES: " + classUri + " - " + property);
        values = this.countValues(classUri, property);

        System.out.println("COUNT MAX CARDINALITY: " + classUri + " - " + property);
        maxCardinality = this.countMaxCardinalityForProperty(classUri, property);

        /*if(isInverseFunctionalForValues(classUri, property))
        {
            System.out.println("Inverse functional property for "+ property +" for class "+ classUri);
            maxValue = 1;
        }*/

        boolean isInverse = false;
        insertFacetData(st, classUri, property, range, instances, values, maxValue, maxCardinality, isInverse);
    }

    private void insertFacetData(PreparedStatement st, String classUri, String property, String range, int instances, int values, int maxValue, int maxCardinality, boolean inverse) throws SQLException {
        st.setString(1, classUri);
        st.setString(2, property);
        st.setInt(3, instances);
        st.setInt(4, values);
        //st.setDouble(5, entropy);
        st.setInt(5, maxValue); //TODO: make it a boolean
        st.setInt(6, maxCardinality); //TODO: make it a boolean
        String type = null;
        if(range==null){
            type = new TypeDetector(classUri, property, inverse).detectType();
            if(type.equals(rdfs("Resource")))
                range = new TypeDetector(classUri, property, inverse).detectRange();
        }
        st.setString(7, range);
        st.setString(8, type);
        st.setBoolean(9, inverse);

        st.executeUpdate();
    }

    // Generate filter to get properties for class and also all subclasses
    private String makeTypesFilter(String classURI) {
        StringBuilder out=new StringBuilder();
        out.append("?type=<" + classURI + ">");

        for (String subclassURI: getSubClasses(classURI))
            out.append(" || ?type=<"+subclassURI+">");

        return out.toString();
    }

    public void destroy() throws SQLException {
        conn.close();
    }


    private String rdfs(String resource) {
        return null;
    }
    private static class RhizomerRDF {
        public static RhizomerRDF instance() {
            return null;
        }
        public ResultSet querySelect(String queryForClasses, int reasoning) {
            return null;
        }

        public boolean queryAsk(String s) {
            return false;
        }
    }
    private class MetadataStore {
        public static final int REASONING = 1;
    }
}
