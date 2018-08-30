package com.pesc.tebdi.partitioner

import org.apache.spark.Partitioner

class FollowKeyPartitioner (numParts: Int = 2) extends Partitioner{
   override def numPartitions = numParts

  override def getPartition(key: Any): Int = {
    key.asInstanceOf[Int]
  }

  override def equals(other: Any): Boolean = other match {
    case p: FollowKeyPartitioner =>
      p.numPartitions == numPartitions
    case _ =>
      false
  }
}