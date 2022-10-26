package cn.ffcs.is.mss.analyzer.ml.pca;


import Jama.EigenvalueDecomposition;
import Jama.Matrix;
import cn.ffcs.is.mss.analyzer.ml.utils.MinHeap;

/**
 * @Auther chenwei
 * @Description PCA主成分分析
 * @Date: Created in 2018/3/9 14:48
 * @Modified By
 */
public class PCA {


    /**
     * @Auther chenwei
     * @Description pac降维算法
     * @Date: Created in 2018/3/12 14:07
     * @param date
     * @param k
     * @return
     */
    public static double[][] pac(double[][] date,int k){
        double[] average = getAverage(date);
        date = PCA.dataSubAverage(date,average);

        double[][] c = new double[average.length][average.length];

        for (int i = 0; i < average.length; i++){
            for (int j = 0; j < average.length; j++){
                c[i][j] = PCA.cov(date,i,j);
            }
        }



        double[][] eigenVectors = PCA.eig(c,k);

        return PCA.getFinalData(date,eigenVectors);
    }

    /**
     * @Auther chenwei
     * @Description 求每一列数据的平均值
     * @Date: Created in 2018/3/9 14:55
     * @param date
     * @return
     */
    private static double[] getAverage(double[][] date){
        if (date.length > 0) {
            double[] average = new double[date[0].length];
            for (int j = 0; j < date[0].length; j++) {
                double sum = 0;
                for (int i = 0; i < date.length; i++) {
                    sum += date[i][j];
                }
                average[j] = sum / date.length;
            }
            return average;
        }else {
            return null;
        }

    }


    /**
     * @Auther chenwei
     * @Description 对于所有的值减去
     * @Date: Created in 2018/3/9 15:06
     * @param date
     * @param average
     * @return
     */
    private static double[][] dataSubAverage(double[][] date,double[] average){

        for (int i = 0; i < date.length; i++){
            for (int j = 0; j < average.length; j++){
                date[i][j] = date[i][j] - average[j];
            }
        }

        return date;
    }


    /**
     * @Auther chenwei
     * @Description 计算协方差,因为之前已经减过平均值所以不再减
     * @Date: Created in 2018/3/9 15:53
     * @param date
     * @param x
     * @param y
     * @return
     */
    private static double cov(double[][] date,int x,int y){

        double cov = 0.0;
        for (int i = 0; i < date.length;i++){
            cov += date[i][x] * date[i][y];
        }
        return cov / (date.length - 1);

    }

    /**
     * @Auther chenwei
     * @Description 获取协方差矩阵的特征值前k个的特征向量
     * @Date: Created in 2018/3/9 16:29
     * @param cov
     * @return
     */
    private static double[][] eig(double[][] cov,int k){

        int length = cov.length;
        double [][] eigenvalueVectors = new double[length][k];

        Matrix matrix = new Matrix(cov);

        EigenvalueDecomposition eigenvalueDecomposition = matrix.eig();

        Matrix matrixD = eigenvalueDecomposition.getD();
        Matrix matrixV = eigenvalueDecomposition.getV();


        Object[] array = new Object[length];

        for (int i = 0; i < length; i++){
            array[i] = matrixD.get(i,i);
        }

        MinHeap minHeap = new MinHeap(array,k,Double.class);
        double min = (double)minHeap.getMin();

        int index = 0;
        for (int i = 0; i < length; i++){
            double temp = matrixD.get(i,i);
            if (temp >= min) {
                for (int j = 0; j < length; j++) {
                    eigenvalueVectors[j][index] = matrixV.get(j,i);
                }
                index++;
            }
        }

        return eigenvalueVectors;

    }

    /**
     * @Auther chenwei
     * @Description 获取降维之后的数组
     * @Date: Created in 2018/3/12 14:05
     * @param date
     * @param eigenVectors
     * @return
     */
    private static double[][] getFinalData(double[][] date, double[][] eigenVectors){
        Matrix matrixDate = new Matrix(date);
        Matrix matrixEigenVectors = new Matrix(eigenVectors);

        return matrixDate.times(matrixEigenVectors).getArrayCopy();

    }


}
