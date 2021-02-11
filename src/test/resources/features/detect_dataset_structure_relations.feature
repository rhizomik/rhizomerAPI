# Created by http://rhizomik.net/~roberto/

Feature: Detect dataset structure as relationships
  In order to explore the structure of a dataset as a network
  As a data manager
  I want to detect all the classes defined in the dataset and the relations among them

  Background: Existing dataset in local server storing file data
    Given I login as "user" with password "password"
    And There is a new dataset by "user" with id "mixed"
    And The dataset "mixed" has a mock server
    And The server for dataset "mixed" stores data
      | data                            | graph                                 |
      | data/nasa-apollo13.ttl          | http://rhizomik.net/dataset/apollo13  |
      | data/got.ttl                    | http://rhizomik.net/dataset/got       |
    And The following data graphs are set for dataset "mixed"
      | http://rhizomik.net/dataset/apollo13          |
    And The query type for dataset "mixed" is set to "DETAILED"
    And The inference for dataset "mixed" is set to "false"

  Scenario: The extracted relations for an existing class are those instantiated in the dataset
    Given I create a class in dataset "mixed" with URI "http://xmlns.com/foaf/0.1/Person", label "Person" and instance count 2
    When I extract the relations for class "foaf:Person" in dataset "mixed"
    Then The retrieved relationships are
      | classCurie      | propertyCurie      | rangeCurie         | uses |
      | foaf:Person     | space:performed    | space:MissionRole  |   2  |
      | foaf:Person     | owl:sameAs         | rdfs:Resource      |   1  |
      | foaf:Person     | rdf:type           | rdfs:Resource      |   2  |

  Scenario: Change the dataset graph to another of the server graphs and extract relations
    Given The following data graphs are set for dataset "mixed"
      | http://rhizomik.net/dataset/got                 |
    And I create a class in dataset "mixed" with URI "http://dbpedia.org/ontology/Noble", label "Noble" and instance count 430
    When I extract the relations for class "dbo:Noble" in dataset "mixed"
    Then The retrieved relationships are
      | classCurie      | propertyCurie      | rangeCurie         | uses |
      | dbo:Noble       | foaf:depiction     | rdfs:Resource      |  50  |
      | dbo:Noble       | mydomain:appearsIn | dbo:Book           | 761  |
      | dbo:Noble       | dbo:allegiance     | dbo:Organisation   | 337  |
      | dbo:Noble       | dbo:lastAppearance | dbo:Book           | 118  |
      | dbo:Noble       | rdf:type           | rdfs:Resource      | 860  |
      | dbo:Noble       | owl:sameAs         | rdfs:Resource      |  28  |
