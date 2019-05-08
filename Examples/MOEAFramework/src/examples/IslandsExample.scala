package examples

import scala.collection.mutable.HashMap

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext

import com.ufrj.pesc.moeasp.adaptors.MOEAFrameworkAdaptor
import com.ufrj.pesc.moeasp.core.IslandsSparkExecutor
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

    val oc = OptimizationContext(moeaAdaptor, "examples.KnapsackProblemExample", "NSGAII", parameterMap,
      totalPopulationSize = 10000,
      numOfIslands = 50,
      migrationSizeInIslandPercentage = 0.1,
      numOfMigrations = 5,
      numberOfEvaluationsInIslandRatio = 10,
      true, true,
      "/media/leo/work/git/lab-spark/Examples/populations/")

    val (result, population) = (new IslandsSparkExecutor(sc, oc)).run()

    val front = result.toList

    moeaAdaptor.printPopulation(front)

    println("Population size of last shuffle: " + population.size)

  }

}
