package com.mvohm.quadruple.quadruple_benchmark;

import java.util.Locale;
import java.util.Random;

/**
 * Generates reproducible sets of strings for the parsing benchmarks.
 *
 * <p>The strings are built from random digits with a fixed seed, without using {@code Quadruple},
 * so they are exactly the same for any version of {@code Quadruple} being benchmarked.
 * The speed of parsing depends on the number of digits and the magnitude of the exponent,
 * not on whether the string represents an exact {@code Quadruple} value, so random digits are adequate.
 */
public final class ParseData {

  /** The kinds of strings, each loading a particular part of the parser. */
  public enum StringSet {
    /** Typical user input: 1..9 significant digits, e.g. {@code 123.45}, {@code -0.001}, {@code 6.02e23}. */
    SHORT,
    /** 40 significant digits in the {@code toString()} format, e.g. {@code -1.234...e-17}; exponents -40..40. */
    FULL,
    /** 60..100 significant digits (more than the precision, require rounding); exponents -40..40. */
    LONG,
    /** 40 significant digits, decimal exponents of random magnitude up to the limits of the normal range. */
    BIG_EXP,
    /** 40 significant digits, exponents within the subnormal range (-646456994..-646457030). */
    SUBNORMAL,
    /** Integers of 1..38 digits without a decimal point or an exponent, e.g. {@code 12345678901234567890}. */
    INTEGER,
    /** Designations of special values and zeros, e.g. {@code NaN}, {@code -Infinity}, {@code MAX_VALUE}, {@code 0.0}. */
    SPECIAL,
  }

  /** The decimal exponent of {@code Quadruple.MAX_VALUE} is 646456993, of {@code MIN_NORMAL} is -646456993. */
  private static final int MAX_NORMAL_EXP10 = 646_456_992;
  private static final int SUBNORMAL_EXP10_MAX = -646_456_994;    // Below MIN_NORMAL
  private static final int SUBNORMAL_EXP10_MIN = -646_457_030;    // Above MIN_VALUE (~ 6.7e-646457032)

  private static final String[] SPECIAL_VALUES = {
      "NaN", "Infinity", "-Infinity", "+Infinity", "MIN_VALUE", "MAX_VALUE", "MIN_NORMAL",
      "Quadruple.MAX_VALUE", "Quadruple.NaN", "0", "0.0", "-0.0", "0e0",
  };

  private ParseData() {}

  /** Generates {@code size} strings of the given kind. The result depends only on {@code set} and {@code size}. */
  public static String[] strings(StringSet set, int size) {
    final Random rand = new Random(BenchData.SEED + 200 + set.ordinal());
    final String[] result = new String[size];
    for (int i = 0; i < size; i++) {
      result[i] = switch (set) {
        case SHORT      -> shortString(rand);
        case FULL       -> scientific(rand, 40, -40 + rand.nextInt(81));
        case LONG       -> scientific(rand, 60 + rand.nextInt(41), -40 + rand.nextInt(81));
        case BIG_EXP    -> scientific(rand, 40, bigExponent(rand));
        case SUBNORMAL  -> scientific(rand, 40, SUBNORMAL_EXP10_MIN + rand.nextInt(SUBNORMAL_EXP10_MAX - SUBNORMAL_EXP10_MIN + 1));
        case INTEGER    -> sign(rand) + digits(rand, 1 + rand.nextInt(38));
        case SPECIAL    -> SPECIAL_VALUES[rand.nextInt(SPECIAL_VALUES.length)];
      };
    }
    return result;
  }

  /**
   * 1..9 significant digits with the decimal point at a random position (possibly with leading zeros, like 0.00123),
   * a third of them with an exponent in the range -30..30.
   */
  private static String shortString(Random rand) {
    final String digits = digits(rand, 1 + rand.nextInt(9));
    final int pointPos = rand.nextInt(digits.length() + 4) - 3;   // -3..length: number of digits before the point
    final String number =
        pointPos <= 0               ? "0." + "0".repeat(-pointPos) + digits :
        pointPos >= digits.length() ? digits :
                                      digits.substring(0, pointPos) + "." + digits.substring(pointPos);
    final String exponent = rand.nextInt(3) == 0 ? "e" + (rand.nextInt(61) - 30) : "";
    return sign(rand) + number + exponent;
  }

  /** A number in the form {@code [-]d.ddd...e[+-]nn} with the given number of significant digits. */
  private static String scientific(Random rand, int significantDigits, int exp10) {
    final String digits = digits(rand, significantDigits);
    return String.format(Locale.ROOT, "%s%c.%se%+03d", sign(rand), digits.charAt(0), digits.substring(1), exp10);
  }

  /** A decimal exponent with a log-uniformly distributed magnitude (1 to 9 digits), within the normal range. */
  private static int bigExponent(Random rand) {
    final int magnitudeDigits = 1 + rand.nextInt(9);
    final int lowLimit = magnitudeDigits == 1 ? 0 : (int)Math.pow(10, magnitudeDigits - 1);
    final int highLimit = Math.min((int)Math.min(Math.pow(10, magnitudeDigits), Integer.MAX_VALUE), MAX_NORMAL_EXP10);
    final int magnitude = lowLimit + rand.nextInt(highLimit - lowLimit);
    return rand.nextBoolean() ? -magnitude : magnitude;
  }

  /** A string of the given number of random decimal digits, the first one non-zero. */
  private static String digits(Random rand, int count) {
    final StringBuilder sb = new StringBuilder(count);
    sb.append((char)('1' + rand.nextInt(9)));
    for (int i = 1; i < count; i++)
      sb.append((char)('0' + rand.nextInt(10)));
    return sb.toString();
  }

  private static String sign(Random rand) {
    return rand.nextBoolean() ? "-" : "";
  }

}
