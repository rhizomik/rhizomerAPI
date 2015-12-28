# Created by http://rhizomik.net/~roberto/

Feature: Create server pond
  In order to define a subset of data to explore
  As a data manager
  I want to create a pond of graphs from the associated server

  Scenario: create pond with no server or graphs and default values
    When a manager creates a pond with id "vegetables"
    Then the response status is 201
    And there is pond with URI "http://localhost/ponds/vegetables"



   # | id          | pondGraphs | server | classes | queryType | inferenceEnabled | sampleSize | coverage |
   # | vegetables  | null       | null   | null    | SIMPLE    | true             | 0          | 0        |