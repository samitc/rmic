package imcCore.Utils.GeneralContractInterface;

import imcCore.contract.annotations.ContractInterface;
import imcCore.contract.annotations.ContractMethod;

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

    int f4();
}
