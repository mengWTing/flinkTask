package cn.ffcs.is.mss.analyzer.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Auther chenwei
 * @Description
 * @Date: Created in 2017/10/30 下午12:31
 * @Modified By
 */
public class IPUtil {

    private static final String SYMBOL = "\\.";
    private static final String RANGE_SYMBOL = "[~|-]";
    private static final String MASK_SYMBOL = "[/]";
    private static final String NUMBER = "(25[0-5]|2[0-4]\\d|[0-1]\\d{2}|[1-9]?\\d)";

    //ip掩码表示1.1.1.1/24
    private static Pattern IP_MASK_PATTERN = Pattern.compile(
        "^" + NUMBER + SYMBOL + NUMBER + SYMBOL + NUMBER + SYMBOL + NUMBER +  MASK_SYMBOL +"[0-9]*$");
    //ip范围表示1.1.1.1~1.1.1.2
    private static Pattern IP_RANGE_PATTERN = Pattern.compile(
        "^" + NUMBER + SYMBOL + NUMBER + SYMBOL + NUMBER + SYMBOL + NUMBER + RANGE_SYMBOL + NUMBER + SYMBOL + NUMBER + SYMBOL + NUMBER + SYMBOL + NUMBER + "$");

    //ip范围表示1.1.1.1-255
    private static Pattern IP_RANGE_PATTERN_2 = Pattern.compile(
        "^" + NUMBER + SYMBOL + NUMBER + SYMBOL + NUMBER + SYMBOL + NUMBER + RANGE_SYMBOL + "(.*)$");
    //ip1.1.1.1
    private static Pattern IP_PATTERN = Pattern
        .compile("^" + NUMBER + SYMBOL + NUMBER + SYMBOL + NUMBER + SYMBOL + NUMBER + "$");

    /**
     * @Auther chenwei
     * @Description 将二进制表示的ip转换成点分十进制法
     * @Date: Created in 2017/10/17 下午9:29
     * @param binaryIp
     * @return
     */
    public static String binaryIpToDecimalIp(String binaryIp){

        int length = binaryIp.length() / 8;
        String [] ipPart = new String[4];

        for (int i = 0; i < length; i++){
            ipPart[i] = binaryIp
                .substring(8 * i, 8 * (i+1));
        }

        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < 4; i++){

            if (ipPart[i] == null || ipPart[i].length() < 1){
                ipPart[i] = "0";
            }
            stringBuilder.append(Integer.parseInt(ipPart[i],2)).append(".");

        }

