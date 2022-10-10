package dev.appeazethecheese.votepoints.data;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.hibernate.Session;
import org.hibernate.query.Query;

public class QueryWrapper<T> implements AutoCloseable {

    private CriteriaQuery<T> criteriaQuery;
    private Root<T> root;
    private final Session session;
    private final CriteriaBuilder builder;
    private final Class<T> type;

    public QueryWrapper(Class<T> type, Session session){
        this.type = type;
        this.session = session;
        builder = session.getCriteriaBuilder();
        clearQuery();
    }

    public void clearQuery(){
        criteriaQuery = builder.createQuery(type);
        root = criteriaQuery.from(type);
        criteriaQuery.select(root);
    }

    public Query<T> query(){
        return session.createQuery(criteriaQuery);
    }

    public CriteriaBuilder builder() {
        return builder;
    }

    public Session session() {
        return session;
    }

    public CriteriaQuery<T> criteria() {
        return criteriaQuery;
    }

    public Root<T> root() {
        return root;
    }

    @Override
    public void close() {
        session.close();
    }
}
