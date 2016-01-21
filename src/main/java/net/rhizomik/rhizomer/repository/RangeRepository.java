package net.rhizomik.rhizomer.repository;

import net.rhizomik.rhizomer.model.DatasetClassFacetRangeId;
import net.rhizomik.rhizomer.model.Range;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

/**
 * Created by http://rhizomik.net/~roberto/
 */
@RepositoryRestResource
public interface RangeRepository extends PagingAndSortingRepository<Range, DatasetClassFacetRangeId> {}
