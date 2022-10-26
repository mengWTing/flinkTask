package cn.ffcs.is.mss.analyzer.utils;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PathFilter;

/**
 * @Auther chenwei
 * @Description
 * @Date: Created in 2017/12/22 11:52
 * @Modified By
 */
public class MapReduceUtils {


    /**
     * @Auther chenwei
     * @Description 按正则过滤MapReduce读取文件的类
     * @Date: Created in 2017/12/22 11:53
     */
    public static class RegexExcludePathFilter implements PathFilter {

        private final String regex;
        public RegexExcludePathFilter(String regex) {
            this.regex = regex;
        }

        @Override
        public boolean accept(Path path) {

            return path.toString().matches(regex);

        }
    }

}
