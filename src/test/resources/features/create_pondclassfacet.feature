# Created by http://rhizomik.net/~roberto/

Feature: Create class facets in a pond
  In order to explore the structure of pond subset of data
  As a data manager
  I want to identify the classes facets in it

  Background: Existing pond
    Given a pond with id "vegetables"
    And a class in pond "vegetables" with URI "http://examples.org#Potato", label "Potato" and instance count 1

  Scenario: Manually define a class facet
    When I create facets for class "examples:Potato" in pond "vegetables"
      | uri                             | label   | uses    | differentValues | relation | ranges     |
      | http://xmlns.com/foaf/0.1/name  | name    | 1       | 1               | false    | xsd:string |
    Then the response status is 201
    And exists a facet with id "/ponds/vegetables/classes/examples:Potato/facets/foaf:name"
    And The retrieved facet is
      | uri                                             | label     | uses    | differentValues | ranges            | relation   |
      | http://xmlns.com/foaf/0.1/name                  | name      | 1       | 1               | xsd:string        | false      |