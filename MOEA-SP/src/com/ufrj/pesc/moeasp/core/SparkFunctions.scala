package com.ufrj.pesc.moeasp.core

import scala.util.Random

import com.ufrj.pesc.moeasp.util.Utils

object SparkFunctions extends Serializable {

  def inIslandRun(pc: OptimizationContext, islandId: Int, iter: Iterator[Individual]): Iterator[Individual] = {

    val parent_population = iter.toList.map(ind => ind._2)

    val (result, descendant_population) = pc.moeaAdaptor.run(pc, parent_population)

    descendant_population.map(s => (islandId, s))
  }

  def setNewIslands(oc: OptimizationContext, islandId: Int, iter: Iterator[Individual]): Iterator[Individual] = {

    val individualList = iter.toList
    
    val newIndividualArray = oc.moeaAdaptor.markMigration(oc, islandId, individualList)

    newIndividualArray.toList.iterator
  }

  def getNondominatedPopulationInIsland(oc: OptimizationContext, islandId: Int, iter: Iterator[Individual]): Iterator[Individual] = {

    var population = iter.toList.map(ind => ind._2)

    population = oc.moeaAdaptor.getNondominatedPopulation(population).toList

    val individual = population.map(s => (islandId, s))

    individual.iterator
  }
}