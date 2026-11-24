package com.mvohm.quadruple.quadruple_benchmark;

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

import com.mvohm.quadruple.Quadruple;
import com.mvohm.quadruple.quadruple_benchmark.BenchData.DataSet;
import com.mvohm.quadruple.quadruple_benchmark.BenchData.Operands;

/**
 * Arithmetic of {@link Quadruple} with {@code Quadruple} arguments, in the instance and the static forms.
 *
 * <p>Each benchmark invocation processes all {@link #SIZE} operand pairs; JMH divides the time by {@code SIZE}
 * (see {@link OperationsPerInvocation}), so the reported time is per one operation.
 *
 * <ul>
 * <li>Instance form: {@code r[i].assign(a[i]).add(b[i])} -- the result goes to a preallocated instance,
 *     the operands never change, so every pass computes the same values.
 *     {@link #assign} measures the cost of the copying included in it.
 * <li>Static form: {@code Quadruple.add(a[i], b[i])} -- creates a new instance, which is passed to the
 *     {@link Blackhole} (storing it into an array would add GC write-barrier costs and keep the garbage alive).
 * </ul>
 * The operands are generated once per fork and never change.
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(value = 3, jvmArgsAppend = {"-Xms2g", "-Xmx2g"})
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
public class QuadArithmeticBench {

  /** The number of operand pairs per invocation. 2 x 4K operands of about 40 bytes fit into the L2 cache. */
  static final int SIZE = 4096;

  @Param({"NORMAL", "WIDE_EXP", "SUBNORMAL", "SUBNORMAL_MIXED"})
  private DataSet dataSet;

  private Quadruple[] op1, op2;     // The operands, never change
  private Quadruple[] sqrtArgs;     // Absolute values of op1
  private Quadruple[] results;      // Targets for the instance forms

  @Setup(Level.Trial)
  public void setup() {
    final Operands operands = BenchData.operands(dataSet, SIZE);
    op1 = operands.first();
    op2 = operands.second();
    sqrtArgs = BenchData.absValues(op1);
    results = BenchData.copies(op1);
  }

  // ---- Baseline: the copying included in the instance forms -------------------------

  @Benchmark @OperationsPerInvocation(SIZE)
  public void assign() {
    for (int i = 0; i < SIZE; i++)
      results[i].assign(op1[i]);
  }

  // ---- Instance forms -----------------------------------------------------------------

  @Benchmark @OperationsPerInvocation(SIZE)
  public void add_instance() {
    for (int i = 0; i < SIZE; i++)
      results[i].assign(op1[i]).add(op2[i]);
  }

  @Benchmark @OperationsPerInvocation(SIZE)
  public void subtract_instance() {
    for (int i = 0; i < SIZE; i++)
      results[i].assign(op1[i]).subtract(op2[i]);
  }

  @Benchmark @OperationsPerInvocation(SIZE)
  public void multiply_instance() {
    for (int i = 0; i < SIZE; i++)
      results[i].assign(op1[i]).multiply(op2[i]);
  }

  @Benchmark @OperationsPerInvocation(SIZE)
  public void divide_instance() {
    for (int i = 0; i < SIZE; i++)
      results[i].assign(op1[i]).divide(op2[i]);
  }

  @Benchmark @OperationsPerInvocation(SIZE)
  public void sqrt_instance() {
    for (int i = 0; i < SIZE; i++)
      results[i].assign(sqrtArgs[i]).sqrt();
  }

  // ---- Static forms -------------------------------------------------------------------

  @Benchmark @OperationsPerInvocation(SIZE)
  public void add_static(Blackhole bh) {
    for (int i = 0; i < SIZE; i++)
      bh.consume(Quadruple.add(op1[i], op2[i]));
  }

  @Benchmark @OperationsPerInvocation(SIZE)
  public void subtract_static(Blackhole bh) {
    for (int i = 0; i < SIZE; i++)
      bh.consume(Quadruple.subtract(op1[i], op2[i]));
  }

  @Benchmark @OperationsPerInvocation(SIZE)
  public void multiply_static(Blackhole bh) {
    for (int i = 0; i < SIZE; i++)
      bh.consume(Quadruple.multiply(op1[i], op2[i]));
  }

  @Benchmark @OperationsPerInvocation(SIZE)
  public void divide_static(Blackhole bh) {
    for (int i = 0; i < SIZE; i++)
      bh.consume(Quadruple.divide(op1[i], op2[i]));
  }

  @Benchmark @OperationsPerInvocation(SIZE)
  public void sqrt_static(Blackhole bh) {
    for (int i = 0; i < SIZE; i++)
      bh.consume(Quadruple.sqrt(sqrtArgs[i]));
  }

}
