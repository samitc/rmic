package imcCore.Utils.GeneralClasses;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

public class E {
    public C c;
    public Integer g;
    public Stream<C> s;
    public T t;

    boolean equals(E e, Set<Object> f, Set<Object> o) {
        boolean tnContain = f.add(this);
        boolean enContain = f.add(e);
        return tnContain == enContain && (tnContain || (t == e.t || (t != null && Objects.equals(c, e.c) && Objects.equals(g, e.g) && t.equals(e.t, f, o))));
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof E)) {
            return false;
        }
        return equals((E) obj, new HashSet<>(), new HashSet<>());
    }
}
