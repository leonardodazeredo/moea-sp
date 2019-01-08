package examples

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext

import com.pesc.moeasp.adaptor.MOEAFrameworkAdaptor
import com.pesc.moeasp.core.IslandsSparkSingleRunner

import chapter.KnapsackProblem

object SingleJobExample {

  implicit def arrayToList[A](a: Array[A]) = a.toList

  def main(args: Array[String]): Unit = {

    val conf = new SparkConf()
      .setAppName("main")
      .setMaster("local")

    val sc = new SparkContext(conf)

    val problem = new KnapsackProblem();

    val moeaAdaptor = new MOEAFrameworkAdaptor()

    val oc = OptimizationContext(moeaAdaptor, problem,
      totalPopulationSize = 50000,
      numOfIslands = 100,
      migrationSizeInIslandPercentage = 0.1,
      numOfMigrations = 4,
      numberOfEvaluationsInIslandRatio = 10)

    val (result, population) = (new IslandsSparkSingleRunner(sc, oc)).run()

    val front = result.toList

    moeaAdaptor.printPopulation(front)

    moeaAdaptor.showPlot("NSGAII", front)

    println("Population size of last shuffle: " + population.size)

  }

}
