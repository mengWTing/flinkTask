package cn.ffcs.is.mss.analyzer.druid.model.java;

import com.metamx.tranquility.partition.HashCodePartitioner;
import java.io.Serializable;
import java.net.URLDecoder;
import java.util.HashSet;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.json.JSONObject;

/**
 * @Auther chenwei
 * @Description
 * @Date: Created in 2017/10/31 下午2:48
 * @Modified By
 */
public class OperationModel  extends HashCodePartitioner<OperationModel> implements Serializable {
    private static final long serialVersionUID = 1L;
    //写入druid的时间戳格式
    public static final String druidFormat = "+08:00";

    private static final String REGEX = "\\|";

    private static Map<String,String> placeMap;
    private static Map<String,String> systemMap;
    private static Map<String,String> majorMap;
    private static Map<String,String> usedPlaceMap;
    private static Map<String,String> sysIdMap;
    private static Map<String,HashSet<String>> usedPlacesMap;

    public static Pattern sysidPattern = Pattern.compile(".*value\\(sysid\\)=(.*?)&value.*");
    public static String GCFZ = "工辅";

    //时间戳
    public long timeStamp;

    //人力编码
    public String userName;
    //目的IP
    public String destinationIp;
    //目的PORT
    public String destinationPort;
    //登录专业
    public String loginMajor;
    //登录系统
    public String loginSystem;

    //源IP
    public String sourceIp;
    //源PORT
    public String sourcePort;
    //登录地
    public String loginPlace;
    //常用登录地
    public String usedPlace;
    //是否异地登录
    public String isRemote;

    //操作
    public String operate;


    //上行流量
    public long inputOctets;
    //下行流量
    public long outputOctets;
    //总流量
    public long octets;
    //连接次数
    public long connCount;

    public OperationModel(){
        super();
    }

    public OperationModel(String userName, String destinationIp, String destinationPort,
        String loginMajor, String loginSystem, String sourceIp, String sourcePort,
        String loginPlace, String usedPlace, String isRemote, String operate, long timeStamp,
        long inputOctets, long outputOctets, long octets, long connCount) {

        //时间戳
        this.timeStamp = timeStamp;

        //人力编码
        this.userName = userName;
        //目的IP
        this.destinationIp = destinationIp;
        //目的PORT
        this.destinationPort = destinationPort;
        //登录专业
        this.loginMajor = loginMajor;
        //登录系统
        this.loginSystem = loginSystem;

        //源IP
        this.sourceIp = sourceIp;
        //源PORT
        this.sourcePort = sourcePort;
        //登录地
        this.loginPlace = loginPlace;
        //常用登录地
        this.usedPlace = usedPlace;
        //是否异地登录
        this.isRemote = isRemote;

        //操作
        this.operate = operate;


        //上行流量
        this.inputOctets = inputOctets;
        //下行流量
        this.outputOctets = outputOctets;
        //总流量
        this.octets = octets;
        //连接次数
        this.connCount = connCount;


    }

    public static OperationModel getOperationModel(String line){
        String[] values = line.split("\\|",-1);

        if (values.length == 25) {

            //人力编码
            String userName = values[0].toUpperCase().trim();

            if (userName.length() == 0) {
                userName = "匿名用户";
            }

            //目的IP
            String destinationIp = values[1].trim();

            if (destinationIp.length() == 0) {
                destinationIp = "0.0.0.0";
            }

            //目的PORT
            String destinationPort = values[2].trim();

            if (destinationPort.length() == 0) {
                destinationPort = "-1";
            }

            //本次访问的url
            String url = getUrl(values[6]);
            //本次访问的host
            String host = values[5];
            //登录系统
            String loginSystem = getSystem(host, url);
            //登录专业
            String loginMajor = getMajor(loginSystem, host);

            //源IP
            String sourceIp = values[3].trim();

            if (sourceIp.length() == 0) {
                sourceIp = "0.0.0.0";
            }

            //源PORT
            String sourcePort = values[4].trim();

            if (sourcePort.length() == 0) {
                sourcePort = "-1";
            }

            //常用登录地
            String usedPlace = usedPlaceMap.get(userName);

            if (usedPlace == null || usedPlace.length() == 0) {
                usedPlace = "常用登陆地不详";
            }

            //本次登录地
            String loginPlace = getPlace(sourceIp);

            //是否异地登录
            String isRemote = getIsRemote(loginPlace, usedPlace);

            //根据url匹配的操作
            String operate = getOperate(url, loginMajor, loginSystem);

            //时间戳
            Long timeStamp = null;
            try {
                timeStamp = Long.parseLong(values[10].trim());
            } catch (Exception e) {

            }
            //上行流量
            Long inputOctets = null;
            try {
                inputOctets = Long.parseLong(values[11].trim());
            } catch (Exception e) {

            }
            //下行流量
            Long outputOctets = null;
            try {
                outputOctets = Long.parseLong(values[12].trim());
            } catch (Exception e) {

            }
            //总流量
            Long octets = inputOctets + outputOctets;
            //连接次数
            long connCount = 1L;

            return new OperationModel(userName, destinationIp,
                destinationPort, loginMajor, loginSystem, sourceIp, sourcePort, loginPlace,
                usedPlace, isRemote, operate, timeStamp, inputOctets, outputOctets, octets,
                connCount);
        }
        return null;
    }

