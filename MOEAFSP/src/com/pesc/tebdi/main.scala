package com.pesc.tebdi

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext

import com.pesc.tebdi.adaptor.MOEAFrameworkAdaptor

import chapter.KnapsackProblem

object main extends App {

  implicit def arrayToList[A](a: Array[A]) = a.toList

  //Start the Spark context
  val conf = new SparkConf()
    .setAppName("main")
    .setMaster("local")

  val sc = new SparkContext(conf)

  val runner = new MOEAFrameworkAdaptor()

  val problem = new KnapsackProblem();

  val iniPopulation = runner.generateRandomPopulation(problem, 5000)

  val (result, population) = runner.runNSGAII_SP(sc, problem, iniPopulation)

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