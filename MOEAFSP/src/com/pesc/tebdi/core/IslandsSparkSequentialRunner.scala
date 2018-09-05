package com.pesc.tebdi.core

import scala.collection.mutable.HashMap

import org.apache.spark.SparkContext

import com.pesc.tebdi.partitioner.FollowKeyPartitioner
import com.pesc.tebdi.util.Utils

class IslandsSparkSequentialRunner(sparkContext: SparkContext, optimizationContext: OptimizationContext) extends IslandsSpark(sparkContext, optimizationContext) {

  private val nondominatedPopulationPerMigrationMap = new HashMap[Int, Iterable[MOEASpSolution]]

  private var currentMigration = 0

  private var state = 0

  private var need_stop = false

  def run(): (Iterable[MOEASpSolution], Iterable[MOEASpSolution]) = {

    implicit def arrayToList[A](a: Array[A]) = a.toList

    val oc = optimizationContext

    val sc = sparkContext

    val iniPopulation = oc.moeaAdaptor.generateRandomPopulation(oc.problem, oc.totalPopulationSize)

    val iniPopulationWithId = iniPopulation.map(s => (0, s))

    var rddCurrentPopulation = sc.parallelize(iniPopulationWithId.to[Seq], oc.numOfIslands)

    for (i <- 1 to oc.numOfMigrations) {

      rddCurrentPopulation = rddCurrentPopulation.mapPartitionsWithIndex((index, iter) => SparkFunctions.inIslandRun(oc, index, iter))

      rddCurrentPopulation = rddCurrentPopulation.mapPartitionsWithIndex((index, iter) => SparkFunctions.setNewIslands(oc, index, iter))

      rddCurrentPopulation.persist()

      val rddCurrentNondominatedPopulationpopulation = rddCurrentPopulation.mapPartitionsWithIndex((index, iter) => SparkFunctions.getNondominatedPopulationInIsland(oc, index, iter))

      val currentNondominatedPopulationpopulation = rddCurrentNondominatedPopulationpopulation.collect.toList.map(ind => ind._2)
      nondominatedPopulationPerMigrationMap += (i -> oc.moeaAdaptor.getNondominatedPopulation(currentNondominatedPopulationpopulation))

      rddCurrentPopulation = rddCurrentPopulation.partitionBy(new FollowKeyPartitioner(oc.numOfIslands))

    }

    rddCurrentPopulation = rddCurrentPopulation.mapPartitionsWithIndex((index, iter) => SparkFunctions.inIslandRun(oc, index, iter))

    rddCurrentPopulation = rddCurrentPopulation.mapPartitionsWithIndex((index, iter) => SparkFunctions.getNondominatedPopulationInIsland(oc, index, iter))

    val final_population = rddCurrentPopulation.collect.toList.map(ind => ind._2)

    Utils.unPersistAllRdds(sc)

    (oc.moeaAdaptor.getNondominatedPopulation(final_population), final_population)

  }

  def getNondominatedPopulation(migrantion: Int) {
    nondominatedPopulationPerMigrationMap(migrantion)
  }

  def getNondominatedPopulation() {
    nondominatedPopulationPerMigrationMap(currentMigration)
  }

  def stop() {
    need_stop = true
  }

  def checkNeedStop(): Boolean = {
    if (need_stop) {
      need_stop = false
      return need_stop
    }

    false
  }

}