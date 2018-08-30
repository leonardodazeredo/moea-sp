package com.pesc.tebdi.core

import scala.collection.JavaConverters.asScalaIteratorConverter
import scala.collection.JavaConverters.seqAsJavaListConverter

import org.apache.spark.SparkContext
import org.moeaframework.core.NondominatedPopulation
import org.moeaframework.core.Problem
import org.moeaframework.core.Solution

import com.pesc.tebdi.adaptor.MOEAFrameworkAdaptor
import com.pesc.tebdi.partitioner.RandomPartitioner

import chapter.KnapsackProblem

case class ProblemContext(problem: Problem)

class IslandsSpark extends Serializable {

  def inIslandRun(pc: ProblemContext, iter: Iterator[Individual]): Iterator[Individual] = {

    val runner = new MOEAFrameworkAdaptor()

    val parent_population = iter.toList.map(ind => ind._2)

    val (result, descendant_population) = runner.runNSGAII(pc.problem, parent_population)
    var i = 0
    descendant_population.map(s => { i += 1; (i, s) })
  }

  def run(sc: SparkContext): (Iterator[Solution], Iterator[Solution]) = {

    implicit def arrayToList[A](a: Array[A]) = a.toList

    var numOfMigrations = 0

    val runner = new MOEAFrameworkAdaptor()

    val problem = new KnapsackProblem();

    val iniPopulation = runner.generateRandomPopulation(problem, 5555)
    var i = 0
    val iniPopulationWithId = iniPopulation.map(s => { i += 1; (i, s) })

    val pc = ProblemContext(problem)

    var rdd = sc.parallelize(iniPopulationWithId.to[Seq])
    rdd.partitionBy(new RandomPartitioner(10))
    rdd.cache()

    while (numOfMigrations < 100) {
      rdd = rdd.mapPartitions(iter => inIslandRun(pc, iter), false)
      rdd.partitionBy(new RandomPartitioner(10))
      rdd.cache()
      numOfMigrations += 1
    }

    val final_population = rdd.collect.toList.toList.map(ind => ind._2)

    val mergedSolutions = new NondominatedPopulation(final_population.asInstanceOf[List[Solution]].asJava);

    (mergedSolutions.iterator.asScala, final_population.iterator)
  }

}