package net.rhizomik.rhizomer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import cucumber.api.java.Before;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import net.rhizomik.rhizomer.model.Class;
import net.rhizomik.rhizomer.model.ExpectedClass;
import net.rhizomik.rhizomer.model.Pond;
import net.rhizomik.rhizomer.model.Server;
import net.rhizomik.rhizomer.repository.ClassRepository;
import net.rhizomik.rhizomer.repository.PondRepository;
import net.rhizomik.rhizomer.repository.ServerRepository;
import net.rhizomik.rhizomer.service.SPARQLService;
import org.apache.jena.riot.RDFDataMgr;
import org.hamcrest.collection.IsIterableContainingInOrder;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
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

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
    private Dataset dataset;
    private Pond pond;

    private ObjectMapper mapper = new ObjectMapper();

    @Autowired private WebApplicationContext wac;
    @Autowired private PondRepository pondRepository;
    @Autowired private ClassRepository classRepository;
    @Autowired private ServerRepository serverRepository;

    @Autowired private SPARQLService sparqlService;

    @Configuration
    static class SPARQLServiceMockConfig {
        @Bean
        public SPARQLService sparqlService() {
            return Mockito.mock(SPARQLService.class);
        }
    }

    @Before
    public void setup() {
        this.mockMvc = //MockMvcBuilders.standaloneSetup(pondClassController).build();
                MockMvcBuilders
                .webAppContextSetup(this.wac)
                //.apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Given("^a pond with id \"([^\"]*)\"$")
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

    @And("^exists a class with id \"([^\"]*)\" and label \"([^\"]*)\" and instance count (\\d+)$")
    public void existsAClassWithIdAndLabelAndInstanceCount(String classUriStr, String classLabel, int instanceCount) throws Throwable {
        this.result = mockMvc.perform(get(new URI(classUriStr))
                .accept(MediaType.APPLICATION_JSON));
        this.result.andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(classUriStr)))
                .andExpect(jsonPath("$.label", is(classLabel)))
                .andExpect(jsonPath("$.instanceCount", is(instanceCount)));
    }

    @Given("^There is a pond \"([^\"]*)\" on a local server storing \"([^\"]*)\" in graph \"([^\"]*)\"$")
    public void There_is_a_pond_on_a_local_server(String pondId, String dataFile, URI graph) throws Throwable {
        dataset = DatasetFactory.createMem();
        Server server = new Server(new URL("http://test/sparql"));
        pond = new Pond(pondId);
        pond.setServer(serverRepository.save(server));
        pond.addPondGraph(graph.toString());
        pond = pondRepository.save(pond);
        Model model = RDFDataMgr.loadModel(dataFile);
        dataset.addNamedModel(graph.toString(), model);
    }

    @When("^I extract the classes from pond \"([^\"]*)\"$")
    public void I_extract_the_classes_from_pond(String pondId) throws Throwable {
        Mockito.when(sparqlService.querySelect(Matchers.isA(Server.class), Matchers.isA(Query.class), Matchers.anyList(), Matchers.anyList()))
                .thenAnswer(invocationOnMock -> {
                    Query query = (Query) invocationOnMock.getArguments()[1];
                    List<String> graphs = (List<String>) invocationOnMock.getArguments()[2];
                    Model queryDataset = ModelFactory.createDefaultModel();
                    graphs.forEach(graph -> queryDataset.add(dataset.getNamedModel(graph)));
                    graphs.forEach(query::addGraphURI);
                    logger.info("Sending to {} query: \n{}", "mockServer", query);
                    QueryExecution qexec = QueryExecutionFactory.create(query, queryDataset);
                    return qexec.execSelect();
                });
        this.result = mockMvc.perform(get("/ponds/{pondId}/classes", pondId)
                .accept(MediaType.APPLICATION_JSON));
    }

    @Then("^The list of classes in pond \"([^\"]*)\" is")
    public void The_list_of_classes_in_is(String pondId, List<ExpectedClass> expectedClasses) throws Throwable {
        String json = this.result.andReturn().getResponse().getContentAsString();
        List<ExpectedClass> actualClasses = mapper.readValue(json, mapper.getTypeFactory().constructCollectionType(List.class, ExpectedClass.class));
        assertThat(actualClasses, IsIterableContainingInOrder.contains(expectedClasses.toArray()));
    }
}
