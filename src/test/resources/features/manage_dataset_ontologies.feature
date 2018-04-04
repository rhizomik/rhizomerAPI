# Created by http://rhizomik.net/~roberto/

Feature: Manage dataset ontologies
  In order to control dataset inference to facilitate data explorations
  As a data manager
  I want to manage the set of ontologies associated to a dataset

  Background: Existing dataset with classes and facets
    Given I login as "user" with password "password"
    And There is a new dataset with id "apollo13"
    And The dataset "apollo13" has a mock server
    And The following ontologies are set for dataset "apollo13"
      | data/nasa-schema.ttl            |
    And The size of dataset "apollo13" ontologies graph is 27

  Scenario: Add an ontology to a dataset
    When I add ontologies to the dataset "apollo13"
      | data/foaf.rdf                   |
    Then the response status is 201
    And The following ontologies are defined for the dataset "apollo13"
      | data/nasa-schema.ttl            |
      | data/foaf.rdf                   |
    And The size of dataset "apollo13" ontologies graph is 658

  Scenario: Set the dataset ontologies
    When The following ontologies are set for dataset "apollo13"
      | data/foaf.rdf                   |
    Then the response status is 200
    And The following ontologies are defined for the dataset "apollo13"
      | data/foaf.rdf                   |
    And The size of dataset "apollo13" ontologies graph is 631

  Scenario: Clear dataset ontologies
    When The following ontologies are set for dataset "apollo13"
      |                                 |
    Then the response status is 200
    And The following ontologies are defined for the dataset "apollo13"
      |                                 |
    And The size of dataset "apollo13" ontologies graph is 0
