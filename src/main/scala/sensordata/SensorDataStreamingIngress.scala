package sensordata

import akka.http.scaladsl.common.EntityStreamingSupport
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

import SensorDataJsonSupport._
import cloudflow.akkastream._
import cloudflow.streamlets._
import cloudflow.streamlets.avro._
import cloudflow.akkastream.util.scaladsl._

class SensorDataStreamingIngress extends AkkaServerStreamlet {
  val out: CodecOutlet[SensorData]     = AvroOutlet[SensorData]("out", RoundRobinPartitioner)
  override def shape(): StreamletShape = StreamletShape.withOutlets(out)

  implicit val entityStreamingSupport            = EntityStreamingSupport.json()
  override def createLogic(): AkkaStreamletLogic = HttpServerLogic.defaultStreaming(this, out)
}
