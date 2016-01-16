package net.rhizomik.rhizomer;

import com.fasterxml.jackson.databind.ObjectMapper;
import cucumber.api.java.Before;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import net.rhizomik.rhizomer.model.Class;
import net.rhizomik.rhizomer.model.*;
import net.rhizomik.rhizomer.repository.ClassRepository;
import net.rhizomik.rhizomer.repository.PondRepository;
import net.rhizomik.rhizomer.service.SPARQLService;
import net.rhizomik.rhizomer.service.SPARQLServiceMockFactory;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationContextLoader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.net.URI;
import java.net.URL;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Created by http://rhizomik.net/~roberto/
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { EyeApplication.class, APIStepdefs.SPARQLServiceMockConfig.class },
        loader = SpringApplicationContextLoader.class)
@WebAppConfiguration
@IntegrationTest
public class APIStepdefs {
    private static final Logger logger = LoggerFactory.getLogger(APIStepdefs.class);

    private MockMvc mockMvc;
    private ResultActions result;

    private ObjectMapper mapper = new ObjectMapper();

    @Autowired private WebApplicationContext wac;
    @Autowired private PondRepository pondRepository;
    @Autowired private ClassRepository classRepository;
    @Autowired private SPARQLService sparqlService;

    @Configuration
    static class SPARQLServiceMockConfig {
        @Bean
        public SPARQLService sparqlService() {
            return SPARQLServiceMockFactory.build();
        }
    }

