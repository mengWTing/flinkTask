/*
 * @project mss
 * @company Fujian Fujitsu Communication Software Co., Ltd.
 * @author chenwei
 * @date 2019-06-02 22:55:28
 * @version v1.0
 * @update [no] [date YYYY-MM-DD] [name] [description]
 */
package cn.ffcs.is.mss.analyzer.utils.encryption;


import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Base64;
import java.util.Random;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;

/**
 *
 *
 * @author chenwei
 * @date 2019-06-02 22:55:28
 * @title EncryptionUtil
 * @update [no] [date YYYY-MM-DD][name] [description]
 */
// gcc -I /Library/Java/JavaVirtualMachines/jdk1.8.0_144.jdk/Contents/Home/include -I /usr/local/Cellar/openssl/1.0.2r/include -I /Library/Java/JavaVirtualMachines/jdk1.8.0_144.jdk/Contents/Home/include/darwin -L /usr/local/Cellar/openssl/1.0.2r/lib/ -l ssl -l crypto encryption.c  -fPIC -shared -o libencryption.so
public class EncryptionUtil {

    public EncryptionUtil(){

    }

    public EncryptionUtil(String fileSystemType, String absolutePath,String hdfsPath) {
        try {

            String isLoadEncryption = System.getProperty("isLoadEncryption", "false");

            if (isLoadEncryption.equals("false")) {
                if (!fileSystemType.equals("file:///")) {
                    //先去hdfs上下载.so类库到各个节点上
                    try {
                        Configuration conf = new Configuration();
                        FileSystem fileSystem = FileSystem.get(URI.create(fileSystemType), conf);
                        FSDataInputStream in = fileSystem
                            .open(new Path(hdfsPath));
                        FileOutputStream out = new FileOutputStream(absolutePath);
                        IOUtils.copyBytes(in, out, 4096, true);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                System.load(absolutePath);
                System.setProperty("isLoadEncryption", "true");
            }
        }catch (Exception e){
            e.printStackTrace();
        }


    }

    public static void main(String[] args) {


        System.load("/Users/chenwei/vscode/password-encryption/libencryption.so");
        String data = "123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ~`!@#$%^&*()_+-={}[];:|,./<>? 黄颖茹 ";

        System.out.println(getCipher(data));
        System.out.println(getPlain(getCipher(data)));


    }

    /**
     * 根据要加密的数据得到密文
     * 密文是十进制数组按照|分割的字符串并用base64编码
     * @param data
     * @return
     */
    public static String getCipher(String data){
        EncryptionUtil encryptionUtil = new EncryptionUtil();
        StringBuilder cipher = new StringBuilder();
        int[] array = encryptionUtil.encryption(data);
        for (int i = 0; i < array.length; i++){
            cipher.append(array[i]);
            if (i +1 != array.length) {
                cipher.append("|");
            }
        }

        return Base64.getEncoder().encodeToString(cipher.toString().getBytes());
    }

    /**
     * 根据要解密的数据得到明文
     * 密文是十进制数组按照|分割的字符串并用base64编码
     * @param cipher
     * @return
     */
    public static String getPlain(String cipher){
        EncryptionUtil encryptionUtil = new EncryptionUtil();
        cipher = new String(Base64.getDecoder().decode(cipher));

        String[] values = cipher.split("\\|",-1);
        int array[] = new int[values.length];
        for (int i = 0; i < array.length; i++){
            array[i] = Integer.parseInt(values[i]);
        }

        return encryptionUtil.decryption(array);
    }



    /**
     * 获取16进制秘钥
     * length是秘钥长度
     * @param length
     * @return
     */
    public static String getKey(int length) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < length; i++) {
            result.append(Integer.toHexString(new Random().nextInt(16)));
        }

        return result.toString();
    }
    /**
     * 加密数据
     */
    public native int[] encryption(String data);

    /**
     * 解密数据
     */
    public native String decryption(int[] encrypt);

}
