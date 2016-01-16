# Created by http://rhizomik.net/~roberto/

Feature: Manage pond ontologies
  In order to control dataset inference to facilitate data explorations
  As a data manager
  I want to manage the set of ontologies associated to a pond

  Background: Existing pond with classes and facets
    Given There is a pond "apollo13o" on a local server storing "data/nasa-apollo13.ttl" in graph "http://rhizomik.net/pond/apollo13o"
    And The following ontologies are set for pond "apollo13o"
      | data/nasa-schema.ttl            |
    And The size of pond "apollo13o" ontologies graph is 27

  Scenario: Add an ontology to a pond
    When I add ontologies to the pond "apollo13o"
      | data/foaf.rdf                   |
    Then the response status is 201
    And The following ontologies are defined for the pond "apollo13o"
      | data/nasa-schema.ttl            |
      | data/foaf.rdf                   |
    And The size of pond "apollo13o" ontologies graph is 658

  Scenario: Set the pond ontologies
    When The following ontologies are set for pond "apollo13o"
      | data/foaf.rdf                   |
    Then the response status is 200
    And The following ontologies are defined for the pond "apollo13o"
      | data/foaf.rdf                   |
    And The size of pond "apollo13o" ontologies graph is 631

  Scenario: Clear pond ontologies
    When The following ontologies are set for pond "apollo13o"
      |                                 |
    Then the response status is 200
    And The following ontologies are defined for the pond "apollo13o"
      |                                 |
    And The size of pond "apollo13o" ontologies graph is 0
