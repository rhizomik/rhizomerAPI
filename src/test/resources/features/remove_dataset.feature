# Created by http://rhizomik.net/~roberto/

Feature: Remove dataset
  In order to remove no longer needed dataset explorations
  As a data manager
  I want to delete a dataset and all its detected classes and facets

  Background: Existing dataset with classes and facets
    Given I login as "user" with password "password"
    And There is a new dataset by "user" with id "apollo13"
    And The query type for dataset "apollo13" is set to "FULL"
    And The dataset "apollo13" has a mock server
    And The server for dataset "apollo13" stores data
      | data                            | graph                                 |
      | data/nasa-apollo13.ttl          | http://rhizomik.net/dataset/apollo13  |
    And I extract the classes from dataset "apollo13"
    And I extract the facets for class "foaf:Person" in dataset "apollo13"

  Scenario: dataset is deleted together with its classes and facets
    When I delete a dataset with id "apollo13"
    Then the response status is 200
    And There is no dataset with id "apollo13"
    And There is no class "foaf:Person" in dataset "apollo13"
    And There is no facet "foaf:name" for class "foaf:Person" in dataset "apollo13"