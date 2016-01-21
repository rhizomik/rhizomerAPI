# Created by http://rhizomik.net/~roberto/

Feature: Remove dataset
  In order to remove no longer needed dataset explorations
  As a data manager
  I want to delete a dataset and all its detected classes and facets

  Background: Existing dataset with classes and facets
    Given There is a dataset with id "apollo13r"
    And The query type for dataset "apollo13r" is set to "FULL"
    And The dataset "apollo13r" has a mock server
    And The dataset "apollo13r" server stores data
      | data                            | graph                                  |
      | data/nasa-apollo13.ttl          | http://rhizomik.net/dataset/apollo13r  |
    When I add the graphs to the dataset "apollo13r"
      | http://rhizomik.net/dataset/apollo13r    |
    When I extract the classes from dataset "apollo13r"
    When I extract the facets for class "foaf:Person" in dataset "apollo13r"

  Scenario: dataset is deleted together with its classes and facets
    When I delete a dataset with id "apollo13r"
    Then the response status is 200
    And There is no dataset with id "apollo13r"
    And There is no class "foaf:Person" in dataset "apollo13r"
    And There is no facet "foaf:name" for class "foaf:Person" in dataset "apollo13r"