    /**
     * @Auther chenwei
     * @Description 去掉host的端口部分
     * @Date: Created in 2018/1/22 16:33
     * @param host
     * @return
     */
    public static String getHost(String host){
        if (host.contains(":")){
            String[] words = host.split(":",-1);
            return words[0];
        }else {
            return host;
        }

    }

    /**
     * @Auther chenwei
     * @Description 根据IP获取登录地
     * @Date: Created in 2017/11/10 下午8:50
     * @param ip
     * @return
     */
    public static String getPlace(String ip){

        String[] ipPart = ip.split("\\.",-1);

        if (placeMap != null) {

            if (ipPart.length == 4) {

                if (placeMap.containsKey(ip)) {
                    return placeMap.get(ip);
                }

                String ipTwo = ipPart[0] + "." + ipPart[1];
                String ipThree = ipTwo + "." + ipPart[2];

                if (placeMap.containsKey(ipThree)) {
                    return placeMap.get(ipThree);
                }

                if (placeMap.containsKey(ipTwo)) {
                    return placeMap.get(ipTwo);
                }

                if (placeMap.containsKey(ipPart[0])) {
                    return placeMap.get(ipPart[0]);
                }

            }
        }
        return "未知地点";
    }

    /**
     * @Auther chenwei
     * @Description 根据系统名获取专业名
     * @Date: Created in 2017/11/27 18:32
     * @param system
     * @return
     */
    public static String getMajor(String system,String host){

        if (majorMap != null) {

            String major = majorMap.get(system);
            if (major != null && major.length() > 0) {
                return major;
            } else if (systemMap.containsKey(host)){
                system = systemMap.get(host);
                major = majorMap.get(system);
                return major;
            }else {
                return "未知专业";
            }
        }else {
            return "未知专业";
        }

    }

    /**
     * @Auther chenwei
     * @Description 根据IP获取登录系统
     * @Date: Created in 2017/11/10 下午8:50
     * @param host
     * @param url
     * @return
     */
    public static String getSystem(String host,String url){

        String system = null;

        if (systemMap!= null) {

            if (systemMap.containsKey(host.toLowerCase()) || systemMap.containsKey(getHost(host).toLowerCase())){
                system = systemMap.get(host.toLowerCase());
                if (system == null){
                    system = systemMap.get(getHost(host).toLowerCase());
                }

                String major = getMajor(system,host);
                if (major.equals(GCFZ)){
                    Matcher matcher = sysidPattern.matcher(url.trim());
                    if (matcher.find()) {
                        String sysid = matcher.group(1);
                        String systemTemp = sysIdMap.get(sysid.trim().toUpperCase());

                        String[] words = system.split("-",-1);
                        if (words.length == 2 && systemTemp != null){

                            return systemTemp + "-" + words[0];
                        }

                    }
                }

            }


        }

        if (system != null && system.length() > 0){
            return system;
        }else {
            return "未知系统";
        }

    }


    /**
     * @Auther chenwei
     * @Description 根据url登录专业登录系统获取操作
     * @Date: Created in 2017/11/10 下午9:07
     * @param url
     * @param loginMajor
     * @param loginSystem
     * @return
     */
    public static String getOperate(String url,String loginMajor,String loginSystem){

        return "未识别操作";

    }

