package net.rhizomik.rhizomer.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by http://rhizomik.net/~roberto/
 */
public class IncomingFacet {
    private static final Logger logger = LoggerFactory.getLogger(IncomingFacet.class);

    private String rangeUri;
    private String rangeCurie;
    private String uri;
    private String curie;
    private String label;
    private int uses;
    private List<Domain> domains = new ArrayList<>();

    public IncomingFacet() { }

    public IncomingFacet(String rangeUri, String uri, String label, int uses) {
        this.rangeUri = rangeUri;
        this.rangeCurie = Curie.uriStrToCurie(rangeUri);
        this.uri = uri;
        this.curie = Curie.uriStrToCurie(uri);
        this.label = label;
        this.uses = uses;
    }

    public String getRangeUri() { return rangeUri; }

    public void setRangeUri(String rangeUri) { this.rangeUri = rangeUri; }

    public String getRangeCurie() { return rangeCurie; }

    public void setRangeCurie(String rangeCurie) { this.rangeCurie = rangeCurie; }

    public String getUri() { return uri; }

    public void setUri(String uri) { this.uri = uri; }

    public String getCurie() { return this.curie; }

    public void setCurie(String curie) { this.curie = curie; }

    public String getLabel() { return label; }

    public void setLabel(String label) { this.label = label; }

    public int getUses() { return uses; }

    public void setUses(int uses) { this.uses = uses; }

    public List<Domain> getDomains() { return this.domains; }

    public void addDomain(Domain domain) { this.domains.add(domain); }

    public void setDomains(List<Domain> domains) { this.domains = domains; }

    @Override
    public String toString() {
        return "Facet{" +
                "rangeCurie=" + getRangeCurie() +
                ", curie='" + getCurie() + '\'' +
                ", label='" + getLabel() + '\'' +
                ", domains=" + getDomains().toString() +
                '}';
    }

    public static class Domain {
        private String uri;
        private String curie;
        private String label;
        private int count;

        public Domain() { }

        public Domain(String uri, String label, int count) {
            this.uri = uri;
            this.curie = Curie.uriStrToCurie(uri.toString());
            this.label = label;
            this.count = count;
        }

        public String getUri() { return uri; }

        public void setUri(String uri) { this.uri = uri; }

        public String getCurie() { return curie; }

        public void setCurie(String curie) { this.curie = curie; }

        public String getLabel() { return label; }

        public void setLabel(String label) { this.label = label; }

        public int getCount() { return count; }

        public void setCount(int count) { this.count = count; }

        @Override
        public String toString() {
            return "Domain{" +
                    "uri=" + uri +
                    ", curie='" + curie + '\'' +
                    ", label='" + label + '\'' +
                    ", count=" + count +
                    '}';
        }
    }
}
