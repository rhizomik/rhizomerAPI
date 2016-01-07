# Created by http://rhizomik.net/~roberto/

Feature: Create classes in a pond
  In order to explore the structure of pond subset of data
  As a data manager
  I want to identify the classes in it

  Background: Existing pond
    Given a pond with id "vegetables"

  Scenario: manually define a class
    When I create a class in pond "vegetables" with URI "http://examples.org#Potato", label "Potato" and instance count 1
    Then the response status is 201
    And exists a class with id "/ponds/vegetables/classes/examples:Potato"
    And The retrieved class is
      | id                                        | label       | instanceCount | uri                         |
      | /ponds/vegetables/classes/examples:Potato | Potato      | 1             | http://examples.org#Potato  |

  Scenario: manually define repeated class
    Given a class in pond "vegetables" with URI "http://examples.org#Tomato", label "Tomato" and instance count 2
    When I create a class in pond "vegetables" with URI "http://examples.org#Tomato", label "tomato2" and instance count 2
    Then the response status is 409 and message contains "Class with URI http://examples.org#Tomato already exists in Pond vegetables"
    And exists a class with id "/ponds/vegetables/classes/examples:Tomato"
    And The retrieved class is
      | id                                        | label       | instanceCount | uri                         |
      | /ponds/vegetables/classes/examples:Tomato | Tomato      | 2             | http://examples.org#Tomato  |