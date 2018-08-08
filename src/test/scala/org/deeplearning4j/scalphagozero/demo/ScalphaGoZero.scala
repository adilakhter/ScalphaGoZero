package org.deeplearning4j.scalphagozero.demo

import org.deeplearning4j.nn.conf.{ ComputationGraphConfiguration, NeuralNetConfiguration }
import org.deeplearning4j.nn.graph.ComputationGraph
import org.deeplearning4j.nn.modelimport.keras.KerasModelImport
import org.deeplearning4j.scalphagozero.agents.ZeroAgent
import org.deeplearning4j.scalphagozero.encoders.ZeroEncoder
import org.deeplearning4j.scalphagozero.experience.{ ZeroExperienceBuffer, ZeroExperienceCollector }
import org.deeplearning4j.scalphagozero.simulation.Simulator
import org.nd4j.linalg.io.ClassPathResource

object ScalphaGoZero {

  def main(args: Array[String]): Unit = {
    val boardSize = 19
    val encoder = ZeroEncoder(boardSize, boardSize)

    val modelPath = "dual_res.h5" // TODO: generate first
    val modelResource = new ClassPathResource(modelPath, ScalphaGoZero.getClass.getClassLoader)

    // TODO: Json: error because no  training conf, no output layers
    //  Layer "value_head_output" of type DenseLayer is set as network output (but isn't an IOutputLayer).
    // TODO: HDF5: init_batch_norm gamma shape error. Maybe the other layer will occur too?!

//    val config: ComputationGraphConfiguration =
//      KerasModelImport.importKerasModelConfiguration(modelResource.getFile.getAbsolutePath)
//    val model = new ComputationGraph(config)
//    model.init()

    val model = KerasModelImport.importKerasModelAndWeights(modelResource.getFile.getAbsolutePath, false)

    val blackAgent = new ZeroAgent(model, encoder, roundsPerMove = 10, c = 2.0)
    val whiteAgent = new ZeroAgent(model, encoder, roundsPerMove = 10, c = 2.0)

    val blackCollector = new ZeroExperienceCollector()
    val whiteCollector = new ZeroExperienceCollector()

    blackAgent.setCollector(blackCollector)
    whiteAgent.setCollector(whiteCollector)

    for (i <- 0 until 5)
      Simulator.simulateGame(boardSize, boardSize, blackAgent, blackCollector, whiteAgent, whiteCollector)

    val experience = ZeroExperienceBuffer.combineExperience(List(blackCollector, whiteCollector))

    //blackAgent.train(experience)
  }

}