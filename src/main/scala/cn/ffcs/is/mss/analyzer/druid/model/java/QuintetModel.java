package cn.ffcs.is.mss.analyzer.druid.model.java;

import com.metamx.tranquility.partition.HashCodePartitioner;
import java.io.Serializable;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.json.JSONObject;


/**
 * @Auther chenwei
 * @Description 五元组话单model类
 * @Date: Created in 2017/10/31 下午2:15
 * @Modified By
 */
public class QuintetModel extends HashCodePartitioner<QuintetModel> implements Serializable {

    //写入druid的时间戳格式
    public static final String druidFormat = "+08:00";


    //时间戳
    private Long timeStamp;
    //源IP
    private String sourceIp;
    //源端口
    private String sourcePort;
    //源MAC
    private String sourceMac;
    //目的IP
    private String destinationIp;
    //目的PORT
    private String destinationPort;
    //目的MAC
    private String destinationMac;
    //协议类型，参考附表PortocolID字段定义
    private String protocolId;
    //VLAN号，第一层vlan
    private String vlanId;
    //采集分段标识(探针号)
    private String probeId;
    //流入字节数
    private Long inputOctets;
    //流出字节数
    private Long outputOctets;
    //总字节数
    private Long octets;
    //流入包数
    private Long inputPacket;
    //流出包数
    private Long outputPacket;
    //总包数
    private Long packet;
    //流入重传包数
    private Long inputRetransPacket;
    //流出重传包数
    private Long outputRetransPacket;
    //重传包数
    private Long retransPacket;
    //流入重传时延(总时延)-ms
    private Long inputRetransDelay;
    //流出重传时延(总时延)-ms
    private Long outputRetransDelay;
    //总时延
    private Long retransDelay;
    //流入RESET数量
    private Long inputRest;
    //流出RESET数量
    private Long outputRest;
    //RESET数量
    private Long rest;
    //连接是否成功（1-成功、0-失败、2-分割）
    //1.4层连接：会话数-连接失败；2.7层连接：会话数
    private String isSucceed;
    //流入小包数：根据配置规定判断小包字节数
    private Long inputSmallPacket;
    //流出小包数：根据配置规定判断小包字节数
    private Long outputSmallPacket;
    //小包数：根据配置规定判断小包字节数
    private Long smallPacket;
    //流入中包数：根据配置规定判断中包字节数
    private Long inputMediumPacket;
    //流出中包数：根据配置规定判断中包字节数
    private Long outputMediumPacket;
    //中包数：根据配置规定判断中包字节数
    private Long mediumPacket;
    //流入大包数：根据配置规定判断大包字节数
    private Long inputLargePacket;
    //流出大包数；根据配置规定判断大包字节数
    private Long outputLargePacket;
    //大包数；根据配置规定判断大包字节数
    private Long largePacket;
    //流入零窗口数
    private Long inputZeroWindow;
    //流出零窗口数
    private Long outputZeroWindow;
    //零窗口数
    private Long zeroWindow;
//    //开始时间戳-ms
//    private String startTime;
    //结束时间戳-ms
    private Long finishTime;
    //SYN时间戳-ms
    private Long synTime;
    //SYN ACK时间戳-ms
    private Long synAckTime;
    //ACK时间戳-ms
    private Long ackTime;
    //Time to live
    private Long ttl;
    //TCP协议或UDP协议（1：TCP、0：UDP）
    private String protocol;
    //连接次数
    private Long connCount;


    public QuintetModel() {
        super();
    }

