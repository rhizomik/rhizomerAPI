# Created by http://rhizomik.net/~roberto/

Feature: Manage dataset ontologies
  In order to control dataset inference to facilitate data explorations
  As a data manager
  I want to manage the set of ontologies associated to a dataset

  Background: Existing dataset with classes and facets
    Given I login as "user" with password "password"
    And There is a new dataset by "user" with id "apollo13"
    And The dataset "apollo13" has a mock server
    And The server for dataset "apollo13" stores data
      | data                            | graph                                 |
      | data/nasa-schema.ttl            | http://rhizomik.net/schema/nasa       |
      | data/foaf.rdf                   | http://xmlns.com/foaf/0.1/            |
    And I add the graphs to the dataset "apollo13" ontologies
      | http://rhizomik.net/schema/nasa            |
    And The size of dataset "apollo13" ontology graphs is 36

  Scenario: Add an ontology to a dataset
    When I add the graphs to the dataset "apollo13" ontologies
      | http://xmlns.com/foaf/0.1/      |
    Then the response status is 201
    And The following ontologies are defined for the dataset "apollo13"
      | http://rhizomik.net/schema/nasa            |
      | http://xmlns.com/foaf/0.1/                 |
    And The size of dataset "apollo13" ontology graphs is 667

  Scenario: Set the dataset ontologies
    When The following ontology graphs are set for dataset "apollo13"
      | http://xmlns.com/foaf/0.1/                 |
    Then the response status is 200
    And The following ontologies are defined for the dataset "apollo13"
      | http://xmlns.com/foaf/0.1/                 |
    And The size of dataset "apollo13" ontology graphs is 631

  Scenario: Clear dataset ontologies
    When The following ontology graphs are set for dataset "apollo13"
      |                                 |
    Then the response status is 200
    And The following ontologies are defined for the dataset "apollo13"
      |                                 |
    And The size of dataset "apollo13" ontology graphs is 0
