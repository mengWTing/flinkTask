
/**************************************************************
 *
 * Copyright ? 2013，北京福富软件技术股份有限公司
 * All Rights Reserved.
 * ------------------------------------------------------------
 * 文件名称：IniProperties.java
 * 文件摘要：ini文件操作工具类
 * 初始版本：V1.0.0
 * 初始作者：郑炎
 * 完成日期：2013
 *
 **************************************************************/
package cn.ffcs.is.mss.analyzer.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**************************************************************
 *
 * 类名：IniProperties<br>
 * <br>
 * 功能：Ini配置文件操作类<br>
 * <br>
 * 作者：ZhengYan 2015-3-10<br>
 * <br>
 * <br>
 * 修改记录：<br>
 * 		日期				修改人			修改说明<br>
 *
 **************************************************************/
public class IniProperties
{
    public Map<String, Map<String, Object>> context;

    private String currentSection = "";

//	private String filePath;

    /**************************************************************************
     *
     * 函数名：IniPropertiesUtil(String filePath)<br>
     * <br>
     * 功能：构造方法<br>
     * <br>
     * 作者：ZhengYan 2013-03-04<br>
     * <br>
     * 参数表：<br>
     * String filePath -> Ini文件路径 <br>
     * 修改记录：<br>
     * 日期 修改人 修改说明<br>
     *
     **************************************************************************/
    public IniProperties(String filePath) throws IOException {
        System.out.println(new File(filePath).getAbsolutePath());
        InputStream stream = new FileInputStream(filePath);
        init(stream);
        stream.close();
    }

    public IniProperties(String filePath,String charSet) throws IOException {
        InputStream stream = new FileInputStream(filePath);
        init(stream,charSet);
        stream.close();
    }

    /**************************************************************************
     *
     * 函数名：IniPropertiesUtil(String filePath)<br>
     * <br>
     * 功能：构造方法<br>
     * <br>
     * 作者：ZhengYan 2013-03-04<br>
     * <br>
     * 参数表：<br>
     * InputStream stream -> Ini输入流 <br>
     * 修改记录：<br>
     * 日期 修改人 修改说明<br>
     *
     **************************************************************************/
    public IniProperties(InputStream stream) throws IOException {
        init(stream);
    }

    /**************************************************************
     *
     * 功能：构造方法<br>
     * <br>
     * 作者：郑炎 2014-1-16<br>
     * <br>
     * @param stream 输入流
     * @param charset 字符集名
     * @throws IOException
     * <br>
     * 修改记录：<br>
     * 		日期				修改人			修改说明<br>
     *
     **************************************************************/
    public IniProperties(InputStream stream,String charset) throws IOException {
        init(stream,charset);
    }

    private void init(InputStream stream) throws IOException {
        init(stream, "UTF-8");
    }

    private void init(InputStream stream,String charset) throws IOException {
        context = new HashMap<>();
        context.put(currentSection, new HashMap<>());
        BufferedReader reader = new BufferedReader(new InputStreamReader(
            stream, charset));
        {
            for (String line; (line = reader.readLine()) != null;) {
                int indexLeft = line.indexOf("[");
                int indexRight = line.indexOf("]");
                line = line.trim();

                if("".equals(line)) {
                    continue;
                }

                if (line.contains("#")) {
                    continue;
                }

                if (indexLeft == 0 && indexRight > indexLeft) {
                    currentSection = line.substring(indexLeft + 1, indexRight);
                    if (!context.containsKey(currentSection)) {
                        context.put(currentSection, new HashMap<>());
                    }
                } else if (line.contains("=")) {
                    String[] parameter = line.split("=");
                    String key, value;
                    if (parameter.length <= 0) {
                        key = "";
                        value = "";
                    } else if (parameter.length == 1) {
                        key = parameter[0].trim();
                        value = "";
                    } else {
                        key = parameter[0].trim();
                        value = "";
                        for(int i = 1;i<parameter.length;i++)
                        {
                            value = value + parameter[i];
                            if(i < parameter.length - 1) {
                                value = value + "=";
                            }
                        }

                        value = value.split(";")[0].trim();
                    }

                    context.get(currentSection).put(key, value);
                }
            }
        }
        reader.close();
    }

    public Map<String,Object> getMap(String section){
        return context.get(section);
    }

    /**************************************************************************
     *
     * 函数名：getValue(String section, String key)<br>
     * <br>
     * 功能：获取指定节点下指定键的值<br>
     * <br>
     * 作者：ZhengYan 2013-03-04<br>
     * <br>
     * 参数表：<br>
     * String section -> 节点名(没有节点名的用"") String key -> 键名 <br>
     * 返回值：<br>
     * String -> 值<br>
     * <br>
     * 修改记录：<br>
     * 日期 修改人 修改说明<br>
     *
     **************************************************************************/
    public String getValue(String section, String key) {
        if(section == null) {
            section = "";
        }
        if (!context.containsKey(section)) {
            return null;
        }
        if (!context.get(section).containsKey(key)) {
            return null;
        }
        return (String)context.get(section).get(key);
    }

