package imcCore.Utils.GeneralClasses;

public class Q {
    public int a;
    public Q q;

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Q && a == ((Q) obj).a;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(a);
    }

    public boolean realEquals(Q q) {
        return a == q.a && this.q.equals(q.q);
    }
}
