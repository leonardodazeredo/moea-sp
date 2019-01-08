package com.pesc.moeasp

import com.pesc.moeasp.adaptor.MOEASpAdaptor

package object core {
  type MOEASpSolution = Object

  type MOEASpProblem = Object
  
  type MOEASpAlgorithm = Object

  type Individual = (Int, MOEASpSolution)

  case class OptimizationContext(moeaAdaptor: MOEASpAdaptor, problem: MOEASpProblem, algorithm: MOEASpAlgorithm,
                                 totalPopulationSize:              Int,
                                 numOfIslands:                     Int,
                                 migrationSizeInIslandPercentage:  Double,
                                 numOfMigrations:                  Int,
                                 numberOfEvaluationsInIslandRatio: Double)
}