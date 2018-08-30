package com.pesc.tebdi.adaptor

import org.apache.spark.SparkContext
import org.moeaframework.core.Problem
import org.moeaframework.core.Solution

import com.pesc.tebdi.core.OptimizationContext

trait MOEAAdaptor extends Serializable {

  def generateRandomPopulation(problem: Problem, size: Int): Iterable[Solution]

  def runNSGAII(pc: OptimizationContext, iniPopulation: Iterable[Solution] = List[Solution]()): (Iterator[Solution], Iterator[Solution])

  def getNondominatedPopulation(population: Iterable[Solution]): Iterable[Solution]

  def runNSGAII_MasterSlave_Sp(sc: SparkContext, pc: OptimizationContext, iniPopulation: Iterable[Solution] = List[Solution]()): (Iterator[Solution], Iterator[Solution])

  def showPlot(algorithm: String, population: Iterable[Solution])
}