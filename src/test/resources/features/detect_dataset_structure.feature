# Created by http://rhizomik.net/~roberto/

Feature: Detect dataset structure
  In order to explore the structure of a dataset
  As a data manager
  I want to detect all the classes defined in the dataset and their facets

  Background: Existing dataset in local server storing file data
    Given There is a dataset with id "apollo13s"
    And The dataset "apollo13s" has a mock server
    And The dataset "apollo13s" server stores data
      | data                            | graph                                     |
      | data/nasa-apollo13.ttl          | http://rhizomik.net/dataset/apollo13s     |
    When I add the graphs to the dataset "apollo13s"
      | http://rhizomik.net/dataset/apollo13s      |
    And The query type for dataset "apollo13s" is set to "FULL"
    And The inference for dataset "apollo13s" is set to "false"

  Scenario: The extracted classes are those instantiated in the dataset
    When I extract the classes from dataset "apollo13s"
    Then The retrieved classes are
      | uri                                           | label       | instanceCount |
      | http://xmlns.com/foaf/0.1/Person              | Person      | 2             |
      | http://purl.org/net/schemas/space/MissionRole | MissionRole | 2             |
      | http://purl.org/net/schemas/space/Mission     | Mission     | 1             |
    And exists a class with id "/datasets/apollo13s/classes/foaf:Person"

  Scenario: The extracted facets for an existing class are those instantiated in the dataset
    Given I create a class in dataset "apollo13s" with URI "http://xmlns.com/foaf/0.1/Person", label "Person" and instance count 2
    When I extract the facets for class "foaf:Person" in dataset "apollo13s"
    Then The retrieved facets are
      | uri                                             | label     | uses    | differentValues | ranges            | relation   |
      | http://xmlns.com/foaf/0.1/name                  | name      | 3       | 3               | xsd:string        | false      |
      | http://purl.org/net/schemas/space/performed     | performed | 2       | 2               | space:MissionRole | true       |
      | http://www.w3.org/1999/02/22-rdf-syntax-ns#type | type      | 2       | 1               | rdfs:Resource     | true       |
      | http://www.w3.org/2002/07/owl#sameAs            | sameAs    | 1       | 1               | rdfs:Resource     | true       |
    And exists a facet with id "/datasets/apollo13s/classes/foaf:Person/facets/foaf:name"
    And The retrieved facet is
      | uri                                             | label     | uses    | differentValues | ranges            | relation   |
      | http://xmlns.com/foaf/0.1/name                  | name      | 3       | 3               | xsd:string        | false      |
