package net.rhizomik.rhizomer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.DataTableType;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.CucumberContextConfiguration;
import net.rhizomik.rhizomer.model.Class;
import net.rhizomik.rhizomer.model.*;
import net.rhizomik.rhizomer.model.ExpectedRelationship;
import net.rhizomik.rhizomer.repository.ClassRepository;
import net.rhizomik.rhizomer.repository.DatasetRepository;
import net.rhizomik.rhizomer.repository.SPARQLEndPointRepository;
import net.rhizomik.rhizomer.repository.UserRepository;
import net.rhizomik.rhizomer.service.AnalizeDataset;
import net.rhizomik.rhizomer.service.DetailedQueries;
import net.rhizomik.rhizomer.service.SPARQLService;
import net.rhizomik.rhizomer.service.SPARQLServiceMockFactory;
import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.json.JSONObject;
import org.junit.Assert;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.transaction.Transactional;
import java.io.StringWriter;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Created by http://rhizomik.net/~roberto/
 */
@CucumberContextConfiguration
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
    private Integer endPointId;

    @Autowired private WebApplicationContext wac;
    @Autowired private DatasetRepository datasetRepository;
    @Autowired private SPARQLEndPointRepository endPointRepository;
    @Autowired private ClassRepository classRepository;
    @Autowired private AnalizeDataset analizeDataset;
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

    @DataTableType
    public ExpectedClass expectedClassEntry(Map<String, String> entry) {
        return new ExpectedClass(entry.get("id"), entry.get("uri"), entry.get("labels"),
                entry.get("curie"), Integer.parseInt(entry.get("instanceCount")));
    }

    @DataTableType
    public ExpectedFacet expectedFacetEntry(Map<String, String> entry) {
        return new ExpectedFacet(entry.get("uri"), entry.get("labels"),
                Integer.parseInt(entry.getOrDefault("timesUsed", "0")),
                Integer.parseInt(entry.getOrDefault("differentValues", "0")),
                Boolean.parseBoolean(entry.get("relation")), entry.get("range"), entry.get("domainURI"),
                Boolean.parseBoolean(entry.get("allBlank")));
    }

    @DataTableType
    public ExpectedRange expectedRangeEntry(Map<String, String> entry) {
        return new ExpectedRange(entry.get("uri"), entry.get("label"), entry.get("curie"),
                Integer.parseInt(entry.getOrDefault("timesUsed", "0")),
                Integer.parseInt(entry.getOrDefault("differentValues", "0")),
                Boolean.parseBoolean(entry.get("relation")), Boolean.parseBoolean(entry.get("allBlank")));
    }

    @DataTableType
    public ExpectedRangeValue expectedRangeValueEntry(Map<String, String> entry) {
        return new ExpectedRangeValue(entry.get("value"), entry.get("curie"), entry.get("uri"), entry.get("labels"),
                Integer.parseInt(entry.getOrDefault("count", "0")));
    }

    @DataTableType
    public ExpectedRelationship expectedRelationship(Map<String, String> entry) {
        return new ExpectedRelationship(entry.get("classCurie"), entry.get("propertyCurie"), entry.get("rangeCurie"),
                Integer.parseInt(entry.getOrDefault("uses", "0")));
    }

    @DataTableType
    public Statement expectedStatement(Map<String, String> entry) {
        String sString = entry.get("subject");
        String pString = entry.get("predicate");
        String oString = entry.get("object");
        String literal = null;
        String lang = null;
        if (oString.contains("\"")) {
            oString = oString.replaceAll("\"", "");
            if (oString.contains("@")) {
                literal = oString.split("@")[0];
                lang = oString.split("@")[1];
            } else {
                literal = oString;
            }
        }
        return ResourceFactory.createStatement(
                ResourceFactory.createResource(sString),
                ResourceFactory.createProperty(pString),
                lang != null ? ResourceFactory.createLangLiteral(literal, lang) :
                literal != null ? ResourceFactory.createPlainLiteral(literal) :
                ResourceFactory.createResource(oString));
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
    @Transactional
    public void aDatasetWithId(String username, String datasetId) throws Throwable {
        datasetRepository.findById(datasetId).ifPresent(dataset -> {
                    endPointRepository.deleteByDataset(dataset);
                    datasetRepository.delete(dataset);
                });
        Dataset newDataset = new Dataset(datasetId);
        Assert.assertTrue(userRepository.findById(username).isPresent());
        newDataset.setOwner(username);
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
                "\"labelsStr\": \"{1}\", " +
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

    @Then("^it doesn't exist a dataset with id \"([^\"]*)\"$")
    public void itDoesnTExistADatasetWithId(String datasetId) throws Throwable {
        this.result = mockMvc.perform(get("/datasets/{datasetId}", datasetId)
                .accept(MediaType.APPLICATION_JSON)
                .with(authenticate()));
        this.result.andExpect(status().isNotFound());
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
        SPARQLEndPoint endPoint = new SPARQLEndPoint(dataset);
        endPoint.setQueryEndPoint(new URL("http://sparql/mock"));
        endPoint.setWritable(true);
        String endPointJason = mapper.writeValueAsString(endPoint);
        this.result = mockMvc.perform(post("/datasets/{datasetId}/endpoints", datasetId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(endPointJason)
                .accept(MediaType.APPLICATION_JSON)
                .with(authenticate()))
                .andExpect(status().isCreated());
        endPointId = JsonPath.read(result.andReturn().getResponse().getContentAsString(), "$.id");
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
                        post("/datasets/{id}/endpoints/{id}/server?graph={graph}",
                                datasetId, endPointId, graph.get("graph"))
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
        Dataset dataset = datasetRepository.findById(datasetId).get();
        this.result = mockMvc.perform(get("/datasets/{datasetId}/endpoints/{id}", datasetId, endPointId)
                            .accept(MediaType.APPLICATION_JSON)
                            .with(authenticate()))
                        .andExpect(status().isOk());
        String endpointJson = this.result.andReturn().getResponse().getContentAsString();
        SPARQLEndPoint endpoint = mapper.readValue(endpointJson, SPARQLEndPoint.class);
        endpoint.setDataset(dataset);
        endpoint.setInferenceEnabled(inference);
        this.result = mockMvc.perform(put("/datasets/{datasetId}/endpoints/{id}", datasetId, endPointId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(endpoint))
                .accept(MediaType.APPLICATION_JSON)
                .with(authenticate()));
        this.result.andExpect(jsonPath("$.inferenceEnabled", is(inference)));
    }

    @And("^Add endpoint to dataset \"([^\"]*)\"$")
    public void theDatasetServerIsSetTo(String datasetId, Map<String, String> props) throws Throwable {
        Dataset dataset = datasetRepository.findById(datasetId).get();
        SPARQLEndPoint endPoint = new SPARQLEndPoint(dataset);
        endPoint.setQueryEndPoint(new URL(props.get("QueryEndPoint")));
        endPoint.setUpdateEndPoint(props.containsKey("UpdateEndPoint") ? new URL(props.get("UpdateEndPoint")) : null);
        endPoint.setUpdateUsername(props.get("UpdateUsername"));
        endPoint.setQueryUsername(props.get("QueryUsername"));
        endPoint.setWritable(Boolean.parseBoolean(props.get("Writable")));
        this.result = mockMvc.perform(post("/datasets/{datasetId}/endpoints", datasetId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new JSONObject(mapper.writeValueAsString(endPoint))
                        .put("queryPassword", props.get("QueryPassword"))
                        .put("updatePassword", props.get("UpdatePassword")).toString())
                .accept(MediaType.APPLICATION_JSON)
                .with(authenticate()))
                .andExpect(status().isCreated());
        endPointId = JsonPath.read(result.andReturn().getResponse().getContentAsString(), "$.id");
    }

    @When("^I retrieve the dataset with id \"([^\"]*)\"$")
    public void iRetrieveTheDatasetWithId(String datasetId) throws Throwable {
        this.result = mockMvc.perform(get("/datasets/{datasetId}", datasetId)
            .accept(MediaType.APPLICATION_JSON)
            .with(authenticate()));
    }

    @When("^I list the graphs in dataset \"([^\"]*)\" server$")
    public void iListTheGraphsInDatasetServer(String datasetId) throws Throwable {
        this.result = mockMvc.perform(get("/datasets/{id}/endpoints/{id}/server/graphs", datasetId, endPointId)
                .accept(MediaType.APPLICATION_JSON)
                .with(authenticate()))
                .andExpect(status().isOk());
    }

    @Then("^The retrieved graphs are$")
    public void theRetrievedGraphsAre(DataTable expectedGraphs) throws Throwable {
        String json = this.result.andReturn().getResponse().getContentAsString();
        List<String> actualGraphs = mapper.readValue(json, mapper.getTypeFactory().constructCollectionType(List.class, String.class));
        assertThat(actualGraphs, containsInAnyOrder(expectedGraphs.asList().toArray()));
    }

    @When("^I add ontologies to the dataset \"([^\"]*)\"$")
    public void iAddTheOntologyToTheDataset(String datasetId, DataTable ontologies) throws Throwable {
        String ontologiesJson = mapper.writeValueAsString(ontologies.asList());
        this.result = mockMvc.perform(post("/datasets/{datasetId}/ontologies", datasetId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(ontologiesJson)
                .accept(MediaType.APPLICATION_JSON)
                .with(authenticate()));
        this.result.andExpect(status().isCreated());
        this.result.andExpect(jsonPath("$", hasItems(ontologies.asList().toArray())));
    }

    @When("^I add the graphs to the dataset \"([^\"]*)\"$")
    public void iAddTheGraphToTheDataset(String datasetId, DataTable graphs) throws Throwable {
        String graphsJson = mapper.writeValueAsString(graphs.asList());
        this.result = mockMvc.perform(post("/datasets/{id}/endpoints/{id}/graphs", datasetId, endPointId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(graphsJson)
                .accept(MediaType.APPLICATION_JSON)
                .with(authenticate()));
        this.result.andExpect(status().isCreated());
        this.result.andExpect(jsonPath("$", hasItems(graphs.asList().toArray())));
    }

    @When("^I add the graphs to the dataset \"([^\"]*)\" ontologies$")
    public void iAddTheGraphToTheDatasetOntologies(String datasetId, DataTable graphs) throws Throwable {
        String graphsJson = mapper.writeValueAsString(graphs.asList());
        this.result = mockMvc.perform(post("/datasets/{id}/endpoints/{id}/ontologies", datasetId, endPointId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(graphsJson)
                .accept(MediaType.APPLICATION_JSON)
                .with(authenticate()));
        this.result.andExpect(status().isCreated());
        this.result.andExpect(jsonPath("$", hasItems(graphs.asList().toArray())));
    }

    @When("^The following data graphs are set for dataset \"([^\"]*)\"$")
    public void theFollowingDataGraphsAreSetForDataset(String datasetId, DataTable graphs) throws Throwable {
        List<String> validGraphs = graphs.asList().stream().filter(Objects::nonNull).collect(Collectors.toList());
        String graphsJson = mapper.writeValueAsString(validGraphs);
        this.result = mockMvc.perform(put("/datasets/{datasetId}/endpoints/{id}/graphs", datasetId, endPointId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(graphsJson)
                .accept(MediaType.APPLICATION_JSON)
                .with(authenticate()));
        this.result.andExpect(status().isOk());
        this.result.andExpect(jsonPath("$", containsInAnyOrder(validGraphs.toArray())));
    }

    @When("^The following ontology graphs are set for dataset \"([^\"]*)\"$")
    public void theFollowingOntologyGraphsAreSetForDataset(String datasetId, DataTable graphs) throws Throwable {
        List<String> validGraphs = graphs.asList().stream().filter(Objects::nonNull).collect(Collectors.toList());
        String graphsJson = mapper.writeValueAsString(validGraphs);
        this.result = mockMvc.perform(put("/datasets/{datasetId}/endpoints/{id}/ontologies", datasetId, endPointId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(graphsJson)
                .accept(MediaType.APPLICATION_JSON)
                .with(authenticate()));
        this.result.andExpect(status().isOk());
        this.result.andExpect(jsonPath("$", containsInAnyOrder(validGraphs.toArray())));
    }

    @And("^The following ontologies are defined for the dataset \"([^\"]*)\"$")
    public void theFollowingOntologiesAreDefinedForTheDataset(String datasetId, DataTable ontologies) throws Throwable {
        List<String> validOntologies = ontologies.asList().stream().filter(Objects::nonNull).collect(Collectors.toList());
        this.result = mockMvc.perform(get("/datasets/{datasetId}/endpoints/{id}/ontologies", datasetId, endPointId)
                .accept(MediaType.APPLICATION_JSON)
                .with(authenticate()));
        this.result.andExpect(jsonPath("$", containsInAnyOrder(validOntologies.toArray())));
    }

    @And("^The following data graphs are defined for the dataset \"([^\"]*)\"$")
    public void theFollowingDataGraphsAreDefinedForTheDataset(String datasetId, DataTable graphs) throws Throwable {
        List<String> validGraphs = graphs.asList().stream().filter(Objects::nonNull).collect(Collectors.toList());
        this.result = mockMvc.perform(get("/datasets/{id}/endpoints/{id}/graphs", datasetId, endPointId)
                .accept(MediaType.APPLICATION_JSON)
                .with(authenticate()));
        this.result.andExpect(jsonPath("$", containsInAnyOrder(validGraphs.toArray())));
    }

    @And("^The size of dataset \"([^\"]*)\" data graphs is (\\d+)$")
    @Transactional
    public void theSizeOfDatasetGraphsIs(String datasetId, long expectedSize) throws Throwable {
        Dataset dataset = datasetRepository.findById(datasetId).get();
        this.result = mockMvc.perform(get("/datasets/{id}/endpoints/{id}/graphs", datasetId, endPointId)
                .accept(MediaType.APPLICATION_JSON)
                .with(authenticate()));
        String json = this.result.andReturn().getResponse().getContentAsString();
        List<String> datasetGraphs = mapper.readValue(json,
                mapper.getTypeFactory().constructCollectionType(List.class, String.class));
        SPARQLEndPoint defaultEndPoint = endPointRepository.findByDataset(dataset).get(0);
        long actualSize = datasetGraphs.stream().mapToLong(
                graph -> analizeDataset.countGraphTriples(defaultEndPoint, graph)).sum();
        assertThat(actualSize, is(expectedSize));
    }

    @And("^The size of dataset \"([^\"]*)\" ontology graphs is (\\d+)$")
    @Transactional
    public void theSizeOfDatasetOntologyGraphsIs(String datasetId, long expectedSize) throws Throwable {
        Dataset dataset = datasetRepository.findById(datasetId).get();
        this.result = mockMvc.perform(get("/datasets/{id}/endpoints/{id}/ontologies", datasetId, endPointId)
                .accept(MediaType.APPLICATION_JSON)
                .with(authenticate()));
        String json = this.result.andReturn().getResponse().getContentAsString();
        List<String> datasetGraphs = mapper.readValue(json,
                mapper.getTypeFactory().constructCollectionType(List.class, String.class));
        SPARQLEndPoint defaultEndPoint = endPointRepository.findByDataset(dataset).get(0);
        long actualSize = datasetGraphs.stream().mapToLong(
                graph -> analizeDataset.countGraphTriples(defaultEndPoint, graph)).sum();
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

    @When("^I extract the relations for class \"([^\"]*)\" in dataset \"([^\"]*)\"$")
    public void iExtractTheRelationsForClassInDataset(String classCurie, String datasetId) throws Throwable {
        this.result = mockMvc.perform(get("/datasets/{datasetId}/classes/{classCurie}/relations", datasetId, classCurie)
                .accept(MediaType.APPLICATION_JSON)
                .with(authenticate()))
                .andExpect(status().isOk());
    }

    @When("^I extract the incoming facets from dataset \"([^\"]*)\" for resource$")
    public void iExtractTheIncomingFacetsFromDatasetForResources(String datasetId, DataTable resources) throws Throwable {
        assertThat("A resource URI is required", resources.asList().size(), greaterThan(0));
        this.result = mockMvc.perform(get("/datasets/{datasetId}/incoming?uri={resource}", datasetId,
                        resources.asList().get(0))
                .accept(MediaType.APPLICATION_JSON)
                .with(authenticate()))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());
    }

    @When("^I get the description for resource \"([^\"]*)\" in dataset \"([^\"]*)\"$")
    public void iExtractTheIncomingFacetsFromDatasetForResources(String resource, String datasetId) throws Throwable {
        this.result = mockMvc.perform(get("/datasets/{datasetId}/describe?uri={resource}", datasetId,
                        resource)
                        .accept(MediaType.APPLICATION_JSON)
                        .with(authenticate()))
                .andExpect(request().asyncStarted());
    }

    @Then("The retrieved data includes the following triples")
    public void theRetrievedDataIncludesTheFollowingTriples(List<Statement> expectedTriples) throws Exception {
        // this.mockMvc.perform(asyncDispatch(this.result.andReturn())).andExpect(content().string("http"));
        String data = this.result.andDo(MvcResult::getAsyncResult)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        Model model = ModelFactory.createDefaultModel()
                .read(IOUtils.toInputStream(data, "UTF-8"), null, "JSON-LD");
        List<Statement> actualTriples = model.listStatements().toList();
        assertThat(actualTriples, containsInAnyOrder(expectedTriples.toArray()));
    }

    @When("^I retrieve facet \"([^\"]*)\" ranges$")
    public void iRetrieveFacetRange(String facetId, List<ExpectedRange> expectedRange) throws Throwable {
        String json = mockMvc.perform(get(facetId + "/ranges")
                        .accept(MediaType.APPLICATION_JSON)
                        .with(authenticate()))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        List<ExpectedRange> actualRange = mapper.readValue(json, mapper.getTypeFactory().constructCollectionType(List.class, ExpectedRange.class));
        assertThat(actualRange, containsInAnyOrder(expectedRange.toArray()));
    }

    @When("^I retrieve facet range \"([^\"]*)\" values$")
    public void iRetrieveFacetRangeValues(String facetRangeId, List<ExpectedRangeValue> expectedRangeValues) throws Throwable {
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
    public void iClearDatasetClasses(String datasetId, List<ExpectedClass> newClasses) throws Throwable {
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

    @Then("^The retrieved relationships are$")
    public void theRetrievedRelationsAre(List<ExpectedRelationship> expectedRelations) throws Throwable {
        String json = this.result.andReturn().getResponse().getContentAsString();
        List<ExpectedRelationship> actualRelations = mapper.readValue(json, mapper.getTypeFactory().constructCollectionType(List.class, ExpectedRelationship.class));
        assertThat(actualRelations, containsInAnyOrder(expectedRelations.toArray()));
    }

    @Then("^The retrieved facet is")
    public void theRetrievedFacetIs(List<ExpectedFacet> expectedFacets) throws Throwable {
        String json = this.result.andReturn().getResponse().getContentAsString();
        ExpectedFacet actualFacet = mapper.readValue(json, ExpectedFacet.class);
        assertThat(actualFacet, is(expectedFacets.get(0)));
    }

    @Then("^The retrieved incoming facets are")
    public void theRetrievedIncomingFacetAre(List<Map<String, String>> expectedIncomings) throws Throwable {
        expectedIncomings.forEach(expectedIncoming -> {
            try {
                String facetPath = "$[?(@.curie == '" + expectedIncoming.get("curie") + "')]";
                String domainPath = facetPath + ".domains[?(@.curie == '" + expectedIncoming.get("domain-curie") + "')]";
                this.result.andExpect(jsonPath(facetPath + ".rangeCurie", hasItem(expectedIncoming.get("range-curie"))));
                this.result.andExpect(jsonPath(facetPath + ".labels", hasItem(Labelled.splitLabelsUtil(expectedIncoming.get("label")))));
                this.result.andExpect(jsonPath(facetPath + ".uses", hasItem(Integer.parseInt(expectedIncoming.get("uses")))));
                this.result.andExpect(jsonPath(domainPath + ".labels", hasItem(Labelled.splitLabelsUtil(expectedIncoming.get("domain-label")))));
                this.result.andExpect(jsonPath(domainPath + ".count", hasItem(Integer.parseInt(expectedIncoming.get("count")))));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
