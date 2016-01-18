# Created by http://rhizomik.net/~roberto/
@remote
Feature: Detect pond structure in remote server
  In order to explore the structure of pond subset of data from remote server
  As a data manager
  I want to detect all the classes defined in the dataset and their facets

  Background: Existing pond on remote server
    Given There is a pond with id "pht"
    And The pond "pht" server is set to "http://virtuoso.udl.cat:8890/sparql"
    When I add the graphs to the pond "pht"
      | http://rhizomik.net/PlantHealthThreats        |
    And The query type for pond "pht" is set to "FULL"
    And The inference for pond "pht" is set to "false"

  Scenario: The extracted classes are those instantiated by the data
    When I extract the classes from pond "pht"
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
    And exists a class with id "/ponds/pht/classes/uniprot_1:Taxon"

#  Scenario: The extracted facets for an existing class are those instantiated by the data
#    Given I create a class in pond "pht" with URI "http://xmlns.com/foaf/0.1/Person", label "Person" and instance count 2
#    When I extract the facets for class "foaf:Person" in pond "pht"
#    Then The retrieved facets are
#      | uri                                             | label     | uses    | differentValues | ranges            | relation   |
#      | http://xmlns.com/foaf/0.1/name                  | name      | 3       | 3               | xsd:string        | false      |
#      | http://purl.org/net/schemas/space/performed     | performed | 2       | 2               | space:MissionRole | true       |
#      | http://www.w3.org/1999/02/22-rdf-syntax-ns#type | type      | 2       | 1               | rdfs:Resource     | true       |
#      | http://www.w3.org/2002/07/owl#sameAs            | sameAs    | 1       | 1               | rdfs:Resource     | true       |
#    And exists a facet with id "/ponds/pht/classes/foaf:Person/facets/foaf:name"
#    And The retrieved facet is
#      | uri                                             | label     | uses    | differentValues | ranges            | relation   |
#      | http://xmlns.com/foaf/0.1/name                  | name      | 3       | 3               | xsd:string        | false      |
