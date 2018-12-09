package imcClientServer;

import imcClient.contract.ContractCaller;
import imcCore.Utils.GeneralClasses.ContractPerformanceImpl;
import imcCore.Utils.GeneralContractInterface.IContractPerformance;
import imcCore.contract.Exceptions.NotContractInterfaceType;
import imcCore.contract.Exceptions.NotInterfaceType;
import imcServer.contract.ContractImpl;
import imcServer.contract.Exception.ImplNotMaintainInterface;
import org.junit.Test;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.All)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class ImcPerformanceTesting {
    private static int PORT = 12345;
    private static int NUM_IN_LISTS = 20;
    private ContractImpl<IContractPerformance> server;
    private IContractPerformance client;
    private List<Integer> intList;
    private List<Float> floatList;
    private int[] intArray;
    private char[] chars1Arr;
    private char[] chars2Arr;

    @Test
    public void clientServerBenchmarkTest() throws RunnerException {

        Options opt = new OptionsBuilder()
                .include(ImcPerformanceTesting.class.getSimpleName() + System.getProperty("clientServerBenchTest"))
                .forks(1)
                .build();
        new Runner(opt).run();
    }

    @Setup
    public void setup() throws ImplNotMaintainInterface, NotContractInterfaceType, NotInterfaceType, IOException {
        server = new ContractImpl<>(new ContractPerformanceImpl(), IContractPerformance.class);
        server.open(PORT);
        client = ContractCaller.getInterfaceContract(IContractPerformance.class, "127.0.0.1", PORT);
        intList = new ArrayList<>();
        intArray = new int[NUM_IN_LISTS];
        floatList = new ArrayList<>();
        chars1Arr = new char[NUM_IN_LISTS];
        chars2Arr = new char[NUM_IN_LISTS];
        for (int i = 0; i < NUM_IN_LISTS; i++) {
            intList.add(i);
            intArray[i] = i;
            floatList.add(i * 1.001f);
            chars1Arr[i] = (char) i;
            chars2Arr[i] = (char) (NUM_IN_LISTS - i);
        }
    }

    @TearDown
    public void tearDown() {
        server.close();
    }

    @Benchmark
    public void emptyMethod() {
        client.f1();
    }

    @Benchmark
    public int longParamsWithReturn() {
        return client.f2(intList);
    }

    @Benchmark
    public float longParamsWithNoResult() {
        return client.f3(floatList);
    }

    @Benchmark
    public List<Integer> longParamsReturns() {
        return client.f4(intArray);
    }

    @Benchmark
    public String twoParams() {
        return client.f5(chars1Arr, chars2Arr);
    }

    @Benchmark
    public Integer noResult() {
        return client.f6(PORT, NUM_IN_LISTS);
    }
}
