# Created by http://rhizomik.net/~roberto/

Feature: Manage dataset graphs
  In order to control the data to be considered for exploration
  As a data manager
  I want to manage the set of data graphs associated to a dataset

  Background: Existing dataset with local server and one graph with data
    Given There is a dataset with id "apollo13g"
    And The dataset "apollo13g" has a mock server
    And The dataset "apollo13g" server stores data
      | data                            | graph                                  |
      | data/nasa-apollo13.ttl          | http://rhizomik.net/data/nasa-apollo13 |
      | data/nasa-apollo.ttl            | http://rhizomik.net/data/nasa-apollo   |
    When I add the graphs to the dataset "apollo13g"
      | http://rhizomik.net/data/nasa-apollo13    |
    And The inference for dataset "apollo13g" is set to "false"
    And The size of dataset "apollo13g" data graphs is 23

  Scenario: Add a graph to a dataset
    When I add the graphs to the dataset "apollo13g"
      | http://rhizomik.net/data/nasa-apollo      |
    Then the response status is 201
    And The following data graphs are defined for the dataset "apollo13g"
      | http://rhizomik.net/data/nasa-apollo13    |
      | http://rhizomik.net/data/nasa-apollo      |
    And The size of dataset "apollo13g" data graphs is 1668

  Scenario: Set the dataset graphs
    When The following data graphs are set for dataset "apollo13g"
      | http://rhizomik.net/data/nasa-apollo      |
    Then the response status is 200
    And The following data graphs are defined for the dataset "apollo13g"
      | http://rhizomik.net/data/nasa-apollo      |
    And The size of dataset "apollo13g" data graphs is 1645

  Scenario: Clear dataset graphs
    When The following data graphs are set for dataset "apollo13g"
      |                                 |
    Then the response status is 200
    And The following data graphs are defined for the dataset "apollo13g"
      |                                 |
    And The size of dataset "apollo13g" data graphs is 0
