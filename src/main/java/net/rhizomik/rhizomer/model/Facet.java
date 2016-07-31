package net.rhizomik.rhizomer.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import net.rhizomik.rhizomer.model.id.DatasetClassFacetId;
import org.apache.jena.vocabulary.RDFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by http://rhizomik.net/~roberto/
 */
@Entity
public class Facet {
    private static final Logger logger = LoggerFactory.getLogger(Facet.class);

    @EmbeddedId
    DatasetClassFacetId id;

    private String uri;
    private String label;
    @ManyToOne
    @JsonBackReference
    private Class domain;
    @OneToMany(fetch = FetchType.LAZY, orphanRemoval = true, mappedBy = "facet")
    private List<Range> ranges = new ArrayList<>();

    public Facet() {
        this.id = new DatasetClassFacetId();
    }

    public Facet(Class domain, String curie, String label)
            throws URISyntaxException {
        this(domain, Curie.toUri(curie), label);
    }

    public Facet(String curie, String label) {
        this.uri = Curie.curieToUriStr(curie);
        this.label = label;
    }

    public Facet(Class domain, URI uri, String label) {
        this.id = new DatasetClassFacetId(domain.getDataset(), domain.getUri(), uri);
        this.uri = uri.toString();
        this.label = label;
        this.domain = domain;
    }

    public boolean isRelation() { return ranges.stream().allMatch(Range::isRelation); }

    public DatasetClassFacetId getId() { return id; }

    public URI getUri() {
        try { return new URI(uri); }
        catch (URISyntaxException e) { e.printStackTrace(); }
        return null;
    }

    public void setUri(URI uri) {
        this.uri = uri.toString();
        this.id.setFacetCurie(uri);
    }

    public String getCurie() { return id.getFacetCurie(); }

    public String getLabel() { return label; }

    public void setLabel(String label) { this.label = label; }

    @JsonIgnore
    public List<Range> getRanges() { return ranges; }

    public void addRange(Range range) { ranges.add(range); }

    public String getRange() {
        List<Range> selectedRanges = ranges.stream().sorted((r1, r2) -> Integer.compare(r1.getTimesUsed(), r2.getTimesUsed()))
                .limit(2).collect(Collectors.toList());
        if (selectedRanges.size() == 0)
            return "";
        else if (selectedRanges.size() == 1)
            return selectedRanges.get(0).getUri().toString();
        else if (!selectedRanges.get(0).getUri().equals(URI.create(RDFS.Resource.getURI())))
            return selectedRanges.get(0).getUri().toString();
        else
            return selectedRanges.get(1).getUri().toString();
    }

    public Class getDomain() { return domain; }

    public void setDomain(Class domain) {
        this.domain = domain;
        this.id.setDatasetClassId(domain.getId());
        this.ranges.forEach(range -> range.setFacet(this));
    }

    public int getTimesUsed() { return ranges.stream().mapToInt(Range::getTimesUsed).sum(); }

    public int getDifferentValues() { return ranges.stream().mapToInt(Range::getDifferentValues).sum(); }

    @JsonIgnore
    public boolean getAllLiteral() { return ranges.stream().allMatch(Range::getAllLiteral); }

    @Override
    public String toString() {
        return "Facet{" +
                "id=" + getId() +
                ", label='" + label + '\'' +
                ", domain=" + domain.getId() +
                ", timesUsed=" + getTimesUsed() +
                ", differentValues=" + getDifferentValues() +
                ", isRelation=" + isRelation() +
                ", rangesCuries=" + ranges.toString() +
                '}';
    }
}
