package com.pesc.moeasp

import scala.collection.mutable.HashMap

import com.pesc.moeasp.adaptor.MOEASpAdaptor

package object core {
  type MOEASpSolution = Object

  type MOEASpProblem = Object
  
  type Individual = (Int, MOEASpSolution)

  case class OptimizationContext(moeaAdaptor: MOEASpAdaptor, problem: MOEASpProblem, algorithmId: String, parameterMap: HashMap[String,Object],
                                 totalPopulationSize:              Int,
                                 numOfIslands:                     Int,
                                 migrationSizeInIslandPercentage:  Double,
                                 numOfMigrations:                  Int,
                                 numberOfEvaluationsInIslandRatio: Double)
}