package net.rhizomik.rhizomer.repository;

import net.rhizomik.rhizomer.model.Range;
import net.rhizomik.rhizomer.model.id.DatasetClassFacetRangeId;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

/**
 * Created by http://rhizomik.net/~roberto/
 */
@RepositoryRestResource(exported = false)
public interface RangeRepository extends PagingAndSortingRepository<Range, DatasetClassFacetRangeId> {}
