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
      | range-curie | curie       | label  | uses | domain-curie      | domain-label | count |
      | foaf:Person | space:actor | actor  | 1    | space:MissionRole | MissionRole  | 1     |

  Scenario: Extracts one incoming facets with one domain and two uses
    When I extract the incoming facets from dataset "apollo" for resource
      | http://data.kasabi.com/dataset/nasa/mission/apollo-13 |
    Then The retrieved incoming facets are
      | range-curie   | curie         | label   | uses | domain-curie      | domain-label | count |
      | space:Mission | space:mission | mission | 2    | space:MissionRole | MissionRole  | 2     |

  Scenario: Extracts two incoming facets with one domain each and one use
    When I extract the incoming facets from dataset "apollo" for resource
      | http://data.kasabi.com/dataset/nasa/mission/apollo-13/role/commander |
    Then The retrieved incoming facets are
      | range-curie       | curie             | label       | uses | domain-curie  | domain-label | count |
      | space:MissionRole | space:missionRole | missionRole | 1    | space:Mission | Mission      | 1     |
      | space:MissionRole | space:performed   | performed   | 1    | foaf:Person   | Person       | 1     |
