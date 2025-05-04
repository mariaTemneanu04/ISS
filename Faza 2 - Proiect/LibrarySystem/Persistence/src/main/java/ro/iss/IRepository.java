package ro.iss;


import ro.iss.domain.Entity;

public interface IRepository<ID, E extends Entity<ID>> {
    void save(E entity);
    void delete(ID id);
    void update(E entity);

    E findOne(ID id);
    Iterable<E> findAll();
}
