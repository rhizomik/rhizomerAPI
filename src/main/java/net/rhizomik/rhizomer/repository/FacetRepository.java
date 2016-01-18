package net.rhizomik.rhizomer.repository;

import net.rhizomik.rhizomer.model.Facet;
import net.rhizomik.rhizomer.model.DatasetClassFacetId;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

/**
 * Created by http://rhizomik.net/~roberto/
 */
@RepositoryRestResource
public interface FacetRepository extends PagingAndSortingRepository<Facet, DatasetClassFacetId> {}