    /**
     * @Auther chenwei
     * @Description 根据url获取url(去除非UTF-8字符)
     * @Date: Created in 2017/11/12 下午1:24
     * @param url
     * @return
     */
    public static String getUrl(String url){


        try {
            url = URLDecoder.decode(url,"utf-8");
            return url;
        } catch (Exception e) {
        }


        return url;
    }


    /**
     * @Auther chenwei
     * @Description 根据常用登录地和本次登录地，判断是否异地登录
     * @Date: Created in 2017/11/12 下午1:42
     * @param place
     * @param usedPlace
     * @return
     */
    public static String getIsRemote(String place,String usedPlace){

        if ("未知地点".equals(place) || usedPlace == null ||usedPlace.length() < 1){
            return "false";
        }

        if (usedPlaceMap != null) {

            if (usedPlacesMap.containsKey(usedPlace)) {
                boolean isRemote = usedPlacesMap.get(usedPlace).contains(place);



                if (!isRemote) {
                    return "true";
                } else {

                    return "false";
                }
            } else {
                return "false";
            }

        }else {
            return "false";
        }
    }


    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
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

    public String getLoginMajor() {
        return loginMajor;
    }

    public void setLoginMajor(String loginMajor) {
        this.loginMajor = loginMajor;
    }

    public String getLoginSystem() {
        return loginSystem;
    }

    public void setLoginSystem(String loginSystem) {
        this.loginSystem = loginSystem;
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

    public String getLoginPlace() {
        return loginPlace;
    }

    public void setLoginPlace(String loginPlace) {
        this.loginPlace = loginPlace;
    }

    public String getUsedPlace() {
        return usedPlace;
    }

    public void setUsedPlace(String usedPlace) {
        this.usedPlace = usedPlace;
    }

    public String getIsRemote() {
        return isRemote;
    }

    public void setIsRemote(String isRemote) {
        this.isRemote = isRemote;
    }

    public String getOperate() {
        return operate;
    }

    public void setOperate(String operate) {
        this.operate = operate;
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

    public long getConnCount() {
        return connCount;
    }

    public void setConnCount(long connCount) {
        this.connCount = connCount;
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
        stringBuilder.append("\"userName\":\"" + userName + "\",");
        stringBuilder.append("\"destinationIp\":\"" + destinationIp + "\",");
        stringBuilder.append("\"destinationPort\":\"" + destinationPort + "\",");
        stringBuilder.append("\"loginMajor\":\"" + loginMajor + "\",");
        stringBuilder.append("\"loginSystem\":\"" + loginSystem + "\",");
        stringBuilder.append("\"sourceIp\":\"" + sourceIp + "\",");
        stringBuilder.append("\"sourcePort\":\"" + sourcePort + "\",");
        stringBuilder.append("\"loginPlace\":\"" + loginPlace + "\",");
        stringBuilder.append("\"usedPlace\":\"" + usedPlace + "\",");
        stringBuilder.append("\"isRemote\":\"" + isRemote + "\",");
        stringBuilder.append("\"operate\":\"" + operate + "\",");
        stringBuilder.append("\"inputOctets\":" + inputOctets + ",");
        stringBuilder.append("\"outputOctets\":" + outputOctets + ",");
        stringBuilder.append("\"octets\":" + octets + ",");
        stringBuilder.append("\"connCount\":" + connCount);
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
        jsonObject.put("userName", userName);
        jsonObject.put("destinationIp", destinationIp);
        jsonObject.put("destinationPort", destinationPort);
        jsonObject.put("loginMajor", loginMajor);
        jsonObject.put("loginSystem", loginSystem);
        jsonObject.put("sourceIp", sourceIp);
        jsonObject.put("sourcePort", sourcePort);
        jsonObject.put("loginPlace", loginPlace);
        jsonObject.put("usedPlace", usedPlace);
        jsonObject.put("isRemote", isRemote);
        jsonObject.put("operate", operate);
        jsonObject.put("inputOctets", inputOctets);
        jsonObject.put("outputOctets", outputOctets);
        jsonObject.put("octets", octets);
        jsonObject.put("connCount", connCount);

        return jsonObject.toString();


    }

}
