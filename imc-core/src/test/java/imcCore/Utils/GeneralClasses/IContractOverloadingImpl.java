package imcCore.Utils.GeneralClasses;

import imcCore.Utils.GeneralContractInterface.IContractOverloading;

public class IContractOverloadingImpl implements IContractOverloading {
    public int f1V = 0;
    public int f2VI = 0;
    public int f2VIB = 0;
    public int f3I = 0;
    public int f3II = 0;
    public int f3VS = 0;
    public int f4VB = 0;
    public int f4VI = 0;
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
        f3VS_S = null;
    }
}
