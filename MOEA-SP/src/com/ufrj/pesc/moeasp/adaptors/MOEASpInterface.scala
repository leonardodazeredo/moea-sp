package com.ufrj.pesc.moeasp.adaptors

import org.apache.spark.SparkContext

import com.ufrj.pesc.moeasp.core.OptimizationContext
import com.ufrj.pesc.moeasp.core.MOEASpProblem
import com.ufrj.pesc.moeasp.core.MOEASpSolution
import com.ufrj.pesc.moeasp.core.Individual

trait MOEASpInterface extends Serializable {

  def generateRandomPopulation(problem: MOEASpProblem, size: Int): Iterable[MOEASpSolution]

  def getNondominatedPopulation(population: Iterable[MOEASpSolution]): Iterable[MOEASpSolution]

  def run(oc: OptimizationContext, iniPopulation: Iterable[MOEASpSolution] = List[MOEASpSolution]()): (Iterator[MOEASpSolution], Iterator[MOEASpSolution])
  
  def markMigration(oc: OptimizationContext, islandId: Int, islandPopulation: Iterable[Individual]): Iterable[Individual]

}