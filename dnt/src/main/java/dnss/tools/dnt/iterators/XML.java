package dnss.tools.dnt.iterators;

import java.util.Iterator;

/**
 * Created by Ben on 5/23/2015.
 */
public class XML implements Iterator<Object>, Iterable<Object> {
    public XML() {

    }

    @Override
    public Iterator<Object> iterator() {
        return this;
    }

    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public Object next() {
        return null;
    }
}
