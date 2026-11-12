package demo;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OperationsPerInvocation;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
@Fork(value = 2, jvmArgs = {"-Xms4G", "-Xmx4G"})
public class CounterBenchmark {

    private CounterSync counterSync;
    private CounterLock counterLock;
    private CounterAtomic counterAtomic;
    private CounterVarHandle counterVarHandle;

    @Setup(Level.Iteration)
    public void setupIteration() {
        counterSync = new CounterSync();
        counterLock = new CounterLock();
        counterAtomic = new CounterAtomic();
        counterVarHandle = new CounterVarHandle();
    }

    @Benchmark
    @OperationsPerInvocation(2)
    public void testCounterSync(final Blackhole blackhole) {
        blackhole.consume(counterSync.increment());
        blackhole.consume(counterSync.decrement());
    }

    @Benchmark
    @OperationsPerInvocation(2)
    public void testCounterLock(final Blackhole blackhole) {
        blackhole.consume(counterLock.increment());
        blackhole.consume(counterLock.decrement());
    }

    @Benchmark
    @OperationsPerInvocation(2)
    public void testCounterAtomic(final Blackhole blackhole) {
        blackhole.consume(counterAtomic.increment());
        blackhole.consume(counterAtomic.decrement());
    }

    @Benchmark
    @OperationsPerInvocation(2)
    public void testCounterVarHandle(final Blackhole blackhole) {
        blackhole.consume(counterVarHandle.increment());
        blackhole.consume(counterVarHandle.decrement());
    }
}
