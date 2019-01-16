package com.ufrj.pesc.moeasp.core

import java.util.ArrayList

import scala.util.control.Breaks.break
import scala.util.control.Breaks.breakable

import org.apache.spark.SparkContext

import com.ufrj.pesc.moeasp.partitioner.FollowKeyPartitioner
import com.ufrj.pesc.moeasp.util.Utils

class IslandsSparkSequentialRunner(sparkContext: SparkContext, optimizationContext: OptimizationContext) {

  private val nondominatedPopulationPerMigration = new ArrayList[Iterable[MOEASpSolution]]

  private var currentMigration = 0

  private var running = false

  private var need_stop = false

  def run() {

    (new Thread(new Runner())).start()

  }

  private class Runner() extends Runnable {
    def run() {

      if (running) {
        throw new Exception("Already running")
      }

      running = true

      val oc = optimizationContext

      val sc = sparkContext

      val problemInstance = Utils.instantiate(oc.problemClassName)

      val iniPopulation = oc.moeaAdaptor.generateRandomPopulation(problemInstance, oc.totalPopulationSize)

      val iniPopulationWithId = iniPopulation.map(s => (0, s))

      var rddCurrentPopulation = sc.parallelize(iniPopulationWithId.to[Seq], oc.numOfIslands)

      breakable {
        for (i <- 1 to oc.numOfMigrations) {

          rddCurrentPopulation = rddCurrentPopulation.mapPartitionsWithIndex((index, iter) => SparkFunctions.inIslandRun(oc, index, iter))

          rddCurrentPopulation = rddCurrentPopulation.mapPartitionsWithIndex((index, iter) => SparkFunctions.setNewIslands(oc, index, iter))

          rddCurrentPopulation.persist()

          val rddCurrentNondominatedPopulationpopulation = rddCurrentPopulation.mapPartitionsWithIndex((index, iter) => SparkFunctions.getNondominatedPopulationInIsland(oc, index, iter))

          val currentNondominatedPopulationpopulation = rddCurrentNondominatedPopulationpopulation.collect.toList.map(ind => ind._2)
          addNondominatedPopulationPerMigration(currentNondominatedPopulationpopulation)

          if (checkNeedStop()) {
            break
          }

          rddCurrentPopulation = rddCurrentPopulation.partitionBy(new FollowKeyPartitioner(oc.numOfIslands))

        }
      }

      if (!checkNeedStop()) {
        rddCurrentPopulation = rddCurrentPopulation.mapPartitionsWithIndex((index, iter) => SparkFunctions.inIslandRun(oc, index, iter))

        val rddFinalNondominatedPopulationpopulation = rddCurrentPopulation.mapPartitionsWithIndex((index, iter) => SparkFunctions.getNondominatedPopulationInIsland(oc, index, iter))

        val final_population = rddFinalNondominatedPopulationpopulation.collect.toList.map(ind => ind._2)
        addNondominatedPopulationPerMigration(final_population)
      }

      Utils.unPersistAllRdds(sc)
      resetStates()
    }
  }

  def addNondominatedPopulationPerMigration(currentNondominatedPopulationpopulation: Iterable[MOEASpSolution]) = synchronized {
    nondominatedPopulationPerMigration.add(optimizationContext.moeaAdaptor.getNondominatedPopulation(currentNondominatedPopulationpopulation))
    currentMigration += 1
  }

  def getNondominatedPopulation(migrantion: Int): Iterable[MOEASpSolution] = synchronized {
    nondominatedPopulationPerMigration.get(migrantion)
  }

  def getNondominatedPopulation(): Iterable[MOEASpSolution] = {
    getNondominatedPopulation(currentMigration - 1)
  }

  def requestStop() = synchronized {
    need_stop = true
  }

  private def resetStates() = synchronized {
    need_stop = false
    running = false
  }

  def checkNeedStop(): Boolean = {
    need_stop
  }

}