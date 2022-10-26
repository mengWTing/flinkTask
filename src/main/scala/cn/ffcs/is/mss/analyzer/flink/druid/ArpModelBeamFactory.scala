package cn.ffcs.is.mss.analyzer.flink.druid

import cn.ffcs.is.mss.analyzer.druid.model.scala.ArpModel
import com.metamx.common.Granularity
import com.metamx.tranquility.beam.{Beam, ClusteredBeamTuning}
import com.metamx.tranquility.druid._
import com.metamx.tranquility.flink.BeamFactory
import io.druid.data.input.impl.TimestampSpec
import io.druid.granularity.PeriodGranularity
import org.apache.curator.framework.CuratorFrameworkFactory
import org.apache.curator.retry.BoundedExponentialBackoffRetry
import org.joda.time.{DateTime, DateTimeZone, Period}


class ArpModelBeamFactory (conf : collection.mutable.HashMap[String,String]) extends BeamFactory[ArpModel]{
  lazy val makeBeam: Beam[ArpModel] = {
//     Tranquility uses ZooKeeper (through Curator framework) for coordination.
    val curator = CuratorFrameworkFactory.newClient(
      conf("tranquility.zk.connect"),
      new BoundedExponentialBackoffRetry(100, 3000, 5)
    )
    curator.start()

    val indexService = "druid/overlord" // Your overlord's druid.service, with slashes replaced by colons.
    val discoveryPath = "/druid/discovery" // Your overlord's druid.discovery.curator.path
    val dataSource = conf("druid.arp.source")
    val dimensions = ArpModel.Columns
    val aggregators = ArpModel.Metrics

    // Expects simpleEvent.timestamp to return a Joda DateTime object.

    DruidBeams
      .builder((arpModel: ArpModel) => new DateTime(arpModel.timeStamp))
      .timestampSpec(new TimestampSpec("timeStamp","auto",null))
      .curator(curator)
      .discoveryPath(discoveryPath)
      .location(DruidLocation.create(indexService, dataSource))
      .rollup(DruidRollup(SpecificDruidDimensions(dimensions), aggregators, new PeriodGranularity(Period.parse("PT1S"), null, DateTimeZone.forID(ArpModel.druidFormat))))
      .tuning(
        ClusteredBeamTuning
          .builder.segmentGranularity(Granularity.HOUR)
          .windowPeriod(new Period("PT15M"))
          .build)
      .druidBeamConfig(DruidBeamConfig.builder().randomizeTaskId(true).build()).buildBeam()

  }
}
