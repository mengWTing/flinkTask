package cn.ffcs.is.mss.analyzer.flink.druid

import cn.ffcs.is.mss.analyzer.druid.model.scala.DnsModel
import com.metamx.common.Granularity
import com.metamx.tranquility.beam.{Beam, ClusteredBeamTuning}
import com.metamx.tranquility.druid._
import com.metamx.tranquility.flink.BeamFactory
import io.druid.data.input.impl.TimestampSpec
import io.druid.granularity.PeriodGranularity
import io.druid.query.aggregation.LongSumAggregatorFactory
import org.apache.curator.framework.CuratorFrameworkFactory
import org.apache.curator.retry.BoundedExponentialBackoffRetry
import org.joda.time.{DateTime, DateTimeZone, Period}


class DnsModelBeamFactory (conf: collection.mutable.HashMap[String,String]) extends BeamFactory[DnsModel]{
  lazy val makeBeam: Beam[DnsModel] = {
    // Tranquility uses ZooKeeper (through Curator framework) for coordination.
    val curator = CuratorFrameworkFactory.newClient(
      conf("tranquility.zk.connect"),
      new BoundedExponentialBackoffRetry(100, 3000, 5)
    )
    curator.start()

    val indexService = "druid/overlord" // Your overlord's druid.service, with slashes replaced by colons.
    val discoveryPath = "/druid/discovery" // Your overlord's druid.discovery.curator.path
    val dataSource = conf("druid.dns.source")
    val dimensions = DnsModel.Columns
    val aggregators = DnsModel.Metrics

    // Expects simpleEvent.timestamp to return a Joda DateTime object.
    DruidBeams
      .builder((dnsModel: DnsModel) => new DateTime(dnsModel.timeStamp))
      .timestampSpec(new TimestampSpec("timeStamp","auto",null))
      .curator(curator)
      .discoveryPath(discoveryPath)
      .location(DruidLocation.create(indexService, dataSource))
      .rollup(DruidRollup(SpecificDruidDimensions(dimensions), aggregators, new PeriodGranularity(Period.parse("PT1S"), null, DateTimeZone.forID(DnsModel.druidFormat))))
      .tuning(
        ClusteredBeamTuning
          .builder.segmentGranularity(Granularity.HOUR)
          .windowPeriod(new Period("PT15M"))
          .build)
      .druidBeamConfig(DruidBeamConfig.builder().randomizeTaskId(true).build()).buildBeam()

  }
}