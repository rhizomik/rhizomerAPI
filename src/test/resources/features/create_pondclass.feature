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
    And created class with href "http://localhost/classes/examples_potato@vegetables"