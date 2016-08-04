package net.rhizomik.rhizomer.repository;

import net.rhizomik.rhizomer.model.Facet;
import net.rhizomik.rhizomer.model.id.DatasetClassFacetId;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

/**
 * Created by http://rhizomik.net/~roberto/
 */
@RepositoryRestResource(exported = false)
public interface FacetRepository extends PagingAndSortingRepository<Facet, DatasetClassFacetId> {}
