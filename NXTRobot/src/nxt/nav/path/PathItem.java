package nxt.nav.path;

import nxt.nav.Navigation;

/**
 * An interface to an item on a path that can be traversed.
 * 
 * @author Andrei Purcarus
 *
 */
public interface PathItem {
	public void traverseForward(Navigation navigation);

	public void traverseBackward(Navigation navigation);
}
