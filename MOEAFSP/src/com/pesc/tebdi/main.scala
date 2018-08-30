package com.pesc.tebdi

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext

import com.pesc.tebdi.adaptor.MOEAFrameworkAdaptor

import chapter.KnapsackProblem
import com.pesc.tebdi.core.IslandsSpark

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

    val (result, population) = islandsRunner.run(sc)

    var i = 1
    for (solution <- result) {

      var objectives = solution.getObjectives();

      println("Solution " + i + ":");
      println("	" + objectives(0));
      println("	" + objectives(1));
      println("	" + solution.getVariable(0));
      i += 1
    }
  }

  def test_masterSlave(sc: SparkContext) {
    val runner = new MOEAFrameworkAdaptor()

    val problem = new KnapsackProblem();

    val iniPopulation = runner.generateRandomPopulation(problem, 5555)

    val (result, population) = runner.runNSGAII_MasterSlave_Sp(sc, problem, iniPopulation.iterator)

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