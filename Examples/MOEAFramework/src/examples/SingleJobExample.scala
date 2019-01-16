package examples

import scala.collection.mutable.HashMap

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext

import com.ufrj.pesc.moeasp.adaptors.MOEAFrameworkAdaptor
import com.ufrj.pesc.moeasp.core.IslandsSparkSingleRunner
import com.ufrj.pesc.moeasp.core.MOEASpParameter
import com.ufrj.pesc.moeasp.core.OptimizationContext

import com.ufrj.pesc.moeasp.util.Utils

object SingleJobExample {

  def main(args: Array[String]): Unit = {

    val conf = new SparkConf()
      .setAppName("main")
      .setMaster("local")

    val sc = new SparkContext(conf)

    val moeaAdaptor = new MOEAFrameworkAdaptor()

    val parameterMap = HashMap[String, MOEASpParameter]()

    val oc = OptimizationContext(moeaAdaptor, "examples.KnapsackProblem", "NSGAII", parameterMap,
      totalPopulationSize = 50000,
      numOfIslands = 100,
      migrationSizeInIslandPercentage = 0.1,
      numOfMigrations = 4,
      numberOfEvaluationsInIslandRatio = 10)

    val (result, population) = (new IslandsSparkSingleRunner(sc, oc)).run()

    val front = result.toList

    moeaAdaptor.printPopulation(front)

    println("Population size of last shuffle: " + population.size)

  }

}
