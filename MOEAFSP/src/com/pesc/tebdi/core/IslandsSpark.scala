package com.pesc.tebdi.core

import scala.collection.JavaConverters._

import org.apache.spark.SparkContext
import org.moeaframework.core.Problem
import org.moeaframework.core.Solution

import com.pesc.tebdi.adaptor.MOEAFrameworkAdaptor

import chapter.KnapsackProblem
import org.moeaframework.core.NondominatedPopulation

case class ProblemContext(problem: Problem)

class IslandsSpark extends Serializable {

  def inIslandRun(pc: ProblemContext, iter: Iterator[Solution]): Iterator[Solution] = {

    val runner = new MOEAFrameworkAdaptor()

    val (result, population) = runner.runNSGAII(pc.problem, iter)

    population
  }

  def run(sc: SparkContext): (Iterator[Solution], Iterator[Solution]) = {

    implicit def arrayToList[A](a: Array[A]) = a.toList

    val runner = new MOEAFrameworkAdaptor()

    val problem = new KnapsackProblem();

    val iniPopulation = runner.generateRandomPopulation(problem, 5555)

    val pc = ProblemContext(problem)

    var rdd = sc.parallelize(iniPopulation.to[Seq])

    rdd = rdd.mapPartitions(iter => inIslandRun(pc, iter), false)

    val ss = rdd.collect.toList

    val mergedSolutions = new NondominatedPopulation(ss.asInstanceOf[List[Solution]].asJava);

    (mergedSolutions.iterator.asScala, ss.iterator)
  }

}