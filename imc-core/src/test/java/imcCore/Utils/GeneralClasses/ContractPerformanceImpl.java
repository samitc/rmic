package imcCore.Utils.GeneralClasses;

import imcCore.Utils.GeneralContractInterface.IContractPerformance;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ContractPerformanceImpl implements IContractPerformance {
    @Override
    public void f1() {

    }

    @Override
    public int f2(List<Integer> values) {
        return values.stream().reduce(0, (integer, integer2) -> integer + integer2);
    }

    @Override
    public float f3(List<Float> values) {
        return values.stream().reduce(0.0f, (aFloat, aFloat2) -> aFloat + aFloat2);
    }

    @Override
    public List<Integer> f4(int... vals) {
        return Arrays.stream(vals).boxed().collect(Collectors.toList());
    }

    @Override
    public String f5(char[] chars, char[] oChars) {
        return String.valueOf(chars) + String.valueOf(oChars);
    }

    @Override
    public Integer f6(int val, int val2) {
        return val + val2;
    }
}
