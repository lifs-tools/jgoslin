/*
 * 
 */
package de.isas.lipidomics.goslin.molecularsubspecies;

import de.isas.lipidomics.palinom.exceptions.ParsingException;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import de.isas.lipidomics.domain.FattyAcid;
import de.isas.lipidomics.domain.LipidLevel;
import de.isas.lipidomics.domain.LipidMolecularSubspecies;
import de.isas.lipidomics.domain.LipidSpecies;
import de.isas.lipidomics.palinom.goslin.GoslinVisitorParser;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;

/**
 *
 * @author nils.hoffmann
 */
@Slf4j
public class MagStepDefs {

    private String lipidname;
    private LipidSpecies lipid;

    @Given("the mag lipid molecular sub species name is {string}")
    public void the_mag_lipid_molecular_sub_species_name_is(String string) {
        this.lipidname = string;
    }

    @When("I parse a monoacylglycerol with name {string}")
    public void i_parse_a_monoacylglycerol_with_name(String string) {
        GoslinVisitorParser parser = new GoslinVisitorParser();
        try {
            lipid = parser.parse(string).getLipid();
        } catch (ParsingException pe) {
            log.error("Caught parsing exception: ", pe);
        }
    }
    
    @Then("I should get a mag lipid of category {string} and species {string} {string} headgroup")
    public void i_should_get_a_mag_lipid_of_category_and_species_MAG_headgroup(String category, String species, String headgroup) {
        Assert.assertEquals(category, this.lipid.getLipidCategory().name());
        Assert.assertEquals(species, this.lipid.getLipidString(LipidLevel.SPECIES));
        Assert.assertEquals(headgroup, this.lipid.getHeadGroup().getName());
    }

    @Then("the first mag fatty acid at position {int} with {int} carbon atoms, {int} double bonds and {int} hydroxy groups.")
    public void the_first_mag_fatty_acid_at_position_with_carbon_atoms_double_bonds_and_hydroxy_groups(Integer position, Integer nCarbon, Integer nDoubleBonds, Integer nHydroxy) {

        if (this.lipid instanceof LipidMolecularSubspecies) {
            LipidMolecularSubspecies lms = (LipidMolecularSubspecies) this.lipid;
            FattyAcid fa1 = lms.getFa().get("FA1");
            Assert.assertEquals(position.intValue(), fa1.getPosition());
            Assert.assertEquals(nCarbon.intValue(), fa1.getNCarbon());
            Assert.assertEquals(nDoubleBonds.intValue(), fa1.getNDoubleBonds());
            Assert.assertEquals(nHydroxy.intValue(), fa1.getNHydroxy());
        }
    }

    protected String toString(LipidMolecularSubspecies referenceLipid) {
        return referenceLipid.getHeadGroup() + " " + referenceLipid.getFa().
                get("FA1").
                getNCarbon() + ":" + referenceLipid.getFa().
                get("FA1").
                getNDoubleBonds() + ";" + referenceLipid.getFa().
                get("FA1").
                getNHydroxy();
    }
}
