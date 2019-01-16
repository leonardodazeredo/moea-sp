package examples

import scala.collection.mutable.HashMap

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext

import com.ufrj.pesc.moeasp.adaptors.MOEAFrameworkAdaptor
import com.ufrj.pesc.moeasp.core.IslandsSparkSequentialRunner
import com.ufrj.pesc.moeasp.core.MOEASpParameter
import com.ufrj.pesc.moeasp.core.OptimizationContext

import chapter.KnapsackProblem

object SequentialJobsExample {

  def main(args: Array[String]): Unit = {

    val conf = new SparkConf()
      .setAppName("main")
      .setMaster("local")

    val sc = new SparkContext(conf)

    val problem = new KnapsackProblem();

    val moeaAdaptor = new MOEAFrameworkAdaptor()

    val parameterMap = HashMap[String, MOEASpParameter]()

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
