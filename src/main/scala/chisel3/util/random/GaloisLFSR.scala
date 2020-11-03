// SPDX-License-Identifier: Apache-2.0

package chisel3.util.random

import chisel3._

/** Galois Linear Feedback Shift Register (LFSR) generator.
  *
  * A Galois LFSR can be generated by defining a width and a set of tap points. Optionally, an initial seed and a
  * reduction operation ([[XOR]], the default, or [[XNOR]]) can be used to augment the generated hardware. The resulting
  * hardware has support for a run-time programmable seed (via [[PRNGIO.seed]]) and conditional increment (via
  * [[PRNGIO.increment]]).
  *
  * $seedExplanation
  *
  * In the example below, a 4-bit LFSR Fibonacci LFSR is constructed. The tap points are defined as four and three
  * (using LFSR convention of indexing from one). This results in the hardware configuration shown in the diagram.
  *
  * {{{
  * val lfsr4 = Module(new GaloisLFSR(4, Set(4, 3))
  * // +-----------------+---------------------------------------------------------+
  * // |                 |                                                         |
  * // |   +-------+     v     +-------+           +-------+           +-------+   |
  * // |   |       |   +---+   |       |           |       |           |       |   |
  * // +-->|  x^4  |-->|XOR|-->|  x^3  |---------->|  x^2  |---------->|  x^1  |---+
  * //     |       |   +---+   |       |           |       |           |       |
  * //     +-------+           +-------+           +-------+           +-------+
  * }}}
  *
  * If you require a maximal period Galois LFSR of a specific width, you can use [[MaxPeriodGaloisLFSR]]. If you only
  * require a pseudorandom [[UInt]] you can use the [[GaloisLFSR$ GaloisLFSR companion object]].
  * @see [[https://en.wikipedia.org/wiki/Linear-feedback_shift_register#Galois_LFSRs]]
  * $paramWidth
  * $paramTaps
  * $paramSeed
  * $paramReduction
  * $paramStep
  * $paramUpdateSeed
  */
class GaloisLFSR(
  width: Int,
  taps: Set[Int],
  seed: Option[BigInt] = Some(1),
  val reduction: LFSRReduce = XOR,
  step: Int = 1,
  updateSeed: Boolean = false) extends PRNG(width, seed, step, updateSeed) with LFSR {

  def delta(s: Seq[Bool]): Seq[Bool] = {
    val first = s.head
    (s.tail :+ first)
      .zipWithIndex
      .map {
        case (a, i) if taps(i + 1) && (i + 1 != s.size) => reduction(a, first)
        case (a, _)                                     => a
      }
  }

}

/** A maximal period Galois Linear Feedback Shift Register (LFSR) generator. The maximal period taps are sourced from
  * [[LFSR.tapsMaxPeriod LFSR.tapsMaxPeriod]].
  * {{{
  * val lfsr8 = Module(new MaxPeriodGaloisLFSR(8))
  * }}}
  * $paramWidth
  * $paramSeed
  * $paramReduction
  */
class MaxPeriodGaloisLFSR(width: Int, seed: Option[BigInt] = Some(1), reduction: LFSRReduce = XOR)
    extends GaloisLFSR(width, LFSR.tapsMaxPeriod.getOrElse(width, LFSR.badWidth(width)).head, seed, reduction)

/** Utility for generating a pseudorandom [[UInt]] from a [[GaloisLFSR]].
  *
  * For example, to generate a pseudorandom 8-bit [[UInt]] that changes every cycle, you can use:
  * {{{
  * val pseudoRandomNumber = GaloisLFSR.maxPeriod(8)
  * }}}
  *
  * @define paramWidth @param width of pseudorandom output
  * @define paramTaps @param taps a set of tap points to use when constructing the LFSR
  * @define paramIncrement @param increment when asserted, a new random value will be generated
  * @define paramSeed @param seed an initial value for internal LFSR state
  * @define paramReduction @param reduction the reduction operation (either [[XOR]] or
  * [[XNOR]])
  */
object GaloisLFSR {

  /** Return a pseudorandom [[UInt]] generated from a [[FibonacciLFSR]].
    * $paramWidth
    * $paramTaps
    * $paramIncrement
    * $paramSeed
    * $paramReduction
    */
  def apply(
    width: Int,
    taps: Set[Int],
    increment: Bool = true.B,
    seed: Option[BigInt] = Some(1),
    reduction: LFSRReduce = XOR): UInt = PRNG(new GaloisLFSR(width, taps, seed, reduction), increment)

  /** Return a pseudorandom [[UInt]] generated using a maximal period [[GaloisLFSR]]
    * $paramWidth
    * $paramIncrement
    * $paramSeed
    * $paramReduction
    */
  def maxPeriod(
    width: Int,
    increment: Bool = true.B,
    seed: Option[BigInt] = Some(1),
    reduction: LFSRReduce = XOR): UInt = PRNG(new MaxPeriodGaloisLFSR(width, seed, reduction), increment)

}
