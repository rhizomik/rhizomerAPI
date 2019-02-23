package net.rhizomik.rhizomer.model;

import net.rhizomik.rhizomer.service.Queries;
import net.rhizomik.rhizomer.service.OptimizedQueries;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.util.StringUtils;

/**
 * Created by http://rhizomik.net/~roberto/
 */
public class QueriesTest {
    Queries queries = new OptimizedQueries();

    @Test
    public void testNoAdditional2Samples() throws Exception {
        int classCount = 10, sampleSize = 5; double coverage = 0.5;
        String selectsUnion = queries.addSamples(classCount, sampleSize, coverage);
        Assert.assertThat(StringUtils.countOccurrencesOf(selectsUnion, "\n"), Matchers.is(0));
        Assert.assertThat(selectsUnion, Matchers.is(""));
    }

    @Test
    public void testOneAdditional2Sample() throws Exception {
        int classCount = 10, sampleSize = 2; double coverage = 0.5;
        String selectsUnion = queries.addSamples(classCount, sampleSize, coverage);
        Assert.assertThat(StringUtils.countOccurrencesOf(selectsUnion, "\n"), Matchers.is(1));
        Assert.assertThat(selectsUnion, Matchers.containsString("OFFSET 5 LIMIT 2"));
    }

    @Test
    public void testTwoAdditional2Samples() throws Exception {
        int classCount = 10, sampleSize = 2; double coverage = 0.6;
        String selectsUnion = queries.addSamples(classCount, sampleSize, coverage);
        Assert.assertThat(StringUtils.countOccurrencesOf(selectsUnion, "\n"), Matchers.is(2));
        Assert.assertThat(selectsUnion, Matchers.containsString("OFFSET 4 LIMIT 2"));
        Assert.assertThat(selectsUnion, Matchers.containsString("OFFSET 8 LIMIT 2"));
    }

    @Test
    public void testNoAdditional1Samples() throws Exception {
        int classCount = 10, sampleSize = 1; double coverage = 0.1;
        String selectsUnion = queries.addSamples(classCount, sampleSize, coverage);
        Assert.assertThat(StringUtils.countOccurrencesOf(selectsUnion, "\n"), Matchers.is(0));
        Assert.assertThat(selectsUnion, Matchers.is(""));
    }

    @Test
    public void testOneAdditional1Sample() throws Exception {
        int classCount = 10, sampleSize = 1; double coverage = 0.2;
        String selectsUnion = queries.addSamples(classCount, sampleSize, coverage);
        Assert.assertThat(StringUtils.countOccurrencesOf(selectsUnion, "\n"), Matchers.is(1));
        Assert.assertThat(selectsUnion, Matchers.containsString("OFFSET 5 LIMIT 1"));
    }

    @Test
    public void testThreeAdditional1Samples() throws Exception {
        int classCount = 10, sampleSize = 1; double coverage = 0.4;
        String selectsUnion = queries.addSamples(classCount, sampleSize, coverage);
        Assert.assertThat(StringUtils.countOccurrencesOf(selectsUnion, "\n"), Matchers.is(3));
        Assert.assertThat(selectsUnion, Matchers.containsString("OFFSET 3 LIMIT 1"));
        Assert.assertThat(selectsUnion, Matchers.containsString("OFFSET 6 LIMIT 1"));
        Assert.assertThat(selectsUnion, Matchers.containsString("OFFSET 9 LIMIT 1"));
    }

    @Test
    public void test11Samples() throws Exception {
        int classCount = 10, sampleSize = 11; double coverage = 0.5;
        String selectsUnion = queries.addSamples(classCount, sampleSize, coverage);
        Assert.assertThat(StringUtils.countOccurrencesOf(selectsUnion, "\n"), Matchers.is(0));
        Assert.assertThat(selectsUnion, Matchers.is(""));
    }
}