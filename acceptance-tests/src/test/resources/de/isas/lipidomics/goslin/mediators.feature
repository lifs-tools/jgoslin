Feature: Is this a valid Mediator name?
  This scenario tests different mediators.

  Scenario Outline: A Mediator is a fatty acyl. The mediators in this scenario have no defined stereochemistry, they are just classified on structural sub species level.
    Given the lipid structural sub species name is <lipid_structural_sub_species>
    When I parse a lipid mediator with name <lipid_structural_sub_species>
    Then I should get a lipid of category <lipid_category> and species <lipid_species> <headgroup> headgroup.

    Examples:
      | lipid_structural_sub_species  | lipid_category | lipid_species | headgroup    |
      | "10-HDoHE"                    | "FA"           | "10-HDoHE"    | "10-HDoHE"   |
      | "11,12-DHET"                  | "FA"           | "11,12-DHET"  | "11,12-DHET" |
      | "11(12)-EET"                  | "FA"           | "11(12)-EET"  | "11(12)-EET" |
