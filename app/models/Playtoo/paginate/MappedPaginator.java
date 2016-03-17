package models.Playtoo.paginate;

import java.util.HashMap;
import java.util.Map;

import models.Playtoo.paginate.strategy.ByKeyRecordLocatorStrategy;

public class MappedPaginator<K, T> extends Paginator<K, T> {

	protected MappedPaginator() {
		this(new HashMap<K, T>());
	}

	public MappedPaginator(Map<K, T> map) {
		super(new ByKeyRecordLocatorStrategy<K, T>(map));
	}
}
