package net.rhizomik.rhizomer.model;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.PrefixDocumentFormat;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.formats.TurtleDocumentFormat;
import org.semanticweb.owlapi.io.StringDocumentTarget;
import org.semanticweb.owlapi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * Created by http://rhizomik.net/~roberto/
 */
public class Ontology {
    private static final Logger logger = LoggerFactory.getLogger(Ontology.class);

    Pond pond;
    OWLOntologyManager manager;
    OWLOntology ontology;
    IRI ontologyIRI;
    OWLDataFactory df;

    public Ontology(Pond pond) throws OWLOntologyCreationException {
        this.pond = pond;
        ontologyIRI = IRI.create(pond.getPondUri()+".owl");
        manager = OWLManager.createOWLOntologyManager();
        ontology = manager.createOntology(ontologyIRI);
        df = OWLManager.getOWLDataFactory();
    }

    public String getOWL(String format) throws OWLOntologyStorageException {
        generateOWLClasses();
        PrefixDocumentFormat documentFormat = null;
        switch (format) {
            case "TTL": case "ttl": case "Turtle": case "turtle":
                documentFormat= new TurtleDocumentFormat();
                break;
            default:
                documentFormat = new RDFXMLDocumentFormat();
                break;
        }
        documentFormat.copyPrefixesFrom(CURIE.getPrefixes());
        StringDocumentTarget output = new StringDocumentTarget();
        manager.saveOntology(ontology, documentFormat, output);
        return output.toString();
    }

    private void generateOWLClasses() {
        logger.info("Generating ontology: {}", this.ontologyIRI);
        Collection<Class> pondClasses = pond.getOntologyClasses().values();
        int index = 0;
        for(Class pClass: pondClasses) {
            index++;
            logger.info("\n{}/{} Processing class: {}", index, pondClasses.size(), pClass.getUri());
            if (isOmittedClass(pClass))
                continue;
            OWLClass oClass = df.getOWLClass(IRI.create(pClass.getUri()));
            OWLAxiom declare = df.getOWLDeclarationAxiom(oClass);
            manager.addAxiom(ontology, declare);
            OWLLiteral label = df.getOWLLiteral(pClass.getLabel());
            OWLAnnotation labelAnnot = df.getOWLAnnotation(df.getRDFSLabel(), label);
            OWLAxiom labelAxiom = df.getOWLAnnotationAssertionAxiom(oClass.getIRI(), labelAnnot);
            manager.addAxiom(ontology, labelAxiom);
            // OWLAxiom axiom = df.getOWLSubClassOfAxiom(clsA, clsB);
            for(Facet facet: pClass.getFacets().values()) {
                if (!isOmittedProperty(facet))
                    generateOWLRestriction(facet, oClass);
            }
        }
    }

    private void generateOWLRestriction(Facet facet, OWLClass oClass) {
        if (facet.getRanges().length > 0) {
            OWLProperty property;
            if (facet.isRelation())
                property = df.getOWLObjectProperty(IRI.create(facet.getUri()));
            else
                property = df.getOWLDataProperty(IRI.create(facet.getUri()));
            OWLAxiom declare = df.getOWLDeclarationAxiom(property);
            manager.addAxiom(ontology, declare);
            OWLLiteral label = df.getOWLLiteral(facet.getLabel());
            OWLAnnotation labelAnnot = df.getOWLAnnotation(df.getRDFSLabel(), label);
            OWLAxiom labelAxiom = df.getOWLAnnotationAssertionAxiom(property.getIRI(), labelAnnot);
            manager.addAxiom(ontology, labelAxiom);

            OWLClassExpression hasPropertyAllRange;
            if (facet.isRelation()) {
                OWLClass range = df.getOWLClass(IRI.create(facet.getRange()));
                hasPropertyAllRange = df.getOWLObjectAllValuesFrom(property.asOWLObjectProperty(), range);
            }
            else {
                OWLDatatype range = df.getOWLDatatype(IRI.create(facet.getRange()));
                hasPropertyAllRange = df.getOWLDataAllValuesFrom(property.asOWLDataProperty(), range);
            }
            OWLSubClassOfAxiom subClassAxiom = df.getOWLSubClassOfAxiom(oClass, hasPropertyAllRange);
            manager.addAxiom(ontology, subClassAxiom);
        }
    }

    private boolean isOmittedClass(Class pClass) {
        for(String omitted: Queries.omitClasses) {
            if (pClass.getUri().toString().contains(omitted))
                return true;
        }
        return false;
    }

    private boolean isOmittedProperty(Facet facet) {
        for(String omitted: Queries.omitProperties) {
            if (facet.getUri().toString().contains(omitted))
                return true;
        }
        return false;
    }
 }
