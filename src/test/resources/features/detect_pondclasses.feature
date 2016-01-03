# Created by http://rhizomik.net/~roberto/

Feature: Detect classes in a pond
  In order to explore the structure of pond subset of data
  As a data manager
  I want to detect all the classes defined in it

  Background: Existing pond in local server storing file data
    Given There is a pond "apollo13" on a local server storing "data/nasa-apollo13.ttl" in graph "http://ontolake.net/data"
    #And The query type for pond "apollo13" is "FULL"
    #And The query sample size for pond "apollo13" is 10
    #And The samples coverage pond "apollo13" is 0.3

  Scenario: The extracted classes are those instantiated by the data
    When I extract the classes from pond "apollo13"
    Then The list of classes in pond "apollo13" is
      | id                                        | label       | instanceCount |
      | /ponds/apollo13/classes/foaf:Person       | Person      | 2             |
      | /ponds/apollo13/classes/space:Role        | Role        | 2             |
      | /ponds/apollo13/classes/space:MissionRole | MissionRole | 2             |
      | /ponds/apollo13/classes/space:Mission     | Mission     | 1             |
