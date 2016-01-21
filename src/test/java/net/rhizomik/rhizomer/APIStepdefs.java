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
import net.rhizomik.rhizomer.repository.DatasetRepository;
import net.rhizomik.rhizomer.repository.FacetRepository;
import net.rhizomik.rhizomer.repository.RangeRepository;
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
import org.springframework.context.annotation.Profile;
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
    @Autowired private DatasetRepository datasetRepository;
    @Autowired private ClassRepository classRepository;
    @Autowired private FacetRepository facetRepository;
    @Autowired private RangeRepository rangeRepository;
    @Autowired private SPARQLService sparqlService;

    @Configuration
    @Profile("LOCAL_SERVER_TESTING")
    static class SPARQLServiceMockConfig {
        @Bean
        public SPARQLService sparqlService() {
            return SPARQLServiceMockFactory.build();
        }
    }

    @Configuration
    @Profile("REMOTE_SERVER_TESTING")
    static class SPARQLServiceConfig {
        @Bean
        public SPARQLService sparqlService() {
            return new SPARQLService();
        }
    }

    @Before
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac)
                //.apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Given("^There is a dataset with id \"([^\"]*)\"$")
    public void aDatasetWithId(String datasetId) throws Throwable {
        datasetRepository.save(new Dataset(datasetId));
    }

    @Given("^a class in dataset \"([^\"]*)\" with URI \"([^\"]*)\", label \"([^\"]*)\" and instance count (\\d+)$")
    public void aClassInDatasetWithURILabelAndInstanceCount(String datasetId, String classUriStr, String classLabel, int instanceCount) throws Throwable {
        Dataset dataset = datasetRepository.findOne(datasetId);
        classRepository.save(new Class(dataset, new URI(classUriStr), classLabel, instanceCount));
    }

    @When("^I create a dataset with id \"([^\"]*)\"$")
    public void aManagerCreatesADatasetWithId(String datasetId) throws Throwable {
        Dataset dataset = new Dataset(datasetId);
        String json = mapper.writeValueAsString(dataset);
        this.result = mockMvc.perform(post("/datasets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .accept(MediaType.APPLICATION_JSON));
    }

    @When("^I create a class in dataset \"([^\"]*)\" with URI \"([^\"]*)\", label \"([^\"]*)\" and instance count (\\d+)$")
    public void iCreateAClassWithURILabelAndInstanceCount(String datasetId, String classUriStr, String classLabel, int instanceCount) throws Throwable {
        String json = MessageFormat.format("'{' " +
                "\"uri\": \"{0}\", " +
                "\"label\": \"{1}\", " +
                "\"instanceCount\": {2,number,integer} '}'", classUriStr, classLabel, instanceCount);
        this.result = mockMvc.perform(post("/datasets/{datasetId}/classes", datasetId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .accept(MediaType.APPLICATION_JSON));
    }

    @When("^I create facets for class \"([^\"]*)\" in dataset \"([^\"]*)\"$")
    public void iCreateAFacetForClassInDatasetWith(String classCurie, String datasetId, List<ExpectedFacet> facets) throws Throwable {
        String json = mapper.writeValueAsString(facets.get(0));

        this.result = mockMvc.perform(post("/datasets/{datasetId}/classes/{classCurie}/facets", datasetId, classCurie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .accept(MediaType.APPLICATION_JSON));
    }

    @And("^I create ranges for facet \"([^\"]*)\" of class \"([^\"]*)\" in dataset \"([^\"]*)\"$")
    public void iCreateRangesForFacetOfClassInDataset(String facetCurie, String classCurie, String datasetId, List<Range> ranges) throws Throwable {
        for (Range range: ranges) {
            String json = mapper.writeValueAsString(range);
            this.result = mockMvc.perform(post("/datasets/{datasetId}/classes/{classCurie}/facets/{facetCurie}/ranges",
                                            datasetId, classCurie, facetCurie)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json)
                    .accept(MediaType.APPLICATION_JSON));
            this.result.andExpect(status().isCreated());
        }
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

    @And("^exists a dataset with id \"([^\"]*)\"$")
    public void existsADatasetWithId(String datasetId) throws Throwable {
        this.result = mockMvc.perform(get("/datasets/{datasetId}", datasetId)
                .accept(MediaType.APPLICATION_JSON));
        this.result.andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(datasetId)));
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

    @And("^There is no dataset with id \"([^\"]*)\"$")
    public void thereIsNoDatasetWithId(String datasetId) throws Throwable {
        this.result = mockMvc.perform(get("/datasets/{datasetId}", datasetId)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @And("^There is no class \"([^\"]*)\" in dataset \"([^\"]*)\"$")
    public void thereIsNoClassInDataset(String classCurie, String datasetId) throws Throwable {
        this.result = mockMvc.perform(get("/datasets/{datasetId}/classes/{classCurie}", datasetId, classCurie)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @And("^There is no facet \"([^\"]*)\" for class \"([^\"]*)\" in dataset \"([^\"]*)\"$")
    public void thereIsNoFacetForClassInDataset(String facetCurie, String classCurie, String datasetId) throws Throwable {
        this.result = mockMvc.perform(get("/datasets/{datasetId}/classes/{classCurie}/facets/{facetCurie}", datasetId, classCurie, facetCurie)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Given("^The dataset \"([^\"]*)\" has a mock server$")
    public void theDatasetHasAMockServer(String datasetId) throws Throwable {
        Dataset dataset = datasetRepository.findOne(datasetId);
        dataset.setSparqlEndPoint(new URL("http://sparql/mock"));
        datasetRepository.save(dataset);
    }

    @And("^The dataset \"([^\"]*)\" server stores data$")
    public void theDatasetServerStoresData(String datasetId, List<Map<String, String>> graphs) throws Throwable {
        Dataset dataset = datasetRepository.findOne(datasetId);
        graphs.stream().forEach(graph -> sparqlService.loadData(dataset.getSparqlEndPoint(), graph.get("graph"), graph.get("data")));
    }

    @When("^I delete a dataset with id \"([^\"]*)\"$")
    public void iDeleteADatasetWithId(String datasetId) throws Throwable {
        this.result = mockMvc.perform(delete("/datasets/{datasetId}", datasetId));
    }

    @And("^The query type for dataset \"([^\"]*)\" is set to \"([^\"]*)\"$")
    public void The_query_type_for_dataset_is(String datasetId, String queryTypeString) throws Throwable {
        existsADatasetWithId(datasetId);
        String datasetJson = this.result.andReturn().getResponse().getContentAsString();
        Dataset dataset = mapper.readValue(datasetJson, Dataset.class);
        dataset.setQueryType(Queries.QueryType.valueOf(queryTypeString));
        datasetJson = mapper.writeValueAsString(dataset);
        this.result = mockMvc.perform(put("/datasets/{datasetId}", datasetId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(datasetJson)
                .accept(MediaType.APPLICATION_JSON));
        this.result.andExpect(jsonPath("$.queryType", is(queryTypeString)));
    }

    @And("^The inference for dataset \"([^\"]*)\" is set to \"([^\"]*)\"$")
    public void theInferenceForDatasetIsSetTo(String datasetId, boolean inference) throws Throwable {
        existsADatasetWithId(datasetId);
        String datasetJson = this.result.andReturn().getResponse().getContentAsString();
        Dataset dataset = mapper.readValue(datasetJson, Dataset.class);
        dataset.setInferenceEnabled(inference);
        datasetJson = mapper.writeValueAsString(dataset);
        this.result = mockMvc.perform(put("/datasets/{datasetId}", datasetId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(datasetJson)
                .accept(MediaType.APPLICATION_JSON));
        this.result.andExpect(jsonPath("$.inferenceEnabled", is(inference)));
    }

    @And("^The dataset \"([^\"]*)\" server is set to \"([^\"]*)\"$")
    public void theDatasetServerIsSetTo(String datasetId, URL sparqlEndPoint) throws Throwable {
        existsADatasetWithId(datasetId);
        String datasetJson = this.result.andReturn().getResponse().getContentAsString();
        Dataset dataset = mapper.readValue(datasetJson, Dataset.class);
        dataset.setSparqlEndPoint(sparqlEndPoint);
        datasetJson = mapper.writeValueAsString(dataset);
        this.result = mockMvc.perform(put("/datasets/{datasetId}", datasetId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(datasetJson)
                .accept(MediaType.APPLICATION_JSON));
        this.result.andExpect(jsonPath("$.sparqlEndPoint", is(sparqlEndPoint.toString())));
    }

    @When("^I add ontologies to the dataset \"([^\"]*)\"$")
    public void iAddTheOntologyToTheDataset(String datasetId, List<String> ontologies) throws Throwable {
        String ontologiesJson = mapper.writeValueAsString(ontologies);
        this.result = mockMvc.perform(post("/datasets/{datasetId}/ontologies", datasetId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(ontologiesJson)
                .accept(MediaType.APPLICATION_JSON));
        this.result.andExpect(status().isCreated());
        this.result.andExpect(jsonPath("$", hasItems(ontologies.toArray())));
    }

    @When("^I add the graphs to the dataset \"([^\"]*)\"$")
    public void iAddTheGraphToTheDataset(String datasetId, List<String> graphs) throws Throwable {
        String graphsJson = mapper.writeValueAsString(graphs);
        this.result = mockMvc.perform(post("/datasets/{datasetId}/graphs", datasetId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(graphsJson)
                .accept(MediaType.APPLICATION_JSON));
        this.result.andExpect(status().isCreated());
        this.result.andExpect(jsonPath("$", hasItems(graphs.toArray())));
    }

    @And("^The following ontologies are set for dataset \"([^\"]*)\"$")
    public void theFollowingOntologiesAreSetForDataset(String datasetId, List<String> ontologies) throws Throwable {
        ontologies = ontologies.stream().filter(s -> s.length()>0).collect(Collectors.toList());
        String ontologiesJson = mapper.writeValueAsString(ontologies);
        this.result = mockMvc.perform(put("/datasets/{datasetId}/ontologies", datasetId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(ontologiesJson)
                .accept(MediaType.APPLICATION_JSON));
        this.result.andExpect(status().isOk());
        this.result.andExpect(jsonPath("$", containsInAnyOrder(ontologies.toArray())));
    }

    @When("^The following data graphs are set for dataset \"([^\"]*)\"$")
    public void theFollowingDataGraphsAreSetForDataset(String datasetId, List<String> graphs) throws Throwable {
        graphs = graphs.stream().filter(s -> s.length()>0).collect(Collectors.toList());
        String graphsJson = mapper.writeValueAsString(graphs);
        this.result = mockMvc.perform(put("/datasets/{datasetId}/graphs", datasetId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(graphsJson)
                .accept(MediaType.APPLICATION_JSON));
        this.result.andExpect(status().isOk());
        this.result.andExpect(jsonPath("$", containsInAnyOrder(graphs.toArray())));
    }

    @And("^The following ontologies are defined for the dataset \"([^\"]*)\"$")
    public void theFollowingOntologiesAreDefinedForTheDataset(String datasetId, List<String> ontologies) throws Throwable {
        ontologies = ontologies.stream().filter(s -> s.length()>0).collect(Collectors.toList());
        this.result = mockMvc.perform(get("/datasets/{datasetId}/ontologies", datasetId)
                .accept(MediaType.APPLICATION_JSON));
        this.result.andExpect(jsonPath("$", containsInAnyOrder(ontologies.toArray())));
    }

    @And("^The following data graphs are defined for the dataset \"([^\"]*)\"$")
    public void theFollowingDataGraphsAreDefinedForTheDataset(String datasetId, List<String> graphs) throws Throwable {
        graphs = graphs.stream().filter(s -> s.length()>0).collect(Collectors.toList());
        this.result = mockMvc.perform(get("/datasets/{datasetId}/graphs", datasetId)
                .accept(MediaType.APPLICATION_JSON));
        this.result.andExpect(jsonPath("$", containsInAnyOrder(graphs.toArray())));
    }

    @And("^The size of dataset \"([^\"]*)\" ontologies graph is (\\d+)$")
    public void theSizeOfDatasetOntologiesGraphIs(String datasetId, int expectedSize) throws Throwable {
        Dataset dataset = datasetRepository.findOne(datasetId);
        int actualSize = sparqlService.countGraphTriples(dataset.getSparqlEndPoint(), dataset.getDatasetOntologiesGraph().toString());
        assertThat(actualSize, is(expectedSize));
    }

    @And("^The size of dataset \"([^\"]*)\" data graphs is (\\d+)$")
    public void theSizeOfDatasetGraphsIs(String datasetId, int expectedSize) throws Throwable {
        Dataset dataset = datasetRepository.findOne(datasetId);
        this.result = mockMvc.perform(get("/datasets/{datasetId}/graphs", datasetId)
                .accept(MediaType.APPLICATION_JSON));
        String json = this.result.andReturn().getResponse().getContentAsString();
        List<String> datasetGraphs = mapper.readValue(json, mapper.getTypeFactory().constructCollectionType(List.class, String.class));
        int actualSize = datasetGraphs.stream().mapToInt(graph -> sparqlService.countGraphTriples(dataset.getSparqlEndPoint(), graph)).sum();
        assertThat(actualSize, is(expectedSize));
    }

    @When("^I extract the classes from dataset \"([^\"]*)\"$")
    public void I_extract_the_classes_from_dataset(String datasetId) throws Throwable {
        this.result = mockMvc.perform(get("/datasets/{datasetId}/classes", datasetId)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @When("^I extract the facets for class \"([^\"]*)\" in dataset \"([^\"]*)\"$")
    public void iExtractTheFacetsForClassInDataset(String classCurie, String datasetId) throws Throwable {
        this.result = mockMvc.perform(get("/datasets/{datasetId}/classes/{classCurie}/facets", datasetId, classCurie)
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
