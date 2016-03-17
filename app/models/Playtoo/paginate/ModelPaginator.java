package models.Playtoo.paginate;

import java.util.List;

import play.db.jpa.GenericModel;

/**
 * This implementation of {@link Paginator} lets you paginate over the rows for
 * a specified Play! framework GenericModel. The class must extend from the
 * GenericModel helper class.
 *
 * @param <T>
 */
public class ModelPaginator<T extends GenericModel> extends
		JPAPaginator<Long, T> {

	public ModelPaginator(Class<T> typeToken) {
		super(typeToken);
	}

	public ModelPaginator(Class<T> typeToken, List<Long> keys) {
		super(typeToken, keys);
	}

	public ModelPaginator(Class<T> typeToken, String filter, Object... params) {
		super(typeToken, filter, params);
	}

	public ModelPaginator<T> orderBy(String orderByClause) {
		jpaStrategy().setOrderBy(orderByClause);
		return this;
	}

}
