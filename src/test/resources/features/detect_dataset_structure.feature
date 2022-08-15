# Created by http://rhizomik.net/~roberto/

Feature: Detect dataset structure
  In order to explore the structure of a dataset
  As a data manager
  I want to detect all the classes defined in the dataset and their facets

  Background: Existing dataset in local server storing file data
    Given I login as "user" with password "password"
    And There is a new dataset by "user" with id "mixed"
    And The dataset "mixed" has a mock server
    And The server for dataset "mixed" stores data
      | data                            | graph                                 |
      | data/nasa-apollo13.ttl          | http://rhizomik.net/dataset/apollo13  |
      | data/got.ttl                    | http://rhizomik.net/dataset/got       |
    And The following data graphs are set for dataset "mixed"
      | http://rhizomik.net/dataset/apollo13          |
    And The query type for dataset "mixed" is set to "DETAILED"
    And The inference for dataset "mixed" is set to "false"

  Scenario: The extracted classes are those instantiated in the dataset
    When I extract the classes from dataset "mixed"
    Then The retrieved classes are
      | uri                                           | labels      | instanceCount |
      | http://xmlns.com/foaf/0.1/Person              | Person      | 2             |
      | http://purl.org/net/schemas/space/MissionRole | MissionRole | 2             |
      | http://purl.org/net/schemas/space/Mission     | Mission     | 1             |
      | http://www.w3.org/2000/01/rdf-schema#Resource | Resource    | 2             |
    And exists a class with id "/datasets/mixed/classes/foaf:Person"

  Scenario: Retrieve just the top most instantiated classes
    When I extract the top 3 classes from dataset "mixed"
    Then The retrieved classes are
      | uri                                           | labels      | instanceCount |
      | http://xmlns.com/foaf/0.1/Person              | Person      | 2             |
      | http://purl.org/net/schemas/space/MissionRole | MissionRole | 2             |
      | http://www.w3.org/2000/01/rdf-schema#Resource | Resource    | 2             |

  Scenario: Retrieve top classes containing text in URI or label and ignoring case
    When I extract the top 2 classes from dataset "mixed" containing "mission"
    Then The retrieved classes are
      | uri                                           | labels      | instanceCount |
      | http://purl.org/net/schemas/space/MissionRole | MissionRole | 2             |
      | http://purl.org/net/schemas/space/Mission     | Mission     | 1             |

  Scenario: The extracted facets for an existing class are those instantiated in the dataset
    Given I create a class in dataset "mixed" with URI "http://xmlns.com/foaf/0.1/Person", label "Person" and instance count 2
    When I extract the facets for class "foaf:Person" in dataset "mixed"
    Then The retrieved facets are
      | uri                                             | labels    | timesUsed | differentValues | range             | relation   |
      | http://xmlns.com/foaf/0.1/name                  | name      | 3         | 3               | xsd:string        | false      |
      | http://purl.org/net/schemas/space/performed     | performed | 2         | 2               | space:MissionRole | true       |
      | http://www.w3.org/2000/01/rdf-schema#label      | label     | 2         | 2               | xsd:string        | false      |
      | http://www.w3.org/1999/02/22-rdf-syntax-ns#type | type      | 2         | 1               | rdfs:Resource     | true       |
      | http://www.w3.org/2002/07/owl#sameAs            | sameAs    | 1         | 1               | rdfs:Resource     | true       |
    And exists a facet with id "/datasets/mixed/classes/foaf:Person/facets/foaf:name"
    And The retrieved facet is
      | uri                                             | labels    | timesUsed | differentValues | range             | relation   |
      | http://xmlns.com/foaf/0.1/name                  | name      | 3         | 3               | xsd:string        | false      |
    And I retrieve facet range "/datasets/mixed/classes/foaf:Person/facets/space:performed/ranges/space:MissionRole" values
      | value                                                                         | count | curie                     | uri                                                                           | labels                        |
      | http://data.kasabi.com/dataset/nasa/mission/apollo-13/role/commander          | 1     | kasabi:commander          | http://data.kasabi.com/dataset/nasa/mission/apollo-13/role/commander          | Comandante de MisiÃ³n Apollo 13@es \|\| Comandante de la MisiÃ³n Apollo 13@es \|\| Apollo 13 Mission Commander |
      | http://data.kasabi.com/dataset/nasa/mission/apollo-13/role/lunar-module-pilot | 1     | kasabi:lunar-module-pilot | http://data.kasabi.com/dataset/nasa/mission/apollo-13/role/lunar-module-pilot | Piloto del MÃ³dulo Lunar Apollo 13@es \|\| Apollo 13 Lunar Module Pilot@en |

  Scenario: Change the dataset graph to another of the server graphs and extract classes
    Given The following data graphs are set for dataset "mixed"
      | http://rhizomik.net/dataset/got                 |
    When I extract the classes from dataset "mixed"
    Then The retrieved classes are
      | uri                                             | labels             | instanceCount |
      | http://dbpedia.org/ontology/FictionalCharacter  | FictionalCharacter | 916           |
      | http://dbpedia.org/ontology/Noble               | Noble              | 430           |
      | http://dbpedia.org/ontology/Book                | Book               | 5             |
      | http://dbpedia.org/ontology/Organisation        | Organisation       | 11            |
    And exists a class with id "/datasets/mixed/classes/dbo:Noble"

  Scenario: Change the dataset graph to another of the server graphs and extract facets
    Given The following data graphs are set for dataset "mixed"
      | http://rhizomik.net/dataset/got                 |
    And I create a class in dataset "mixed" with URI "http://dbpedia.org/ontology/Noble", label "Noble" and instance count 430
    When I extract the facets for class "dbo:Noble" in dataset "mixed"
    Then The retrieved facets are
      | uri                                            | labels          | timesUsed  | differentValues | range | relation |
      | http://www.w3.org/2000/01/rdf-schema#label     | label           | 430  | 430 | xsd:string      | false |
      | http://www.w3.org/2000/01/rdf-schema#comment   | comment         | 50   | 50  | xsd:string      | false |
      | http://xmlns.com/foaf/0.1/depiction            | depiction       | 50   | 50  | rdfs:Resource   | true  |
      | http://mydomain.org/ontology/appearsIn         | appearsIn       | 761  | 5   | dbo:Book        | true  |
      | http://dbpedia.org/ontology/allegiance         | allegiance      | 337  | 11  | dbo:Organisation| true  |
      | http://dbpedia.org/ontology/lastAppearance     | lastAppearance  | 118  | 5   | dbo:Book        | true  |
      | http://www.w3.org/1999/02/22-rdf-syntax-ns#type| type            | 860  | 2   | rdfs:Resource   | true  |
      | http://dbpedia.org/property/genre              | genre           | 430  | 2   | xsd:string      | false |
      | http://dbpedia.org/ontology/deathDate          | deathDate       | 118  | 4   | xsd:gYear       | false |
      | http://mydomain.org/ontology/deathChapter      | deathChapter    | 109  | 47  | xsd:int         | false |
      | http://www.w3.org/2002/07/owl#sameAs           | sameAs          | 28   | 28  | rdfs:Resource   | true  |
      | http://mydomain.org/ontology/bookIntroChapter  | bookIntroChapter| 424  | 69  | xsd:int         | false |
      | http://www.w3.org/2004/02/skos/core#altLabel   | altLabel        | 50   | 50  | xsd:string      | false |
      | http://xmlns.com/foaf/0.1/name                 | name            | 430  | 430 | xsd:string      | false |
    And exists a facet with id "/datasets/mixed/classes/dbo:Noble/facets/dbo:allegiance"
    And The retrieved facet is
      | uri                                    | labels     | timesUsed | differentValues | range            | relation |
      | http://dbpedia.org/ontology/allegiance | allegiance | 337	    | 11	          | dbo:Organisation | true     |

  Scenario: Recompute classes after changing the dataset graph and clearing dataset classes
    Given I extract the classes from dataset "mixed"
    And The following data graphs are set for dataset "mixed"
      | http://rhizomik.net/dataset/got                 |
    When I set the dataset "mixed" classes to
      | uri                                             | label              | instanceCount |
    And I extract the classes from dataset "mixed"
    Then The retrieved classes are
      | uri                                             | labels             | instanceCount |
      | http://dbpedia.org/ontology/FictionalCharacter  | FictionalCharacter | 916           |
      | http://dbpedia.org/ontology/Noble               | Noble              | 430           |
      | http://dbpedia.org/ontology/Book                | Book               | 5             |
      | http://dbpedia.org/ontology/Organisation        | Organisation       | 11            |
    And exists a class with id "/datasets/mixed/classes/dbo:FictionalCharacter"
