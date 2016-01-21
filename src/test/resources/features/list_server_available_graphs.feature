# Created by http://rhizomik.net/~roberto/

Feature: List server available graphs
  In order to know the available data in a dataset server
  As a data manager
  I want to list all the data graphs in the server

  Background: Existing dataset in local server storing file data
    Given There is a dataset with id "apollo13l"
    And The dataset "apollo13l" has a mock server
    And The dataset "apollo13l" server stores data
      | data                            | graph                                     |
      | data/nasa-apollo13.ttl          | http://rhizomik.net/dataset/apollo13-l    |
      | data/nasa-apollo.ttl            | http://rhizomik.net/dataset/apollo-l      |

  Scenario: The available graphs for the dataset server are those stored
    When I list the graphs in dataset "apollo13l" server
    Then The retrieved graphs are
      | http://rhizomik.net/dataset/apollo13-l    |
      | http://rhizomik.net/dataset/apollo-l      |
