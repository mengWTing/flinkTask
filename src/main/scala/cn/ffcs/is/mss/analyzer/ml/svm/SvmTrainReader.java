package cn.ffcs.is.mss.analyzer.ml.svm;

import java.io.BufferedReader;
import java.util.ArrayList;

/**
 * @author hanyu
 * @title SvmTrainReader
 * @date 2020-10-14 14:33
 * @description 读取样本数据，转化成计算矩阵
 * @update [no][date YYYY-MM-DD][name][description]
 */
public class SvmTrainReader {
    //String strs[]  为样本数据特征转化后的特征数据
    //lines 为样本数据个数
    //2: 4.0 0.0 2.0 4.0 0.0 0.0 0.0 0.0 0.0
    // private SvmDataLine getSVMDataLine(String strs[] ) {
    private SvmDataLine getSVMDataLine(String[] strs) {
        String[] s1;
        ArrayList<Double> curX = new ArrayList<Double>();
        int y = 0;
        try {
            //strs = br.readLine().split(" ");
            y = (int) Double.parseDouble(strs[0]);
            //s1 = strs[1].split(" ");
            for (int i = 1; i < strs.length; i++)
                curX.add(Double.parseDouble(strs[i]));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return new SvmDataLine(curX, y);
    }

    //public SvmData getSVMData(int lines, String strs[] ) {
    public SvmTrainData getSVMData(String[] strs) {
        SvmTrainData svmTrainData = SvmTrainData.getInstance();
        SvmDataLine svmDataLine = getSVMDataLine(strs);
        svmTrainData.addVector(svmDataLine.x, svmDataLine.y);
        return svmTrainData;


    }

//    public static void main(String[] args) {
//        SvmFileReader svmFileReader = new SvmFileReader();
//        String string= "1 1 2 5 6";
//        String[] s = string.split(" ", -1);
//        SvmData svmData = svmFileReader.getSVMData(1, s);
//        System.out.println(svmData);
//    }


}

