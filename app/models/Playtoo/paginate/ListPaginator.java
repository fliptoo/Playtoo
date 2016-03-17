package models.Playtoo.paginate;

import java.util.List;

import models.Playtoo.paginate.strategy.ListRecordLocatorStrategy;

public class ListPaginator<T> extends Paginator<Object, T> {

	public ListPaginator(List<T> values, final int size) {
		super(new ListRecordLocatorStrategy<T>(values) {

			@Override
			public int max() {
				return size;
			}
		});
	}

}
