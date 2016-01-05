# Created by http://rhizomik.net/~roberto/

Feature: Detect classes in a pond
  In order to explore the structure of pond subset of data
  As a data manager
  I want to detect all the classes defined in it

  Background: Existing pond in local server storing file data
    Given There is a pond "apollo13" on a local server storing "data/nasa-apollo13.ttl" in graph "http://ontolake.net/data"
    And The inference for pond "apollo13" is set to "false"

  Scenario: The extracted classes are those instantiated by the data
    When I extract the classes from pond "apollo13"
    Then The list of classes in pond "apollo13" is
      | id                                        | label       | instanceCount | uri                                           |
      | /ponds/apollo13/classes/foaf:Person       | Person      | 2             | http://xmlns.com/foaf/0.1/Person              |
      | /ponds/apollo13/classes/space:Role        | Role        | 2             | http://purl.org/net/schemas/space/Role        |
      | /ponds/apollo13/classes/space:MissionRole | MissionRole | 2             | http://purl.org/net/schemas/space/MissionRole |
      | /ponds/apollo13/classes/space:Mission     | Mission     | 1             | http://purl.org/net/schemas/space/Mission     |
    And exists a class with id "/ponds/apollo13/classes/foaf:Person" and label "Person" and instance count 2
