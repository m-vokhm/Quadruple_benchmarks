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

import com.mvohm.quadruple.Quadruple;
import com.mvohm.quadruple.quadruple_benchmark.ParseData.StringSet;

/**
 * Parsing of strings: {@link Quadruple#assign(String)} into preallocated instances
 * (the constructor {@code Quadruple(String)} just creates an empty instance and calls {@code assign()}).
 *
 * <p>As in {@link QuadArithmeticBench}, each invocation processes {@link #SIZE} strings,
 * the reported time is per one string.
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(value = 3, jvmArgsAppend = {"-Xms2g", "-Xmx2g"})
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
public class ParseBench {

  static final int SIZE = QuadArithmeticBench.SIZE;

  @Param({"SHORT", "FULL", "LONG", "BIG_EXP", "SUBNORMAL", "INTEGER", "SPECIAL"})
  private StringSet stringSet;

  private String[] strings;
  private Quadruple[] results;

  @Setup(Level.Trial)
  public void setup() {
    strings = ParseData.strings(stringSet, SIZE);
    results = new Quadruple[SIZE];
    for (int i = 0; i < SIZE; i++)
      results[i] = new Quadruple();
  }

  @Benchmark @OperationsPerInvocation(SIZE)
  public void assign_String() {
    for (int i = 0; i < SIZE; i++)
      results[i].assign(strings[i]);
  }

}
