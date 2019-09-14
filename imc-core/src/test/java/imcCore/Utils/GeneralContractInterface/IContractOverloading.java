package imcCore.Utils.GeneralContractInterface;

import imcCore.Utils.GeneralClasses.C;
import imcCore.Utils.GeneralClasses.E;
import imcCore.Utils.GeneralClasses.Q;
import imcCore.Utils.GeneralClasses.T;
import imcCore.contract.annotations.ContractInterface;
import imcCore.contract.annotations.ContractMethod;
import org.apache.commons.math3.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@ContractInterface
public interface IContractOverloading {
    @ContractMethod
    Object f8(List<Integer> nums, List<String> strs, float[] flts, boolean b);

    @ContractMethod
    void f1();

    @ContractMethod
    void f2(int a, boolean b);

    @ContractMethod
    void f3(String str);

    @ContractMethod
    void f4(int a);

    @ContractMethod
    int f3(int a);

    @ContractMethod(sendResult = false)
    int f3();

    @ContractMethod
    void f4(boolean b);

    @ContractMethod(sendResult = true)
    void f2(int a);

    @ContractMethod
    String[] f5();

    @ContractMethod
    int[] f6();

    @ContractMethod
    List<String> f7(List<Integer> l);

    @ContractMethod
    void f9(Object... params);

    @ContractMethod
    Integer f9(ContainerObject o);

    @ContractMethod
    ContainerObject f9(ContainerObject o1, List<ContainerObject> o2, ContainerObject... o3);

    @ContractMethod
    ContainerObject f9B(ContainerObject o1, List<ContainerObject> o2, ContainerObject... o3);

    @ContractMethod
    TestArrayList<Integer> fa1(List<Integer> testArrayList);

    @ContractMethod
    C fa21(C c);

    @ContractMethod
    E fa22(E e);

    @ContractMethod
    T fa23(T t);

    @ContractMethod
    C fa24(E e, T t);

    @ContractMethod
    Pair<Q, Q> fa3(Q a, Q b);

    int f4();

    class ContainerObject {
        public Object object;

        public ContainerObject(Object object) {
            this.object = object;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof ContainerObject &&
                    ((object.getClass().isArray() && Arrays.equals((Object[]) object, ((Object[]) ((ContainerObject) obj).object)))
                            || object.equals(((ContainerObject) (obj)).object));
        }
    }

    class TestArrayList<T> extends ArrayList<T> {
        public int hashTemp;

        public TestArrayList(List<T> testArrayList, int hash) {
            super(testArrayList);
            hashTemp = hash;
        }

        @Override
        public int hashCode() {
            return hashTemp;
        }
    }
}
