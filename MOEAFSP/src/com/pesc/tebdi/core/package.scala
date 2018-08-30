package com.pesc.tebdi

import org.moeaframework.core.Problem
import org.moeaframework.core.Solution
import com.pesc.tebdi.adaptor.MOEASpAdaptor

package object core {
  type Individual = (Int, Solution)

  case class OptimizationContext(moeaAdaptor: MOEASpAdaptor, problem: Problem,
    totalPopulationSize: Int,
    numOfIslands: Int,
    migrationSizeInIslandPercentage: Double,
    numOfMigrations: Int, 
    numberOfEvaluationsInIslandRatio: Double)
}