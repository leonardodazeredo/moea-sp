package com.pesc.tebdi.partitioner

import scala.util.Random

import org.apache.spark.Partitioner

class RandomPartitioner(numParts: Int = 2) extends Partitioner {
  override def numPartitions = numParts

  override def getPartition(key: Any): Int = {
    (new Random()).nextInt(numPartitions - 1)
  }

  override def equals(other: Any): Boolean = other match {
    case p: RandomPartitioner =>
      p.numPartitions == numPartitions
    case _ =>
      false
  }
}