package net.rhizomik.rhizomer.service;

import org.apache.jena.atlas.lib.Pair;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by http://rhizomik.net/~roberto/
 */
public class PrefixCCMapTest {

    PrefixCCMap prefixcc;

    @Before
    public void setUp() throws Exception {
        prefixcc = new PrefixCCMap();
    }

    @Test
    public void testGenerateNewPrefix2Components() throws Exception {
        String uri = "http://testing.org/test";
        String curie = prefixcc.generatePrefix(uri);
        assertEquals("testing", curie);
    }

    @Test
    public void testGenerateNewPrefix1Component() throws Exception {
        String uri = "http://localhost:8080/test";
        String curie = prefixcc.generatePrefix(uri);
        assertEquals("localhost", curie);
    }

    @Test
    public void testAbbrev() throws Exception {
        String uri = "http://testing.org/test";
        Pair<String,String> curiePair = prefixcc.abbrev(uri);
        assertEquals(new Pair<>("testing", "test"), curiePair);
    }

    @Test
    public void testAbbreviateSlash() throws Exception {
        String uri = "http://testing.org/test";
        String curie = prefixcc.abbreviate(uri);
        assertEquals("testing:test", curie);
    }

    @Test
    public void testAbbreviateAnchor() throws Exception {
        String uri = "http://testing.org#test";
        String curie = prefixcc.abbreviate(uri);
        assertEquals("testing:test", curie);
    }

    @Test
    public void testAbbreviateRepeatingPrefix() throws Exception {
        String uri1 = "http://localhost:8080/test";
        String curie1 = prefixcc.abbreviate(uri1);
        assertEquals("localhost:test", curie1);

        String uri2 = "http://localhost/test";
        String curie2 = prefixcc.abbreviate(uri2);
        assertEquals("localhost_1:test", curie2);

        String uri3 = "http://localhost.org/test";
        String curie3 = prefixcc.abbreviate(uri3);
        assertEquals("localhost_2:test", curie3);
    }

    @Test
    public void testAbbreviateExistingPrefix() throws Exception {
        prefixcc.add("test", "http://testing.org#");
        String uri = "http://testing.org#name";
        String curie = prefixcc.abbreviate(uri);
        assertEquals("test:name", curie);
    }

    @Test
    public void testAbbreviateUnexistingPrefix() throws Exception {
        String uri = "http://testing.org#name";
        String curie = prefixcc.abbreviate(uri);
        assertEquals("testing:name", curie);
    }

    @Test
    public void testAbbreviateWithoutLocalnameSlash() throws Exception {
        String uri = "http://testing.org/";
        String curie = prefixcc.abbreviate(uri);
        assertEquals("testing:", curie);
    }

//    @Test
//    public void testAbbreviateWithoutLocalnameNoSlash() throws Exception {
//        String uri = "http://testing.org";
//        String curie = prefixcc.abbreviate(uri);
//        assertEquals("testing:", curie);
//    }

    @Test
    public void testAbbreviateWithoutLocalnameAnchor() throws Exception {
        String uri = "http://testing.org#";
        String curie = prefixcc.abbreviate(uri);
        assertEquals("testing:", curie);
    }

    @Test
    public void testPrefixccNamespaceLookupExisting() throws Exception {
        assertEquals("http://xmlns.com/foaf/0.1/", prefixcc.prefixCCNamespaceLookup("foaf"));
    }

    @Test
    public void testPrefixccNamespaceLookupUnexisting() throws Exception {
        assertEquals(null, prefixcc.prefixCCNamespaceLookup("nonexisting"));
    }

    @Test
    public void testPrefixccReverseLookupExisting() throws Exception {
        assertEquals("foaf", prefixcc.prefixCCReverseLookup("http://xmlns.com/foaf/0.1/"));
    }

    @Test
    public void testPrefixccReverseLookupUnexisting() throws Exception {
        assertEquals(null, prefixcc.prefixCCReverseLookup("http://whatever.org/nonexisting"));
    }

    @Test
    public void testAbbreviatePrefixImportedFromPrefixCC() throws Exception {
        String uri = "http://xmlns.com/foaf/0.1/name";
        String curie = prefixcc.abbreviate(uri);
        assertEquals("foaf:name", curie);
    }

    @Test
    public void testExpandPrefixImportedFromPrefixCC() throws Exception {
        String curie = "foaf:Agent";
        assertEquals("http://xmlns.com/foaf/0.1/Agent", prefixcc.expand(curie));
    }
}