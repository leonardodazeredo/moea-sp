package com.pesc.tebdi

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext

import com.pesc.tebdi.adaptor.MOEAFrameworkAdaptor
import com.pesc.tebdi.core.IslandsSpark
import com.pesc.tebdi.core.OptimizationContext

import chapter.KnapsackProblem

object main {

  implicit def arrayToList[A](a: Array[A]) = a.toList

  def main(args: Array[String]): Unit = {

    val conf = new SparkConf()
      .setAppName("main")
      .setMaster("local")

    val sc = new SparkContext(conf)

    //    test_masterSlave(sc)
    test_islands(sc)
  }

  def test_islands(sc: SparkContext) {

    val islandsRunner = new IslandsSpark()

    val problem = new KnapsackProblem();

    val moeaAdaptor = new MOEAFrameworkAdaptor()

    val pc = OptimizationContext(moeaAdaptor, problem,
      totalPopulationSize = 50000,
      numOfIslands = 100,
      migrationSizeInIslandPercentage = 0.1,
      numOfMigrations = 4,
      numberOfEvaluationsInIslandRatio = 10)

    val (result, population) = islandsRunner.runSingleJob(sc, pc)

    for ((solution, i) <- result.toList.zipWithIndex) {
      var objectives = solution.getObjectives();
      println("Solution " + i + ":");
      println("	" + objectives(0));
      println("	" + objectives(1));
      println("	" + solution.getVariable(0));
    }

    moeaAdaptor.showPlot("NSGAII", result)

    println("Final population size: " + population.size)
  }

  def test_masterSlave(sc: SparkContext) {
    val moeaAdaptor = new MOEAFrameworkAdaptor()

    val problem = new KnapsackProblem();

    val pc = OptimizationContext(moeaAdaptor, problem,
      totalPopulationSize = 50000,
      numOfIslands = 100,
      migrationSizeInIslandPercentage = 0.1,
      numOfMigrations = 4,
      numberOfEvaluationsInIslandRatio = 10)

    val iniPopulation = moeaAdaptor.generateRandomPopulation(problem, pc.totalPopulationSize)

    val (result, population) = moeaAdaptor.runNSGAII_MasterSlave_Sp(sc, pc, iniPopulation)

    for ((solution, i) <- result.toList.zipWithIndex) {
      var objectives = solution.getObjectives();
      println("Solution " + i + ":");
      println("	" + objectives(0));
      println("	" + objectives(1));
      println("	" + solution.getVariable(0));
    }

    moeaAdaptor.showPlot("NSGAII", result.toList)

    println("Final population size: " + population.size)
  }

}