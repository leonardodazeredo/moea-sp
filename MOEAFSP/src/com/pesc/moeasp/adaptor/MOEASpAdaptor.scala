package com.pesc.moeasp.adaptor

import org.apache.spark.SparkContext

import com.pesc.moeasp.core.OptimizationContext
import com.pesc.moeasp.core.MOEASpProblem
import com.pesc.moeasp.core.MOEASpSolution

trait MOEASpAdaptor extends Serializable {

  def generateRandomPopulation(problem: MOEASpProblem, size: Int): Iterable[MOEASpSolution]

  def getNondominatedPopulation(population: Iterable[MOEASpSolution]): Iterable[MOEASpSolution]

  def showPlot(algorithm: String, population: Iterable[MOEASpSolution])

  def runNSGAII(pc: OptimizationContext, iniPopulation: Iterable[MOEASpSolution] = List[MOEASpSolution]()): (Iterator[MOEASpSolution], Iterator[MOEASpSolution])
  
  def runNSGAII_MasterSlave_Sp(sc: SparkContext, pc: OptimizationContext, iniPopulation: Iterable[MOEASpSolution] = List[MOEASpSolution]()): (Iterator[MOEASpSolution], Iterator[MOEASpSolution])

}