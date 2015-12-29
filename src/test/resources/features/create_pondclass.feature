# Created by http://rhizomik.net/~roberto/

Feature: Create classes in a pond
  In order to explore the structure of pond subset of data
  As a data manager
  I want to identify the classes in it

  Background: Existing pond
    Given a pond with id "vegetables"

  Scenario: manually define a class
    When I create a class in pond "vegetables" with URI "http://examples.org#potato", label "potato" and instance count 1
    Then the response status is 201
    And exists a class with id "/ponds/vegetables/classes/examples:potato"

  Scenario: define again a class
    Given a class in pond "vegetables" with URI "http://examples.org#tomato", label "tomato" and instance count 2
    When I create a class in pond "vegetables" with URI "http://examples.org#tomato", label "tomato2" and instance count 2
    Then the response status is 409
    And exists a class with id "/ponds/vegetables/classes/examples:tomato"