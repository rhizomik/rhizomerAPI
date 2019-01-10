# Created by http://rhizomik.net/~roberto/

Feature: Manage dataset class facets
  In order to manage existing class facets in a dataset
  As a data manager
  I want to be able to set them to a provided set

  Background: Existing dataset with classes and facets
    Given I login as "user" with password "password"
    And There is a new dataset with id "apollo13"
    And The query type for dataset "apollo13" is set to "FULL"
    And The dataset "apollo13" has a mock server
    And The server for dataset "apollo13" stores data
      | data                            | graph                                 |
      | data/nasa-apollo13.ttl          | http://rhizomik.net/dataset/apollo13  |
    And I add the graphs to the dataset "apollo13"
      | http://rhizomik.net/dataset/apollo13    |
    And I extract the classes from dataset "apollo13"
    And I extract the facets for class "foaf:Person" in dataset "apollo13"

  Scenario: clear the facets for a class
    When I set the facets for class "foaf:Person" in dataset "apollo13" to
      | uri                             | label   |
    Then the response status is 200
    And The retrieved facets are
      | uri                             | label   | timesUsed | differentValues | relation | range         |

  Scenario: set the facets for a class
    When I set the facets for class "foaf:Person" in dataset "apollo13" to
      | uri                             | label   |
      | http://xmlns.com/foaf/0.1/name  | name    |
    Then the response status is 200
    And The retrieved facets are
      | uri                             | label   | timesUsed | differentValues | relation | range         |
      | http://xmlns.com/foaf/0.1/name  | name    | 0         | 0               | true     | rdfs:Resource |