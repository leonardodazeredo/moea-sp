package com.pesc.tebdi.adaptor

import org.moeaframework.core.Solution
import org.moeaframework.core.Problem

trait MOEAAdaptor extends Serializable{
  
  def generateRandomPopulation(problem: Problem, size: Int): Iterable[Solution]
  
  def runNSGAII(problem: Problem, iniPopulation: Iterable[Solution] = List[Solution]()): (Iterator[Solution], Iterator[Solution])
  
  def getNondominatedPopulation(population: Iterable[Solution]): Iterable[Solution]
  
}