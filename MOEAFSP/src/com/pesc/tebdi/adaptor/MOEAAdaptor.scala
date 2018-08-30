package com.pesc.tebdi.adaptor

import org.moeaframework.core.Problem
import org.moeaframework.core.Solution

import com.pesc.tebdi.core.OptimizationContext

trait MOEAAdaptor extends Serializable {

  def generateRandomPopulation(problem: Problem, size: Int): Iterable[Solution]

  def runNSGAII(pc: OptimizationContext, iniPopulation: Iterable[Solution] = List[Solution]()): (Iterator[Solution], Iterator[Solution])

  def getNondominatedPopulation(population: Iterable[Solution]): Iterable[Solution]

}