        return  stringBuilder.deleteCharAt(stringBuilder.length() - 1).toString();

    }

    /**
     * @Auther chenwei
     * @Description 将点分十进制法转换成二进制表示的ip
     * @Date: Created in 2017/10/17 下午9:19
     * @param decimalIp
     * @return
     */
    public static String decimalIpToBinaryIp(String decimalIp){

        StringBuilder stringBuilder = new StringBuilder();
        if (isIp(decimalIp)) {
            String[] values = decimalIp.split(SYMBOL);

            for (String value : values) {

                String binaryString = Integer.toBinaryString(Integer.parseInt(value));

                for (int num = binaryString.length(); num < 8; num++) {
                    stringBuilder.append("0");
                }
                stringBuilder.append(binaryString);
            }
            return stringBuilder.toString();
        }else {
            return null;
        }
    }

    /**
     * @Auther chenwei
     * @Description 二进制表示的ip加一
     * @Date: Created in 2017/10/17 下午7:20
     * @param binaryIp
     * @return
     */
    public static String addBinaryOne(String binaryIp){
        int[] binaryNums = new int[binaryIp.length()];

        // 将字符数组中的值转换了数值的0或者1
        for (int i = 0; i < binaryIp.length(); i++) {
            binaryNums[i] = binaryIp.charAt(i) - '0';
        }


        int binaryNumsLastIndex = binaryNums.length - 1; // 字符数组ca最后一个索引下标
        int carry = 1; // 下位的进位标识
        int result; // 加的结果

        // 计算1010101101 + 1
        while (binaryNumsLastIndex >= 0) {
            result = binaryNums[binaryNumsLastIndex] + carry;
            binaryNums[binaryNumsLastIndex] = result % 2;
            carry = result / 2;

            binaryNumsLastIndex--;
        }


        // 将字符数组中的值转换了字符的0或者1
        for (int i = 0; i < binaryNums.length; i++) {
            binaryNums[i] += '0';
        }


        // 不需要扩展一位
        if (carry == 0) {

            char[] ch = new char[binaryNums.length];
            for (int i = 0; i < binaryNums.length; i++) {
                ch[i] = (char) (binaryNums[i]);
            }

            return new String(ch);
        }
        // 需要扩展一位
        else {
            char[] ch = new char[binaryNums.length + 1];
            ch[0] = '1';
            for (int i = 0; i < binaryNums.length; i++) {
                ch[i + 1] = (char) (binaryNums[i]);
            }
            return new String(ch);
        }
    }


    /**
     * @Auther chenwei
     * @Description 判断是否为掩码表示法
     * @Date: Created in 2017/10/30 下午1:17
     * @param ip
     * @return
     */
    public static boolean isIpMask(String ip){

        Matcher matcher = IP_MASK_PATTERN.matcher(ip);

        return matcher.find();


    }


    /**
     * @Auther chenwei
     * @Description 判断是否为范围表示法
     * @Date: Created in 2017/10/30 下午1:18
     * @param ip
     * @return
     */
    public static boolean isIpRange(String ip){

        Matcher matcher = IP_RANGE_PATTERN.matcher(ip);

        return matcher.find();


    }

    /**
     * @Auther chenwei
     * @Description 判断是否为ip
     * @Date: Created in 2017/10/30 下午1:18
     * @param ip
     * @return
     */
    public static boolean isIp(String ip){

        Matcher matcher = IP_PATTERN.matcher(ip);

        return matcher.find();


    }

    /**
     * @Auther chenwei
     * @Description 判断是否为ip部分表示
     * @Date: Created in 2017/10/30 下午1:20
     * @param ip
     * @return
     */
    public static boolean isIpPart(String ip){


        String[] parts = ip.split(SYMBOL);
        if (parts.length <= 4) {

            for (String part : parts) {
                try {
                    int temp = Integer.parseInt(part);
                    if (temp < 0 || 255 < temp){
                        return false;
                    }
                }catch (Exception e){
                    return false;
                }


            }
            return true;
        }
            return false;

    }



    /**
     * @Auther chenwei
     * @Description 将各种形式的ip转换为部分表示
     * @Date: Created in 2017/10/30 下午11:14
     * @param ip
     * @return
     */
    public static List<String> getIpParts(String ip){

        List<String> list = new ArrayList<>();

        if (isIp(ip)){
            list.add(ip);
            return list;
        }

        if (isIpPart(ip)){
            list.add(ip);
            return list;
        }

        if (isIpMask(ip)) {
            list = markIpToPartIp(ip);
            return list;
        }

        if (isIpRange(ip)){
            list = rangeIpToPartIp(ip);
            return list;
        }
        if(isIpRange2(ip)){
            list = range2IpToPartIp(ip);
            return list;
        }
        ip = compositeIpToIpRange(ip);
        if (ip.length() != 0){
            list = rangeIpToPartIp(ip);
            System.out.println(ip);
            return list;
        }
        return list;
    }

    /**
     * @Auther chenwei
     * @Description 范围表示的ip转换为ip部分表示
     * @Date: Created in 2018/1/26 18:39
     * @param ip
     * @return
     */
    public static List<String> rangeIpToPartIp(String ip){
        List<String> list = new ArrayList<>();
        String[] values = ip.split(RANGE_SYMBOL,-1);

        String[] ipOneParts = values[0].split(SYMBOL,-1);
        String binaryIp = decimalIpToBinaryIp(values[1]);
        if (binaryIp != null) {
            String[] ipTwoParts = binaryIpToDecimalIp(addBinaryOne(binaryIp))
                .split(SYMBOL, -1);

            int index = 3;
            while (ipOneParts[index].equals("0") && ipTwoParts[index].equals("0")) {

                index--;

            }

            StringBuilder ipStart = new StringBuilder();
            for (int i = 0; i < index; i++) {
                ipStart.append(ipOneParts[i]).append(".");
            }

            ipStart.deleteCharAt(ipStart.length() - 1);
            int start = Integer.parseInt(ipOneParts[index]);
            int end = Integer.parseInt(ipTwoParts[index]);
            if (!ipOneParts[index - 1].equals(ipTwoParts[index - 1])) {
                end = 256;
            }

            for (; start < end; start++) {
                list.add(ipStart.toString() + "." + start);
            }

        }
        return list;

    }


    public static String ipToPartIp(String ip,int length){

        String[] values = ip.split(SYMBOL,-1);
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < length / 8; i++){
            result.append(values[i]).append(".");
        }

        result.deleteCharAt(result.length() - 1);
        return result.toString();

    }

    /**
     * @Auther chenwei
     * @Description 将ip掩码表示为ip部分
     * @Date: Created in 2017/10/30 下午11:01
     * @param ip
     * @return
     */
    public static List<String> markIpToPartIp(String ip){

        List<String> list = new ArrayList<>();
        if(isIpMask(ip)) {

            String[] values = ip.split(MASK_SYMBOL, -1);
            if (values.length > 0) {
                String ipBinaryStr = decimalIpToBinaryIp(values[0]);
                if (ipBinaryStr != null) {
                    StringBuilder ipBinary = new StringBuilder(ipBinaryStr);
                    int length = Integer.parseInt(values[1]);

                    if (length % 8 == 0) {

                        String[] parts = values[0].split(SYMBOL, -1);
                        StringBuilder partIp = new StringBuilder();
                        for (int i = 0; i < length / 8; i++) {
                            partIp.append(parts[i]);
                            partIp.append(".");
                        }
                        partIp.deleteCharAt(partIp.length() - 1);
                        list.add(partIp.toString());

                    } else {
                        StringBuilder ipBinaryStart = new StringBuilder(
                            ipBinary.substring(0, length));

                        int zeroLength = (length / 8 + 1) * 8 - length;
                        int end = (int) Math.pow(2, zeroLength);
                        for (int i = 0; i < end; i++) {

                            list.add(ipToPartIp(
                                binaryIpToDecimalIp(ipBinaryStart + getBinary(i, zeroLength)),
                                (length / 8 + 1) * 8));
                        }


                    }
                }
            }
        }
        return list;
    }

    /**
     * @Auther chenwei
     * @Description 将Ip范围表示转换为ip掩码表示
     * @Date: Created in 2017/10/30 下午11:02
     * @param ip
     * @return
     */
    public static String rangeIpToMaskIp(String ip){

        if (isIpRange(ip)) {

            String[] values = ip.split(RANGE_SYMBOL, -1);

            String binaryIp1 = decimalIpToBinaryIp(values[0]);
            String binaryIp2 = decimalIpToBinaryIp(values[1]);
            if (binaryIp2 != null && binaryIp1 != null) {
                String ipBinary2 = addBinaryOne(binaryIp2);

                StringBuilder stringBuilder = new StringBuilder(binaryIp1);

                for (int i = binaryIp1.length() - 1; i >= 0; i--) {
                    if (binaryIp1.charAt(i) == ipBinary2.charAt(i)) {
                        stringBuilder.deleteCharAt(i);
                    } else {
                        break;
                    }
                }

                int length = stringBuilder.length();
                for (int i = stringBuilder.length(); i < 32; i++) {

                    stringBuilder.append("0");

                }

                return binaryIpToDecimalIp(stringBuilder.toString()) + "/" + length;
            }
        }
            return null;


    }


    /**
     * @Auther chenwei
     * @Description 将点分十进制法转化为十进制表示
     * @Date: Created in 2017/12/21 15:11
     */
    public static long getIpLong(String ip) {

        long ipLong = 0L;
        if (isIp(ip)) {
            String[] values = ip.split(SYMBOL, -1);

            for (int i = 0; i < 4; i++) {

                long temp = Long.parseLong(values[i]);

                ipLong = ipLong + temp * (long) Math.pow(256, 4 - i - 1);

            }

        }
        return ipLong;


    }


    /**
     * @Auther chenwei
     * @Description 获取数字指定长度的二进制数
     * @Date: Created in 2018/1/26 16:45
     * @param num
     * @param length
     * @return
     */
    public static String getBinary(int num,int length){

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(Integer.toBinaryString(num));

        for (int i = stringBuilder.length(); i < length; i++){
            stringBuilder.insert(0,"0");
        }

        return stringBuilder.toString();

    }

    /**
     * @Auther chenwei
     * @Description 将1.1.x.x-1.1.x.x转化为1.1.0.0-1.1.255.255
     * @Date: Created in 2018/2/5 14:02
     * @param Ip
     * @return
     */
    public static String compositeIpToIpRange(String Ip){

        StringBuilder stringBuilder = new StringBuilder();

        Pattern pattern = Pattern.compile(NUMBER);

        String[] ips = Ip.split(RANGE_SYMBOL,-1);
        if (ips.length == 2){
            for (int i = 0; i < 2; i++) {
                String[] values = ips[i].split(SYMBOL, -1);
                if (values.length == 4) {
                    for (int j = 0; j < 4; j++){
                        Matcher matcher = pattern.matcher(values[j]);
                        if (matcher.find()){
                            stringBuilder.append(values[j]);
                        }else {
                            if (i == 0){
                                stringBuilder.append("0");
                            }else {
                                stringBuilder.append("255");
                            }
                        }
                        stringBuilder.append(".");
                    }
                }
                stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                stringBuilder.append("-");
            }
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);

        }

        return stringBuilder.toString();

    }


    /**
     * @Auther chenwei
     * @Description 是否为另一种ip表示法;
     * @Date: Created in 2018/3/15 18:16
     * @param ip
     * @return
     */
    public static boolean isIpRange2(String ip){

        Matcher matcher = IP_RANGE_PATTERN_2.matcher(ip);
        if (matcher.find()){
            return isIpPart(matcher.group(5));
        }else {
            return false;
        }

    }

    /**
     * 将Ip范围另一种表示转换为ip掩码表示
     * @param ip
     * @return
     */
    public static List<String> range2IpToPartIp(String ip){

        List<String> list = new ArrayList<>();

        String ipOne = ip.split(RANGE_SYMBOL,-1)[0];
        String[] ipOneParts = ipOne.split("\\.",-1);

        String ipTwo = ip.split(RANGE_SYMBOL,-1)[1];
        String[] ipTwoParts = ipTwo.split("\\.",-1);

        int length = ipTwo.split("\\.",-1).length;

        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < 4 - length; i++){
            stringBuilder.append(ipOneParts[i]).append(".");
        }
        list.add(stringBuilder.toString());
        for (int i = 0; i < length; i++){
            Iterator<String> iterator = list.iterator();
            List<String> tempList = new ArrayList<>();
            while (iterator.hasNext()){
                String string = iterator.next();
                iterator.remove();
                for (int j = Integer.parseInt(ipOneParts[4 - length + i]); j <= Integer.parseInt(ipTwoParts[i]); j++){
                    tempList.add(string + j + ".");
                }
            }

            list.addAll(tempList);
        }


        for (int i = 0; i < list.size(); i++){
            String string = list.get(i);
            list.set(i,string.substring(0,string.length() - 1));
        }

        return list;

    }
}

