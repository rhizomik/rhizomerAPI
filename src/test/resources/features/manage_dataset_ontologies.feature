# Created by http://rhizomik.net/~roberto/

Feature: Manage dataset ontologies
  In order to control dataset inference to facilitate data explorations
  As a data manager
  I want to manage the set of ontologies associated to a dataset

  Background: Existing dataset with classes and facets
    Given There is a dataset with id "apollo13o"
    And The dataset "apollo13o" has a mock server
    And The following ontologies are set for dataset "apollo13o"
      | data/nasa-schema.ttl            |
    And The size of dataset "apollo13o" ontologies graph is 27

  Scenario: Add an ontology to a dataset
    When I add ontologies to the dataset "apollo13o"
      | data/foaf.rdf                   |
    Then the response status is 201
    And The following ontologies are defined for the dataset "apollo13o"
      | data/nasa-schema.ttl            |
      | data/foaf.rdf                   |
    And The size of dataset "apollo13o" ontologies graph is 658

  Scenario: Set the dataset ontologies
    When The following ontologies are set for dataset "apollo13o"
      | data/foaf.rdf                   |
    Then the response status is 200
    And The following ontologies are defined for the dataset "apollo13o"
      | data/foaf.rdf                   |
    And The size of dataset "apollo13o" ontologies graph is 631

  Scenario: Clear dataset ontologies
    When The following ontologies are set for dataset "apollo13o"
      |                                 |
    Then the response status is 200
    And The following ontologies are defined for the dataset "apollo13o"
      |                                 |
    And The size of dataset "apollo13o" ontologies graph is 0
