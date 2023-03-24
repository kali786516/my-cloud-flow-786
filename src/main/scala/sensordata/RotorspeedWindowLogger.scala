package sensordata

import akka.stream.scaladsl.RunnableGraph
import cloudflow.akkastream._
import cloudflow.akkastream.scaladsl._
import cloudflow.streamlets._
import cloudflow.streamlets.avro._

class RotorspeedWindowLogger extends AkkaStreamlet {
  val in: CodecInlet[Metric]         = AvroInlet[Metric]("in")
  override val shape: StreamletShape = StreamletShape(in)
  override def createLogic(): AkkaStreamletLogic = new RunnableGraphStreamletLogic() {
    override def runnableGraph(): RunnableGraph[_] = sourceWithCommittableContext(in).via(flow).to(committableSink)
    def flow =
      FlowWithCommittableContext[Metric]()
        .grouped(5)
        .map { rotorSpeedWindow =>
          val (avg, _) = rotorSpeedWindow.map(_.value).foldLeft((0.0, 1)) { case ((avg, idx), next) => (avg + (next - avg) / idx, idx + 1) }

          system.log.info(s"Average rotorspeed is: $avg")

          avg
        }
        .mapContext(_.last) // TODO: this is a tricky one to understand...
  }
}
