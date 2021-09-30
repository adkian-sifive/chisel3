// SPDX-License-Identifier: Apache-2.0

// Tests which demonstrate the error conditions and the resulting error
//   messages for cross-module value reads and writes.
// Elaboration of OuterAssignExample and OuterReadExample is expected;
//   the error messages are intercepted and printed

package chiselTests

import chisel3._
import chisel3.stage.ChiselStage
import chisel3.testers.BasicTester

class InnerExample extends Module {
  val a, b, c  = IO(Input(Bool()))
  val d, e, f  = IO(Input(Bool()))
  val foo, bar = IO(Input(UInt(8.W)))
  val out      = IO(Output(UInt(8.W)))

  val myReg = RegInit(0.U(8.W))
  out := myReg

  when (a && b && c) {
    myReg := foo
  }
  when (d && e && f) {
    myReg := bar
  }
}


class OuterAssignExample extends Module {
  val a, b, c  = IO(Input(Bool()))
  val d, e, f  = IO(Input(Bool()))
  val foo, bar = IO(Input(UInt(8.W)))
  val out      = IO(Output(UInt(8.W)))

  val myReg = RegInit(0.U(8.W))
  out := myReg

  when (a && b && c) {
    myReg := foo
  }
  when (d && e && f) {
    myReg := bar
  }
  
  val inner = Module(new InnerExample())
  
  inner.myReg := false.B
}

class OuterReadExample extends Module {
  val a, b, c  = IO(Input(Bool()))
  val d, e, f  = IO(Input(Bool()))
  val foo, bar = IO(Input(UInt(8.W)))
  val out      = IO(Output(UInt(8.W)))

  val myReg = RegInit(0.U(8.W))
  out := myReg

  when (a && b && c) {
    myReg := foo
  }
  when (d && e && f) {
    myReg := bar
  }
  
  val inner = Module(new InnerExample())
  
  myReg := inner.myReg
}

class XModuleRefError extends ChiselPropSpec {
  property("Assign elaboration should fail") {
    val errout = intercept[Exception] {ChiselStage.elaborate { new OuterAssignExample }}
    println(errout)
  }

  property("Read elaboration should fail") {
    val errout = intercept[Exception] {ChiselStage.elaborate { new OuterReadExample }}
    println(errout)
  }
}
