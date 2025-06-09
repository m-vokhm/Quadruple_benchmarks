/*

 Copyright 2021 M.Vokhmentsev

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

*/

package com.mvohm.quadruple.quadruple_benchmark;

/**
 * A set of convenience static methods to be used by other classes of the quadruple.quadruple_benchmark package.
 * Includes mainly a number of methods to convert values of various types to each other,
 * and to output values to console.
 *
 * @author M.Vokhmentev
 *
 */

public class AuxMethods {

 /****************************************************************************************
  *** Output to the console **************************************************************
  ****************************************************************************************/

  /** == System.out.println(); */
	public static void say() 	{ System.out.println(); }

	/** == System.out.println(Object o);
	 * @param o {@code Object} to print */
	public static void say(Object o) 	{ System.out.println(o); }

	/** == System.out.print(Object o);
   * @param o {@code Object} to print */
	public static void say_(Object o) 	{ System.out.print(o); }

	/** == System.out.println(String.format(String format, Object... args)
	 * @param format a format string to format the {@code args}
	 * @param args arguments to format
	 * @see String#format(String, Object...)
	 */
	public static void say(String format, Object... args) { System.out.println(String.format(format, args)); }

  /** == System.out.print(String.format(String format, Object... args)
   * @param format a format string to format the {@code args}
   * @param args arguments to format
   * @see String#format(String, Object...)
   */
	public static void say_(String format, Object... args) { System.out.print(String.format(format, args)); }

	/** Terminates execution with exit code == 0 */
	public static void exit() { System.exit(0); }

}
