package imcCore.Utils.GeneralClasses;

import java.util.HashSet;
import java.util.Set;

public class C {
    public int i;
    public C s, a, b;

    private boolean equals0(C c, Set<C> t, Set<C> o) {
        boolean tnContain = t.add(s);
        boolean onContain = o.add(c.s);
        if (tnContain != onContain) {
            return false;
        }
        return tnContain || equals(c, t, o);
    }

    private static boolean nullEquals(C th, C c, Set<C> t, Set<C> o) {
        if (th == null) {
            return c == null;
        }
        return th.equals0(c, t, o);
    }

    private boolean equals(C c, Set<C> t, Set<C> o) {
        if (i != c.i) {
            return false;
        }
        return nullEquals(s, c.s, t, o) && nullEquals(a, c.a, t, o) && nullEquals(b, c.b, t, o);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof C)) {
            return false;
        }
        return equals((C) obj, new HashSet<>(), new HashSet<>());
    }
}
