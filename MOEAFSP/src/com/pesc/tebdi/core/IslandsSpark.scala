package com.pesc.tebdi.core

import scala.collection.JavaConverters.asScalaIteratorConverter
import scala.collection.JavaConverters.seqAsJavaListConverter
import scala.util.Random

import org.apache.spark.SparkContext
import org.moeaframework.core.NondominatedPopulation
import org.moeaframework.core.Problem
import org.moeaframework.core.Solution

import com.pesc.tebdi.adaptor.MOEAFrameworkAdaptor
import com.pesc.tebdi.partitioner.FollowKeyPartitioner
import com.pesc.tebdi.util.Utils

import chapter.KnapsackProblem

case class ProblemContext(problem: Problem)

class IslandsSpark extends Serializable {

  val numOfIslands = 100

  val numOfMigrationsMax = 4

  val migrationPercentage = 0.1

  def inIslandRun(pc: ProblemContext, islandId: Int, iter: Iterator[Individual]): Iterator[Individual] = {

    val runner = new MOEAFrameworkAdaptor()

    val parent_population = iter.toList.map(ind => ind._2)

    val (result, descendant_population) = runner.runNSGAII(pc.problem, parent_population)

    descendant_population.map(s => (islandId, s))
  }

  def setNewIslands(numOfIslands: Int, migrationPercentage: Double, islandId: Int, iter: Iterator[Individual]): Iterator[Individual] = {

    val individualList = iter.toList

    val islandsIdsList = List.range(0, numOfIslands).filterNot(i => i == islandId)

    val indexesList = Random.shuffle(List.range(0, individualList.size)).take((migrationPercentage * individualList.size).toInt)

    val random = new Random()
    
    val newIndividualArray = individualList.toArray

    for (i <- indexesList) {
      val newIslandId = Utils.getRandomElement(islandsIdsList.to[Seq], random)
      newIndividualArray(i) = (newIslandId, individualList(i)._2)
    }
    
    for ((e, i) <- newIndividualArray.toList.zipWithIndex){
      println(e)
    }

    newIndividualArray.toList.iterator
  }

  def run(sc: SparkContext): (Iterator[Solution], Iterator[Solution]) = {

    implicit def arrayToList[A](a: Array[A]) = a.toList

    var numOfMigrations = 0

    val runner = new MOEAFrameworkAdaptor()

    val problem = new KnapsackProblem();

    val iniPopulation = runner.generateRandomPopulation(problem, 5000)
    var i = 0
    val iniPopulationWithId = iniPopulation.map(s => { i += 1; (i, s) })

    val pc = ProblemContext(problem)

    var rdd = sc.parallelize(iniPopulationWithId.to[Seq], numOfIslands)

    while (numOfMigrations < numOfMigrationsMax) {
      rdd = rdd.mapPartitionsWithIndex((index, iter) => inIslandRun(pc, index, iter))

      rdd = rdd.mapPartitionsWithIndex((index, iter) => setNewIslands(numOfIslands, migrationPercentage, index, iter))

      rdd = rdd.partitionBy(new FollowKeyPartitioner(numOfIslands))

      rdd.cache()

      numOfMigrations += 1
    }

    val final_population = rdd.collect.toList.map(ind => ind._2)

    val mergedSolutions = new NondominatedPopulation(final_population.asInstanceOf[List[Solution]].asJava);

    Utils.showPlot(mergedSolutions)

    (mergedSolutions.iterator.asScala, final_population.iterator)
  }

}