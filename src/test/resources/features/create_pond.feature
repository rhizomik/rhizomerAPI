# Created by http://rhizomik.net/~roberto/

Feature: Create server pond
  In order to define a subset of data to explore
  As a data manager
  I want to create a pond of graphs from the associated server

  Scenario: create pond with no server or graphs and default values
    When I create a pond with id "vegetables"
    Then the response status is 201
    And exists a pond with id "vegetables"