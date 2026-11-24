package com.mvohm.quadruple.quadruple_benchmark;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OperationsPerInvocation;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import com.mvohm.quadruple.quadruple_benchmark.BenchData.DataSet;
import com.mvohm.quadruple.quadruple_benchmark.BenchData.Operands;

/**
 * {@link BigDecimal} arithmetic with the precision of 38 decimal digits, for comparison with
 * {@link QuadArithmeticBench}. The operands are the same values as there, converted to {@code BigDecimal};
 * the subnormal data sets are not applicable.
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(value = 3, jvmArgsAppend = {"-Xms2g", "-Xmx2g"})
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
public class BigDecimalArithmeticBench {

  static final int SIZE = QuadArithmeticBench.SIZE;

  /** Precision comparable with that of Quadruple (128 bits ~ 38.5 decimal digits) */
  private static final MathContext MC_38 = new MathContext(38, RoundingMode.HALF_EVEN);

  @Param({"NORMAL", "WIDE_EXP"})
  private DataSet dataSet;

  private BigDecimal[] op1, op2, sqrtArgs;

  @Setup(Level.Trial)
  public void setup() {
    final Operands operands = BenchData.operands(dataSet, SIZE);
    op1 = BenchData.toBigDecimals(operands.first());
    op2 = BenchData.toBigDecimals(operands.second());
    sqrtArgs = BenchData.toBigDecimals(BenchData.absValues(operands.first()));
  }

  @Benchmark @OperationsPerInvocation(SIZE)
  public void add(Blackhole bh) {
    for (int i = 0; i < SIZE; i++)
      bh.consume(op1[i].add(op2[i], MC_38));
  }

  @Benchmark @OperationsPerInvocation(SIZE)
  public void subtract(Blackhole bh) {
    for (int i = 0; i < SIZE; i++)
      bh.consume(op1[i].subtract(op2[i], MC_38));
  }

  @Benchmark @OperationsPerInvocation(SIZE)
  public void multiply(Blackhole bh) {
    for (int i = 0; i < SIZE; i++)
      bh.consume(op1[i].multiply(op2[i], MC_38));
  }

  @Benchmark @OperationsPerInvocation(SIZE)
  public void divide(Blackhole bh) {
    for (int i = 0; i < SIZE; i++)
      bh.consume(op1[i].divide(op2[i], MC_38));
  }

  @Benchmark @OperationsPerInvocation(SIZE)
  public void sqrt(Blackhole bh) {
    for (int i = 0; i < SIZE; i++)
      bh.consume(sqrtArgs[i].sqrt(MC_38));
  }

}
