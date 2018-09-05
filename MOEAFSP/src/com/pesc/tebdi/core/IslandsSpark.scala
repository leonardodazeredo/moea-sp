package com.pesc.tebdi.core

import org.apache.spark.SparkContext

abstract class IslandsSpark(sparkContext: SparkContext, optimizationContext: OptimizationContext) {

  def run(): (Iterable[MOEASpSolution], Iterable[MOEASpSolution])

}