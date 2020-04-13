# Created by http://rhizomik.net/~roberto/
@remote
Feature: Detect dataset structure in remote Virtuoso server
  In order to explore the structure of dataset subset of data from remote Virtuoso server
  As a data manager
  I want to detect all the classes defined in the dataset and their facets

  Background: Existing dataset in remote server
    Given I login as "user" with password "password"
    And There is a new dataset by "user" with id "esdbpedia"
    And Add endpoint to dataset "esdbpedia"
      | QueryEndPoint     | http://es.dbpedia.org/sparql |
    And The following data graphs are set for dataset "esdbpedia"
      | http://www.openlinksw.com/schemas/virtrdf#       |
    And The query type for dataset "esdbpedia" is set to "DETAILED"

  Scenario: The extracted classes are those instantiated by the data
    When I extract the classes from dataset "esdbpedia"
    Then The retrieved classes are
      | uri                                                              | label                  | instanceCount |
      | http://www.openlinksw.com/schemas/virtrdf#QuadMapFormat          | QuadMapFormat          | 126           |
      | http://www.openlinksw.com/schemas/virtrdf#QuadStorage            | QuadStorage            | 3             |
      | http://www.openlinksw.com/schemas/virtrdf#array-of-QuadMapFormat | array-of-QuadMapFormat | 126           |
      | http://www.openlinksw.com/schemas/virtrdf#QuadMapFText           | QuadMapFText           | 4             |
      | http://www.openlinksw.com/schemas/virtrdf#QuadMap                | QuadMap                | 2             |
      | http://www.openlinksw.com/schemas/virtrdf#array-of-QuadMapColumn | array-of-QuadMapColumn | 8             |
      | http://www.openlinksw.com/schemas/virtrdf#array-of-QuadMapATable | array-of-QuadMapATable | 2             |
      | http://www.openlinksw.com/schemas/virtrdf#QuadMapATable          | QuadMapATable          | 2             |
      | http://www.openlinksw.com/schemas/virtrdf#array-of-string        | array-of-string        | 2             |
      | http://www.openlinksw.com/schemas/virtrdf#array-of-QuadMap       | array-of-QuadMap       | 3             |
      | http://www.openlinksw.com/schemas/virtrdf#QuadMapValue           | QuadMapValue           | 8             |
      | http://www.openlinksw.com/schemas/virtrdf#QuadMapColumn          | QuadMapColumn          | 8             |
    And exists a class with id "/datasets/esdbpedia/classes/openlinks:QuadMap"

  Scenario: The extracted facets for an existing class are those instantiated in the dataset
    Given I create a class in dataset "esdbpedia" with URI "http://www.openlinksw.com/schemas/virtrdf#QuadMap", label "QuadMap" and instance count 2
    When I extract the facets for class "openlinks:QuadMap" in dataset "esdbpedia"
    Then The retrieved facets are
      | uri                                                      | label             | timesUsed | differentValues | range                  | relation |
      | http://www.openlinksw.com/schemas/virtrdf#qmPredicateMap | qmPredicateMap    | 2         | 2               | openlinks:QuadMapValue | true	   |
      | http://www.openlinksw.com/schemas/virtrdf#qmMatchingFlags| qmMatchingFlags   | 2         | 1               | rdfs:Resource          | true	   |
      | http://www.openlinksw.com/schemas/virtrdf#qmObjectMap    | qmObjectMap       | 2         | 2               | openlinks:QuadMapValue | true	   |
      | http://www.w3.org/1999/02/22-rdf-syntax-ns#type          | type              | 2         | 1               | rdfs:Resource          | true	   |
      | http://www.openlinksw.com/schemas/virtrdf#qmTableName    | qmTableName       | 2         | 2               | xsd:string             | false	   |
      | http://www.openlinksw.com/schemas/virtrdf#qmGraphMap     | qmGraphMap        | 2         | 2               | openlinks:QuadMapValue | true	   |
      | http://www.openlinksw.com/schemas/virtrdf#qmSubjectMap   | qmSubjectMap      | 2         | 2               | openlinks:QuadMapValue | true	   |
    And exists a facet with id "/datasets/esdbpedia/classes/openlinks:QuadMap/facets/openlinks:qmPredicateMap"
    And The retrieved facet is
      | uri                                                      | label             | timesUsed | differentValues | range                  | relation |
      | http://www.openlinksw.com/schemas/virtrdf#qmPredicateMap | qmPredicateMap    | 2         | 2               | openlinks:QuadMapValue | true	   |
