package com.pesc.tebdi.adaptor

import org.apache.spark.SparkContext

import com.pesc.tebdi.core.OptimizationContext
import com.pesc.tebdi.core.MOEASpProblem
import com.pesc.tebdi.core.MOEASpSolution

trait MOEASpAdaptor extends Serializable {

  def generateRandomPopulation(problem: MOEASpProblem, size: Int): Iterable[MOEASpSolution]

  def runNSGAII(pc: OptimizationContext, iniPopulation: Iterable[MOEASpSolution] = List[MOEASpSolution]()): (Iterator[MOEASpSolution], Iterator[MOEASpSolution])

  def getNondominatedPopulation(population: Iterable[MOEASpSolution]): Iterable[MOEASpSolution]

  def runNSGAII_MasterSlave_Sp(sc: SparkContext, pc: OptimizationContext, iniPopulation: Iterable[MOEASpSolution] = List[MOEASpSolution]()): (Iterator[MOEASpSolution], Iterator[MOEASpSolution])

  def showPlot(algorithm: String, population: Iterable[MOEASpSolution])
}