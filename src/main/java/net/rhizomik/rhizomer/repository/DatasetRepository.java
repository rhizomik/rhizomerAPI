package net.rhizomik.rhizomer.repository;

import net.rhizomik.rhizomer.model.Dataset;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by http://rhizomik.net/~roberto/
 */
@Repository
public interface DatasetRepository extends PagingAndSortingRepository<Dataset, String> {}
