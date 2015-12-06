package net.rhizomik.rhizomer.repository;

import net.rhizomik.rhizomer.model.Class;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

/**
 * Created by http://rhizomik.net/~roberto/
 */
@RepositoryRestResource
public interface ClassRepository extends PagingAndSortingRepository<Class, String> {}
