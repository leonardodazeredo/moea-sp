package com.pesc.tebdi

import com.pesc.tebdi.adaptor.MOEASpAdaptor

package object core {
  type MOEASpSolution = Object

  type MOEASpProblem = Object

  type Individual = (Int, MOEASpSolution)

  case class OptimizationContext(moeaAdaptor: MOEASpAdaptor, problem: MOEASpProblem,
                                 totalPopulationSize:              Int,
                                 numOfIslands:                     Int,
                                 migrationSizeInIslandPercentage:  Double,
                                 numOfMigrations:                  Int,
                                 numberOfEvaluationsInIslandRatio: Double)
}