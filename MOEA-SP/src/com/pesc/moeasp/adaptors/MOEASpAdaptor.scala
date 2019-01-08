package com.pesc.moeasp.adaptors

import org.apache.spark.SparkContext

import com.pesc.moeasp.core.OptimizationContext
import com.pesc.moeasp.core.MOEASpProblem
import com.pesc.moeasp.core.MOEASpSolution

trait MOEASpAdaptor extends Serializable {

  def generateRandomPopulation(problem: MOEASpProblem, size: Int): Iterable[MOEASpSolution]

  def getNondominatedPopulation(population: Iterable[MOEASpSolution]): Iterable[MOEASpSolution]

  def run(pc: OptimizationContext, iniPopulation: Iterable[MOEASpSolution] = List[MOEASpSolution]()): (Iterator[MOEASpSolution], Iterator[MOEASpSolution])

}