package net.rhizomik.rhizomer;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import cucumber.api.java.Before;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import java.io.StringWriter;
import java.net.URI;
import java.net.URL;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.rhizomik.rhizomer.model.Class;
import net.rhizomik.rhizomer.model.Dataset;
import net.rhizomik.rhizomer.model.ExpectedClass;
import net.rhizomik.rhizomer.model.ExpectedFacet;
import net.rhizomik.rhizomer.model.ExpectedRange;
import net.rhizomik.rhizomer.model.ExpectedRangeValue;
import net.rhizomik.rhizomer.model.User;
import net.rhizomik.rhizomer.repository.ClassRepository;
import net.rhizomik.rhizomer.repository.DatasetRepository;
import net.rhizomik.rhizomer.repository.UserRepository;
import net.rhizomik.rhizomer.service.DetailedQueries;
import net.rhizomik.rhizomer.service.SPARQLService;
import net.rhizomik.rhizomer.service.SPARQLServiceMockFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootContextLoader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * Created by http://rhizomik.net/~roberto/
 */
@ContextConfiguration(
    classes = {RhizomerAPIApplication.class, APIStepdefs.SPARQLServiceMockConfig.class},
    loader = SpringBootContextLoader.class
)
@DirtiesContext
@RunWith(SpringRunner.class)
@WebAppConfiguration
public class APIStepdefs {
    private static final Logger logger = LoggerFactory.getLogger(APIStepdefs.class);

    private MockMvc mockMvc;
    private ResultActions result;

    private ObjectMapper mapper = new ObjectMapper();

    @Autowired private WebApplicationContext wac;
    @Autowired private DatasetRepository datasetRepository;
    @Autowired private ClassRepository classRepository;
    @Autowired private SPARQLService sparqlService;
    @Autowired private UserRepository userRepository;

    private static String currentUsername;
    private static String currentPassword;

