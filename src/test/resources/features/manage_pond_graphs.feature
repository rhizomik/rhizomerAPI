# Created by http://rhizomik.net/~roberto/

Feature: Manage pond graphs
  In order to control the data to be considered for exploration
  As a data manager
  I want to manage the set of data graphs associated to a pond

  Background: Existing pond with local server and one graph with data
    Given There is a pond "apollo13g" on a local server storing "data/nasa-apollo13.ttl" in graph "http://rhizomik.net/data/nasa-apollo13"
    And The inference for pond "apollo13g" is set to "false"
    And The pond "apollo13g" server stores data
      | data                            | graph                                 |
      | data/nasa-apollo.ttl            | http://rhizomik.net/data/nasa-apollo  |
    And The size of pond "apollo13g" data graphs is 23

  Scenario: Add a graph to a pond
    When I add the graphs to the pond "apollo13g"
      | http://rhizomik.net/data/nasa-apollo      |
    Then the response status is 201
    And The following data graphs are defined for the pond "apollo13g"
      | http://rhizomik.net/data/nasa-apollo13    |
      | http://rhizomik.net/data/nasa-apollo      |
    And The size of pond "apollo13g" data graphs is 1668

  Scenario: Set the pond graphs
    When The following data graphs are set for pond "apollo13g"
      | http://rhizomik.net/data/nasa-apollo      |
    Then the response status is 200
    And The following data graphs are defined for the pond "apollo13g"
      | http://rhizomik.net/data/nasa-apollo      |
    And The size of pond "apollo13g" data graphs is 1645

  Scenario: Clear pond graphs
    When The following data graphs are set for pond "apollo13g"
      |                                 |
    Then the response status is 200
    And The following data graphs are defined for the pond "apollo13g"
      |                                 |
    And The size of pond "apollo13g" data graphs is 0
