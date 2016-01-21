# Created by http://rhizomik.net/~roberto/
@remote
Feature: Detect dataset structure in remote server
  In order to explore the structure of dataset subset of data from remote server
  As a data manager
  I want to detect all the classes defined in the dataset and their facets

  Background: Existing dataset on remote server
    Given There is a dataset with id "pht"
    And The dataset "pht" server is set to "http://virtuoso.udl.cat:8890/sparql"
    When I add the graphs to the dataset "pht"
      | http://rhizomik.net/PlantHealthThreats        |
    And The query type for dataset "pht" is set to "FULL"
    And The inference for dataset "pht" is set to "false"

  Scenario: The extracted classes are those instantiated by the data
    When I extract the classes from dataset "pht"
    Then The retrieved classes are
      | uri                                                                     | label                 | instanceCount |
      | http://rhizomik.net/ontologies/PlantHealthThreats#Shape	                | Shape                 | 15            |
      | http://purl.obolibrary.org/obo/PO_0025131	                            | PO_0025131            | 31            |
      | http://rhizomik.net/ontologies/PlantHealthThreats#ExternalAgent         | ExternalAgent         | 2             |
      | http://rhizomik.net/ontologies/PlantHealthThreats#Symptom               | Symptom               | 107           |
      | http://rhizomik.net/ontologies/RemoteSources#RemoteSource               | RemoteSource          | 2             |
      | http://rhizomik.net/ontologies/PlantHealthThreats#SymptomExpresion      | SymptomExpresion      | 67            |
      | http://rhizomik.net/ontologies/PlantHealthThreats#DiseaseOrPest         | DiseaseOrPest         | 115           |
      | http://www.w3.org/2000/01/rdf-schema#Resource                           | Resource              | 3             |
      | http://rhizomik.net/ontologies/PlantHealthThreats#TransmissionMechanism | TransmissionMechanism | 16            |
      | http://purl.uniprot.org/core/Taxon                                      | Taxon                 | 28            |
      | http://rhizomik.net/ontologies/PlantHealthThreats#Vector                | Vector                | 21            |
    And exists a class with id "/datasets/pht/classes/uniprot_1:Taxon"

  Scenario: The extracted facets for an existing class are those instantiated in the dataset
    Given I create a class in dataset "pht" with URI "http://rhizomik.net/ontologies/PlantHealthThreats#Symptom", label "Symptom" and instance count 107
    When I extract the facets for class "rhizomik:Symptom" in dataset "pht"
    Then The retrieved facets are
      | uri                                             | label     | uses    | differentValues | range             | relation   |
      | http://www.w3.org/2000/01/rdf-schema#label      | label     | 162     | 162             | xsd:string        | false      |
      | http://www.w3.org/1999/02/22-rdf-syntax-ns#type | type      | 107     | 1               | rdfs:Resource     | true       |
    And exists a facet with id "/datasets/pht/classes/rhizomik:Symptom/facets/rdfs:label"
    And The retrieved facet is
      | uri                                             | label     | uses    | differentValues | range             | relation   |
      | http://www.w3.org/2000/01/rdf-schema#label      | label     | 162     | 162             | xsd:string        | false      |
