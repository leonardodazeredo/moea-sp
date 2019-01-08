package examples

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext

import com.pesc.moeasp.adaptor.MOEAFrameworkAdaptor
import com.pesc.moeasp.core.OptimizationContext

import chapter.KnapsackProblem

object MasterSlaveJobsExample {

  implicit def arrayToList[A](a: Array[A]) = a.toList

  def main(args: Array[String]): Unit = {

    val conf = new SparkConf()
      .setAppName("main")
      .setMaster("local")

    val sc = new SparkContext(conf)

    val moeaAdaptor = new MOEAFrameworkAdaptor()

    val problem = new KnapsackProblem();

    val pc = OptimizationContext(moeaAdaptor, problem,
      totalPopulationSize = 50000,
      numOfIslands = 100,
      migrationSizeInIslandPercentage = 0.1,
      numOfMigrations = 4,
      numberOfEvaluationsInIslandRatio = 10)

    val iniPopulation = moeaAdaptor.generateRandomPopulation(problem, pc.totalPopulationSize)

    val (result, population) = moeaAdaptor.runNSGAII_MasterSlave_Sp(sc, pc, iniPopulation)

    val front = result.toList

    moeaAdaptor.printPopulation(front)

    moeaAdaptor.showPlot("NSGAII", front)

  }

}
