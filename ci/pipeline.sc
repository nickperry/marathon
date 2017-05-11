#!/usr/bin/env amm

import ammonite.ops._
import ammonite.ops.ImplicitWd._

import $file.provision

import $ivy.`com.amazonaws:aws-java-sdk-s3:1.11.127`
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
import com.amazonaws.services.s3.{ AmazonS3, AmazonS3Client }
import com.amazonaws.services.s3.model.PutObjectRequest
import com.amazonaws.services.s3.transfer.{ TransferManager, TransferManagerBuilder, Upload }
import com.amazonaws.services.s3.transfer.Transfer.TransferState

import java.io.File

// Color definitions
object Colors {
val BrightRed = "\u001b[31;1m"
val BrightGreen = "\u001b[32;1m"
val BrightBlue = "\u001b[34;1m"
val Reset = "\u001b[0m"
}

def stage(name: String)(block: => Unit): Unit = {
  println(s"${Colors.BrightBlue}${"*" * 20}")
  println(name)
  println("*" * 20)
  println(Colors.Reset)

  block
}

/**
 * Runs compile and tests targets of sbt.
 */
@main
def compileAndTest(): Unit = stage("Compile and Test") {
  //%('sbt, 'clean, 'coverage, 'testWithCoverageReport, "integration:test", 'scapegoat)
  %('sbt, 'test)
}

/**
 * Create tarball and docker image.
 */
@main
def packageTarball(): Unit = stage("Package") {
  //%('sbt, 'clean, "universal:packageXzTarball")
  %('sbt, ";universal:packageBin ;universal:packageXzTarball")
}

/**
 * Publish packages to s3.
 */
@main
def publishPackagesS3(): Unit = stage("Publish") {

  val tarball: File = (ls! pwd / 'target / 'universal |? (_.ext == "txz")).head.toIO

  println(s"Publishing ${tarball.getPath()}")

  val s3client = new AmazonS3Client(new DefaultAWSCredentialsProviderChain())
  val transfer: TransferManager = TransferManagerBuilder.standard().withS3Client(s3client).build()
  val request = new PutObjectRequest("marathon-artifacts", "test.txz", tarball)
  val upload: Upload = transfer.upload(request)
  while(!upload.isDone()) {
    val progress = upload.getProgress()
    println(s"${progress.getPercentTransferred()} % ${upload.getState()}")
    Thread.sleep(1000)
  }
  transfer.shutdownNow(true)
  assert(upload.getState() == TransferState.Completed, s"Upload finished with ${upload.getState()}")
}

/**
 * Pipeline for MacOS.
 */
@main
def mac(): Unit = {
  compileAndTest()
}

/**
 * Pipeline definition for Jenkins builds.
 */
@main
def jenkins(): Unit = {
  provision.all()
  compileAndTest()
 // packageTarball()
 // publishPackagesS3()
}
