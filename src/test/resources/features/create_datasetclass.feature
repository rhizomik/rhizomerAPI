# Created by http://rhizomik.net/~roberto/

Feature: Create classes in a dataset
  In order to explore the structure of a dataset
  As a data manager
  I want to identify the classes in it

  Background: Existing dataset
    Given There is a new dataset by "user" with id "vegetables"

  Scenario: manually define a class
    Given I login as "user" with password "password"
    When I create a class in dataset "vegetables" with URI "http://examples.org#Potato", label "Potato" and instance count 1
    Then the response status is 201
    And exists a class with id "/datasets/vegetables/classes/examples:Potato"
    And The retrieved class is
      | id                                           | labels      | instanceCount | uri                         | curie           |
      | /datasets/vegetables/classes/examples:Potato | Potato      | 1             | http://examples.org#Potato  | examples:Potato |

  Scenario: manually define repeated class
    Given I login as "user" with password "password"
    And a class in dataset "vegetables" with URI "http://examples.org#Tomato", label "Tomato" and instance count 1
    When I create a class in dataset "vegetables" with URI "http://examples.org#Tomato", label "tomato2" and instance count 2
    Then the response status is 409 and message contains "Class with URI 'http://examples.org#Tomato' already exists in Dataset 'vegetables'"
    And exists a class with id "/datasets/vegetables/classes/examples:Tomato"
    And The retrieved class is
      | id                                           | labels      | instanceCount | uri                         | curie           |
      | /datasets/vegetables/classes/examples:Tomato | Tomato      | 1             | http://examples.org#Tomato  | examples:Tomato |
