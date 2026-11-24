package com.mvohm.quadruple.quadruple_benchmark;

import java.math.BigDecimal;
import java.util.Random;

import com.mvohm.quadruple.Quadruple;

/**
 * Generates reproducible operand sets for the benchmarks.
 *
 * <p>The speed of {@link Quadruple} operations depends on the code branch the operands lead to,
 * so the operands are generated in several {@link DataSet data sets}, each targeting a particular branch.
 * All the values are pseudo-random with a fixed seed, so every run and every version of
 * {@code Quadruple} under test gets exactly the same operands. All elements are distinct objects
 * with independently generated values.
 *
 * <p>The values are built directly from their binary parts (sign, exponent, two 64-bit words of the mantissa),
 * so that the generation does not depend on the arithmetic of the {@code Quadruple} version being benchmarked.
 */
public final class BenchData {

  /** The seed of the pseudo-random generator; the same for all runs. */
  public static final long SEED = 20261124L;

  /** The kinds of operand pairs, each leading to a particular branch of the {@code Quadruple} code. */
  public enum DataSet {
    /**
     * The common case: random signs, random 128-bit mantissas,
     * unbiased binary exponents in the range -64..64 (values roughly from 5e-20 to 4e19).
     * The exponents of the operands are close, so addition and subtraction really combine both mantissas.
     */
    NORMAL,

    /**
     * The exponents of the operands differ by 130..1129, so that for addition and subtraction the smaller operand
     * falls entirely beyond the 128 bits of the mantissa of the larger one. Which of the operands is the larger
     * is chosen randomly. The exponents stay within -1200..1200, far from overflow or underflow.
     */
    WIDE_EXP,

    /**
     * Both operands are subnormal (exponent 0, random non-zero mantissa, random sign).
     * Addition, subtraction and square root work with subnormal inputs;
     * division gives normal results of moderate magnitude; multiplication underflows to zero.
     */
    SUBNORMAL,

    /**
     * The first operand is subnormal, the second one is a normal number in the range 0.5..2.
     * Multiplication and division then take subnormal inputs and give subnormal results;
     * addition and subtraction degenerate (the subnormal operand is negligible).
     */
    SUBNORMAL_MIXED,
  }

  /** A pair of equal-length operand arrays; {@code first[i]} and {@code second[i]} form the i-th pair. */
  public record Operands(Quadruple[] first, Quadruple[] second) {}

  private static final int NORMAL_EXP_RANGE   = 64;   // NORMAL: unbiased exponents in -64..64
  private static final int WIDE_EXP_MIN_DIFF  = 130;  // WIDE_EXP: the exponents differ at least by this
  private static final int WIDE_EXP_SPREAD    = 1000; // WIDE_EXP: and at most by WIDE_EXP_MIN_DIFF + WIDE_EXP_SPREAD - 1

  private BenchData() {}

  /**
   * Generates {@code size} operand pairs of the given kind.
   * The result depends only on {@code dataSet} and {@code size}.
   */
  public static Operands operands(DataSet dataSet, int size) {
    final Random rand = new Random(SEED + dataSet.ordinal());
    final Quadruple[] first = new Quadruple[size], second = new Quadruple[size];
    for (int i = 0; i < size; i++) {
      switch (dataSet) {
        case NORMAL -> {
          first[i]  = randomNormal(rand, randomExponent(rand, -NORMAL_EXP_RANGE, NORMAL_EXP_RANGE));
          second[i] = randomNormal(rand, randomExponent(rand, -NORMAL_EXP_RANGE, NORMAL_EXP_RANGE));
        }
        case WIDE_EXP -> {
          final int largerExp  = randomExponent(rand, -NORMAL_EXP_RANGE, NORMAL_EXP_RANGE);
          final int smallerExp = largerExp - WIDE_EXP_MIN_DIFF - rand.nextInt(WIDE_EXP_SPREAD);
          final boolean firstIsLarger = rand.nextBoolean();
          first[i]  = randomNormal(rand, firstIsLarger ? largerExp : smallerExp);
          second[i] = randomNormal(rand, firstIsLarger ? smallerExp : largerExp);
        }
        case SUBNORMAL -> {
          first[i]  = randomSubnormal(rand);
          second[i] = randomSubnormal(rand);
        }
        case SUBNORMAL_MIXED -> {
          first[i]  = randomSubnormal(rand);
          second[i] = randomNormal(rand, randomExponent(rand, -1, 0));
        }
      }
    }
    return new Operands(first, second);
  }

  /** Returns new instances holding the absolute values of the given ones (e.g. as arguments for {@code sqrt}). */
  public static Quadruple[] absValues(Quadruple[] values) {
    final Quadruple[] result = new Quadruple[values.length];
    for (int i = 0; i < values.length; i++)
      result[i] = new Quadruple(values[i]).abs();
    return result;
  }

  /**
   * Converts the given values to {@code BigDecimal}.
   * Practical only for moderate exponents ({@link DataSet#NORMAL}, {@link DataSet#WIDE_EXP}):
   * subnormal values would require decimal numbers of hundreds of millions of digits.
   */
  public static BigDecimal[] toBigDecimals(Quadruple[] values) {
    final BigDecimal[] result = new BigDecimal[values.length];
    for (int i = 0; i < values.length; i++)
      result[i] = values[i].bigDecimalValue();
    return result;
  }

  /** Returns new instances with the same values, to be used as mutable working copies. */
  public static Quadruple[] copies(Quadruple[] values) {
    final Quadruple[] result = new Quadruple[values.length];
    for (int i = 0; i < values.length; i++)
      result[i] = new Quadruple(values[i]);
    return result;
  }

  /** A random unbiased exponent in the range {@code min..max} inclusive. */
  private static int randomExponent(Random rand, int min, int max) {
    return min + rand.nextInt(max - min + 1);
  }

  /** A normal value with a random sign, a random 128-bit mantissa and the given unbiased exponent. */
  private static Quadruple randomNormal(Random rand, int unbiasedExponent) {
    return new Quadruple(rand.nextBoolean(), Quadruple.EXPONENT_BIAS + unbiasedExponent,
                         rand.nextLong(), rand.nextLong());
  }

  /** A subnormal value with a random sign and a random non-zero 128-bit mantissa. */
  private static Quadruple randomSubnormal(Random rand) {
    long mantHi, mantLo;
    do {
      mantHi = rand.nextLong();
      mantLo = rand.nextLong();
    } while ((mantHi | mantLo) == 0);
    return new Quadruple(rand.nextBoolean(), Quadruple.EXPONENT_OF_SUBNORMAL, mantHi, mantLo);
  }

}
