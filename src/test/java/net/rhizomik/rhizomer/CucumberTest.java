package net.rhizomik.rhizomer;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

/**
 * Created by http://rhizomik.net/~roberto/
 */

@RunWith(Cucumber.class)
@CucumberOptions(plugin={"pretty"}, features="src/test/resources", tags = "not @remote")
public class CucumberTest {}
