// SPDX-License-Identifier: Apache-2.0

/**
  * Tests which demonstrate the error conditions and the resulting error
  *   messages for cross-module value reads and writes.
  *
  * Elaboration of OuterAssignExample and OuterReadExample is expected;
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

class XModuleRefError extends ChiselPropSpec {
  property("Assign elaboration should fail") {
    // ChiselStage.elaborate { new OuterAssignExample }
    (new ChiselStage).emitVerilog(new OuterAssignExample, Array("--full-stacktrace"))
    // val errout = intercept[Exception] {ChiselStage.elaborate { new OuterAssignExample }}
    // println(errout)
  }

  property("Read elaboration should fail") {
    (new ChiselStage).emitVerilog(new OuterReadExample, Array("--full-stacktrace"))
    // val errout = intercept[Exception] {ChiselStage.elaborate { new OuterReadExample }}
    // println(errout)
  }
}
