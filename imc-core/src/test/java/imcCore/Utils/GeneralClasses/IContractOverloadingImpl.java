package imcCore.Utils.GeneralClasses;

import imcCore.Utils.GeneralContractInterface.IContractOverloading;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class IContractOverloadingImpl implements IContractOverloading {
    public int f1V = 0;
    public int f2VI = 0;
    public int f2VIB = 0;
    public int f3I = 0;
    public int f3II = 0;
    public int f3VS = 0;
    public int f4VB = 0;
    public int f4VI = 0;
    public int f5SA = 0;
    public int f6IA = 0;
    public int f7LSLI = 0;
    public int f8 = 0;
    public int f9a = 0;
    public int f9b = 0;
    public int f9c = 0;
    public int f9cB = 0;
    public String f3VS_S = null;

    @Override
    public void f1() {
        f1V++;
    }

    @Override
    public void f2(int a, boolean b) {
        f2VIB++;
    }

    @Override
    public void f3(String str) {
        f3VS_S = str;
        f3VS++;
    }

    @Override
    public void f4(int a) {
        f4VI++;
    }

    @Override
    public int f3(int a) {
        f3II++;
        return a;
    }

    @Override
    public int f3() {
        f3I++;
        return -5;
    }

    @Override
    public void f4(boolean b) {
        f4VB++;
    }

    @Override
    public void f2(int a) {
        f2VI++;
    }

    @Override
    public String[] f5() {
        f5SA++;
        return new String[]{"a", "b", "c", "d"};
    }

    @Override
    public int[] f6() {
        f6IA++;
        return new int[]{1, 2, 3, 4, 9};
    }

    @Override
    public List<String> f7(List<Integer> l) {
        f7LSLI++;
        return l.stream().map(Object::toString).collect(Collectors.toList());
    }

    @Override
    public Object f8(List<Integer> nums, List<String> strs, float[] flts, boolean b) {
        f8++;
        Object ret = null;
        if (b) {
            int max = Integer.MIN_VALUE;
            for (Integer num : nums) {
                if (max < num) {
                    ret = num;
                    max = num;
                }
            }
        } else {
            float max = Float.MIN_VALUE;
            for (int i = 0; i < flts.length; i++) {
                if (max < flts[i]) {
                    ret = strs.get(i);
                    max = flts[i];
                }
            }
        }
        return ret;
    }

    @Override
    public void f9(Object... params) {
        f9a++;
    }

    @Override
    public Integer f9(ContainerObject o) {
        f9b++;
        if (o.object instanceof Integer) {
            return (Integer) o.object;
        }
        return -1;
    }

    @Override
    public ContainerObject f9(ContainerObject o1, List<ContainerObject> o2, ContainerObject... o3) {
        f9c++;
        List<Object> objects = new ArrayList<>();
        objects.add(o1.object);
        objects.addAll(o2);
        return new ContainerObject(objects);
    }

    @Override
    public ContainerObject f9B(ContainerObject o1, List<ContainerObject> o2, ContainerObject... o3) {
        f9cB++;
        return new ContainerObject(Arrays.asList(o3));
    }

    @Override
    public int f4() {
        return 0;
    }

    public void resetCals() {
        f1V = 0;
        f2VI = 0;
        f2VIB = 0;
        f3I = 0;
        f3II = 0;
        f3VS = 0;
        f4VB = 0;
        f4VI = 0;
        f5SA = 0;
        f6IA = 0;
        f7LSLI = 0;
        f8 = 0;
        f9a = 0;
        f9b = 0;
        f9c = 0;
        f9cB = 0;
        f3VS_S = null;
    }
}
