package imcCore.Utils.GeneralClasses;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

public class T {
    public C c;
    public Integer g;
    public Stream<C> s;
    public E e;
    public T t;

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof T)) {
            return false;
        }
        return equals((T) obj, new HashSet<>(), new HashSet<>());
    }

    boolean equals(T q, Set<Object> f, Set<Object> o) {
        boolean tnContain = f.add(t);
        boolean onContain = o.add(q.t);
        if (tnContain != onContain) {
            return false;
        }
        return (tnContain || t.equals(q.t, f, o))&&(e == q.e || (e != null && Objects.equals(c, q.c) && Objects.equals(g, q.g) && e.equals(q.e, f, o)));
    }
}
