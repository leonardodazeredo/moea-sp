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

    val pc = OptimizationContext(moeaAdaptor, problem, populationSize = 5555, numOfIslands = 100, migrationPercentage = 0.1)

    val (result, population) = islandsRunner.run(sc, pc)

    var i = 1
    for (solution <- result) {

      var objectives = solution.getObjectives();

      println("Solution " + i + ":");
      println("	" + objectives(0));
      println("	" + objectives(1));
      println("	" + solution.getVariable(0));
      i += 1
    }
    
    moeaAdaptor.showPlot(result)

    println("population size", population.size)
  }

  def test_masterSlave(sc: SparkContext) {
    val runner = new MOEAFrameworkAdaptor()

    val problem = new KnapsackProblem();

    val iniPopulation = runner.generateRandomPopulation(problem, 5555)

    val (result, population) = runner.runNSGAII_MasterSlave_Sp(sc, problem, iniPopulation)

    println(population.size)

    var i = 1
    for (solution <- result) {

      var objectives = solution.getObjectives();

      println("Solution " + i + ":");
      //      println("	" + objectives(0));
      //      println("	" + objectives(1));
      println("	" + solution.getVariable(0));
      i += 1
    }
  }

}