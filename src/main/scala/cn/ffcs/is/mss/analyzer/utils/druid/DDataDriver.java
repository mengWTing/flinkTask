package cn.ffcs.is.mss.analyzer.utils.druid;

/**
 * @author linlr@ffcs.cn
 * 2016-8-04 14:03:24
 */
public class DDataDriver {
    private BrokerAccessor broker;

    private static int MAX_CONNS_IN_POOL = 150;
    private static int MAX_BROKER_CONNS = 100;

    public DDataDriver(String host, int port) {

        broker = new BrokerAccessor(host, port, MAX_BROKER_CONNS);
    }

    public static void adjustPoolSettings(int maxConnsInPool, int maxBrokerConns) {
        MAX_CONNS_IN_POOL = maxConnsInPool;
        MAX_BROKER_CONNS = maxBrokerConns;
        BrokerAccessor.setMaxConnections(MAX_CONNS_IN_POOL);
    }

    public BrokerAccessor getBroker() {
        return broker;
    }

    public void setBroker(BrokerAccessor broker) {
        this.broker = broker;
    }


}
