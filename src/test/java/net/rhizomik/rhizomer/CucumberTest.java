package net.rhizomik.rhizomer;

import cucumber.api.CucumberOptions;
import org.junit.runner.RunWith;
import cucumber.api.junit.Cucumber;

/**
 * Created by http://rhizomik.net/~roberto/
 */

@RunWith(Cucumber.class)
@CucumberOptions(plugin={"pretty"}, features="src/test/resources", tags = {"~@remote"})
public class CucumberTest {}