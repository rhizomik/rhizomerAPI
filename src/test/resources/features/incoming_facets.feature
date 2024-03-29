# Created by http://rhizomik.net/~roberto/

Feature: Describe resource in dataset
  In order to explore the instances in a dataset
  As a data manager
  I want to detect all the classes defined in the dataset and their facets

  Background: Existing dataset in local server storing file data
    Given I login as "user" with password "password"
    And There is a new dataset by "user" with id "apollo"
    And The dataset "apollo" has a mock server
    And The server for dataset "apollo" stores data
      | data                            | graph                                 |
      | data/nasa-apollo13.ttl          | http://rhizomik.net/dataset/apollo13  |
      | data/nasa-schema.ttl            | http://rhizomik.net/schema/nasa       |
    And The following data graphs are set for dataset "apollo"
      | http://rhizomik.net/dataset/apollo13          |
    And I add the graphs to the dataset "apollo" ontologies
      | http://rhizomik.net/schema/nasa         |
    And The query type for dataset "apollo" is set to "DETAILED"
    And The inference for dataset "apollo" is set to "false"

  Scenario: Extracts one incoming facet with one domain and one use
    When I extract the incoming facets from dataset "apollo" for resource
      | http://data.kasabi.com/dataset/nasa/person/fredwallacehaisejr |
    Then The retrieved incoming facets are
      | range-curie | curie       | uses | domain-curie      | count | label | domain-label                             |
      | foaf:Person | space:actor | 1    | space:MissionRole | 1     | actor | Mission Role@en \|\| Rol en la Misión@es |

  Scenario: Extracts one incoming facets with one domain and two uses
    When I extract the incoming facets from dataset "apollo" for resource
      | http://data.kasabi.com/dataset/nasa/mission/apollo-13 |
    Then The retrieved incoming facets are
      | range-curie   | curie         | uses | domain-curie      | count | label                     | domain-label                             |
      | space:Mission | space:mission | 2    | space:MissionRole | 2     | mission@en \|\| misión@es | Mission Role@en \|\| Rol en la Misión@es |

  Scenario: Extracts two incoming facets with one domain each and one use
    When I extract the incoming facets from dataset "apollo" for resource
      | http://data.kasabi.com/dataset/nasa/mission/apollo-13/role/commander |
    Then The retrieved incoming facets are
      | range-curie       | curie             |  uses | domain-curie  | count | label                                    | domain-label              |
      | space:MissionRole | space:missionRole |  1    | space:Mission | 1     | mission role@en \|\| rol en la misión@es | Mission@en \|\| Misión@es |
      | space:MissionRole | space:performed   |  1    | foaf:Person   | 1     | performed@en \|\| realizó@es             | Person@en \|\| Persona@es |
