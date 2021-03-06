import com.twitter.scalding._
import com.twitter.scalding.mathematics.Matrix


/*
* MatrixTutorial5.scala
*
* Loads a directed graph adjacency matrix where a[i,j] = 1 if there is an edge from a[i] to b[j]
* and computes the jaccard similarity between any two pairs of vectors
* 
  yarn jar target/scalding-tutorial-0.11.2.jar MatrixTutorial5 --local\
    --input data/graph.tsv --output target/data/jaccardSim.tsv
*
*/

class MatrixTutorial5(args : Args) extends Job(args) {
  
  import Matrix._

  val adjacencyMatrix = Tsv( args("input"), ('user1, 'user2, 'rel) )
    .read
    .toMatrix[Long,Long,Double]('user1, 'user2, 'rel)

  val aBinary = adjacencyMatrix.binarizeAs[Double]
 
  // intersectMat holds the size of the intersection of row(a)_i n row (b)_j
  val intersectMat = aBinary * aBinary.transpose
  val aSumVct = aBinary.sumColVectors
  val bSumVct = aBinary.sumRowVectors

  //Using zip to repeat the row and column vectors values on the right hand
  //for all non-zeroes on the left hand matrix
  val xMat = intersectMat.zip(aSumVct).mapValues( pair => pair._2 )
  val yMat = intersectMat.zip(bSumVct).mapValues( pair => pair._2 )
  
  val unionMat = xMat + yMat - intersectMat
  //We are guaranteed to have Double both in the intersection and in the union matrix
  intersectMat.zip(unionMat)
              .mapValues( pair => pair._1 / pair._2 )
              .write(Tsv( args("output") ))

}

