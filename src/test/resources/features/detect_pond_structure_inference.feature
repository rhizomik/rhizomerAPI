# Created by http://rhizomik.net/~roberto/

Feature: Detect pond structure considering inference
  In order to explore the structure of pond subset of data
  As a data manager
  I want to detect all the classes defined in the dataset and their facets considering inference

  Background: Existing pond in local server storing file data
    Given There is a pond "apollo13i" on a local server storing "data/nasa-apollo13.ttl" in graph "http://rhizomik.net/pond/apollo13i"
    And The following ontologies are set for pond "apollo13i"
      | data/nasa-schema.ttl                                                            |
    And The query type for pond "apollo13i" is set to "FULL"
    And The inference for pond "apollo13i" is set to "true"

  Scenario: The extracted classes are those instantiated by the data
    When I extract the classes from pond "apollo13i"
    Then The retrieved classes are
      | uri                                           | label          | instanceCount |
      | http://xmlns.com/foaf/0.1/Person              | Person         | 2             |
      | http://purl.org/net/schemas/space/MissionRole | MissionRole    | 2             |
      | http://purl.org/net/schemas/space/Mission     | Mission        | 1             |
      | http://purl.org/net/schemas/space/Role        | Role           | 2             |
    And exists a class with id "/ponds/apollo13i/classes/space:Role"

  Scenario: The extracted facets for an existing class are those instantiated by the data
    Given I create a class in pond "apollo13i" with URI "http://xmlns.com/foaf/0.1/Person", label "Person" and instance count 2
    When I extract the facets for class "foaf:Person" in pond "apollo13i"
    Then The retrieved facets are
      | uri                                             | label     | uses    | differentValues | ranges            | relation   |
      | http://xmlns.com/foaf/0.1/name                  | name      | 3       | 3               | xsd:string        | false      |
      | http://purl.org/net/schemas/space/performed     | performed | 2       | 2               | space:MissionRole | true       |
      | http://www.w3.org/1999/02/22-rdf-syntax-ns#type | type      | 2       | 1               | rdfs:Resource     | true       |
      | http://www.w3.org/2002/07/owl#sameAs            | sameAs    | 1       | 1               | rdfs:Resource     | true       |
    And exists a facet with id "/ponds/apollo13i/classes/foaf:Person/facets/foaf:name"
    And The retrieved facet is
      | uri                                             | label     | uses    | differentValues | ranges            | relation   |
      | http://xmlns.com/foaf/0.1/name                  | name      | 3       | 3               | xsd:string        | false      |
