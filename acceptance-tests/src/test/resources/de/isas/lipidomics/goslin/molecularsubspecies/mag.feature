Feature: Is this a valid Monoacylglycerol sub species name?
  This scenario tests different Monoacylglycerol FA specifications.

  Scenario Outline: A Monoacylglycerol with one fatty acid chains, zero or more double bonds, and zero or more hydroxy groups
    Given the mag lipid molecular sub species name is <lipid_molecular_sub_species>
    When I parse a monoacylglycerol with name <lipid_molecular_sub_species>
    Then I should get a mag lipid of category <lipid_category> and species <lipid_species> <headgroup> headgroup
    And the first mag fatty acid at position <fa1_pos> with <fa1_n_carb> carbon atoms, <fa1_n_db> double bonds and <fa1_n_hydroxy> hydroxy groups.

    Examples:
      | lipid_molecular_sub_species  | lipid_category |  lipid_species | headgroup | fa1_pos |fa1_n_carb | fa1_n_db | fa1_n_hydroxy |
      | "MAG 34:5"                   | "GL"           |   "MAG 34:5"   |     "MAG" |  1      |        34 |        5 |             0 |
      | "MAG 16:1"                   | "GL"           |   "MAG 16:1"   |     "MAG" |  1      |        16 |        1 |             0 |
      | "MAG 16:1;5"                 | "GL"           |   "MAG 16:1;5" |     "MAG" |  1      |        16 |        1 |             5 |
