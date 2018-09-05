package com.pesc.tebdi.core

import scala.util.Random

import com.pesc.tebdi.util.Utils

object SparkFunctions extends Serializable {

  def inIslandRun(pc: OptimizationContext, islandId: Int, iter: Iterator[Individual]): Iterator[Individual] = {

    val parent_population = iter.toList.map(ind => ind._2)

    val (result, descendant_population) = pc.moeaAdaptor.runNSGAII(pc, parent_population)

    descendant_population.map(s => (islandId, s))
  }

  def setNewIslands(pc: OptimizationContext, islandId: Int, iter: Iterator[Individual]): Iterator[Individual] = {

    val individualList = iter.toList

    val islandsIdsList = List.range(0, pc.numOfIslands).filterNot(i => i == islandId)

    val indexesList = Random.shuffle(List.range(0, individualList.size)).take((pc.migrationSizeInIslandPercentage * individualList.size).toInt)

    val random = new Random()

    val newIndividualArray = individualList.toArray

    for (i <- indexesList) {
      val newIslandId = Utils.getRandomElement(islandsIdsList.to[Seq], random)
      newIndividualArray(i) = (newIslandId, individualList(i)._2)
    }

    newIndividualArray.toList.iterator
  }

  def getNondominatedPopulationInIsland(oc: OptimizationContext, islandId: Int, iter: Iterator[Individual]): Iterator[Individual] = {

    var population = iter.toList.map(ind => ind._2)

    population = oc.moeaAdaptor.getNondominatedPopulation(population).toList

    val individual = population.map(s => (islandId, s))

    individual.iterator
  }
}