package com.mvohm.quadruple.quadruple_benchmark;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OperationsPerInvocation;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import com.mvohm.quadruple.Quadruple;
import com.mvohm.quadruple.quadruple_benchmark.BenchData.DataSet;

/**
 * Conversions between {@link Quadruple} and other types (parsing of strings is in a separate benchmark).
 *
 * <p>Conversions to {@code Quadruple} are measured with {@code assign()} into preallocated instances
 * (the constructors just create an empty instance and call {@code assign()}).
 * Conversions from {@code Quadruple} use the {@link DataSet#NORMAL} values.
 *
 * <p>As in {@link QuadArithmeticBench}, each invocation processes {@link #SIZE} values,
 * the reported time is per one conversion.
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(value = 3, jvmArgsAppend = {"-Xms2g", "-Xmx2g"})
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
public class ConversionBench {

  static final int SIZE = QuadArithmeticBench.SIZE;

  // Sources for conversions to Quadruple
  private long[]        longs;
  private double[]      normalDoubles, subnormalDoubles;
  private BigDecimal[]  longBigDecimals;    // Exact values of NORMAL Quadruples, ~100 digits
  private BigDecimal[]  shortBigDecimals;   // Up to 19 digits, scale -20..20

  // Sources for conversions from Quadruple
  private Quadruple[]   quads;

  // Targets
  private Quadruple[]   results;
  private double[]      doubleResults;

  @Setup(Level.Trial)
  public void setup() {
    longs             = BenchData.randomLongs(SIZE);
    normalDoubles     = BenchData.randomNormalDoubles(SIZE);
    subnormalDoubles  = BenchData.randomSubnormalDoubles(SIZE);
    quads             = BenchData.operands(DataSet.NORMAL, SIZE).first();
    longBigDecimals   = BenchData.toBigDecimals(quads);
    shortBigDecimals  = BenchData.shortBigDecimals(SIZE);
    results           = BenchData.copies(quads);
    doubleResults     = new double[SIZE];
  }

  // ---- To Quadruple ------------------------------------------------------------------

  @Benchmark @OperationsPerInvocation(SIZE)
  public void assign_long() {
    for (int i = 0; i < SIZE; i++)
      results[i].assign(longs[i]);
  }

  @Benchmark @OperationsPerInvocation(SIZE)
  public void assign_double() {
    for (int i = 0; i < SIZE; i++)
      results[i].assign(normalDoubles[i]);
  }

  @Benchmark @OperationsPerInvocation(SIZE)
  public void assign_doubleSubnormal() {
    for (int i = 0; i < SIZE; i++)
      results[i].assign(subnormalDoubles[i]);
  }

  @Benchmark @OperationsPerInvocation(SIZE)
  public void assign_BigDecimalLong() {
    for (int i = 0; i < SIZE; i++)
      results[i].assign(longBigDecimals[i]);
  }

  @Benchmark @OperationsPerInvocation(SIZE)
  public void assign_BigDecimalShort() {
    for (int i = 0; i < SIZE; i++)
      results[i].assign(shortBigDecimals[i]);
  }

  // ---- From Quadruple ----------------------------------------------------------------

  @Benchmark @OperationsPerInvocation(SIZE)
  public void doubleValue() {
    for (int i = 0; i < SIZE; i++)
      doubleResults[i] = quads[i].doubleValue();
  }

  @Benchmark @OperationsPerInvocation(SIZE)
  public void bigDecimalValue(Blackhole bh) {
    for (int i = 0; i < SIZE; i++)
      bh.consume(quads[i].bigDecimalValue());
  }

  @Benchmark @OperationsPerInvocation(SIZE)
  public void toString(Blackhole bh) {
    for (int i = 0; i < SIZE; i++)
      bh.consume(quads[i].toString());
  }

}
