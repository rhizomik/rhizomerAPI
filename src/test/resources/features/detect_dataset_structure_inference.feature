# Created by http://rhizomik.net/~roberto/

Feature: Detect dataset structure considering inference
  In order to explore the structure of a dataset
  As a data manager
  I want to detect all the classes defined in the dataset and their facets considering inference

  Background: Existing dataset in local server storing file data
    Given I login as "user" with password "password"
    And There is a new dataset by "user" with id "apollo13"
    And The dataset "apollo13" has a mock server
    And The server for dataset "apollo13" stores data
      | data                            | graph                                 |
      | data/nasa-apollo13.ttl          | http://rhizomik.net/dataset/apollo13  |
      | data/nasa-schema.ttl            | http://rhizomik.net/schema/nasa       |
    And The following data graphs are set for dataset "apollo13"
      | http://rhizomik.net/dataset/apollo13    |
    And I add the graphs to the dataset "apollo13" ontologies
      | http://rhizomik.net/schema/nasa         |
    And The query type for dataset "apollo13" is set to "DETAILED"
    And The inference for dataset "apollo13" is set to "true"

  Scenario: The extracted classes are those instantiated in the dataset
    When I extract the classes from dataset "apollo13"
    Then The retrieved classes are
      | uri                                           | labels                                    | instanceCount |
      | http://xmlns.com/foaf/0.1/Person              | Person@en \|\| Persona@es                 | 2             |
      | http://purl.org/net/schemas/space/MissionRole | Mission Role@en \|\| Rol en la MisiÃ³n@es | 2             |
      | http://purl.org/net/schemas/space/Mission     | Mission@en \|\| MisiÃ³n@es                | 1             |
      | http://purl.org/net/schemas/space/Role        | Role@en \|\| Rol@es                       | 2             |
    And exists a class with id "/datasets/apollo13/classes/space:Role"

  Scenario: The extracted facets for an existing class foaf:Person are those instantiated in the dataset
    Given I create a class in dataset "apollo13" with URI "http://xmlns.com/foaf/0.1/Person", label "Person" and instance count 2
    When I extract the facets for class "foaf:Person" in dataset "apollo13"
    Then The retrieved facets are
      | uri                                             | timesUsed | differentValues | range             | relation | labels                        |
      | http://xmlns.com/foaf/0.1/name                  | 3       | 3                 | xsd:string        | false    | name                          |
      | http://purl.org/net/schemas/space/performed     | 2       | 2                 | space:MissionRole | true     | performed@en \|\| realizÃ³@es |
      | http://www.w3.org/2000/01/rdf-schema#label      | 2       | 2                 | xsd:string        | false    | label                         |
      | http://www.w3.org/1999/02/22-rdf-syntax-ns#type | 2       | 1                 | rdfs:Resource     | true     | type                          |
      | http://www.w3.org/2002/07/owl#sameAs            | 1       | 1                 | rdfs:Resource     | true     | sameAs                        |
    And exists a facet with id "/datasets/apollo13/classes/foaf:Person/facets/foaf:name"
    And The retrieved facet is
      | uri                                             | labels    | timesUsed | differentValues | range       | relation   |
      | http://xmlns.com/foaf/0.1/name                  | name      | 3         | 3               | xsd:string  | false      |

  Scenario: The extracted facets for an existing class space:MissionRole are those instantiated in the dataset
    Given I create a class in dataset "apollo13" with URI "http://purl.org/net/schemas/space/Mission", label "Mission" and instance count 2
    When I extract the facets for class "space:Mission" in dataset "apollo13"
    Then The retrieved facets are
      | uri                                             | timesUsed | differentValues | range             | relation | labels                                    |
      | http://purl.org/net/schemas/space/missionRole   | 2         | 2               | space:MissionRole | true     | mission role@en \|\| rol en la misiÃ³n@es |
      | http://purl.org/dc/terms/title                  | 1         | 1               | xsd:string        | false    | title                                     |
      | http://www.w3.org/2000/01/rdf-schema#label      | 1         | 1               | xsd:string        | false    | label                                     |
      | http://www.w3.org/1999/02/22-rdf-syntax-ns#type | 1         | 1               | rdfs:Resource     | true     | type                                      |
      | http://www.w3.org/2002/07/owl#sameAs            | 1         | 1               | rdfs:Resource     | true     | sameAs                                    |
    And exists a facet with id "/datasets/apollo13/classes/space:Mission/facets/space:missionRole"
    And The retrieved facet is
      | uri                                             | timesUsed | differentValues | range             | relation | labels                                    |
      | http://purl.org/net/schemas/space/missionRole   | 2         | 2               | space:MissionRole | true     | mission role@en \|\| rol en la misiÃ³n@es |
    And I retrieve facet "/datasets/apollo13/classes/space:Mission/facets/space:missionRole" ranges
      | uri                                             | timesUsed | differentValues | curie             | relation | label                                     |
      | http://purl.org/net/schemas/space/MissionRole   | 2         | 2               | space:MissionRole | true     | Mission Role@en \|\| Rol en la MisiÃ³n@es |

