package examples

import scala.collection.JavaConverters.iterableAsScalaIterableConverter
import scala.collection.JavaConverters.seqAsJavaListConverter

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.moeaframework.algorithm.AbstractEvolutionaryAlgorithm
import org.moeaframework.core.NondominatedSortingPopulation
import org.moeaframework.core.Problem
import org.moeaframework.core.Solution
import org.moeaframework.core.comparator.ChainedComparator
import org.moeaframework.core.comparator.CrowdingComparator
import org.moeaframework.core.comparator.ParetoDominanceComparator
import org.moeaframework.core.operator.GAVariation
import org.moeaframework.core.operator.InjectedInitialization
import org.moeaframework.core.operator.TournamentSelection
import org.moeaframework.core.operator.real.PM
import org.moeaframework.core.operator.real.SBX

import com.ufrj.pesc.moeasp.adaptors.MOEAFrameworkAdaptor
import com.ufrj.pesc.moeasp.adaptors.NSGAII_SP

object MasterSlaveJobsExample {

  implicit def arrayToList[A](a: Array[A]) = a.toList

  def main(args: Array[String]): Unit = {

    val conf = new SparkConf()
      .setAppName("main")
      .setMaster("local")

    val sc = new SparkContext(conf)

    val moeaAdaptor = new MOEAFrameworkAdaptor()

    val problem = new KnapsackProblemExample();

    val iniPopulation = moeaAdaptor.generateRandomPopulation(problem, 50000)

    val initialization = new InjectedInitialization(
      problem.asInstanceOf[Problem],
      iniPopulation.size,
      iniPopulation.asInstanceOf[List[Solution]].asJava);

    val selection = new TournamentSelection(
      2,
      new ChainedComparator(
        new ParetoDominanceComparator(),
        new CrowdingComparator()));

    val variation = new GAVariation(
      new SBX(1.0, 25.0),
      new PM(1.0 / problem.asInstanceOf[Problem].getNumberOfVariables(), 30.0));

    val algorithm = new NSGAII_SP(
      sc,
      problem.asInstanceOf[Problem],
      new NondominatedSortingPopulation(),
      null, // no archive
      selection,
      variation,
      initialization);

    while (algorithm.getNumberOfEvaluations < 5000) {
      algorithm.step();
    }

    val front = algorithm.getResult.asScala.iterator.toList

    moeaAdaptor.printPopulation(front)

  }

}
