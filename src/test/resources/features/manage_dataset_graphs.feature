# Created by http://rhizomik.net/~roberto/

Feature: Manage dataset graphs
  In order to control the data to be considered for exploration
  As a data manager
  I want to manage the set of data graphs associated to a dataset

  Background: Existing dataset with local server and one graph with data
    Given I login as "user" with password "password"
    And There is a new dataset by "user" with id "apollo13"
    And The dataset "apollo13" has a mock server
    And The server for dataset "apollo13" stores data
      | data                            | graph                                  |
      | data/nasa-apollo13.ttl          | http://rhizomik.net/data/nasa-apollo13 |
      | data/nasa-apollo.ttl            | http://rhizomik.net/data/nasa-apollo   |
    And The following data graphs are set for dataset "apollo13"
      |                                           |
    And I add the graphs to the dataset "apollo13"
      | http://rhizomik.net/data/nasa-apollo13    |
    And The inference for dataset "apollo13" is set to "false"
    And The size of dataset "apollo13" data graphs is 33

  Scenario: Add a graph to a dataset
    When I add the graphs to the dataset "apollo13"
      | http://rhizomik.net/data/nasa-apollo      |
    Then the response status is 201
    And The following data graphs are defined for the dataset "apollo13"
      | http://rhizomik.net/data/nasa-apollo13    |
      | http://rhizomik.net/data/nasa-apollo      |
    And The size of dataset "apollo13" data graphs is 1678

  Scenario: Set the dataset graphs
    When The following data graphs are set for dataset "apollo13"
      | http://rhizomik.net/data/nasa-apollo      |
    Then the response status is 200
    And The following data graphs are defined for the dataset "apollo13"
      | http://rhizomik.net/data/nasa-apollo      |
    And The size of dataset "apollo13" data graphs is 1645

  Scenario: Clear dataset graphs
    When The following data graphs are set for dataset "apollo13"
      |                                 |
    Then the response status is 200
    And The following data graphs are defined for the dataset "apollo13"
      |                                 |
    And The size of dataset "apollo13" data graphs is 0
