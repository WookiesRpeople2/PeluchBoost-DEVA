package packages.ORM.repository;

import java.util.List;

public interface BaseRepository<T, ID> {
    T save(T entity);
    T findById(ID id);
    List<T> findAll();
    void deleteById(ID id);
    void delete(T entity);
    boolean existsById(ID id);
    long count();
}
