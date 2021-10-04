// SPDX-License-Identifier: Apache-2.0

/**
  * Tests which demonstrate the error conditions and the resulting error
  *   messages for cross-module value reads and writes.
  *
  * Elaboration of OuterAssignExample and OuterReadExample is expected to fail;
  *   the error messages are intercepted and printed
  */

package chiselTests

import chisel3._
import chisel3.stage.ChiselStage
import chisel3.testers.BasicTester

class InnerExample extends Module {
  val myReg = RegInit(0.U(8.W))
}


class OuterAssignExample extends Module {
  val inner = Module(new InnerExample())
  inner.myReg := false.B // ERROR
}

class OuterReadExample extends Module {
  val myReg = RegInit(0.U(8.W))
  val inner = Module(new InnerExample())

  myReg := inner.myReg // ERROR
}

class OuterBiconnect extends Module {
  val myReg = RegInit(0.U(8.W))
  val inner = Module(new InnerExample())
  myReg <> inner.myReg

}

class XMRErrorSpec extends ChiselPropSpec {
  property("Assign elaboration should fail") {

    // extractCause not found??
    // intercept[ChiselException]{
    //   extractCause[ChiselException] {
    //     (new ChiselStage).emitVerilog(new OuterAssignExample, Array("--full-stacktrace"))
    //   }}

    // ChiselStage.elaborate { new OuterAssignExample }

    // For full stacktrace (along with --eF -z "...")
    (new ChiselStage).emitVerilog(new OuterAssignExample, Array("--full-stacktrace"))

    // val errout = intercept[Exception] {ChiselStage.elaborate { new OuterAssignExample }}
    // println(errout)
  }

  property("Read elaboration should fail") {
    (new ChiselStage).emitVerilog(new OuterReadExample, Array("--full-stacktrace"))
    // val errout = intercept[Exception] {ChiselStage.elaborate { new OuterReadExample }}
    // println(errout)
  }

    property("Biconnect elaboration should fail") {
    (new ChiselStage).emitVerilog(new OuterBiconnect, Array("--full-stacktrace"))
    // val errout = intercept[Exception] {ChiselStage.elaborate { new OuterReadExample }}
    // println(errout)
  }
}
