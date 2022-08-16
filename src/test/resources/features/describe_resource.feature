# Created by http://rhizomik.net/~roberto/

Feature: Describe resource in dataset
  In order to explore the instances in a dataset
  As a data manager
  I want to retrieve all the triples directly associated to a particular resource

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

  Scenario: Describe a resource which includes labels in different languages from data and ontology graphs
    When I get the description for resource "http://data.kasabi.com/dataset/nasa/mission/apollo-13/role/lunar-module-pilot" in dataset "apollo"
    Then The retrieved data includes the following triples
      | subject | predicate | object |
      | http://data.kasabi.com/dataset/nasa/mission/apollo-13/role/lunar-module-pilot | http://www.w3.org/2000/01/rdf-schema#label      | "Piloto del Módulo Lunar Apollo 13"@es                       |
      | http://data.kasabi.com/dataset/nasa/mission/apollo-13/role/lunar-module-pilot | http://www.w3.org/2000/01/rdf-schema#label      | "Apollo 13 Lunar Module Pilot"@en                            |
      | http://data.kasabi.com/dataset/nasa/mission/apollo-13/role/lunar-module-pilot | http://purl.org/net/schemas/space/role          | http://data.kasabi.com/dataset/nasa/roles/lunar-module-pilot |
      | http://data.kasabi.com/dataset/nasa/mission/apollo-13/role/lunar-module-pilot | http://purl.org/net/schemas/space/mission       | http://data.kasabi.com/dataset/nasa/mission/apollo-13        |
      | http://data.kasabi.com/dataset/nasa/mission/apollo-13/role/lunar-module-pilot | http://purl.org/net/schemas/space/actor         | http://data.kasabi.com/dataset/nasa/person/fredwallacehaisejr|
      | http://data.kasabi.com/dataset/nasa/mission/apollo-13/role/lunar-module-pilot | http://www.w3.org/1999/02/22-rdf-syntax-ns#type | http://purl.org/net/schemas/space/MissionRole                |
      | http://purl.org/net/schemas/space/mission                                     | http://www.w3.org/2000/01/rdf-schema#label      | "mission"@en                                                 |
      | http://purl.org/net/schemas/space/mission                                     | http://www.w3.org/2000/01/rdf-schema#label      | "misión"@es                                                  |
      | http://data.kasabi.com/dataset/nasa/mission/apollo-13                         | http://www.w3.org/2000/01/rdf-schema#label      | "Apollo 13"                                                  |
      | http://data.kasabi.com/dataset/nasa/roles/lunar-module-pilot                  | http://www.w3.org/2000/01/rdf-schema#label      | "Lunar Module Pilot"@en                                      |
      | http://data.kasabi.com/dataset/nasa/roles/lunar-module-pilot                  | http://www.w3.org/2000/01/rdf-schema#label      | "Piloto del Módulo Lunar"@es                                 |
      | http://purl.org/net/schemas/space/role                                        | http://www.w3.org/2000/01/rdf-schema#label      | "role"@en                                                    |
      | http://purl.org/net/schemas/space/role                                        | http://www.w3.org/2000/01/rdf-schema#label      | "rol"@es                                                     |
      | http://purl.org/net/schemas/space/actor                                       | http://www.w3.org/2000/01/rdf-schema#label      | "actor"                                                      |
      | http://data.kasabi.com/dataset/nasa/person/fredwallacehaisejr                 | http://www.w3.org/2000/01/rdf-schema#label      | "Fred Wallace Haise, Jr."                                    |
      | http://purl.org/net/schemas/space/MissionRole                                 | http://www.w3.org/2000/01/rdf-schema#label      | "Mission Role"@en                                            |
      | http://purl.org/net/schemas/space/MissionRole                                 | http://www.w3.org/2000/01/rdf-schema#label      | "Rol en la Misión"@es                                        |
