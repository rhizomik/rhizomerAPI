package net.rhizomik.rhizomer.repository;

import net.rhizomik.rhizomer.model.Class;
import net.rhizomik.rhizomer.model.id.DatasetClassId;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by http://rhizomik.net/~roberto/
 */
@Repository
public interface ClassRepository extends PagingAndSortingRepository<Class, DatasetClassId> {}
