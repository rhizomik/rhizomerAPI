# Created by http://rhizomik.net/~roberto/

Feature: Create server dataset
  In order to define a subset of data to explore
  As a data manager
  I want to create a dataset of graphs from the associated server

  Scenario: create dataset with no server or graphs and default values
    When I create a dataset with id "vegetables"
    Then the response status is 201
    And exists a dataset with id "vegetables"