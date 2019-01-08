package com.pesc.moeasp

import scala.collection.mutable.HashMap

import com.ufrj.pesc.moeasp.adaptors.MOEASpAdaptor

package object core {
  type MOEASpSolution = Object

  type MOEASpProblem = Object
  
  type MOEASpParameter = Object
  
  type Individual = (Int, MOEASpSolution)

  case class OptimizationContext(moeaAdaptor: MOEASpAdaptor, problem: MOEASpProblem, algorithmId: String, parameterMap: HashMap[String,MOEASpParameter],
                                 totalPopulationSize:              Int,
                                 numOfIslands:                     Int,
                                 migrationSizeInIslandPercentage:  Double,
                                 numOfMigrations:                  Int,
                                 numberOfEvaluationsInIslandRatio: Double)
}