    static RequestPostProcessor authenticate() {
        return currentUsername!=null ? httpBasic(currentUsername, currentPassword) : anonymous();
    }

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
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
        // Clear authentication credentials at the start of every test.
        currentPassword = "";
        currentUsername = "";
    }

    @Given("^I login as \"([^\"]*)\" with password \"([^\"]*)\"$")
    public void iLoginAsWithPassword(String username, String password) throws Throwable {
        currentUsername = username;
        currentPassword = password;
    }

    @Given("^I'm not logged in$")
    public void iMNotLoggedIn() throws Throwable {
        currentUsername = currentPassword = null;
    }

    @Given("^Exists a user \"([^\"]*)\" with password \"([^\"]*)\"$")
    public void existsAUserWithPassword(String username, String password) throws Throwable {
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(password);
        userRepository.save(newUser);
    }

    @Given("^There is a new dataset by \"([^\"]*)\" with id \"([^\"]*)\"$")
    public void aDatasetWithId(String username, String datasetId) throws Throwable {
        if (datasetRepository.existsById(datasetId))
            datasetRepository.deleteById(datasetId);
        Dataset newDataset = new Dataset(datasetId);
        newDataset.setOwner(userRepository.findById(username).get());
        datasetRepository.save(newDataset);
    }

    @Given("^a class in dataset \"([^\"]*)\" with URI \"([^\"]*)\", label \"([^\"]*)\" and instance count (\\d+)$")
    public void aClassInDatasetWithURILabelAndInstanceCount(String datasetId, String classUriStr, String classLabel, int instanceCount) throws Throwable {
        Dataset dataset = datasetRepository.findById(datasetId).get();
        classRepository.save(new Class(dataset, new URI(classUriStr), classLabel, instanceCount));
    }

    @When("^I create a dataset with id \"([^\"]*)\"$")
    public void iCreateADatasetWithId(String datasetId) throws Throwable {
        Dataset dataset = new Dataset(datasetId);
        String json = mapper.writeValueAsString(dataset);
        this.result = mockMvc.perform(post("/datasets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .accept(MediaType.APPLICATION_JSON)
                .with(authenticate()));
    }

    @When("^I create a public dataset with id \"([^\"]*)\"$")
    public void iCreateAPublicDatasetWithId(String datasetId) throws Throwable {
        Dataset dataset = new Dataset(datasetId);
        dataset.setPublic(true);
        String json = mapper.writeValueAsString(dataset);
        this.result = mockMvc.perform(post("/datasets")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json)
            .accept(MediaType.APPLICATION_JSON)
            .with(authenticate()));
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
                .accept(MediaType.APPLICATION_JSON)
                .with(authenticate()));
    }

    @When("^I create facets for class \"([^\"]*)\" in dataset \"([^\"]*)\"$")
    public void iCreateAFacetForClassInDatasetWith(String classCurie, String datasetId, List<ExpectedFacet> facets) throws Throwable {
        String json = mapper.writeValueAsString(facets.get(0));

        this.result = mockMvc.perform(post("/datasets/{datasetId}/classes/{classCurie}/facets", datasetId, classCurie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .accept(MediaType.APPLICATION_JSON)
                .with(authenticate()));
        this.result.andExpect(status().isCreated());

    }

    @When("^I set the facets for class \"([^\"]*)\" in dataset \"([^\"]*)\" to$")
    public void iSetTheFacetsForClassInDatasetTo(String classCurie, String datasetId,  List<ExpectedFacet> facets) throws Throwable {
        //graphs = graphs.stream().filter(s -> s.length()>0).collect(Collectors.toList());
        String facetsJson = mapper.writeValueAsString(facets);
        this.result = mockMvc.perform(put("/datasets/{datasetId}/classes/{classCurie}/facets", datasetId, classCurie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(facetsJson)
                .accept(MediaType.APPLICATION_JSON)
                .with(authenticate()));
    }

    @And("^I create ranges for facet \"([^\"]*)\" of class \"([^\"]*)\" in dataset \"([^\"]*)\"$")
    public void iCreateRangesForFacetOfClassInDataset(String facetCurie, String classCurie, String datasetId, List<ExpectedRange> ranges) throws Throwable {
        for (ExpectedRange range: ranges) {
            String json = mapper.writeValueAsString(range);
            this.result = mockMvc.perform(post("/datasets/{datasetId}/classes/{classCurie}/facets/{facetCurie}/ranges",
                                    datasetId, classCurie, facetCurie)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json)
                    .accept(MediaType.APPLICATION_JSON)
                    .with(authenticate()));
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
                .accept(MediaType.APPLICATION_JSON)
                .with(authenticate()));
        this.result.andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(datasetId)));
    }

    @And("^exists a class with id \"([^\"]*)\"$")
    public void existsAClassWithId(String classUriStr) throws Throwable {
        this.result = mockMvc.perform(get(new URI(classUriStr))
                .accept(MediaType.APPLICATION_JSON)
                .with(authenticate()));
        this.result.andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(classUriStr)));
    }

    @And("^exists a facet with id \"([^\"]*)\"$")
    public void existsAFacetWithId(String facetUriStr) throws Throwable {
        this.result = mockMvc.perform(get(new URI(facetUriStr))
                .accept(MediaType.APPLICATION_JSON)
                .with(authenticate()));
        this.result.andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(facetUriStr)));
    }

    @And("^There is no dataset with id \"([^\"]*)\"$")
    public void thereIsNoDatasetWithId(String datasetId) throws Throwable {
        this.result = mockMvc.perform(get("/datasets/{datasetId}", datasetId)
                .accept(MediaType.APPLICATION_JSON)
                .with(authenticate()))
                .andExpect(status().isNotFound());
    }

    @And("^There is no class \"([^\"]*)\" in dataset \"([^\"]*)\"$")
    public void thereIsNoClassInDataset(String classCurie, String datasetId) throws Throwable {
        this.result = mockMvc.perform(get("/datasets/{datasetId}/classes/{classCurie}", datasetId, classCurie)
                .accept(MediaType.APPLICATION_JSON)
                .with(authenticate()))
                .andExpect(status().isNotFound());
    }

    @And("^There is no facet \"([^\"]*)\" for class \"([^\"]*)\" in dataset \"([^\"]*)\"$")
    public void thereIsNoFacetForClassInDataset(String facetCurie, String classCurie, String datasetId) throws Throwable {
        this.result = mockMvc.perform(get("/datasets/{datasetId}/classes/{classCurie}/facets/{facetCurie}", datasetId, classCurie, facetCurie)
                .accept(MediaType.APPLICATION_JSON)
                .with(authenticate()))
                .andExpect(status().isNotFound());
    }

    @Given("^The dataset \"([^\"]*)\" has a mock server$")
    public void theDatasetHasAMockServer(String datasetId) throws Throwable {
        Dataset dataset = datasetRepository.findById(datasetId).get();
        SPARQLServiceMockFactory.clearDataset();
        dataset.setSparqlEndPoint(new URL("http://sparql/mock"));
        datasetRepository.save(dataset);
    }

    @And("^The server for dataset \"([^\"]*)\" stores data$")
    public void theDatasetServerStoresData(String datasetId, List<Map<String, String>> graphs) {
        graphs.forEach(
            graph -> {
                Model model = RDFDataMgr.loadModel(graph.get("data"));
                StringWriter out = new StringWriter();
                RDFDataMgr.write(out, model, Lang.JSONLD);
                try {
                    this.result = mockMvc.perform(
                        post("/datasets/{id}/server?graph={graph}", datasetId, graph.get("graph"))
                            .contentType("application/ld+json")
                            .content(out.toString())
                            .with(authenticate()))
                        .andDo(MockMvcResultHandlers.print())
                        .andExpect(status().isOk())
                        .andExpect(content().string(Long.toString(model.size())));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
    }

    @When("^I delete a dataset with id \"([^\"]*)\"$")
    public void iDeleteADatasetWithId(String datasetId) throws Throwable {
        this.result = mockMvc.perform(delete("/datasets/{datasetId}", datasetId)
                .with(authenticate()));
    }

    @And("^The query type for dataset \"([^\"]*)\" is set to \"([^\"]*)\"$")
    public void The_query_type_for_dataset_is(String datasetId, String queryTypeString) throws Throwable {
        existsADatasetWithId(datasetId);
        String datasetJson = this.result.andReturn().getResponse().getContentAsString();
        Dataset dataset = mapper.readValue(datasetJson, Dataset.class);
        dataset.setQueryType(DetailedQueries.QueryType.valueOf(queryTypeString));
        datasetJson = mapper.writeValueAsString(dataset);
        this.result = mockMvc.perform(put("/datasets/{datasetId}", datasetId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(datasetJson)
                .accept(MediaType.APPLICATION_JSON)
                .with(authenticate()));
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
                .accept(MediaType.APPLICATION_JSON)
                .with(authenticate()));
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
                .accept(MediaType.APPLICATION_JSON)
                .with(authenticate()));
        this.result.andExpect(jsonPath("$.sparqlEndPoint", is(sparqlEndPoint.toString())));
    }

    @And("^The dataset \"([^\"]*)\" update server is set to \"([^\"]*)\" with username \"([^\"]*)\" and password \"([^\"]*)\"$")
    public void theDatasetUpdateServerIsSetTo(String datasetId, URL updateEndPoint,
        String username, String password) throws Throwable {
        existsADatasetWithId(datasetId);
        String datasetJson = this.result.andReturn().getResponse().getContentAsString();
        Dataset dataset = mapper.readValue(datasetJson, Dataset.class);
        dataset.setUpdateEndPoint(updateEndPoint);
        dataset.setUsername(username);
        dataset.setPassword(password);
        datasetJson = mapper.writeValueAsString(dataset);
        this.result = mockMvc.perform(put("/datasets/{datasetId}", datasetId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(datasetJson)
            .accept(MediaType.APPLICATION_JSON)
            .with(authenticate()));
        this.result.andExpect(jsonPath("$.updateEndPoint", is(updateEndPoint.toString())));
    }

    @When("^I retrieve the dataset with id \"([^\"]*)\"$")
    public void iRetrieveTheDatasetWithId(String datasetId) throws Throwable {
        this.result = mockMvc.perform(get("/datasets/{datasetId}", datasetId)
            .accept(MediaType.APPLICATION_JSON)
            .with(authenticate()));
    }

    @When("^I list the graphs in dataset \"([^\"]*)\" server$")
    public void iListTheGraphsInDatasetServer(String datasetId) throws Throwable {
        this.result = mockMvc.perform(get("/datasets/{datasetId}/server/graphs", datasetId)
                .accept(MediaType.APPLICATION_JSON)
                .with(authenticate()))
                .andExpect(status().isOk());
    }

    @Then("^The retrieved graphs are$")
    public void theRetrievedGraphsAre(List<URI> expectedGraphs) throws Throwable {
        String json = this.result.andReturn().getResponse().getContentAsString();
        List<URI> actualGraphs = mapper.readValue(json, mapper.getTypeFactory().constructCollectionType(List.class, URI.class));
        assertThat(actualGraphs, containsInAnyOrder(expectedGraphs.toArray()));
    }

    @When("^I add ontologies to the dataset \"([^\"]*)\"$")
    public void iAddTheOntologyToTheDataset(String datasetId, List<String> ontologies) throws Throwable {
        String ontologiesJson = mapper.writeValueAsString(ontologies);
        this.result = mockMvc.perform(post("/datasets/{datasetId}/ontologies", datasetId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(ontologiesJson)
                .accept(MediaType.APPLICATION_JSON)
                .with(authenticate()));
        this.result.andExpect(status().isCreated());
        this.result.andExpect(jsonPath("$", hasItems(ontologies.toArray())));
    }

    @When("^I add the graphs to the dataset \"([^\"]*)\"$")
    public void iAddTheGraphToTheDataset(String datasetId, List<String> graphs) throws Throwable {
        String graphsJson = mapper.writeValueAsString(graphs);
        this.result = mockMvc.perform(post("/datasets/{datasetId}/graphs", datasetId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(graphsJson)
                .accept(MediaType.APPLICATION_JSON)
                .with(authenticate()));
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
                .accept(MediaType.APPLICATION_JSON)
                .with(authenticate()));
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
                .accept(MediaType.APPLICATION_JSON)
                .with(authenticate()));
        this.result.andExpect(status().isOk());
        this.result.andExpect(jsonPath("$", containsInAnyOrder(graphs.toArray())));
    }

    @And("^The following ontologies are defined for the dataset \"([^\"]*)\"$")
    public void theFollowingOntologiesAreDefinedForTheDataset(String datasetId, List<String> ontologies) throws Throwable {
        ontologies = ontologies.stream().filter(s -> s.length()>0).collect(Collectors.toList());
        this.result = mockMvc.perform(get("/datasets/{datasetId}/ontologies", datasetId)
                .accept(MediaType.APPLICATION_JSON)
                .with(authenticate()));
        this.result.andExpect(jsonPath("$", containsInAnyOrder(ontologies.toArray())));
    }

    @And("^The following data graphs are defined for the dataset \"([^\"]*)\"$")
    public void theFollowingDataGraphsAreDefinedForTheDataset(String datasetId, List<String> graphs) throws Throwable {
        graphs = graphs.stream().filter(s -> s.length()>0).collect(Collectors.toList());
        this.result = mockMvc.perform(get("/datasets/{datasetId}/graphs", datasetId)
                .accept(MediaType.APPLICATION_JSON)
                .with(authenticate()));
        this.result.andExpect(jsonPath("$", containsInAnyOrder(graphs.toArray())));
    }

    @And("^The size of dataset \"([^\"]*)\" ontologies graph is (\\d+)$")
    public void theSizeOfDatasetOntologiesGraphIs(String datasetId, long expectedSize) throws Throwable {
        Dataset dataset = datasetRepository.findById(datasetId).get();
        long actualSize = sparqlService.countGraphTriples(dataset.getSparqlEndPoint(), dataset.getDatasetOntologiesGraph().toString());
        assertThat(actualSize, is(expectedSize));
    }

    @And("^The size of dataset \"([^\"]*)\" data graphs is (\\d+)$")
    public void theSizeOfDatasetGraphsIs(String datasetId, long expectedSize) throws Throwable {
        Dataset dataset = datasetRepository.findById(datasetId).get();
        this.result = mockMvc.perform(get("/datasets/{datasetId}/graphs", datasetId)
                .accept(MediaType.APPLICATION_JSON)
                .with(authenticate()));
        String json = this.result.andReturn().getResponse().getContentAsString();
        List<String> datasetGraphs = mapper.readValue(json, mapper.getTypeFactory().constructCollectionType(List.class, String.class));
        long actualSize = datasetGraphs.stream().mapToLong(graph -> sparqlService.countGraphTriples(dataset.getSparqlEndPoint(), graph)).sum();
        assertThat(actualSize, is(expectedSize));
    }

    @When("^I extract the classes from dataset \"([^\"]*)\"$")
    public void I_extract_the_classes_from_dataset(String datasetId) throws Throwable {
        this.result = mockMvc.perform(get("/datasets/{datasetId}/classes", datasetId)
                .accept(MediaType.APPLICATION_JSON)
                .with(authenticate()))
                .andExpect(status().isOk());
    }

    @When("^I extract the top (\\d+) classes from dataset \"([^\"]*)\"$")
    public void iExtractTheTopClassesFromDataset(int top, String datasetId) throws Throwable {
        this.result = mockMvc.perform(get("/datasets/{datasetId}/classes?top={top}", datasetId, top)
            .accept(MediaType.APPLICATION_JSON)
            .with(authenticate()))
            .andExpect(status().isOk());
    }

    @When("^I extract the top (\\d+) classes from dataset \"([^\"]*)\" containing \"([^\"]*)\"$")
    public void iExtractTheTopClassesFromDatasetContaining(int top, String datasetId, String text) throws Throwable {
        this.result = mockMvc.perform(
            get("/datasets/{datasetId}/classes?top={top}&containing={text}", datasetId, top, text)
            .accept(MediaType.APPLICATION_JSON)
            .with(authenticate()))
        .andExpect(status().isOk());
    }

    @When("^I extract the facets for class \"([^\"]*)\" in dataset \"([^\"]*)\"$")
    public void iExtractTheFacetsForClassInDataset(String classCurie, String datasetId) throws Throwable {
        this.result = mockMvc.perform(get("/datasets/{datasetId}/classes/{classCurie}/facets", datasetId, classCurie)
                .accept(MediaType.APPLICATION_JSON)
                .with(authenticate()))
                .andExpect(status().isOk());
    }

    @When("^I retrieve facet range \"([^\"]*)\" values$")
    public void iRetrieveFacetRangesValues(String facetRangeId, List<ExpectedRangeValue> expectedRangeValues) throws Throwable {
        String json = mockMvc.perform(get(facetRangeId + "/values")
                .accept(MediaType.APPLICATION_JSON)
                .with(authenticate()))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        List<ExpectedRangeValue> actualRangeValues = mapper.readValue(json, mapper.getTypeFactory().constructCollectionType(List.class, ExpectedRangeValue.class));
        assertThat(actualRangeValues, containsInAnyOrder(expectedRangeValues.toArray()));
    }

    @When("^I set the dataset \"([^\"]*)\" classes to$")
    public void iClearDatasetClasses(String datasetId, List<Class> newClasses) throws Throwable {
        String json = mapper.writeValueAsString(newClasses);
        this.result = mockMvc.perform(put("/datasets/{datasetId}/classes", datasetId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .accept(MediaType.APPLICATION_JSON)
                .with(authenticate()))
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
