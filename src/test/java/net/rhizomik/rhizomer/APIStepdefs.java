package net.rhizomik.rhizomer;

import com.fasterxml.jackson.databind.ObjectMapper;
import cucumber.api.DataTable;
import cucumber.api.java.Before;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import net.rhizomik.rhizomer.model.Pond;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationContextLoader;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by http://rhizomik.net/~roberto/
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = EyeApplication.class, loader = SpringApplicationContextLoader.class)
@WebAppConfiguration
@IntegrationTest
public class APIStepdefs {
    private static final Logger logger = LoggerFactory.getLogger(APIStepdefs.class);

    private MockMvc mockMvc;
    private ResultActions result;

    ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private WebApplicationContext wac;

    @Before
    public void setup() {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(this.wac)
                //.apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @When("^a manager creates a pond with id \"([^\"]*)\"$")
    public void aManagerCreatesAPondWithId(String pondId) throws Throwable {
        Pond pond = new Pond(pondId);
        String json = mapper.writeValueAsString(pond);
        this.result = mockMvc.perform(post("/ponds")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .accept(MediaType.APPLICATION_JSON));
    }

    @Then("^the response status is (\\d+)$")
    public void theResponseStatusIs(int status) throws Throwable {
        this.result.andExpect(status().is(status));
    }

    @And("^there is pond with the following attributes$")
    public void thereIsPondWithTheFollowingAttributes(DataTable pondsTable) throws Throwable {
        Pond pond = pondsTable.asList(Pond.class).get(0);
        this.result.andExpect(jsonPath("$._links.self.href", is("http://localhost/ponds/"+pond.getId())));
    }

    @And("^there is pond with URI \"([^\"]*)\"$")
    public void thereIsPondWithURI(String uriStr) throws Throwable {
        this.result.andExpect(jsonPath("$._links.self.href", is(uriStr)));;
    }
}
