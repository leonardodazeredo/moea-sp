package examples

import scala.collection.mutable.HashMap

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.moeaframework.core.Problem
import org.moeaframework.core.comparator.ChainedComparator
import org.moeaframework.core.comparator.CrowdingComparator
import org.moeaframework.core.comparator.ParetoDominanceComparator
import org.moeaframework.core.operator.GAVariation
import org.moeaframework.core.operator.TournamentSelection
import org.moeaframework.core.operator.real.PM
import org.moeaframework.core.operator.real.SBX

import com.pesc.moeasp.adaptor.MOEAFrameworkAdaptor
import com.pesc.moeasp.core.IslandsSparkSequentialRunner
import com.pesc.moeasp.core.MOEASpParameter
import com.pesc.moeasp.core.OptimizationContext

import chapter.KnapsackProblem

object SequentialJobsExample {

  def main(args: Array[String]): Unit = {
    implicit def arrayToList[A](a: Array[A]) = a.toList

    val conf = new SparkConf()
      .setAppName("main")
      .setMaster("local")

    val sc = new SparkContext(conf)

    val problem = new KnapsackProblem();

    val selection = new TournamentSelection(
      2,
      new ChainedComparator(
        new ParetoDominanceComparator(),
        new CrowdingComparator()));

    val variation = new GAVariation(
      new SBX(1.0, 25.0),
      new PM(1.0 / problem.asInstanceOf[Problem].getNumberOfVariables(), 30.0));

    val moeaAdaptor = new MOEAFrameworkAdaptor()

    val parameterMap = HashMap[String, MOEASpParameter](("selection", selection), ("variation", variation))

    val oc = OptimizationContext(moeaAdaptor, problem, "NSGAII", parameterMap,
      totalPopulationSize = 50000,
      numOfIslands = 100,
      migrationSizeInIslandPercentage = 0.1,
      numOfMigrations = 4,
      numberOfEvaluationsInIslandRatio = 10)

    val runner = new IslandsSparkSequentialRunner(sc, oc)

    runner.run()

    Thread.sleep(80000)

    runner.requestStop()

    val result = runner.getNondominatedPopulation()

    val front = result.toList

    moeaAdaptor.printPopulation(front)

    moeaAdaptor.showPlot("NSGAII", front)

  }
}
