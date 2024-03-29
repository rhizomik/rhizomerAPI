package net.rhizomik.rhizomer.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import net.rhizomik.rhizomer.model.id.DatasetClassFacetId;
import org.apache.jena.vocabulary.RDFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by http://rhizomik.net/~roberto/
 */
@Entity
public class Facet extends Labelled {
    @EmbeddedId
    DatasetClassFacetId id;

    private String uri;
    @ManyToOne
    @JsonIgnore
    private Class domain;
    @OneToMany(fetch = FetchType.LAZY, orphanRemoval = true, mappedBy = "facet", cascade = CascadeType.ALL)
    private List<Range> ranges = new ArrayList<>();

    public Facet() {
        super(null);
        this.id = new DatasetClassFacetId();
    }

    public Facet(Class domain, String curie, String labels) throws URISyntaxException {
        this(domain, Curie.toUri(curie), labels);
    }

    public Facet(String curie, String labels) {
        super(labels);
        this.uri = Curie.curieToUriStr(curie);
    }

    public Facet(Class domain, URI uri, String labels) {
        super(labels);
        this.id = new DatasetClassFacetId(domain.getDataset(), domain.getUri(), uri);
        this.uri = uri.toString();
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

    public List<Range> getRanges(float relevance, long instancesCount) {
        return this.ranges.stream()
                .filter(range -> ((float)range.getTimesUsed() / instancesCount) > relevance)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @JsonIgnore
    public List<Range> getRanges() { return ranges; }

    public void addRange(Range range) { ranges.add(range); }

    public String getRange() {
        List<Range> selectedRanges = ranges.stream().sorted(Comparator.comparingInt(Range::getTimesUsed).reversed())
                .limit(2).collect(Collectors.toList());
        if (selectedRanges.size() == 0) {
            if (isRelation())
                return Curie.curieToUriStr("rdfs:Resource");
            else
                return Curie.curieToUriStr("xsd:string");
        }
        else if (selectedRanges.size() == 1)
            return selectedRanges.get(0).getUri().toString();
        else if (!selectedRanges.get(0).getUri().equals(URI.create(RDFS.Resource.getURI())))
            return selectedRanges.get(0).getUri().toString();
        else
            return selectedRanges.get(1).getUri().toString();
    }

    public Class getDomain() { return domain; }

    public String getDomainURI() { return domain.getUri().toString(); }

    public void setDomain(Class domain) {
        this.domain = domain;
        this.id.setDatasetClassId(domain.getId());
        this.ranges.forEach(range -> range.setFacet(this));
    }

    public int getTimesUsed() { return ranges.stream().mapToInt(Range::getTimesUsed).sum(); }

    public int getDifferentValues() { return ranges.stream().mapToInt(Range::getDifferentValues).sum(); }

    @JsonIgnore
    public boolean getAllLiteral() { return ranges.stream().allMatch(Range::getAllLiteral); }

    public boolean getAllBlank() { return ranges.stream().allMatch(Range::getAllBlank); }

    @Override
    public String toString() {
        return "Facet{" +
                "id=" + getId() +
                ", labels='" + getLabels() + '\'' +
                ", domain=" + domain.getId() +
                ", timesUsed=" + getTimesUsed() +
                ", differentValues=" + getDifferentValues() +
                ", isRelation=" + isRelation() +
                ", rangesCuries=" + ranges.toString() +
                '}';
    }

    @Data
    public static class Relation {
        URI classUri;
        String classLabel;
        String classCurie;
        URI propertyUri;
        String propertyLabel;
        String propertyCurie;
        URI rangeUri;
        String rangeLabel;
        String rangeCurie;
        int uses;

        public Relation(URI classUri, String classLabel, String classCurie,
                            URI propertyUri, String propertyLabel, String propertyCurie,
                            URI rangeUri, String rangeLabel, String rangeCurie, int uses) {
            this.classUri = classUri;
            this.classLabel = classLabel;
            this.classCurie = classCurie;
            this.propertyUri = propertyUri;
            this.propertyLabel = propertyLabel;
            this.propertyCurie = propertyCurie;
            this.rangeUri = rangeUri;
            this.rangeLabel = rangeLabel;
            this.rangeCurie = rangeCurie;
            this.uses = uses;
        }
    }
}