    public QuintetModel(long timeStamp, String sourceIp, String sourcePort, String sourceMac,
        String destinationIp, String destinationPort, String destinationMac, String protocolId,
        String vlanId, String probeId, long inputOctets, long outputOctets, long octets,
        long inputPacket, long outputPacket, long packet, long inputRetransPacket,
        long outputRetransPacket, long retransPacket, long inputRetransDelay,
        long outputRetransDelay, long retransDelay, long inputRest, long outputRest, long rest,
        String isSucceed, long inputSmallPacket, long outputSmallPacket, long smallPacket,
        long inputMediumPacket, long outputMediumPacket, long mediumPacket, long inputLargePacket,
        long outputLargePacket, long largePacket, long inputZeroWindow, long outputZeroWindow,
        long zeroWindow, long finishTime, long synTime, long synAckTime,
        long ackTime, long ttl, String protocol, long connCount) {

        this.timeStamp = timeStamp;
        this.sourceIp = sourceIp;
        this.sourcePort = sourcePort;
        this.sourceMac = sourceMac;
        this.destinationIp = destinationIp;
        this.destinationPort = destinationPort;
        this.destinationMac = destinationMac;
        this.protocolId = protocolId;
        this.vlanId = vlanId;
        this.probeId = probeId;
        this.inputOctets = inputOctets;
        this.outputOctets = outputOctets;
        this.octets = octets;
        this.inputPacket = inputPacket;
        this.outputPacket = outputPacket;
        this.packet = packet;
        this.inputRetransPacket = inputRetransPacket;
        this.outputRetransPacket = outputRetransPacket;
        this.retransPacket = retransPacket;
        this.inputRetransDelay = inputRetransDelay;
        this.outputRetransDelay = outputRetransDelay;
        this.retransDelay = retransDelay;
        this.inputRest = inputRest;
        this.outputRest = outputRest;
        this.rest = rest;
        this.isSucceed = isSucceed;
        this.inputSmallPacket = inputSmallPacket;
        this.outputSmallPacket = outputSmallPacket;
        this.smallPacket = smallPacket;
        this.inputMediumPacket = inputMediumPacket;
        this.outputMediumPacket = outputMediumPacket;
        this.mediumPacket = mediumPacket;
        this.inputLargePacket = inputLargePacket;
        this.outputLargePacket = outputLargePacket;
        this.largePacket = largePacket;
        this.inputZeroWindow = inputZeroWindow;
        this.outputZeroWindow = outputZeroWindow;
        this.zeroWindow = zeroWindow;

        if (finishTime >= timeStamp) {
            this.finishTime = finishTime - timeStamp;
        }else {
            this.finishTime = null;
        }

        if (synTime >= timeStamp) {
            this.synTime = synTime - timeStamp;
        }else {
            this.synTime = null;
        }

        if (synAckTime >= timeStamp) {
            this.synAckTime = synAckTime - timeStamp;
        }else {
            this.synAckTime = null;
        }

        if (ackTime >= timeStamp) {
            this.ackTime = ackTime - timeStamp;
        }else {
            this.ackTime = null;
        }
        this.ttl = ttl;
        this.protocol = protocol;
        this.connCount = connCount;

    }


