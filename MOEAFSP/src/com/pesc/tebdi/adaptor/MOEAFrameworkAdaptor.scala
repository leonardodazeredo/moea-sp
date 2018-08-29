package com.pesc.tebdi.adaptor

import scala.collection.JavaConverters.iterableAsScalaIterableConverter

import org.apache.spark.SparkContext
import org.moeaframework.algorithm.NSGAII
import org.moeaframework.analysis.plot.Plot
import org.moeaframework.core.EpsilonBoxDominanceArchive
import org.moeaframework.core.Initialization
import org.moeaframework.core.NondominatedSortingPopulation
import org.moeaframework.core.Problem
import org.moeaframework.core.Selection
import org.moeaframework.core.Solution
import org.moeaframework.core.Variation
import org.moeaframework.core.comparator.ChainedComparator
import org.moeaframework.core.comparator.CrowdingComparator
import org.moeaframework.core.comparator.ParetoDominanceComparator
import org.moeaframework.core.operator.GAVariation
import org.moeaframework.core.operator.RandomInitialization
import org.moeaframework.core.operator.TournamentSelection
import org.moeaframework.core.operator.real.PM
import org.moeaframework.core.operator.real.SBX

import chapter.KnapsackProblem

class MOEAFrameworkAdaptor {

  def runNSGAII_SP(sc: SparkContext): (Iterable[Solution], Iterable[Solution]) = {

    class NSGAII_SP(sc: SparkContext, problem: Problem, population: NondominatedSortingPopulation, archive: EpsilonBoxDominanceArchive,
      selection: Selection, variation: Variation, initialization: Initialization) extends NSGAII(problem, population, archive, selection, variation, initialization) with Serializable {

      override def evaluateAll(solutions: java.lang.Iterable[Solution]): Unit = {

        implicit def arrayToList[A](a: Array[A]) = a.toList

        val p = problem;

        val solutionScalaList = solutions.asScala
        val solutionsRDD = sc.parallelize(solutionScalaList.to[collection.immutable.Seq])
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

    val problem = new KnapsackProblem();

    val initialization = new RandomInitialization(
      problem,
      1000);

    val selection = new TournamentSelection(
      2,
      new ChainedComparator(
        new ParetoDominanceComparator(),
        new CrowdingComparator()));

    val variation = new GAVariation(
      new SBX(1.0, 25.0),
      new PM(1.0 / problem.getNumberOfVariables(), 30.0));

    val algorithm = new NSGAII_SP(
      sc,
      problem,
      new NondominatedSortingPopulation(),
      null, // no archive
      selection,
      variation,
      initialization);

    while (algorithm.getNumberOfEvaluations < 5000) {
      algorithm.step();
    }

    val p = new Plot()
      .add("NSGAII", algorithm.getResult)
      .show();

    (algorithm.getResult.asScala, algorithm.getPopulation.asScala)
  }

}