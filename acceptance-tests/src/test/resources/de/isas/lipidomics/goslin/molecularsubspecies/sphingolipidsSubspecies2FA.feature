Feature: Is this a valid Sphingolipid sub species name?
  This scenario tests different Sphingolipid head groups and FA specifications.

  Scenario Outline: A Sphingolipid with two fatty acid chains, zero or more double bonds, and zero or more hydroxy groups
    Given the lipid molecular sub species name is <lipid_molecular_sub_species>
    When I parse a sphingolipid with name <lipid_molecular_sub_species>
    Then I should get a lipid of category <lipid_category> and species <lipid_species> <headgroup> headgroup
    And the first fatty acid at position <fa1_pos> with <fa1_n_carb> carbon atoms, <fa1_n_db> double bonds and <fa1_n_hydroxy> hydroxy groups 
    And the second fatty acid at position <fa2_pos> with <fa2_n_carb> carbon atoms, <fa2_n_db> double bonds and <fa2_n_hydroxy> hydroxy groups.

    Examples:
      | lipid_molecular_sub_species  | lipid_category |  lipid_species | headgroup | fa1_pos |fa1_n_carb | fa1_n_db | fa1_n_hydroxy | fa2_pos | fa2_n_carb | fa2_n_db | fa2_n_hydroxy |
      | "PE 34:5"                    | "GP"           |    "PE 34:5"   |      "PE" |  0      |         0 |        0 |             0 |  0      |          0 |        0 |             0 |
      | "PE 18:3;1-16:2"             | "GP"           |    "PE 34:5;1" |      "PE" | -1      |        18 |        3 |             1 | -1      |         16 |        2 |             0 |
      | "PE 18:2;1-16:2"             | "GP"           |    "PE 34:4;1" |      "PE" | -1      |        18 |        2 |             1 | -1      |         16 |        2 |             0 |
      | "PE 18:2-16:2"               | "GP"           |    "PE 34:4"   |      "PE" | -1      |        18 |        2 |             0 | -1      |         16 |        2 |             0 |
      | "PC 16:2-16:1"               | "GP"           |    "PC 32:3"   |      "PC" | -1      |        16 |        2 |             0 | -1      |         16 |        1 |             0 |
      | "PC 16:2/16:1"               | "GP"           |    "PC 32:3"   |      "PC" |  1      |        16 |        2 |             0 |  2      |         16 |        1 |             0 |
      | "PC 18:1/16:2;1"             | "GP"           |    "PC 34:3;1" |      "PC" |  1      |        18 |        1 |             0 |  2      |         16 |        2 |             1 |
      | "PA 18:1;2/16:2;1"           | "GP"           |    "PA 34:3;3" |      "PA" |  1      |        18 |        1 |             2 |  2      |         16 |        2 |             1 |
      | "PI 18:1;2/16:2;1"           | "GP"           |    "PI 34:3;3" |      "PI" |  1      |        18 |        1 |             2 |  2      |         16 |        2 |             1 |
      | "PG 18:1;2/16:2;1"           | "GP"           |    "PG 34:3;3" |      "PG" |  1      |        18 |        1 |             2 |  2      |         16 |        2 |             1 |
      | "PS 18:1;2/16:2;1"           | "GP"           |    "PS 34:3;3" |      "PS" |  1      |        18 |        1 |             2 |  2      |         16 |        2 |             1 |
      | "PE O-18:0a/16:2;1"          | "GP"           |  "PE O-34:2;1a"|      "PE" |  1      |        18 |        0 |             0 |  2      |         16 |        2 |             1 |
      | "PE O-18:1p/16:2;1"          | "GP"           |  "PE O-34:3;1p"|      "PE" |  1      |        18 |        1 |             0 |  2      |         16 |        2 |             1 |
      | "PC O-16:0a/14:2;1"          | "GP"           |  "PC O-30:2;1a"|      "PC" |  1      |        16 |        0 |             0 |  2      |         14 |        2 |             1 |
      | "PC O-16:1p/14:2;1"          | "GP"           |  "PC O-30:3;1p"|      "PC" |  1      |        16 |        1 |             0 |  2      |         14 |        2 |             1 |
      | "LPC O-16:0"                 | "GP"           |   "LPC O-16:0" |     "LPC" |  1      |        16 |        0 |             0 | -1      |          0 |        0 |             0 |
      | "LPC O-16:1"                 | "GP"           |   "LPC O-16:1" |     "LPC" |  1      |        16 |        1 |             0 | -1      |          0 |        0 |             0 |
