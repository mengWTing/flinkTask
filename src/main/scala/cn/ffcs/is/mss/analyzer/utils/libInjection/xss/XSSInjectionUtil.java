package cn.ffcs.is.mss.analyzer.utils.libInjection.xss;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;

/**
 * @Auther chenwei
 * @Description
 * @Date: Created in 2018/12/16 23:47
 * @Modified By
 */

//gcc -I /Library/Java/JavaVirtualMachines/jdk1.8.0_144.jdk/Contents/Home/include -I /Library/Java/JavaVirtualMachines/jdk1.8.0_144.jdk/Contents/Home/darwin libinjection_xss.c libinjection_html5.c libinjection_sqli.c detect.c  -fPIC -shared -o libinjection.so
public class XSSInjectionUtil {

    public XSSInjectionUtil(String fileSystemType, String soPath, String absolutePath) {
        String isLoadInjection = System.getProperty("isLoadXSSInjection", "false");
        if (isLoadInjection.equals("false")) {
//            if (!fileSystemType.equals("fiels:///")) {
//                //先去hdfs上下载.so类库到各个节点上
//                try {
//                    Configuration conf = new Configuration();
//                    FileSystem fileSystem = FileSystem.get(URI.create(fileSystemType), conf);
//                    FSDataInputStream in = fileSystem
//                            .open(new Path(soPath));
//                    FileOutputStream out = new FileOutputStream(absolutePath);
//                    IOUtils.copyBytes(in, out, 4096, true);
//
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
            System.load(absolutePath);
            System.load("/lib64/libc.so.6");
            System.setProperty("isLoadXSSInjection", "true");
        }


        /*System.setProperty("java.library.path", System.getProperty("java.library.path")
                + ":/lib64/" + ":/home/ffcsip/");
        System.out.println(System.getProperty("java.library.path"));
        System.loadLibrary("injection");*/

        // System.out.println("start_______");
    }

    public native int checkXss(String str);


}

