package net.rhizomik.rhizomer.repository;

import net.rhizomik.rhizomer.model.Dataset;
import net.rhizomik.rhizomer.model.SPARQLEndPoint;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by http://rhizomik.net/~roberto/
 */
@Repository
public interface SPARQLEndPointRepository extends PagingAndSortingRepository<SPARQLEndPoint, Integer> {

    List<SPARQLEndPoint> findByDataset(Dataset dataset);
    boolean existsByDataset(Dataset dataset);
    void deleteByDataset(Dataset dataset);
}
