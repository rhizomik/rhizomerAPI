# Created by http://rhizomik.net/~roberto/

Feature: Detect dataset structure
  In order to explore the structure of a dataset
  As a data manager
  I want to detect all the classes defined in the dataset and their facets

  Background: Existing dataset in local server storing file data
    Given There is a new dataset with id "mixed"
    And The dataset "mixed" has a mock server
    And The dataset "mixed" server stores data
      | data                            | graph                                     |
      | data/nasa-apollo13.ttl          | http://rhizomik.net/dataset/apollo13      |
      | data/rdflicenses.ttl            | http://test.com/rdflicense                |
    When I add the graphs to the dataset "mixed"
      | http://rhizomik.net/dataset/apollo13          |
    And The query type for dataset "mixed" is set to "FULL"
    And The inference for dataset "mixed" is set to "false"

  Scenario: The extracted classes are those instantiated in the dataset
    When I extract the classes from dataset "mixed"
    Then The retrieved classes are
      | uri                                           | label       | instanceCount |
      | http://xmlns.com/foaf/0.1/Person              | Person      | 2             |
      | http://purl.org/net/schemas/space/MissionRole | MissionRole | 2             |
      | http://purl.org/net/schemas/space/Mission     | Mission     | 1             |
    And exists a class with id "/datasets/mixed/classes/foaf:Person"

  Scenario: The extracted facets for an existing class are those instantiated in the dataset
    Given I create a class in dataset "mixed" with URI "http://xmlns.com/foaf/0.1/Person", label "Person" and instance count 2
    When I extract the facets for class "foaf:Person" in dataset "mixed"
    Then The retrieved facets are
      | uri                                             | label     | uses    | differentValues | range             | relation   |
      | http://xmlns.com/foaf/0.1/name                  | name      | 3       | 3               | xsd:string        | false      |
      | http://purl.org/net/schemas/space/performed     | performed | 2       | 2               | space:MissionRole | true       |
      | http://www.w3.org/1999/02/22-rdf-syntax-ns#type | type      | 2       | 1               | rdfs:Resource     | true       |
      | http://www.w3.org/2002/07/owl#sameAs            | sameAs    | 1       | 1               | rdfs:Resource     | true       |
    And exists a facet with id "/datasets/mixed/classes/foaf:Person/facets/foaf:name"
    And The retrieved facet is
      | uri                                             | label     | uses    | differentValues | range             | relation   |
      | http://xmlns.com/foaf/0.1/name                  | name      | 3       | 3               | xsd:string        | false      |

  Scenario: Change the dataset graph to another of the server graphs and extract classes
    Given The following data graphs are set for dataset "mixed"
      | http://test.com/rdflicense                      |
    When I extract the classes from dataset "mixed"
    Then The retrieved classes are
      | uri                                             | label       | instanceCount |
      | http://www.w3.org/2000/01/rdf-schema#Resource   | Resource    | 177           |
      | http://www.w3.org/ns/odrl/2/Constraint          | Constraint  | 10            |
      | http://www.w3.org/ns/odrl/2/Prohibition         | Prohibition | 18            |
      | http://www.w3.org/ns/odrl/2/Policy              | Policy      | 175           |
      | http://www.w3.org/ns/odrl/2/Permission          | Permission  | 104           |
      | http://www.w3.org/ns/odrl/2/Duty                | Duty        | 104           |
    And exists a class with id "/datasets/mixed/classes/odrl:Policy"

  Scenario: Change the dataset graph to another of the server graphs and extract facets
    Given The following data graphs are set for dataset "mixed"
      | http://test.com/rdflicense                      |
    And I create a class in dataset "mixed" with URI "http://www.w3.org/ns/odrl/2/Policy", label "Policy" and instance count 175
    When I extract the facets for class "odrl:Policy" in dataset "mixed"
    Then The retrieved facets are
      | uri                                                 | label             | uses  | differentValues | range   | relation   |
      | http://www.w3.org/ns/odrl/2/prohibition	            |	prohibition	    |	41	|	41	|	odrl:Prohibition|	true	|
      | http://purl.org/dc/terms/publisher	                |	publisher	    |	168	|	30	|	xsd:string	    |	false	|
      | http://ns.inria.fr/l4lod/licensingTerms	            |	licensingTerms	|	1	|	1	|	rdfs:Resource	|	true	|
      | http://www.w3.org/2000/01/rdf-schema#comment	    |	comment	        |	2	|	2	|	xsd:string	    |	false	|
      | http://purl.org/dc/terms/alternative	            |	alternative	    |	16	|	16	|	xsd:string	    |	false	|
      | http://www.w3.org/2004/02/skos/core#related	        |	related	        |	2	|	1	|	rdfs:Resource	|	true	|
      | http://www.w3.org/ns/odrl/2/permission	            |	permission	    |	178	|	178	|	odrl:Permission	|	true	|
      | http://www.w3.org/1999/02/22-rdf-syntax-ns#type	    |	type	        |	175	|	1	|	rdfs:Resource	|	true	|
      | http://purl.org/dc/terms/language	                |	language	    |	174	|	33	|	rdfs:Resource	|	true	|
      | http://purl.org/NET/ms-rights#conditionsOfUse	    |	conditionsOfUse	|	26	|	10	|	rdfs:Resource	|	true	|
      | http://purl.org/dc/terms/source	                    |	source	        |	169	|	158	|	rdfs:Resource	|	true	|
      | http://www.w3.org/2000/01/rdf-schema#label	        |	label	        |	178	|	159	|	xsd:string	    |	false	|
      | http://www.w3.org/2000/01/rdf-schema#seeAlso	    |	seeAlso	        |	145	|	136	|	rdfs:Resource	|	true	|
      | http://purl.org/dc/terms/creator	                |	creator	        |	7	|	3	|	rdfs:Resource	|	true	|
      | http://www.w3.org/2000/01/rdf-schema#legalcode	    |	legalcode	    |	2	|	2	|	rdfs:Resource	|	true	|
      | http://purl.org/dc/terms/hasVersion	                |	hasVersion	    |	164	|	8	|	xsd:string	    |	false	|
      | http://purl.org/dc/terms/title	                    |	title	        |	20	|	16	|	xsd:string	    |	false	|
      | http://purl.org/NET/ms-rights#licenseClarinCategory	|	licenseClarinCategory |	1 |	1	|	rdfs:Resource	|	true	|
      | http://creativecommons.org/ns#jurisdiction	        |	jurisdiction	|	108	|	41	|	rdfs:Resource	|	true	|
      | http://xmlns.com/foaf/0.1/logo	                    |	logo	        |	15	|	13	|	rdfs:Resource	|	true	|
      | http://www.w3.org/ns/odrl/2/duty	                |	duty	        |	104	|	104	|	odrl:Duty	    |	true	|
      | http://www.w3.org/2002/07/owl#sameAs	            |	sameAs	        |	3	|	3	|	rdfs:Resource	|	true	|
      | http://purl.org/NET/ms-rights#licenseCategory	    |	licenseCategory	|	5	|	3	|	rdfs:Resource	|	true	|
    And exists a facet with id "/datasets/mixed/classes/odrl:Policy/facets/odrl:prohibition"
    And The retrieved facet is
      | uri                                             | label       | uses | differentValues | range            | relation   |
      | http://www.w3.org/ns/odrl/2/prohibition	        | prohibition | 41	 | 41	           | odrl:Prohibition | true       |

  Scenario: Recompute classes after changing the dataset graph and clearing dataset classes
    Given I extract the classes from dataset "mixed"
    And The following data graphs are set for dataset "mixed"
      | http://test.com/rdflicense                      |
    When I set the dataset "mixed" classes to
      | uri                                             | label       | instanceCount |
    And I extract the classes from dataset "mixed"
    Then The retrieved classes are
      | uri                                             | label       | instanceCount |
      | http://www.w3.org/2000/01/rdf-schema#Resource   | Resource    | 177           |
      | http://www.w3.org/ns/odrl/2/Constraint          | Constraint  | 10            |
      | http://www.w3.org/ns/odrl/2/Prohibition         | Prohibition | 18            |
      | http://www.w3.org/ns/odrl/2/Policy              | Policy      | 175           |
      | http://www.w3.org/ns/odrl/2/Permission          | Permission  | 104           |
      | http://www.w3.org/ns/odrl/2/Duty                | Duty        | 104           |
    And exists a class with id "/datasets/mixed/classes/odrl:Policy"