package com.pesc.tebdi

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext

import com.pesc.tebdi.adaptor.MOEAFrameworkAdaptor

object main extends App {

  implicit def arrayToList[A](a: Array[A]) = a.toList

  //Start the Spark context
  val conf = new SparkConf()
    .setAppName("main")
    .setMaster("local")
  val sc = new SparkContext(conf)

  val runner = new MOEAFrameworkAdaptor()

  val (result, population) = runner.runNSGAII_SP(sc)

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