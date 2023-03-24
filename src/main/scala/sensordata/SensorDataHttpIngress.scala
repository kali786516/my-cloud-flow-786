package sensordata

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import cloudflow.akkastream._
import cloudflow.akkastream.util.scaladsl._

import cloudflow.streamlets._
import cloudflow.streamlets.avro._
import SensorDataJsonSupport._

class SensorDataHttpIngress extends AkkaServerStreamlet {
  val out: CodecOutlet[SensorData]               = AvroOutlet[SensorData]("out").withPartitioner(RoundRobinPartitioner)
  override def shape(): StreamletShape           = StreamletShape.withOutlets(out)
  override def createLogic(): AkkaStreamletLogic = HttpServerLogic.default(this, out)
}