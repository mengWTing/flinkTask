package cn.ffcs.is.mss.analyzer.druid.model.java;

import com.metamx.tranquility.partition.HashCodePartitioner;
import java.io.Serializable;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.json.JSONObject;

/**
 * @Auther chenwei
 * @Description DNS协议话单
 * res.qhmsg.com  |           |    5    |1523438110255|1523438110255|      1      |      0       |     0    |20180411171510
 * QueryDomainName|QueryResult|ReplyCode|  QueryTime  | ResponseTime|RequestNumber|ResponseNumber|Answer rrs|   writetime
 * QueryResult:取查询响应中的解析结果，对于存在多个解析结果的情况，使用分号分隔
 * Answer rrs:0为解析错误
 *
 * ReplyCode:
 * 0 : 没有错误。
 * 1 : 报文格式错误(Format error) - 服务器不能理解请求的报文;
 * 2 : 服务器失败(Server failure) - 因为服务器的原因导致没办法处理这个请求;
 * 3 : 名字错误(Name Error) - 只有对授权域名解析服务器有意义，指出解析的域名不存在;
 * 4 : 没有实现(Not Implemented) - 域名服务器不支持查询类型;
 * 5 : 拒绝(Refused) - 服务器由于设置的策略拒绝给出应答.比如，服务器不希望对某些请求者给出应答，或者服务器不希望进行某些操作（比如区域传送zone transfer）;
 * [6,15] : 保留值，暂未使用。
 *
 * @Date: Created in 2018/4/11 17:15
 * @Modified By
 */
public class DnsModel extends HashCodePartitioner<DnsModel> implements Serializable {
    //写入druid的时间戳格式
    public static final String druidFormat = "+08:00";

    //时间戳
    private long timeStamp;
    //请求查询的DNS域名
    private String queryDomainName;
    //DNS的解析结果
    private String queryResult;
    //DNS响应码
    private String replyCode;
    //时延
    private Long delay;
    //DNS的请求次数
    private Long requestNumber;
    //响应数目
    private Long responseNumber;
    //Answer rrs=0为解析错误
    private String answerRrs;

    public DnsModel() {
        super();
    }

    public DnsModel(String line) {

    }

    public DnsModel(long timeStamp, String queryDomainName, String queryResult, String replyCode,
        long delay, long requestNumber, long responseNumber, String answerRrs) {

        this.timeStamp = timeStamp;
        this.queryDomainName = queryDomainName;
        this.queryResult = queryResult;
        this.replyCode = replyCode;
        this.delay = delay;
        this.requestNumber = requestNumber;
        this.responseNumber = responseNumber;
        this.answerRrs = answerRrs;

    }

    /**
     * @Auther chenwei
     * @Description 根据话单生成DnsModel
     * @Date: Created in 2018/5/16 16:58
     * @param line
     * @return
     */
    public static DnsModel getDnsModel(String line){
        String[] values = line.split("\\|",-1);

        if (values.length == 9) {

            //查询时间戳
            long queryTimeStamp = Long.parseLong(values[3]);
            //写入时间戳
            long responseTimeStamp = Long.parseLong(values[4]);
            //入库时间戳
            long timeStamp = 0;
            //请求查询的DNS域名
            String queryDomainName = values[0];
            //DNS的解析结果
            String queryResult = values[1];
            //DNS响应码
            String replyCode = values[2];
            //时延
            Long delay = null;

            if (responseTimeStamp > 0 && queryTimeStamp > 0 && responseTimeStamp > queryTimeStamp) {
                delay = responseTimeStamp - queryTimeStamp;
                timeStamp = responseTimeStamp;
            } else {
                timeStamp = queryTimeStamp;
            }

            //DNS的请求次数
            long requestNumber = Long.parseLong(values[5]);
            //响应数目
            long responseNumber = Long.parseLong(values[6]);
            //Answer rrs=0为解析错误
            String answerRrs = values[7];
            return new DnsModel(timeStamp, queryDomainName, queryResult, replyCode, delay, requestNumber, responseNumber, answerRrs);
        }
        return null;
    }


    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getQueryDomainName() {
        return queryDomainName;
    }

    public void setQueryDomainName(String queryDomainName) {
        this.queryDomainName = queryDomainName;
    }

    public String getQueryResult() {
        return queryResult;
    }

    public void setQueryResult(String queryResult) {
        this.queryResult = queryResult;
    }

    public String getReplyCode() {
        return replyCode;
    }

    public void setReplyCode(String replyCode) {
        this.replyCode = replyCode;
    }

    public long getDelay() {
        return delay;
    }

    public void setDelay(long delay) {
        this.delay = delay;
    }

    public long getRequestNumber() {
        return requestNumber;
    }

    public void setRequestNumber(long requestNumber) {
        this.requestNumber = requestNumber;
    }

    public long getResponseNumber() {
        return responseNumber;
    }

    public void setResponseNumber(long responseNumber) {
        this.responseNumber = responseNumber;
    }

    public String getAnswerRrs() {
        return answerRrs;
    }

    public void setAnswerRrs(String answerRrs) {
        this.answerRrs = answerRrs;
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
        stringBuilder.append("\"queryDomainName\":\"" + queryDomainName + "\",");
        stringBuilder.append("\"queryResult\":\"" + queryResult + "\",");
        stringBuilder.append("\"replyCode\":\"" + replyCode + "\",");
        stringBuilder.append("\"delay\":" + delay + ",");
        stringBuilder.append("\"requestNumber\":" + requestNumber + ",");
        stringBuilder.append("\"responseNumber\":" + responseNumber + ",");
        stringBuilder.append("\"answerRrs\":\"" + answerRrs + "\"");

        stringBuilder.append("}");

        return stringBuilder.toString();

    }


    /**
     * @Auther chenwei
     * @Description 生成json串
     * @Date: Created in 2018/5/16 16:03
     * @return
     */
    public String getJson(){

        JSONObject jsonObject = new JSONObject();

        jsonObject.put("timeStamp", new DateTime(timeStamp, DateTimeZone.forID(druidFormat)));
        jsonObject.put("queryDomainName", queryDomainName );
        jsonObject.put("queryResult", queryResult);
        jsonObject.put("replyCode", replyCode);
        jsonObject.put("delay", delay);
        jsonObject.put("requestNumber", requestNumber);
        jsonObject.put("responseNumber", responseNumber);
        jsonObject.put("answerRrs", answerRrs);


        return jsonObject.toString();

    }
}
