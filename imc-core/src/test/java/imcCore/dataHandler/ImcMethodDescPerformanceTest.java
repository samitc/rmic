package imcCore.dataHandler;

import imcCore.Utils.GeneralContractInterface.IContractPerformance;
import imcCore.contract.Exceptions.NotContractInterfaceType;
import imcCore.contract.Exceptions.NotContractMethodException;
import imcCore.contract.Exceptions.NotInterfaceType;
import imcCore.contract.ImcClass;
import imcCore.contract.ImcMethod;
import org.junit.Test;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.All)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class ImcMethodDescPerformanceTest {
    private ImcMethod voidVoidMet;
    private MethodPocket emptyPocket;
    private byte[] voidVoidData;
    private ImcMethod intListIMet;
    private MethodPocket intListIPocket;
    private byte[] intListIData;
    private ImcMethod floatListFMet;
    private MethodPocket floatListFPocket;
    private byte[] floatListFData;
    private ImcMethod lIintAMet;
    private MethodPocket lIintAPocket;
    private byte[] lIintAData;
    private ImcMethod strCACAMet;
    private MethodPocket strCACAPocket;
    private byte[] strCACAData;
    private ImcMethod intIIMet;
    private MethodPocket intIIPocket;
    private byte[] intIIData;

    @Test
    public void benchmarkTest() throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(ImcMethodDescPerformanceTest.class.getSimpleName()+System.getProperty("benchTest"))
                .forks(1)
                .build();
        new Runner(opt).run();
    }

    @Test
    public void NetworkTest() throws NotContractInterfaceType, NotInterfaceType, NotContractMethodException, IOException {
        setup();
        String ls = System.lineSeparator();
        System.out.printf("voidVoidData=%d%sintListIData=%d%sfloatListFData=%d%slIintAData=%d%sstrCACAData=%d%sintIIData=%d",
                voidVoidData.length, ls,
                intListIData.length, ls,
                floatListFData.length, ls,
                lIintAData.length, ls,
                strCACAData.length, ls,
                intIIData.length);
    }

    @Setup
    public void setup() throws NotContractInterfaceType, NotInterfaceType, NotContractMethodException, IOException {
        ImcClass imcClass = new ImcClass(IContractPerformance.class);
        Float[] floats = new Float[]{5.7f, 6.4f, 7.5f, 4.99f, 6.23f, 3.9f, 5.8f, 7.2f, 5.1f, 3.4f, 5.7f};
        String hello = "hello";
        String world = "world";
        voidVoidMet = imcClass.getImcMethod(0);
        emptyPocket = new MethodPocket(null, null);
        voidVoidData = voidVoidMet.write(emptyPocket);
        intListIMet = imcClass.getImcMethod(1);
        intListIPocket = MethodPocket.builder().addParam(new ArrayList<>(Arrays.stream(floats).map(Float::intValue).collect(Collectors.toList()))).retObj(Arrays.stream(floats).map(Float::intValue).reduce((integer, integer2) -> integer + integer2).get()).build();
        intListIData = intListIMet.write(intListIPocket);
        floatListFMet = imcClass.getImcMethod(2);
        floatListFPocket = MethodPocket.builder().addParam(new ArrayList<>(Arrays.stream(floats).collect(Collectors.toList()))).retObj(Arrays.stream(floats).reduce((aFloat, aFloat2) -> aFloat + aFloat2).get()).build();
        floatListFData = floatListFMet.write(floatListFPocket);
        lIintAMet = imcClass.getImcMethod(3);
        lIintAPocket = MethodPocket.builder().addParam(Arrays.stream(floats).map(Float::intValue).toArray(Integer[]::new)).retObj(Arrays.stream(floats).map(Float::intValue).collect(Collectors.toList())).build();
        lIintAData = lIintAMet.write(lIintAPocket);
        strCACAMet = imcClass.getImcMethod(4);
        strCACAPocket = MethodPocket.builder().addParam(hello.toCharArray()).addParam(world.toCharArray()).retObj(hello + world).build();
        strCACAData = strCACAMet.write(strCACAPocket);
        intIIMet = imcClass.getImcMethod(5);
        intIIPocket = MethodPocket.builder().addParam(73).addParam(34).retObj(73 * 34).build();
        intIIData = intIIMet.write(intIIPocket);
    }

    @Benchmark
    public byte[] voidVoidWrite() throws IOException {
        return voidVoidMet.write(emptyPocket);
    }

    @Benchmark
    public MethodPocket voidVoidRead() throws InvocationTargetException, IOException, InstantiationException, NoSuchMethodException, IllegalAccessException {
        return voidVoidMet.read(voidVoidData);
    }

    @Benchmark
    public byte[] intListIWrite() throws IOException {
        return intListIMet.write(intListIPocket);
    }

    @Benchmark
    public MethodPocket intListIRead() throws InvocationTargetException, IOException, InstantiationException, NoSuchMethodException, IllegalAccessException {
        return intListIMet.read(intListIData);
    }

    @Benchmark
    public byte[] floatListFWrite() throws IOException {
        return floatListFMet.write(floatListFPocket);
    }

    @Benchmark
    public MethodPocket floatListFRead() throws InvocationTargetException, IOException, InstantiationException, NoSuchMethodException, IllegalAccessException {
        return floatListFMet.read(floatListFData);
    }

    @Benchmark
    public byte[] lIintAWrite() throws IOException {
        return lIintAMet.write(lIintAPocket);
    }

    @Benchmark
    public MethodPocket lIintARead() throws InvocationTargetException, IOException, InstantiationException, NoSuchMethodException, IllegalAccessException {
        return lIintAMet.read(lIintAData);
    }

    @Benchmark
    public byte[] strCACAWrite() throws IOException {
        return strCACAMet.write(strCACAPocket);
    }

    @Benchmark
    public MethodPocket strCACARead() throws InvocationTargetException, IOException, InstantiationException, NoSuchMethodException, IllegalAccessException {
        return strCACAMet.read(strCACAData);
    }

    @Benchmark
    public byte[] intIIWrite() throws IOException {
        return intIIMet.write(intIIPocket);
    }

    @Benchmark
    public MethodPocket intIIRead() throws InvocationTargetException, IOException, InstantiationException, NoSuchMethodException, IllegalAccessException {
        return intIIMet.read(intIIData);
    }
}
