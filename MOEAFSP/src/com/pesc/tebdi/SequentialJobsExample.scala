package com.pesc.tebdi.util

import org.apache.spark.SparkConf
import com.pesc.tebdi.adaptor.MOEAFrameworkAdaptor
import org.apache.spark.SparkContext
import chapter.KnapsackProblem
import com.pesc.tebdi.core.IslandsSparkSequentialRunner
import com.pesc.tebdi.core.OptimizationContext
import org.moeaframework.core.Solution

object SequentialJobsExample {

  def main(args: Array[String]): Unit = {
    implicit def arrayToList[A](a: Array[A]) = a.toList

    val conf = new SparkConf()
      .setAppName("main")
      .setMaster("local")

    val sc = new SparkContext(conf)

    val problem = new KnapsackProblem();

    val moeaAdaptor = new MOEAFrameworkAdaptor()

    val oc = OptimizationContext(moeaAdaptor, problem,
      totalPopulationSize = 50000,
      numOfIslands = 100,
      migrationSizeInIslandPercentage = 0.1,
      numOfMigrations = 4,
      numberOfEvaluationsInIslandRatio = 10)

    val runner = new IslandsSparkSequentialRunner(sc, oc)

    runner.run()

    Thread.sleep(20000)

    runner.requestStop()

    val result = runner.getNondominatedPopulation()

    moeaAdaptor.printPopulation(result.toList)

    moeaAdaptor.showPlot("NSGAII", result)

  }
}