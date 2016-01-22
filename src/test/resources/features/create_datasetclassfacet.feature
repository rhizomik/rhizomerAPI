# Created by http://rhizomik.net/~roberto/

Feature: Create class facets in a dataset
  In order to explore the structure of a dataset
  As a data manager
  I want to identify the class facets in it

  Background: Existing dataset
    Given There is a new dataset with id "vegetables"
    And a class in dataset "vegetables" with URI "http://examples.org#Potato", label "Potato" and instance count 1

  Scenario: Manually define a class facet
    When I create facets for class "examples:Potato" in dataset "vegetables"
      | uri                             | label   |
      | http://xmlns.com/foaf/0.1/name  | name    |
    And I create ranges for facet "foaf:name" of class "examples:Potato" in dataset "vegetables"
      | uri                                     | label   | uses    | differentValues | allLiteral |
      | http://www.w3.org/2001/XMLSchema#string | name    | 1       | 1               | true       |
    Then the response status is 201
    And exists a facet with id "/datasets/vegetables/classes/examples:Potato/facets/foaf:name"
    And The retrieved facet is
      | uri                             | label   | uses    | differentValues | relation | range      |
      | http://xmlns.com/foaf/0.1/name  | name    | 1       | 1               | false    | xsd:string |