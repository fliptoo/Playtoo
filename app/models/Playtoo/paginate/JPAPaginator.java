package models.Playtoo.paginate;

import java.io.Serializable;
import java.util.List;

import models.Playtoo.paginate.strategy.JPARecordLocatorStrategy;

/**
 *
 package play.modules.Playtoo;
 * 
 * import java.io.Serializable; import java.util.List;
 * 
 * import play.modules.paginate.strategy.JPARecordLocatorStrategy;
 * 
 * /** This implementation of {@link Paginator} lets you paginate over the rows
 * for a specified JPA entity. *
 *
 * @param <K>
 * @param <T>
 */
public class JPAPaginator<K, T> extends Paginator<K, T> implements Serializable {
	private static final long serialVersionUID = -2064492602195638937L;

	public JPAPaginator(Class<T> typeToken, List<K> keys) {
		super(new JPARecordLocatorStrategy(typeToken, keys));
	}

	public JPAPaginator(Class<T> typeToken) {
		super(new JPARecordLocatorStrategy(typeToken));
	}

	public JPAPaginator(Class<T> typeToken, String filter, Object... params) {
		super(new JPARecordLocatorStrategy(typeToken, filter, params));
	}

	public JPAPaginator<K, T> orderBy(String orderByClause) {
		jpaStrategy().setOrderBy(orderByClause);
		return this;
	}

	protected JPARecordLocatorStrategy jpaStrategy() {
		return (JPARecordLocatorStrategy) getRecordLocatorStrategy();
	}

	// TODO: try to determine this automagically
	public JPAPaginator withKeyNamed(String key) {
		jpaStrategy().withKeyNamed(key);
		return this;
	}

}
