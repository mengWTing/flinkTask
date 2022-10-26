package cn.ffcs.is.mss.analyzer.druid.model.java;

import com.metamx.tranquility.partition.HashCodePartitioner;
import java.io.Serializable;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.json.JSONObject;

/**
 * @Auther chenwei
 * @Description ARP协议产生的话单
 * 1526452481983|00:24:AC:BC:4D:9A|10.125.255.132|4C:09:B4:F6:DC:D0|10.125.255.131|     1      |    586     |20180516143441
 * startTime    |     srcMac      |     srcIp    |      desMac     |     desIp    |responseCode|responseTime|writetime
 * ms                                                                              0超时,1正常  | 超时时为0
 * @Date: Created in 2018/4/11 18:36
 * @Modified By
 */
public class ArpModel extends HashCodePartitioner<ArpModel> implements Serializable {
    //写入druid的时间戳格式
    public static final String druidFormat = "+08:00";

    //时间
    private long timeStamp;
    //源mac
    private String sourceMac;
    //源ip
    private String sourceIp;
    //目的mac
    private String destinationMac;
    //目的ip
    private String destinationIp;
    //响应情况
    private String responseCode;
    //响应时间
    private Long responseTime;


    public ArpModel(){
        super();
    }

    public ArpModel(long timeStamp, String sourceMac, String sourceIp, String destinationMac,
        String destinationIp, String responseCode, long responseTime) {

        this.timeStamp = timeStamp;
        this.sourceMac = sourceMac;
        this.sourceIp = sourceIp;
        this.destinationMac = destinationMac;
        this.destinationIp = destinationIp;
        this.responseCode = responseCode;
        this.responseTime = responseTime;
    }


    /**
     * @Auther chenwei
     * @Description 根据话单生成ArpModel
     * @Date: Created in 2018/5/16 16:58
     * @param line
     * @return
     */
    public static ArpModel getArpModel(String line) {
        String[] values = line.split("\\|", -1);

        if (values.length == 8) {

            //时间
            long timeStamp = Long.parseLong(values[0]);
            //源mac
            String sourceMac = values[1];
            //源ip
            String sourceIp = values[2];
            //目的mac
            String destinationMac = values[3];
            //目的ip
            String destinationIp = values[4];
            //响应情况
            String responseCode = values[5];
            //响应时间
            Long responseTime = Long.parseLong(values[6]);
            if ("0".equals(responseCode)) {
                responseTime = null;
            }


            return new ArpModel(timeStamp, sourceMac, sourceIp, destinationMac,
                destinationIp, responseCode, responseTime);

        }
        return null;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getSourceMac() {
        return sourceMac;
    }

    public void setSourceMac(String sourceMac) {
        this.sourceMac = sourceMac;
    }

    public String getSourceIp() {
        return sourceIp;
    }

    public void setSourceIp(String sourceIp) {
        this.sourceIp = sourceIp;
    }

    public String getDestinationMac() {
        return destinationMac;
    }

    public void setDestinationMac(String destinationMac) {
        this.destinationMac = destinationMac;
    }

    public String getDestinationIp() {
        return destinationIp;
    }

    public void setDestinationIp(String destinationIp) {
        this.destinationIp = destinationIp;
    }

    public String getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    public long getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(long responseTime) {
        this.responseTime = responseTime;
    }


    /**
     * @Auther chenwei
     * @Description 使用字符串拼接成json串
     * @Date: Created in 2018/5/16 16:03
     * @return
     */
    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("{");
        stringBuilder.append("\"timeStamp\":\"" + new DateTime(timeStamp, DateTimeZone.forID(druidFormat)) + "\",");
        stringBuilder.append("\"sourceMac\":\"" + sourceMac + "\",");
        stringBuilder.append("\"sourceIp\":\"" + sourceIp + "\",");
        stringBuilder.append("\"destinationMac\":\"" + destinationMac + "\",");
        stringBuilder.append("\"destinationIp\":\"" + destinationIp + "\",");
        stringBuilder.append("\"responseCode\":\"" + responseCode + "\",");
        stringBuilder.append("\"responseTime\":" + responseTime);
        stringBuilder.append("}");
        return stringBuilder.toString();
    }

    /**
     * @Auther chenwei
     * @Description 生成json串
     * @Date: Created in 2018/5/16 16:03
     * @return
     */
    public String getJson() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("timeStamp", new DateTime(timeStamp, DateTimeZone.forID(druidFormat)));
        jsonObject.put("sourceMac", sourceMac);
        jsonObject.put("sourceIp", sourceIp);
        jsonObject.put("destinationMac", destinationMac);
        jsonObject.put("destinationIp", destinationIp);
        jsonObject.put("responseCode", responseCode);
        jsonObject.put("responseTime", responseTime);

        return jsonObject.toString();
    }
}
