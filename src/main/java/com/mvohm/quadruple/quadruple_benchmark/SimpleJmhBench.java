package com.mvohm.quadruple.quadruple_benchmark;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.Random;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import com.mvohm.quadruple.Quadruple;
import static com.mvohm.quadruple.quadruple_benchmark.AuxMethods.*;

/**
 * A simple Q&D hand-made benchmarking tool for {@link Quadruple} arithmetic,
 * using JMH (see https://github.com/openjdk/jmh)
 *
 * @author M.Vokhmentev
 *
 */

@State(value = Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(java.util.concurrent.TimeUnit.NANOSECONDS)
@Fork(value = 1)
@Warmup(iterations = 3, time = 5)
@Measurement(iterations = 10, time = 10)

public class SimpleJmhBench {

  // The size of the data arrays whose elements are used to evaluate the arithmetic.
  private static final int DATA_SIZE        =  0x1_0000;  // 65_536
                                            // = 0x10_0000;  // 1 048 576
                                            // = 0x20_0000;  // 2 097 152
                                            // = 0x40_0000;  // 4 194 304
                                            // = 0x80_0000;  // 8 388 608;

  // To perform BigDecimal arithmetic with the precision comparable with this of Quadruple
  private static final MathContext MC_38 = new MathContext(38, RoundingMode.HALF_EVEN);

  private static final int INDEX_MASK     = DATA_SIZE - 1;  // 0xFFFF for 0x10_0000 etc
  private static final int RAND_SEED      = 12345;

  private static final double RAND_SCALE  = 1e39; // To provide a sensible range of operands,
                                                  // so that the actual calculations don't get bypassed

  // The data arrays whose elements are used to evaluate the arithmetic.
  // Filled with random values ​​to avoid systematic errors associated
  // with different calculation paths for different operand values
  private final BigDecimal[]
      bdOperands1     = new BigDecimal[DATA_SIZE],
      bdOperands2     = new BigDecimal[DATA_SIZE],
      bdResults       = new BigDecimal[DATA_SIZE];

  private final Quadruple[]
      quadOperands1_0    = new Quadruple[DATA_SIZE],
      quadOperands1      = new Quadruple[DATA_SIZE],
      quadOperands2      = new Quadruple[DATA_SIZE],
      quadResults   = new Quadruple[DATA_SIZE];

  private int index = 0;

  private Blackhole blackHole;

  @Setup(Level.Trial)
  public void initPatterns(Blackhole blackHole) {
    Locale.setDefault(Locale.US);
    say_("\nGenerating test data, size = %,d\n", DATA_SIZE);
    this.blackHole = blackHole;
    final Random rand = new Random(RAND_SEED); // for reproducibility

    // Fill 1/8 of the buffer with random numbers. Filling the whole buffer may take too long
    for (int i = 0; i < DATA_SIZE / 8; i++) {
      bdOperands1[i] = randomBigDecimal(rand);
      bdOperands2[i] = randomBigDecimal(rand);
      quadOperands1_0[i] = randomQuadruple(rand);
      quadOperands1[i] = quadOperands1_0[i];
      quadOperands2[i] = randomQuadruple(rand);
      if (i % ((DATA_SIZE / 8) / 10) == 0)
        say_(".");
    }

    // Fill the remaining 7/8 with copies of the first 1/8
    for (int i = DATA_SIZE / 8; i < DATA_SIZE; i++) {
      bdOperands1[i] = bdOperands1[i % (DATA_SIZE / 8)];
      bdOperands2[i] = bdOperands2[i % (DATA_SIZE / 8)];                  // these are immutable
      quadOperands1_0[i] = new Quadruple(quadOperands1_0[i % (DATA_SIZE / 8)]); // These will change. Need to refill the buffer after each iteration,
                                                              // so keep once generated number in a separate buffer
      quadOperands1[i] = quadOperands1_0[i];
      quadOperands2[i] = quadOperands2[i % (DATA_SIZE / 8)]; // These don't change
    }
    say("Ready. ");

  }

  @Setup(Level.Invocation)
  public void updateData() {
    if (index == 0) {
//      say_("-");
      copyArray(quadOperands1_0, quadOperands1);
//      say_("- ");
    }
  }

  private void copyArray(Quadruple[] src, Quadruple[] dst) {
    for (int i = 0; i < src.length; i++)
      dst[i] = new Quadruple(src[i]);
  }

  private static Quadruple randomQuadruple(Random rand) {
    return Quadruple.nextRandom(rand).multiply(RAND_SCALE);
  }

  private static BigDecimal randomBigDecimal(Random rand) {
    return Quadruple.nextRandom(rand).multiply(RAND_SCALE).bigDecimalValue();
  }

  //********************************

  @Benchmark
  public void a1_BigDecimal___Addition() {
    blackHole.consume(bdResults[index] = bdOperands1[index].add(bdOperands2[index], MC_38));
    index = ++index & INDEX_MASK;
  }

  @Benchmark
  public void a2_QuadStatic___Addition() {
    blackHole.consume(quadResults[index] = Quadruple.add(quadOperands1[index], quadOperands2[index]));
    index = ++index & INDEX_MASK;
  }

  @Benchmark
  public void a3_QuadInstance_Addition() {
    blackHole.consume(quadOperands1[index].add(quadOperands2[index]));
    index = ++index & INDEX_MASK;
  }

  //********************************

  @Benchmark
  public void b1_BigDecimal___Subtraction() {
    blackHole.consume(bdResults[index] = bdOperands1[index].subtract(bdOperands2[index], MC_38));
    index = ++index & INDEX_MASK;
  }

  @Benchmark
  public void b2_QuadStatic___Subtraction() {
    blackHole.consume(quadResults[index] = Quadruple.subtract(quadOperands1[index], quadOperands2[index]));
    index = ++index & INDEX_MASK;
  }

  @Benchmark
  public void b3_QuadInstance_Subtraction() {
    blackHole.consume(quadOperands1[index].subtract(quadOperands2[index]));
    index = ++index & INDEX_MASK;
  }

  //********************************

  @Benchmark
  public void c1_BigDecimal___Multiplication() {
    blackHole.consume(bdResults[index] = bdOperands1[index].multiply(bdOperands2[index], MC_38));
    index = ++index & INDEX_MASK;
  }

  @Benchmark
  public void c2_QuadStatic___Multiplication() {
    blackHole.consume(quadResults[index] = Quadruple.multiply(quadOperands1[index], quadOperands2[index]));
    index = ++index & INDEX_MASK;
  }

  @Benchmark
  public void c3_QuadInstance_Multiplication() {
    blackHole.consume(quadOperands1[index].multiply(quadOperands2[index]));
    index = ++index & INDEX_MASK;
  }

  //********************************

  @Benchmark
  public void d1_BigDecimal___Division() {
    blackHole.consume(bdResults[index] = bdOperands1[index].divide(bdOperands2[index], MC_38));
    index = ++index & INDEX_MASK;
  }

  @Benchmark
  public void d2_QuadStatic___Division() {
    blackHole.consume(quadResults[index] = Quadruple.divide(quadOperands1[index], quadOperands2[index]));
    index = ++index & INDEX_MASK;
  }

  @Benchmark
  public void d3_QuadInstance_Division() {
    blackHole.consume(quadOperands1[index].divide(quadOperands2[index]));
    index = ++index & INDEX_MASK;
  }

  /**
   * @param args
   * @throws IOException
   */
  private void run(String... args) throws IOException, RunnerException {
    Locale.setDefault(Locale.US);
    final Options opt = new OptionsBuilder()
        .include(SimpleJmhBench.class.getSimpleName())
        .forks(1)
        .build();
    new Runner(opt).run();
  }

  public static void main(String... args) throws IOException, RunnerException {
    new SimpleJmhBench().run(args);
  }


}
