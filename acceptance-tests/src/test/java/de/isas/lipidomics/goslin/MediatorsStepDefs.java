/*
 * 
 */
package de.isas.lipidomics.goslin;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import de.isas.lipidomics.domain.LipidLevel;
import de.isas.lipidomics.domain.LipidSpecies;
import de.isas.lipidomics.palinom.exceptions.ParsingException;
import de.isas.lipidomics.palinom.goslin.GoslinVisitorParser;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;

/**
 *
 * @author nils.hoffmann
 */
@Slf4j
public class MediatorsStepDefs {

    private String lipidname;
    private LipidSpecies lipid;

    @Given("the lipid structural sub species name is {string}")
    public void the_lipid_structural_sub_species_name_is(String string) {
        this.lipidname = string;
        GoslinVisitorParser parser = new GoslinVisitorParser();
        try {
            this.lipid = (LipidSpecies) parser.parse(string).getLipid();
        } catch (ParsingException pe) {
            log.error("Caught parsing exception: ", pe);
        }
    }
    
    @When("I parse a lipid mediator with name {string}")
    public void i_parse(String string) {
        GoslinVisitorParser parser = new GoslinVisitorParser();
        try {
            this.lipid = (LipidSpecies) parser.parse(string).getLipid();
        } catch (ParsingException pe) {
            log.error("Caught parsing exception: ", pe);
        }
    }

    @Then("I should get a lipid of category {string} and species {string} {string} headgroup.")
    public void i_should_get_a_lipid_of_category_and_species_headgroup(String category, String species, String headgroup) {
        Assert.assertEquals(category, this.lipid.getLipidCategory().name());
        Assert.assertEquals(species, this.lipid.getLipidString(LipidLevel.SPECIES));
        Assert.assertEquals(headgroup, this.lipid.getHeadGroup().getName());
    }
}
