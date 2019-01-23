package net.rhizomik.rhizomer.service;

import java.net.URISyntaxException;
import java.util.regex.Pattern;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.riot.system.PrefixMapStd;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Created by http://rhizomik.net/~roberto/
 */
@Service
public class PrefixCCMap extends PrefixMapStd {
    private static final Logger logger = LoggerFactory.getLogger(PrefixCCMap.class);

    @Override
    public boolean contains(String prefix) {
        if (super.contains(prefix))
            return true;

        String prefixccUri = prefixCCNamespaceLookup(prefix);
        if (prefixccUri != null) {
            this.add(prefix, prefixccUri);
            return true;
        }
        else
            return false;
    }

    @Override
    public Pair<String, String> abbrev(String uriStr) {
        Pair<String, String> curiePair = super.abbrev(uriStr);
        if (curiePair == null) {
            Resource uri = ResourceFactory.createResource(uriStr);
            String prefixcc = prefixCCReverseLookup(uri.getNameSpace());
            if (prefixcc != null)
                this.add(prefixcc, uri.getNameSpace());
            else
                try { this.add(generatePrefix(uriStr), uri.getNameSpace()); }
                catch (URISyntaxException e) { return null; }
            curiePair = super.abbrev(uriStr);
        }
        return curiePair;
    }

    @Override
    public String abbreviate(String uriStr) {
        Pair<String, String> curiePair = this.abbrev(uriStr);
        if (curiePair == null)
            return null;
        return curiePair.getLeft()+":"+curiePair.getRight();
    }

    @Override
    public String expand(String prefixedName) {
        String uriStr = super.expand(prefixedName);
        if (uriStr == null) {
            String[] curiePair = prefixedName.split(":");
            if (curiePair.length == 2) {
                String prefix = curiePair[0];
                String namespace = prefixCCNamespaceLookup(prefix);
                if (namespace != null) {
                    super.add(prefix, namespace);
                    uriStr = super.expand(prefixedName);
                }
            }
        }
        return uriStr;
    }

    protected String generatePrefix(String uriStr) throws URISyntaxException {
        java.net.URI uri = new java.net.URI(uriStr);
        String host = uri.getHost();
        String[] hostParts = host.split("\\.");

        String candidatePrefix = null;
        if (hostParts.length > 1)
            candidatePrefix = hostParts[hostParts.length - 2];
        else
            candidatePrefix = hostParts[0];

        if (!this.contains(candidatePrefix))
            return candidatePrefix;
        else {
            int version = 1;
            while(this.contains(candidatePrefix+"_"+version))
                version++;
            return candidatePrefix+"_"+version;
        }
    }

    protected String prefixCCNamespaceLookup(String prefix) {
        RestTemplate restTemplate = new RestTemplate();
        try {
            String response = restTemplate.getForObject("http://prefix.cc/{prefix}.file.{format}", String.class, prefix, "txt");
            String[] pair = response.split("\\s");
            if (pair.length == 2)
                return pair[1];
        } catch (RestClientException e) {
            switch (prefix) {
                case "rdf": return RDF.getURI();
                case "rdfs": return RDFS.getURI();
                case "owl": return OWL.getURI();
                case "xsd": return XSD.getURI();
                case "foaf": return FOAF.getURI();
                default: logger.info("Prefix {} not found in http://prefix.cc \n", prefix);
            }
        }
        return null;
    }

    protected String prefixCCReverseLookup(String uri) {
        RestTemplate restTemplate = new RestTemplate();
        try {
            String response = restTemplate.getForObject("http://prefix.cc/reverse?uri={uri}&format={format}", String.class, uri, "txt");
            String[] pair = response.split("\\s");
            if (pair.length == 2)
                return pair[0];
        } catch (RestClientException e) {
            switch (uri) {
                case "http://www.w3.org/1999/02/22-rdf-syntax-ns#": return "rdf";
                case "http://www.w3.org/2000/01/rdf-schema#": return "rdfs";
                case "http://www.w3.org/2002/07/owl#": return "owl";
                case "http://www.w3.org/2001/XMLSchema#": return "xsd";
                case "http://xmlns.com/foaf/0.1/": return "foaf";
                case "http://purl.org/net/schemas/space/": return "space";
                default: logger.info("Prefix for URI {} not found in http://prefix.cc", uri);
            }
        }
        return null;
    }

    public boolean isCurie(String k) {
        return Pattern.matches("[\\w_][\\w\\d\\.\\-_]+:[\\w\\d\\.\\-_+#']+", k);
    }
}
