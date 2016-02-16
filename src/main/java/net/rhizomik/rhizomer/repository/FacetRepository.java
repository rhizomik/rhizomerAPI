package net.rhizomik.rhizomer.repository;

import net.rhizomik.rhizomer.model.Facet;
import net.rhizomik.rhizomer.model.id.DatasetClassFacetId;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by http://rhizomik.net/~roberto/
 */
@Repository
public interface FacetRepository extends PagingAndSortingRepository<Facet, DatasetClassFacetId> {}