    /**
     * @Auther chenwei
     * @Description 根据话单生成QuintetModel
     * @Date: Created in 2018/5/16 16:58
     * @param line
     * @return
     */
    public static QuintetModel getQuintetModel(String line){

        String[] values = line.split("\\|",-1);

        if (values.length == 35) {

            //时间戳
            long timeStamp = Long.parseLong(values[28]);
            //源IP
            String sourceIp = values[0];
            //源端口
            String sourcePort = values[1];
            //源MAC
            String sourceMac = values[2];
            //目的IP
            String destinationIp = values[3];
            //目的PORT
            String destinationPort = values[4];
            //目的MAC
            String destinationMac = values[5];
            //协议类型，参考附表PortocolID字段定义
            String protocolId = values[6];
            //VLAN号，第一层vlan
            String vlanId = values[7];
            //采集分段标识(探针号)
            String probeId = values[8];
            //流入字节数
            Long inputOctets = Long.parseLong(values[9]);
            //流出字节数
            Long outputOctets = Long.parseLong(values[10]);
            //总字节数
            Long octets = inputOctets + outputOctets;
            //流入包数
            Long inputPacket = Long.parseLong(values[11]);
            //流出包数
            Long outputPacket = Long.parseLong(values[12]);
            //总包数
            Long packet = inputPacket + outputPacket;
            //流入重传包数
            Long inputRetransPacket = Long.parseLong(values[13]);
            //流出重传包数
            Long outputRetransPacket = Long.parseLong(values[14]);
            //重传包数
            Long retransPacket = inputRetransPacket + outputRetransPacket;
            //流入重传时延(总时延)-ms
            Long inputRetransDelay = Long.parseLong(values[15]);
            //流出重传时延(总时延)-ms
            Long outputRetransDelay = Long.parseLong(values[16]);
            //总时延
            Long retransDelay = inputRetransDelay + inputRetransDelay;
            //流入RESET数量
            Long inputRest = Long.parseLong(values[17]);
            //流出RESET数量
            Long outputRest = Long.parseLong(values[18]);
            //RESET数量
            Long rest = inputRest + outputRest;
            //连接是否成功（1-成功、0-失败、2-分割）
            //1.4层连接：会话数-连接失败；2.7层连接：会话数
            String isSucceed = values[19];
            //流入小包数：根据配置规定判断小包字节数
            Long inputSmallPacket = Long.parseLong(values[20]);
            //流出小包数：根据配置规定判断小包字节数
            Long outputSmallPacket = Long.parseLong(values[21]);
            //小包数：根据配置规定判断小包字节数
            Long smallPacket = inputSmallPacket + outputSmallPacket;
            //流入中包数：根据配置规定判断中包字节数
            Long inputMediumPacket = Long.parseLong(values[22]);
            //流出中包数：根据配置规定判断中包字节数
            Long outputMediumPacket = Long.parseLong(values[23]);
            //中包数：根据配置规定判断中包字节数
            Long mediumPacket = inputMediumPacket + outputMediumPacket;
            //流入大包数：根据配置规定判断大包字节数
            Long inputLargePacket = Long.parseLong(values[24]);
            //流出大包数；根据配置规定判断大包字节数
            Long outputLargePacket = Long.parseLong(values[25]);
            //大包数；根据配置规定判断大包字节数
            Long largePacket = inputLargePacket + outputLargePacket;
            //流入零窗口数
            Long inputZeroWindow = Long.parseLong(values[26]);
            //流出零窗口数
            Long outputZeroWindow = Long.parseLong(values[27]);
            //零窗口数
            Long zeroWindow = inputZeroWindow + outputZeroWindow;
            //结束时间戳-ms
            Long finishTime = Long.parseLong(values[29]);
            if (finishTime >= timeStamp) {
                finishTime = finishTime - timeStamp;
            }else {
                finishTime = null;
            }
            //SYN时间戳-ms
            Long synTime = Long.parseLong(values[30]);
            if (synTime >= timeStamp) {
                synTime = synTime - timeStamp;
            }else {
                synTime = null;
            }
            //SYN ACK时间戳-ms
            Long synAckTime = Long.parseLong(values[31]);
            if (synAckTime >= timeStamp) {
                synAckTime = synAckTime - timeStamp;
            }else {
                synAckTime = null;
            }
            //ACK时间戳-ms
            Long ackTime = Long.parseLong(values[32]);
            if (ackTime >= timeStamp) {
                ackTime = ackTime - timeStamp;
            }else {
                ackTime = null;
            }
            //Time to live
            Long ttl = Long.parseLong(values[33]);
            //TCP协议或UDP协议（1：TCP、0：UDP）
            String protocol = "tcp";
            if ("0".equals(values[34])) {
                protocol = "udp";
            }
            //连接次数
            long connCount = 1;

            return new QuintetModel(timeStamp, sourceIp, sourcePort, sourceMac,
                destinationIp, destinationPort, destinationMac, protocolId,
                vlanId, probeId, inputOctets, outputOctets, octets,
                inputPacket, outputPacket, packet, inputRetransPacket,
                outputRetransPacket, retransPacket, inputRetransDelay,
                outputRetransDelay, retransDelay, inputRest, outputRest, rest,
                isSucceed, inputSmallPacket, outputSmallPacket, smallPacket,
                inputMediumPacket, outputMediumPacket, mediumPacket, inputLargePacket,
                outputLargePacket, largePacket, inputZeroWindow, outputZeroWindow,
                zeroWindow, finishTime, synTime, synAckTime,
                ackTime, ttl, protocol, connCount);
        }
        return null;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getSourceIp() {
        return sourceIp;
    }

    public void setSourceIp(String sourceIp) {
        this.sourceIp = sourceIp;
    }

    public String getSourcePort() {
        return sourcePort;
    }

    public void setSourcePort(String sourcePort) {
        this.sourcePort = sourcePort;
    }

    public String getSourceMac() {
        return sourceMac;
    }

    public void setSourceMac(String sourceMac) {
        this.sourceMac = sourceMac;
    }

    public String getDestinationIp() {
        return destinationIp;
    }

    public void setDestinationIp(String destinationIp) {
        this.destinationIp = destinationIp;
    }

    public String getDestinationPort() {
        return destinationPort;
    }

    public void setDestinationPort(String destinationPort) {
        this.destinationPort = destinationPort;
    }

    public String getDestinationMac() {
        return destinationMac;
    }

    public void setDestinationMac(String destinationMac) {
        this.destinationMac = destinationMac;
    }

    public String getProtocolId() {
        return protocolId;
    }

    public void setProtocolId(String protocolId) {
        this.protocolId = protocolId;
    }

    public String getVlanId() {
        return vlanId;
    }

    public void setVlanId(String vlanId) {
        this.vlanId = vlanId;
    }

    public String getProbeId() {
        return probeId;
    }

    public void setProbeId(String probeId) {
        this.probeId = probeId;
    }

    public long getInputOctets() {
        return inputOctets;
    }

    public void setInputOctets(long inputOctets) {
        this.inputOctets = inputOctets;
    }

    public long getOutputOctets() {
        return outputOctets;
    }

    public void setOutputOctets(long outputOctets) {
        this.outputOctets = outputOctets;
    }

    public long getOctets() {
        return octets;
    }

    public void setOctets(long octets) {
        this.octets = octets;
    }

    public long getInputPacket() {
        return inputPacket;
    }

    public void setInputPacket(long inputPacket) {
        this.inputPacket = inputPacket;
    }

    public long getOutputPacket() {
        return outputPacket;
    }

    public void setOutputPacket(long outputPacket) {
        this.outputPacket = outputPacket;
    }

    public long getPacket() {
        return packet;
    }

    public void setPacket(long packet) {
        this.packet = packet;
    }

    public long getInputRetransPacket() {
        return inputRetransPacket;
    }

    public void setInputRetransPacket(long inputRetransPacket) {
        this.inputRetransPacket = inputRetransPacket;
    }

    public long getOutputRetransPacket() {
        return outputRetransPacket;
    }

    public void setOutputRetransPacket(long outputRetransPacket) {
        this.outputRetransPacket = outputRetransPacket;
    }

    public long getRetransPacket() {
        return retransPacket;
    }

    public void setRetransPacket(long retransPacket) {
        this.retransPacket = retransPacket;
    }

    public long getInputRetransDelay() {
        return inputRetransDelay;
    }

    public void setInputRetransDelay(long inputRetransDelay) {
        this.inputRetransDelay = inputRetransDelay;
    }

    public long getOutputRetransDelay() {
        return outputRetransDelay;
    }

    public void setOutputRetransDelay(long outputRetransDelay) {
        this.outputRetransDelay = outputRetransDelay;
    }

    public long getRetransDelay() {
        return retransDelay;
    }

    public void setRetransDelay(long retransDelay) {
        this.retransDelay = retransDelay;
    }

    public long getInputRest() {
        return inputRest;
    }

    public void setInputRest(long inputRest) {
        this.inputRest = inputRest;
    }

    public long getOutputRest() {
        return outputRest;
    }

    public void setOutputRest(long outputRest) {
        this.outputRest = outputRest;
    }

    public long getRest() {
        return rest;
    }

    public void setRest(long rest) {
        this.rest = rest;
    }

    public String getIsSucceed() {
        return isSucceed;
    }

    public void setIsSucceed(String isSucceed) {
        this.isSucceed = isSucceed;
    }

    public long getInputSmallPacket() {
        return inputSmallPacket;
    }

    public void setInputSmallPacket(long inputSmallPacket) {
        this.inputSmallPacket = inputSmallPacket;
    }

    public long getOutputSmallPacket() {
        return outputSmallPacket;
    }

    public void setOutputSmallPacket(long outputSmallPacket) {
        this.outputSmallPacket = outputSmallPacket;
    }

    public long getSmallPacket() {
        return smallPacket;
    }

    public void setSmallPacket(long smallPacket) {
        this.smallPacket = smallPacket;
    }

    public long getInputMediumPacket() {
        return inputMediumPacket;
    }

    public void setInputMediumPacket(long inputMediumPacket) {
        this.inputMediumPacket = inputMediumPacket;
    }

    public long getOutputMediumPacket() {
        return outputMediumPacket;
    }

    public void setOutputMediumPacket(long outputMediumPacket) {
        this.outputMediumPacket = outputMediumPacket;
    }

    public long getMediumPacket() {
        return mediumPacket;
    }

    public void setMediumPacket(long mediumPacket) {
        this.mediumPacket = mediumPacket;
    }

    public long getInputLargePacket() {
        return inputLargePacket;
    }

    public void setInputLargePacket(long inputLargePacket) {
        this.inputLargePacket = inputLargePacket;
    }

    public long getOutputLargePacket() {
        return outputLargePacket;
    }

    public void setOutputLargePacket(long outputLargePacket) {
        this.outputLargePacket = outputLargePacket;
    }

    public long getLargePacket() {
        return largePacket;
    }

    public void setLargePacket(long largePacket) {
        this.largePacket = largePacket;
    }

    public long getInputZeroWindow() {
        return inputZeroWindow;
    }

    public void setInputZeroWindow(long inputZeroWindow) {
        this.inputZeroWindow = inputZeroWindow;
    }

    public long getOutputZeroWindow() {
        return outputZeroWindow;
    }

    public void setOutputZeroWindow(long outputZeroWindow) {
        this.outputZeroWindow = outputZeroWindow;
    }

    public long getZeroWindow() {
        return zeroWindow;
    }

    public void setZeroWindow(long zeroWindow) {
        this.zeroWindow = zeroWindow;
    }

    public long getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(long finishTime) {
        this.finishTime = finishTime;
    }

    public long getSynTime() {
        return synTime;
    }

    public void setSynTime(long synTime) {
        this.synTime = synTime;
    }

    public long getSynAckTime() {
        return synAckTime;
    }

    public void setSynAckTime(long synAckTime) {
        this.synAckTime = synAckTime;
    }

    public long getAckTime() {
        return ackTime;
    }

    public void setAckTime(long ackTime) {
        this.ackTime = ackTime;
    }

    public long getTtl() {
        return ttl;
    }

    public void setTtl(long ttl) {
        this.ttl = ttl;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public long getConnCount() {
        return connCount;
    }

    public void setConnCount(long connCount) {
        this.connCount = connCount;
    }

    @Override
    public String toString() {
        String str = "{";
        str = str + "\"timeStamp\":\"" + new DateTime(timeStamp, DateTimeZone.forID(druidFormat)) + "\",";
        str = str + "\"sourceIp\":\"" + sourceIp + "\",";
        str = str + "\"sourcePort\":\"" + sourcePort + "\",";
        str = str + "\"sourceMac\":\"" + sourceMac + "\",";
        str = str + "\"destinationIp\":\"" + destinationIp + "\",";
        str = str + "\"destinationPort\":\"" + destinationPort + "\",";
        str = str + "\"destinationMac\":\"" + destinationMac + "\",";
        str = str + "\"protocolId\":\"" + protocolId + "\",";
        str = str + "\"vlanId\":\"" + vlanId + "\",";
        str = str + "\"probeId\":\"" + probeId + "\",";
        str = str + "\"inputOctets\":" + inputOctets + ",";
        str = str + "\"outputOctets\":" + outputOctets + ",";
        str = str + "\"octets\":" + octets + ",";
        str = str + "\"inputPacket\":" + inputPacket + ",";
        str = str + "\"outputPacket\":" + outputPacket + ",";
        str = str + "\"packet\":" + packet + ",";
        str = str + "\"inputRetransPacket\":" + inputRetransPacket + ",";
        str = str + "\"outputRetransPacket\":" + outputRetransPacket + ",";
        str = str + "\"retransPacket\":" + retransPacket + ",";
        str = str + "\"inputRetransDelay\":" + inputRetransDelay + ",";
        str = str + "\"outputRetransDelay\":" + outputRetransDelay + ",";
        str = str + "\"retransDelay\":" + retransDelay + ",";
        str = str + "\"inputRest\":" + inputRest + ",";
        str = str + "\"outputRest\":" + outputRest + ",";
        str = str + "\"rest\":" + rest + ",";
        str = str + "\"isSucceed\":\"" + isSucceed + "\",";
        str = str + "\"inputSmallPacket\":" + inputSmallPacket + ",";
        str = str + "\"outputSmallPacket\":" + outputSmallPacket + ",";
        str = str + "\"smallPacket\":" + smallPacket + ",";
        str = str + "\"inputMediumPacket\":" + inputMediumPacket + ",";
        str = str + "\"outputMediumPacket\":" + outputMediumPacket + ",";
        str = str + "\"mediumPacket\":" + mediumPacket + ",";
        str = str + "\"inputLargePacket\":" + inputLargePacket + ",";
        str = str + "\"outputLargePacket\":" + outputLargePacket + ",";
        str = str + "\"largePacket\":" + largePacket + ",";
        str = str + "\"inputZeroWindow\":" + inputZeroWindow + ",";
        str = str + "\"outputZeroWindow\":" + outputZeroWindow + ",";
        str = str + "\"zeroWindow\":" + zeroWindow + ",";
        str = str + "\"finishTime\":" + finishTime + ",";
        str = str + "\"synTime\":" + synTime + ",";
        str = str + "\"synAckTime\":" + synAckTime + ",";
        str = str + "\"ackTime\":" + ackTime + ",";
        str = str + "\"ttl\":" + ttl + ",";
        str = str + "\"protocol\":\"" + protocol + "\",";
        str = str + "\"connCount\":" + connCount + "}";
        return str;
    }

    public String getJson() {

        JSONObject jsonObject = new JSONObject();

        jsonObject.put("timeStamp", new DateTime(timeStamp, DateTimeZone.forID(druidFormat)));
        jsonObject.put("sourceIp", sourceIp);
        jsonObject.put("sourcePort", sourcePort);
        jsonObject.put("sourceMac", sourceMac);
        jsonObject.put("destinationIp", destinationIp);
        jsonObject.put("destinationPort", destinationPort);
        jsonObject.put("destinationMac", destinationMac);
        jsonObject.put("protocolId", protocolId);
        jsonObject.put("vlanId", vlanId);
        jsonObject.put("probeId", probeId);
        jsonObject.put("inputOctets", inputOctets);
        jsonObject.put("outputOctets", outputOctets);
        jsonObject.put("octets", octets);
        jsonObject.put("inputPacket", inputPacket);
        jsonObject.put("outputPacket", outputPacket);
        jsonObject.put("packet", packet);
        jsonObject.put("inputRetransPacket", inputRetransPacket);
        jsonObject.put("outputRetransPacket", outputRetransPacket);
        jsonObject.put("retransPacket", retransPacket);
        jsonObject.put("inputRetransDelay", inputRetransDelay);
        jsonObject.put("outputRetransDelay", outputRetransDelay);
        jsonObject.put("retransDelay", retransDelay);
        jsonObject.put("inputRest", inputRest);
        jsonObject.put("outputRest", outputRest);
        jsonObject.put("rest", rest);
        jsonObject.put("isSucceed", isSucceed);
        jsonObject.put("inputSmallPacket", inputSmallPacket);
        jsonObject.put("outputSmallPacket", outputSmallPacket);
        jsonObject.put("smallPacket", smallPacket);
        jsonObject.put("inputMediumPacket", inputMediumPacket);
        jsonObject.put("outputMediumPacket", outputMediumPacket);
        jsonObject.put("mediumPacket", mediumPacket);
        jsonObject.put("inputLargePacket", inputLargePacket);
        jsonObject.put("outputLargePacket", outputLargePacket);
        jsonObject.put("largePacket", largePacket);
        jsonObject.put("inputZeroWindow", inputZeroWindow);
        jsonObject.put("outputZeroWindow", outputZeroWindow);
        jsonObject.put("zeroWindow", zeroWindow);
        jsonObject.put("finishTime", finishTime);
        jsonObject.put("synTime", synTime);
        jsonObject.put("synAckTime", synAckTime);
        jsonObject.put("ackTime", ackTime);
        jsonObject.put("ttl", ttl);
        jsonObject.put("protocol", protocol);
        jsonObject.put("connCount", connCount);

        return jsonObject.toString();
    }
}
