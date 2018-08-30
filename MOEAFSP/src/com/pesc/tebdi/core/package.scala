package com.pesc.tebdi

import org.moeaframework.core.Problem
import org.moeaframework.core.Solution
import com.pesc.tebdi.adaptor.MOEAAdaptor

package object core {
  type Individual = (Int, Solution)

  case class OptimizationContext(moeaAdaptor: MOEAAdaptor, problem: Problem,
    totalPopulationSize: Int,
    numOfIslands: Int,
    migrationSizeInIslandPercentage: Double,
    numOfMigrations: Int, 
    numberOfEvaluationsInIslandRatio: Double)
}