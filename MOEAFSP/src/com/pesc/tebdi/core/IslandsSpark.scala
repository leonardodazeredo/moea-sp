package com.pesc.tebdi.core

import scala.util.Random

import org.apache.spark.SparkContext
import org.moeaframework.core.Solution

import com.pesc.tebdi.partitioner.FollowKeyPartitioner
import com.pesc.tebdi.util.Utils

class IslandsSpark extends Serializable {

  val numOfIslands = 100

  val numOfMigrationsMax = 4

  val migrationPercentage = 0.1

  def inIslandRun(pc: OptimizationContext, islandId: Int, iter: Iterator[Individual]): Iterator[Individual] = {

    val parent_population = iter.toList.map(ind => ind._2)

    val (result, descendant_population) = pc.moeaAdaptor.runNSGAII(pc.problem, parent_population)

    descendant_population.map(s => (islandId, s))
  }

  def setNewIslands(pc: OptimizationContext, islandId: Int, iter: Iterator[Individual]): Iterator[Individual] = {

    val individualList = iter.toList

    val islandsIdsList = List.range(0, pc.numOfIslands).filterNot(i => i == islandId)

    val indexesList = Random.shuffle(List.range(0, individualList.size)).take((pc.migrationPercentage * individualList.size).toInt)

    val random = new Random()

    val newIndividualArray = individualList.toArray

    for (i <- indexesList) {
      val newIslandId = Utils.getRandomElement(islandsIdsList.to[Seq], random)
      newIndividualArray(i) = (newIslandId, individualList(i)._2)
    }

    //    for ((e, i) <- newIndividualArray.toList.zipWithIndex){
    //      println(e)
    //    }

    newIndividualArray.toList.iterator
  }

  def getNondominatedPopulationInIsland(pc: OptimizationContext, islandId: Int, iter: Iterator[Individual]): Iterator[Individual] = {

    var population = iter.toList.map(ind => ind._2)

    population = pc.moeaAdaptor.getNondominatedPopulation(population).toList

    val individual = population.map(s => (islandId, s))

    individual.iterator
  }

  def run(sc: SparkContext, pc: OptimizationContext): (Iterable[Solution], Iterable[Solution]) = {

    implicit def arrayToList[A](a: Array[A]) = a.toList

    var numOfMigrations = 0

    val iniPopulation = pc.moeaAdaptor.generateRandomPopulation(pc.problem, pc.populationSize)

    val iniPopulationWithId = iniPopulation.map(s => (0, s))

    var rdd = sc.parallelize(iniPopulationWithId.to[Seq], numOfIslands)

    for (i <- 0 to numOfMigrationsMax - 1) {
      rdd = rdd.mapPartitionsWithIndex((index, iter) => inIslandRun(pc, index, iter))
      rdd = rdd.mapPartitionsWithIndex((index, iter) => setNewIslands(pc, index, iter))

      rdd = rdd.partitionBy(new FollowKeyPartitioner(numOfIslands))
    }

    rdd = rdd.mapPartitionsWithIndex((index, iter) => inIslandRun(pc, index, iter))
    rdd = rdd.mapPartitionsWithIndex((index, iter) => setNewIslands(pc, index, iter))

    rdd = rdd.mapPartitionsWithIndex((index, iter) => getNondominatedPopulationInIsland(pc, index, iter))

    val final_population = rdd.collect.toList.map(ind => ind._2)

    (pc.moeaAdaptor.getNondominatedPopulation(final_population), final_population)
  }

}