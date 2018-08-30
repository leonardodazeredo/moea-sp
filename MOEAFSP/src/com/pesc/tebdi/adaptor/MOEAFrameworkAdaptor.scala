package com.pesc.tebdi.adaptor

import scala.collection.JavaConverters.iterableAsScalaIterableConverter
import scala.collection.JavaConverters.seqAsJavaListConverter

import org.apache.spark.SparkContext
import org.moeaframework.algorithm.NSGAII
import org.moeaframework.analysis.plot.Plot
import org.moeaframework.core.EpsilonBoxDominanceArchive
import org.moeaframework.core.Initialization
import org.moeaframework.core.NondominatedPopulation
import org.moeaframework.core.NondominatedSortingPopulation
import org.moeaframework.core.Problem
import org.moeaframework.core.Selection
import org.moeaframework.core.Solution
import org.moeaframework.core.Variation
import org.moeaframework.core.comparator.ChainedComparator
import org.moeaframework.core.comparator.CrowdingComparator
import org.moeaframework.core.comparator.ParetoDominanceComparator
import org.moeaframework.core.operator.GAVariation
import org.moeaframework.core.operator.InjectedInitialization
import org.moeaframework.core.operator.RandomInitialization
import org.moeaframework.core.operator.TournamentSelection
import org.moeaframework.core.operator.real.PM
import org.moeaframework.core.operator.real.SBX

import com.pesc.tebdi.core.OptimizationContext

class MOEAFrameworkAdaptor extends MOEASpAdaptor {

  def generateRandomPopulation(problem: Problem, size: Int): Iterable[Solution] = {
    implicit def arrayToList[A](a: Array[A]) = a.toList

    val ini = new RandomInitialization(problem, size)

    ini.initialize().toList
  }

  def getNondominatedPopulation(population: Iterable[Solution]): Iterable[Solution] = {

    val solutions = new NondominatedPopulation(population.asInstanceOf[List[Solution]].asJava);

    solutions.asScala.toList
  }

  def runNSGAII_MasterSlave_Sp(sc: SparkContext, pc: OptimizationContext, iniPopulation: Iterable[Solution] = List[Solution]()): (Iterator[Solution], Iterator[Solution]) = {

    class NSGAII_SP(sc: SparkContext, problem: Problem, population: NondominatedSortingPopulation, archive: EpsilonBoxDominanceArchive,
      selection: Selection, variation: Variation, initialization: Initialization) extends NSGAII(problem, population, archive, selection, variation, initialization) with Serializable {

      override def evaluateAll(solutions: java.lang.Iterable[Solution]): Unit = {

        implicit def arrayToList[A](a: Array[A]) = a.toList

        val p = problem;

        val solutionScalaList = solutions.asScala
        val solutionsRDD = sc.parallelize(solutionScalaList.to[Seq])
        val rdd = solutionsRDD.map(s => { p.evaluate(s); s })
        val ss = rdd.collect

        this.numberOfEvaluations += ss.size

        var i = 0
        val iter = solutions.iterator()
        while (iter.hasNext()) {
          val s = iter.next().asInstanceOf[Solution]
          s.setConstraints(ss(i).getConstraints)
          s.setObjectives(ss(i).getObjectives)
          i += 1
        }
      }
    }

    if (iniPopulation.isEmpty) {
      val iniPopulation = generateRandomPopulation(pc.problem, pc.totalPopulationSize)
    }

    val initialization = new InjectedInitialization(
      pc.problem,
      iniPopulation.size,
      iniPopulation.asInstanceOf[List[Solution]].asJava);

    val selection = new TournamentSelection(
      2,
      new ChainedComparator(
        new ParetoDominanceComparator(),
        new CrowdingComparator()));

    val variation = new GAVariation(
      new SBX(1.0, 25.0),
      new PM(1.0 / pc.problem.getNumberOfVariables(), 30.0));

    val algorithm = new NSGAII_SP(
      sc,
      pc.problem,
      new NondominatedSortingPopulation(),
      null, // no archive
      selection,
      variation,
      initialization);

    while (algorithm.getNumberOfEvaluations < 5000) {
      algorithm.step();
    }

    (algorithm.getResult.asScala.iterator, algorithm.getPopulation.asScala.iterator)
  }

  def runNSGAII(pc: OptimizationContext, iniPopulation: Iterable[Solution] = List[Solution]()): (Iterator[Solution], Iterator[Solution]) = {

    if (iniPopulation.isEmpty) {
      val iniPopulation = generateRandomPopulation(pc.problem, 1000)
    }

    val initialization = new InjectedInitialization(
      pc.problem,
      iniPopulation.size,
      iniPopulation.asInstanceOf[List[Solution]].asJava);

    val selection = new TournamentSelection(
      2,
      new ChainedComparator(
        new ParetoDominanceComparator(),
        new CrowdingComparator()));

    val variation = new GAVariation(
      new SBX(1.0, 25.0),
      new PM(1.0 / pc.problem.getNumberOfVariables(), 30.0));

    val algorithm = new NSGAII(
      pc.problem,
      new NondominatedSortingPopulation(),
      null, // no archive
      selection,
      variation,
      initialization);

    val size = iniPopulation.size

    while (algorithm.getNumberOfEvaluations < size * pc.numberOfEvaluationsInIslandRatio) {
      algorithm.step();
    }

    (algorithm.getResult.asScala.iterator, algorithm.getPopulation.asScala.iterator)
  }

  def showPlot(algorithm: String, population: Iterable[Solution]) {

    val solutions = new NondominatedPopulation(population.asInstanceOf[List[Solution]].asJava);

    val p = new Plot()
      .add(algorithm, solutions)
      .show();
  }

}