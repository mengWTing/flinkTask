package cn.ffcs.is.mss.analyzer.flink.webbuganalyse.utils;

import org.apache.commons.lang.StringUtils;

import static org.apache.hadoop.yarn.webapp.hamlet.HamletSpec.Media.print;

/**
 * @author hanyu
 * @ClassName EnglishOrCode
 * @date 2022/9/20 15:32
 * @description
 * @update [no][date YYYY-MM-DD][name][description]
 **/
public class EnglishOrCode {
    public static Tokenizer tokenizer = null;

    public static void initializeTokenizer() {
        tokenizer = new Tokenizer();

        //key words
        String keyString = "abstract assert boolean break byte case catch "
                + "char class const continue default do double else enum"
                + " extends false final finally float for goto if implements "
                + "import instanceof int interface long native new null "
                + "package private protected public return short static "
                + "strictfp super switch synchronized this throw throws true "
                + "transient try void volatile while todo org apache";
        String[] keys = keyString.split(" ");
        String keyStr = StringUtils.join(keys, "|");

        tokenizer.add(keyStr, 1);
        tokenizer.add("\\(|\\)|\\{|\\}|\\[|\\]|;|,|\\.|=|>|<|!|~|"
                        + "\\?|:|==|<=|>=|!=|&&|\\|\\||\\+\\+|--|"
                        + "\\+|-|\\*|/|&|\\||\\^|%|\'|\"|\n|\r|\\$|\\#",
                2);//separators, operators, etc

        tokenizer.add("[0-9]+", 3); //number
        tokenizer.add("[a-zA-Z][a-zA-Z0-9_]*", 4);//identifier
        tokenizer.add("@", 4);
    }

//    public static void main(String[] args) throws SQLException, ClassNotFoundException, IOException {
//        initializeTokenizer();
//        String s = "https://blog.csdn.net/weixin_29213655/article/details/114211412packagecom.isoftstone.ifa.web.base.filter;importjava.util.regex.Pattern;importjavax.servlet.http.HttpServletRequest;importjavax.servlet.http.HttpServletRequestWrapper;importorg.apache.commons.lang.StringUtils;importorg.slf4j.Logger;importorg.slf4j.LoggerFactory;public class XssHttpServletRequestWrapper extendsHttpServletRequestWrapper {private static final Logger logger =LoggerFactory.getLogger(XssHttpServletRequestWrapper.class);publicXssHttpServletRequestWrapper(HttpServletRequest request) {super(request);}/*** 对数组参数进行特殊字符过滤*/@OverridepublicString[] getParameterValues(String name) {String[] values= super.getParameterValues(name);if (values != null) {int length =values.length;String[] escapseValues= newString[length];for (int i = 0; i < length; i++) {escapseValues[i]=escapeHtml4(values[i]);}returnescapseValues;}return super.getParameterValues(name);}\n";
//        if (IsStringOrCode(s)) {
//            System.out.println("English");
//        } else {
//            System.out.println("Java Code");
//            System.out.println(IsStringOrCode(s));
//        }
//
//    }


    public static boolean IsJavaCode(String replaced) {
        initializeTokenizer();
        tokenizer.tokenize(replaced);
        String patternString = tokenizer.getTokensString();

//        if (patternString.matches(".*444.*") || patternString.matches("4+")) {
        if (patternString.contains("222222")) {
            return true;
        } else {
            return false;
        }
    }
}
