# Created by http://rhizomik.net/~roberto/

Feature: List server available graphs
  In order to know the available data in a dataset server
  As a data manager
  I want to list all the data graphs in the server

  Background: Existing dataset in local server storing file data
    Given I login as "user" with password "password"
    And There is a new dataset with id "apollo13"
    And The dataset "apollo13" has a mock server
    And The server for dataset "apollo13" stores data
      | data                            | graph                                     |
      | data/nasa-apollo13.ttl          | http://rhizomik.net/dataset/apollo13      |
      | data/nasa-apollo.ttl            | http://rhizomik.net/dataset/apollo        |

  Scenario: The available graphs for the dataset server are those stored
    When I list the graphs in dataset "apollo13" server
    Then The retrieved graphs are
      | http://rhizomik.net/dataset/apollo13      |
      | http://rhizomik.net/dataset/apollo        |
