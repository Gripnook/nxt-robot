package nxt.nav.path;

import java.util.ArrayList;
import java.util.List;

/**
 * A collection of PathItems.
 * 
 * @author Andrei Purcarus
 *
 */
public final class Path {
	private List<PathItem> _items;

	public Path() {
		this._items = new ArrayList<>();
	}

	public final void add(PathItem pathItem) {
		this._items.add(pathItem);
	}

	public final List<PathItem> getItems() {
		return this._items;
	}
}
