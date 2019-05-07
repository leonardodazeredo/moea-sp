package com.ufrj.pesc.moeasp

import scala.collection.mutable.HashMap

import com.ufrj.pesc.moeasp.adaptors.MOEASpAdaptor

package object core {
  type MOEASpSolution = AnyRef

  type MOEASpProblem = AnyRef
  
  type MOEASpParameter = AnyRef
  
  type Individual = (Int, MOEASpSolution)

  case class OptimizationContext(moeaAdaptor: MOEASpAdaptor, problemClassName: String, algorithmId: String, parameterMap: HashMap[String,MOEASpParameter],
                                 totalPopulationSize:              Int,
                                 numOfIslands:                     Int,
                                 migrationSizeInIslandPercentage:  Double,
                                 numOfMigrations:                  Int,
                                 numberOfEvaluationsInIslandRatio: Double,
                                 savePopulationsToFile: Boolean,
                                 populationDir: String)
}