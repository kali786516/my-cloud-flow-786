package sensordata

import akka.stream.scaladsl._
import cloudflow.akkastream._
import cloudflow.akkastream.scaladsl._
import cloudflow.streamlets._
import cloudflow.streamlets.avro._

class SensorDataToMetrics extends AkkaStreamlet {
  val in: CodecInlet[SensorData]     = AvroInlet[SensorData]("in")
  val out: CodecOutlet[Metric]       = AvroOutlet[Metric]("out").withPartitioner(RoundRobinPartitioner)
  override val shape: StreamletShape = StreamletShape(in, out)
  def flow =
    FlowWithCommittableContext[SensorData]()
      .mapConcat { data =>
        List(
          Metric(data.deviceId, data.timestamp, "power", data.measurements.power),
          Metric(data.deviceId, data.timestamp, "rotorSpeed", data.measurements.rotorSpeed),
          Metric(data.deviceId, data.timestamp, "windSpeed", data.measurements.windSpeed)
        )
      }
  override def createLogic(): AkkaStreamletLogic = new RunnableGraphStreamletLogic() {
    override def runnableGraph(): RunnableGraph[_] = sourceWithCommittableContext(in).via(flow).to(committableSink(out))
  }
}
