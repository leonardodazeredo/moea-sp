package com.pesc.moeasp.core

import org.apache.spark.SparkContext

import com.pesc.moeasp.partitioner.FollowKeyPartitioner

class IslandsSparkSingleRunner(sparkContext: SparkContext, optimizationContext: OptimizationContext) {

  def run(): (Iterable[MOEASpSolution], Iterable[MOEASpSolution]) = {

    implicit def arrayToList[A](a: Array[A]) = a.toList

    val oc = optimizationContext

    val sc = sparkContext

    val iniPopulation = optimizationContext.moeaAdaptor.generateRandomPopulation(oc.problem, oc.totalPopulationSize)

    val iniPopulationWithId = iniPopulation.map(s => (0, s))

    var rddCurrentPopulation = sc.parallelize(iniPopulationWithId.to[Seq], optimizationContext.numOfIslands)

    for (i <- 1 to optimizationContext.numOfMigrations) {
      rddCurrentPopulation = rddCurrentPopulation.mapPartitionsWithIndex((index, iter) => SparkFunctions.inIslandRun(oc, index, iter))

      rddCurrentPopulation = rddCurrentPopulation.mapPartitionsWithIndex((index, iter) => SparkFunctions.setNewIslands(oc, index, iter))

      rddCurrentPopulation = rddCurrentPopulation.partitionBy(new FollowKeyPartitioner(optimizationContext.numOfIslands))
    }

    rddCurrentPopulation = rddCurrentPopulation.mapPartitionsWithIndex((index, iter) => SparkFunctions.inIslandRun(oc, index, iter))

    rddCurrentPopulation = rddCurrentPopulation.mapPartitionsWithIndex((index, iter) => SparkFunctions.getNondominatedPopulationInIsland(oc, index, iter))

    val final_population = rddCurrentPopulation.collect.toList.map(ind => ind._2)

    (optimizationContext.moeaAdaptor.getNondominatedPopulation(final_population), final_population)
  }

}