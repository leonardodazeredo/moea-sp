package com.pesc.tebdi.util

import org.moeaframework.analysis.plot.Plot
import org.moeaframework.core.NondominatedPopulation
import scala.util.Random

object Utils {

  def showPlot(result: NondominatedPopulation) {
    val p = new Plot()
      .add("NSGAII", result)
      .show();
  }
  
  def getRandomElement(list: Seq[Int], random: Random): Int = 
    list(random.nextInt(list.length))

}