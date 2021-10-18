package de.isas.lipidomics.goslin;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(plugin = {"html:target/cucumber-html-report",
    "json:target/cucumber.json", "pretty:target/cucumber-pretty.txt",
    "junit:target/cucumber-results.xml"})
public class RunCucumberTest {
}
