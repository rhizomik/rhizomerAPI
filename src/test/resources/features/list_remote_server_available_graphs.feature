# Created by http://rhizomik.net/~roberto/
@remote
Feature: List remote server available graphs
  In order to know the available data in a dataset remote server
  As a data manager
  I want to list all the data graphs in the server

  Background: Existing dataset in remote server
    Given I login as "user" with password "password"
    And There is a new dataset by "user" with id "esdbpedia"
    And Add endpoint to dataset "esdbpedia"
      | QueryEndPoint     | http://es.dbpedia.org/sparql            |

  Scenario: The available graphs for the dataset server are those stored
    When I list the graphs in dataset "esdbpedia" server
    Then The retrieved graphs are
      | http://www.openlinksw.com/schemas/virtrdf# |
      | http://es.dbpedia.org                      |
