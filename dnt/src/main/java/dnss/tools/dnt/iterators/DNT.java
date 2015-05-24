package dnss.tools.dnt.iterators;

import java.io.File;
import java.util.Iterator;

/**
 * Created by Ben on 5/23/2015.
 */
public class DNT implements Iterator<Object>, Iterable<Object> {
    private File file;

    public DNT(File file) {
        this.file = file;
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
