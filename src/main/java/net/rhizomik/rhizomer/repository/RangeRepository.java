package net.rhizomik.rhizomer.repository;

import net.rhizomik.rhizomer.model.Range;
import net.rhizomik.rhizomer.model.id.DatasetClassFacetRangeId;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by http://rhizomik.net/~roberto/
 */
@Repository
public interface RangeRepository extends PagingAndSortingRepository<Range, DatasetClassFacetRangeId> {}
