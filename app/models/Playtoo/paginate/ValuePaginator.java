package models.Playtoo.paginate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import models.Playtoo.paginate.strategy.ByValueRecordLocatorStrategy;

/**
 * This class allows you to paginate over a prepopulated Collection.
 *
 * @param <T>
 */
public class ValuePaginator<T> extends Paginator<Object, T> {

	protected ValuePaginator() {
		this(new ArrayList<T>());
	}

	public ValuePaginator(List<T> values) {
		super(new ByValueRecordLocatorStrategy<T>(values));
	}

	public ValuePaginator(Map<?, T> values) {
		super(new ByValueRecordLocatorStrategy<T>(values.values()));
	}

	public ValuePaginator(Collection<T> values) {
		super(new ByValueRecordLocatorStrategy<T>(values));
	}
}