    @Before
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac)
                //.apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Given("^There is a pond with id \"([^\"]*)\"$")
    public void aPondWithId(String pondId) throws Throwable {
        pondRepository.save(new Pond(pondId));
    }

    @Given("^a class in pond \"([^\"]*)\" with URI \"([^\"]*)\", label \"([^\"]*)\" and instance count (\\d+)$")
    public void aClassInPondWithURILabelAndInstanceCount(String pondId, String classUriStr, String classLabel, int instanceCount) throws Throwable {
        Pond pond = pondRepository.findOne(pondId);
        classRepository.save(new Class(pond, new URI(classUriStr), classLabel, instanceCount));
    }

    @When("^I create a pond with id \"([^\"]*)\"$")
    public void aManagerCreatesAPondWithId(String pondId) throws Throwable {
        Pond pond = new Pond(pondId);
        String json = mapper.writeValueAsString(pond);
        this.result = mockMvc.perform(post("/ponds")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .accept(MediaType.APPLICATION_JSON));
    }

    @When("^I create a class in pond \"([^\"]*)\" with URI \"([^\"]*)\", label \"([^\"]*)\" and instance count (\\d+)$")
    public void iCreateAClassWithURILabelAndInstanceCount(String pondId, String classUriStr, String classLabel, int instanceCount) throws Throwable {
        String json = MessageFormat.format("'{' " +
                "\"uri\": \"{0}\", " +
                "\"label\": \"{1}\", " +
                "\"instanceCount\": {2,number,integer} '}'", classUriStr, classLabel, instanceCount);
        this.result = mockMvc.perform(post("/ponds/{pondId}/classes", pondId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .accept(MediaType.APPLICATION_JSON));
    }

    @When("^I create facets for class \"([^\"]*)\" in pond \"([^\"]*)\"$")
    public void iCreateAFacetForClassInPondWith(String classCurie, String pondId, List<ExpectedFacet> facets) throws Throwable {
        String json = mapper.writeValueAsString(facets.get(0));

        this.result = mockMvc.perform(post("/ponds/{pondId}/classes/{classCurie}/facets", pondId, classCurie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .accept(MediaType.APPLICATION_JSON));
    }

    @Then("^the response status is (\\d+)$")
    public void theResponseStatusIs(int status) throws Throwable {
        this.result.andExpect(status().is(status));
    }

    @Then("^the response status is (\\d+) and message contains \"([^\"]*)\"$")
    public void theResponseStatusIsAndMessageContains(int status, String message) throws Throwable {
        this.result.andExpect(status().is(status))
                .andExpect(jsonPath("$.message", containsString(message)));
    }

    @And("^exists a pond with id \"([^\"]*)\"$")
    public void existsAPondWithId(String pondId) throws Throwable {
        this.result = mockMvc.perform(get("/ponds/{pondId}", pondId)
                .accept(MediaType.APPLICATION_JSON));
        this.result.andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(pondId)));
    }

    @And("^exists a class with id \"([^\"]*)\"$")
    public void existsAClassWithId(String classUriStr) throws Throwable {
        this.result = mockMvc.perform(get(new URI(classUriStr))
                .accept(MediaType.APPLICATION_JSON));
        this.result.andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(classUriStr)));
    }

    @And("^exists a facet with id \"([^\"]*)\"$")
    public void existsAFacetWithId(String facetUriStr) throws Throwable {
        this.result = mockMvc.perform(get(new URI(facetUriStr))
                .accept(MediaType.APPLICATION_JSON));
        this.result.andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(facetUriStr)));
    }

    @And("^There is no pond with id \"([^\"]*)\"$")
    public void thereIsNoPondWithId(String pondId) throws Throwable {
        this.result = mockMvc.perform(get("/ponds/{pondId}", pondId)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @And("^There is no class \"([^\"]*)\" in pond \"([^\"]*)\"$")
    public void thereIsNoClassInPond(String classCurie, String pondId) throws Throwable {
        this.result = mockMvc.perform(get("/ponds/{pondId}/classes/{classCurie}", pondId, classCurie)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @And("^There is no facet \"([^\"]*)\" for class \"([^\"]*)\" in pond \"([^\"]*)\"$")
    public void thereIsNoFacetForClassInPond(String facetCurie, String classCurie, String pondId) throws Throwable {
        this.result = mockMvc.perform(get("/ponds/{pondId}/classes/{classCurie}/facets/{facetCurie}", pondId, classCurie, facetCurie)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Given("^The pond \"([^\"]*)\" has a mock server$")
    public void thePondHasAMockServer(String pondId) throws Throwable {
        Pond pond = pondRepository.findOne(pondId);
        pond.setSparqlEndPoint(new URL("http://sparql/mock"));
        pondRepository.save(pond);
    }

    @And("^The pond \"([^\"]*)\" server stores data$")
    public void thePondServerStoresData(String pondId, List<Map<String, String>> datasets) throws Throwable {
        Pond pond = pondRepository.findOne(pondId);
        datasets.stream().forEach(dataset -> sparqlService.loadData(pond.getSparqlEndPoint(), dataset.get("graph"), dataset.get("data")));
    }

    @When("^I delete a pond with id \"([^\"]*)\"$")
    public void iDeleteAPondWithId(String pondId) throws Throwable {
        this.result = mockMvc.perform(delete("/ponds/{pondId}", pondId));
    }

    @And("^The query type for pond \"([^\"]*)\" is set to \"([^\"]*)\"$")
    public void The_query_type_for_pond_is(String pondId, String queryTypeString) throws Throwable {
        existsAPondWithId(pondId);
        String pondJson = this.result.andReturn().getResponse().getContentAsString();
        Pond pond = mapper.readValue(pondJson, Pond.class);
        pond.setQueryType(Queries.QueryType.valueOf(queryTypeString));
        pondJson = mapper.writeValueAsString(pond);
        this.result = mockMvc.perform(put("/ponds/{pondId}", pondId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(pondJson)
                .accept(MediaType.APPLICATION_JSON));
        this.result.andExpect(jsonPath("$.queryType", is(queryTypeString)));
    }

    @And("^The inference for pond \"([^\"]*)\" is set to \"([^\"]*)\"$")
    public void theInferenceForPondIsSetTo(String pondId, boolean inference) throws Throwable {
        existsAPondWithId(pondId);
        String pondJson = this.result.andReturn().getResponse().getContentAsString();
        Pond pond = mapper.readValue(pondJson, Pond.class);
        pond.setInferenceEnabled(inference);
        pondJson = mapper.writeValueAsString(pond);
        this.result = mockMvc.perform(put("/ponds/{pondId}", pondId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(pondJson)
                .accept(MediaType.APPLICATION_JSON));
        this.result.andExpect(jsonPath("$.inferenceEnabled", is(inference)));
    }

    @When("^I add ontologies to the pond \"([^\"]*)\"$")
    public void iAddTheOntologyToThePond(String pondId, List<String> ontologies) throws Throwable {
        String ontologiesJson = mapper.writeValueAsString(ontologies);
        this.result = mockMvc.perform(post("/ponds/{pondId}/ontologies", pondId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(ontologiesJson)
                .accept(MediaType.APPLICATION_JSON));
        this.result.andExpect(status().isCreated());
        this.result.andExpect(jsonPath("$", hasItems(ontologies.toArray())));
    }

    @When("^I add the graphs to the pond \"([^\"]*)\"$")
    public void iAddTheGraphToThePond(String pondId, List<String> graphs) throws Throwable {
        String graphsJson = mapper.writeValueAsString(graphs);
        this.result = mockMvc.perform(post("/ponds/{pondId}/graphs", pondId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(graphsJson)
                .accept(MediaType.APPLICATION_JSON));
        this.result.andExpect(status().isCreated());
        this.result.andExpect(jsonPath("$", hasItems(graphs.toArray())));
    }

    @And("^The following ontologies are set for pond \"([^\"]*)\"$")
    public void theFollowingOntologiesAreSetForPond(String pondId, List<String> ontologies) throws Throwable {
        ontologies = ontologies.stream().filter(s -> s.length()>0).collect(Collectors.toList());
        String ontologiesJson = mapper.writeValueAsString(ontologies);
        this.result = mockMvc.perform(put("/ponds/{pondId}/ontologies", pondId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(ontologiesJson)
                .accept(MediaType.APPLICATION_JSON));
        this.result.andExpect(status().isOk());
        this.result.andExpect(jsonPath("$", containsInAnyOrder(ontologies.toArray())));
    }

    @When("^The following data graphs are set for pond \"([^\"]*)\"$")
    public void theFollowingDataGraphsAreSetForPond(String pondId, List<String> graphs) throws Throwable {
        graphs = graphs.stream().filter(s -> s.length()>0).collect(Collectors.toList());
        String graphsJson = mapper.writeValueAsString(graphs);
        this.result = mockMvc.perform(put("/ponds/{pondId}/graphs", pondId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(graphsJson)
                .accept(MediaType.APPLICATION_JSON));
        this.result.andExpect(status().isOk());
        this.result.andExpect(jsonPath("$", containsInAnyOrder(graphs.toArray())));
    }

    @And("^The following ontologies are defined for the pond \"([^\"]*)\"$")
    public void theFollowingOntologiesAreDefinedForThePond(String pondId, List<String> ontologies) throws Throwable {
        ontologies = ontologies.stream().filter(s -> s.length()>0).collect(Collectors.toList());
        this.result = mockMvc.perform(get("/ponds/{pondId}/ontologies", pondId)
                .accept(MediaType.APPLICATION_JSON));
        this.result.andExpect(jsonPath("$", containsInAnyOrder(ontologies.toArray())));
    }

    @And("^The following data graphs are defined for the pond \"([^\"]*)\"$")
    public void theFollowingDataGraphsAreDefinedForThePond(String pondId, List<String> graphs) throws Throwable {
        graphs = graphs.stream().filter(s -> s.length()>0).collect(Collectors.toList());
        this.result = mockMvc.perform(get("/ponds/{pondId}/graphs", pondId)
                .accept(MediaType.APPLICATION_JSON));
        this.result.andExpect(jsonPath("$", containsInAnyOrder(graphs.toArray())));
    }

    @And("^The size of pond \"([^\"]*)\" ontologies graph is (\\d+)$")
    public void theSizeOfPondOntologiesGraphIs(String pondId, int expectedSize) throws Throwable {
        Pond pond = pondRepository.findOne(pondId);
        int actualSize = sparqlService.countGraphTriples(pond.getSparqlEndPoint(), pond.getPondOntologiesGraph().toString());
        assertThat(actualSize, is(expectedSize));
    }

    @And("^The size of pond \"([^\"]*)\" data graphs is (\\d+)$")
    public void theSizeOfPondGraphsIs(String pondId, int expectedSize) throws Throwable {
        Pond pond = pondRepository.findOne(pondId);
        this.result = mockMvc.perform(get("/ponds/{pondId}/graphs", pondId)
                .accept(MediaType.APPLICATION_JSON));
        String json = this.result.andReturn().getResponse().getContentAsString();
        List<String> pondGraphs = mapper.readValue(json, mapper.getTypeFactory().constructCollectionType(List.class, String.class));
        int actualSize = pondGraphs.stream().mapToInt(graph -> sparqlService.countGraphTriples(pond.getSparqlEndPoint(), graph)).sum();
        assertThat(actualSize, is(expectedSize));
    }

    @When("^I extract the classes from pond \"([^\"]*)\"$")
    public void I_extract_the_classes_from_pond(String pondId) throws Throwable {
        this.result = mockMvc.perform(get("/ponds/{pondId}/classes", pondId)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @When("^I extract the facets for class \"([^\"]*)\" in pond \"([^\"]*)\"$")
    public void iExtractTheFacetsForClassInPond(String classCurie, String pondId) throws Throwable {
        this.result = mockMvc.perform(get("/ponds/{pondId}/classes/{classCurie}/facets", pondId, classCurie)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Then("^The retrieved classes are$")
    public void theRetrievedClassesAre(List<ExpectedClass> expectedClasses) throws Throwable {
        String json = this.result.andReturn().getResponse().getContentAsString();
        List<ExpectedClass> actualClasses = mapper.readValue(json, mapper.getTypeFactory().constructCollectionType(List.class, ExpectedClass.class));
        assertThat(actualClasses, containsInAnyOrder(expectedClasses.toArray()));
    }

    @Then("^The retrieved class is$")
    public void theRetrievedClassIs(List<ExpectedClass> expectedClasses) throws Throwable {
        String json = this.result.andReturn().getResponse().getContentAsString();
        ExpectedClass actualClass = mapper.readValue(json, ExpectedClass.class);
        assertThat(actualClass, is(expectedClasses.get(0)));
    }

    @Then("^The retrieved facets are$")
    public void theRetrievedFacetsAre(List<ExpectedFacet> expectedFacets) throws Throwable {
        String json = this.result.andReturn().getResponse().getContentAsString();
        List<ExpectedFacet> actualFacets = mapper.readValue(json, mapper.getTypeFactory().constructCollectionType(List.class, ExpectedFacet.class));
        assertThat(actualFacets, containsInAnyOrder(expectedFacets.toArray()));
    }

    @Then("^The retrieved facet is")
    public void theRetrievedFacetIs(List<ExpectedFacet> expectedFacets) throws Throwable {
        String json = this.result.andReturn().getResponse().getContentAsString();
        ExpectedFacet actualFacet = mapper.readValue(json, ExpectedFacet.class);
        assertThat(actualFacet, is(expectedFacets.get(0)));
    }
}