    public int getIntValue(String section, String key)
    {
        try
        {
            return Integer.parseInt(getValue(section,key));
        }catch(Exception ignored){}
        return 0;
    }

    public float getFloatValue(String section, String key)
    {
        try
        {
            return Float.parseFloat(getValue(section,key));
        }catch(Exception ignored){}
        return 0;
    }

    public long getLongValue(String section, String key)
    {
        try
        {
            return Long.parseLong(getValue(section,key));
        }catch(Exception ignored){}
        return 0;
    }

    public boolean getBoolValue(String section, String key)
    {
        try
        {
            return Boolean.parseBoolean(getValue(section,key));
        }catch(Exception ignored){}
        return false;
    }

    public Date getDateValue(String section, String key)
    {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            return formatter.parse(getValue(section, key));
        } catch (Exception ignored) {}

        return new Date();
    }

    public String[] getValues(String section, String key)
    {
        int n = getIntValue(section, key + "Num");

        String[] result = new String[n];

        for(int i = 0; i < n; i++) {
            result[i] = getValue(section, key + i);
        }

        return result;
    }

    /**************************************************************************
     *
     * 函数名：getKeyValues(String section)<br>
     * <br>
     * 功能：获取指定节点下所有键值对<br>
     * <br>
     * 作者：ZhengYan 2013-03-04<br>
     * <br>
     * 参数表：<br>
     * String section -> 节点名(没有节点名的用"") <br>
     * 返回值：<br>
     * Map<String,String> -> 该节点下的所有键值对<br>
     * <br>
     * 修改记录：<br>
     * 日期 修改人 修改说明<br>
     *
     **************************************************************************/
    public Map<String, Object> getKeyValues(String section) {
        if (!context.containsKey(section)) {
            return new HashMap<String, Object>();
        }
        return context.get(section);
    }

    /**************************************************************************
     *
     * 函数名：setValue(String section, String key,String value)<br>
     * <br>
     * 功能：设置指定节点下指定键的值<br>
     * <br>
     * 作者：ZhengYan 2013-03-04<br>
     * <br>
     * 参数表：<br>
     * String section -> 节点名(没有节点名的用"") String key -> 键名 String value -> 值 <br>
     * 修改记录：<br>
     * 日期 修改人 修改说明<br>
     *
     **************************************************************************/
    public void setValue(String section, String key, String value) {
        if (!context.containsKey(section)) {
            context.put(section, new HashMap<String, Object>());
        }
        if (!context.get(section).containsKey(key)) {
            context.get(section).put(key, value);
            return;
        }
        context.get(section).put(key, value);
    }

    /**************************************************************************
     *
     * 函数名：save(OutputStream stream)<br>
     * <br>
     * 功能：将配置内容保存到指定的输出流中<br>
     * <br>
     * 作者：ZhengYan 2013-03-04<br>
     * <br>
     * 参数表：<br>
     * OutputStream stream -> 指定要保存的输出流 <br>
     * 修改记录：<br>
     * 日期 修改人 修改说明<br>
     *
     **************************************************************************/
    public void save(OutputStream stream) throws IOException {
        if (context == null) {
            return;
        }
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
            stream));
        {
            for (String section : context.keySet()) {
                if (section != null && !"".equals(section)) {
                    writer.write("[" + section + "]\r\n");
                }

                for (String key : context.get(section).keySet()) {
                    writer.write(key + "=" + context.get(section).get(key)
                        + "\r\n");
                }
            }

            writer.flush();
        }
        writer.close();
    }

    @Override
    public String toString() {
        if (context == null) {
            return null;
        }
        StringBuilder buffer = new StringBuilder();
        for (String section : context.keySet()) {
            if (section != null && !"".equals(section)) {
                buffer.append("[" + section + "]\r\n");
            }

            for (String key : context.get(section).keySet()) {
                buffer.append(key + "=" + context.get(section).get(key)
                    + "\r\n");
            }
        }

        return buffer.toString();
    }



    /**************************************************************************
     *
     * 函数名：save(String filePath)<br>
     * <br>
     * 功能：将配置内容保存到指定路径的文件中<br>
     * <br>
     * 作者：ZhengYan 2013-03-04<br>
     * <br>
     * 参数表：<br>
     * String filePath -> 指定的要保存的文件路径 <br>
     * 修改记录：<br>
     * 日期 修改人 修改说明<br>
     *
     **************************************************************************/
    public void save(String filePath) throws IOException {
        FileOutputStream fos = new FileOutputStream(filePath);
        this.save(fos);
        fos.close();
    }

}
