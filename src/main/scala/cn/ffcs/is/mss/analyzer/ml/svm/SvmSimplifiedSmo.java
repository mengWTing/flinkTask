package cn.ffcs.is.mss.analyzer.ml.svm;

import java.io.Serializable;
import java.util.*;

/**
 * @author hanyu
 * @title SvmSimplifiedSmo
 * @date 2020-10-12 14:25
 * @description
 * @update [no][date YYYY-MM-DD][name][description]
 */
public class SvmSimplifiedSmo implements Serializable {
    //boundAlpha： 表示x点处于边界上所对应的拉格朗日乘子a的集合
    private HashSet<Integer> boundAlpha = new HashSet<Integer>();
    private Random random = new Random();

    //private SvmData svmData;

    //拉格朗日乘子 a b
    double[][][] a;
    double[][] b;

    //核函数
    double[][] kernel;

    //x[][] 是一个二维向量，每一行记录每一行文本的特征向量权值得矩阵
    private double[][] x;

    //y[] 表示该文本类别
    int[] y;

    //初始化 调用 svmData
//    public SvmSimplifiedSmo() {
//        SvmTrainData svmData = SvmTrainData.getInstance();
//
//    }

    // 其他样本特征值
    // length ：总特征数
    // n: 样本个数
    int length;
    int n;

    /**
     * @return 返回训练完成的拉格朗日乘子a和对应的b
     * @author hanyu
     * @date 2020/10/20 14:41
     * @description svm 训练逻辑
     * traitSum-----length：特征个数
     * sampleNum-----n：样本个数
     * c: 对不在界内的惩罚因子
     * tol：容忍极限值
     * maxPasses： 表示没有改变拉格朗日乘子得最多迭代次数
     * @update [no][date YYYY-MM-DD][name][description]
     */
    public SvmModel train(SvmTrainData svmTrainData, int traitNum, int sampleNum, double C, double tol, int maxPasses) {

        //当前先根据输入文件手动调整特征值个数
        length = traitNum;

        double[][][] a = new double[svmTrainData.getFeatureVectors().size() + 1][svmTrainData.getFeatureVectors().size() + 1][sampleNum];//拉格朗日乘子
        this.a = a;
        double[][] b3 = new double[svmTrainData.getFeatureVectors().size() + 1][svmTrainData.getFeatureVectors().size() + 1];
        this.b = b3;
        /**
         * svm分类器
         */
        for (int id1 = 1; id1 <= svmTrainData.getFeatureVectors().size(); id1++)
            for (int id2 = id1 + 1; id2 <= svmTrainData.getFeatureVectors().size(); id2++) {
                /**
                 * 获取进行训练的2类所含的训练文本向量
                 */
                Set<Collection<Double>> vectors1 = svmTrainData.get(id1);
                Set<Collection<Double>> vectors2 = svmTrainData.get(id2);
                n = 0;
                double[][] x2 = new double[vectors1.size() + vectors2.size()][length];
                int[] y2 = new int[vectors1.size() + vectors2.size()];

                this.y = y2;
                this.x = x2;
                /**
                 * 这里是转换成数组来运算
                 */

                for (Collection<Double> v : vectors1) {
                    int j = 0;
                    Iterator<Double> it = v.iterator();
                    while (it.hasNext()) {
                        double p = it.next();
                        x[n][j++] = p;
                    }
                    y[n++] = 1;
                }

                for (Collection<Double> v : vectors2) {
                    int j = 0;
                    Iterator<Double> it = v.iterator();
                    while (it.hasNext()) {
                        double p = it.next();
                        x[n][j++] = p;
                    }
                    y[n++] = -1;
                }

                kernel = new double[n][n];
                initiateKernel(n);

                this.boundAlpha.clear();
                int noError = 0;
                /**
                 * 将乘子初始化为0
                 */
                for (int i = 0; i < n; i++) {
                    a[id1][id2][i] = 0;
                }
                int passes = 0;
                while (passes < maxPasses) {
                    /**
                     * num_changed_alphas 表示改变乘子的次数（基本上是成对改变的）
                     */
                    noError++;
                    if (noError > 100 * n) break;
                    int num_changed_alphas = 0;
                    for (int i = 0; i < n; i++) {
                        double Ei = getOffSet(i, id1, id2);
                        /**
                         * 把违背KKT条件的ai作为第一个
                         * 满足KKT条件的情况是：
                         * yi*f(i) >= 1 and alpha == 0 (正确分类)
                         * yi*f(i) == 1 and 0<alpha < C (在边界上的支持向量)
                         * yi*f(i) <= 1 and alpha == C (在边界之间)
                         *
                         * ri = y[i] * Ei = y[i] * f(i) - y[i]^2 >= 0
                         * 如果ri < 0并且alpha < C 则违反了KKT条件
                         * 因为原本ri < 0 应该对应的是alpha = C
                         * 同理，ri > 0并且alpha > 0则违反了KKT条件
                         * 因为原本ri > 0对应的应该是alpha =0
                         */
                         if ((y[i] * Ei < -tol && a[id1][id2][i] < C) ||
                                (y[i] * Ei > tol && a[id1][id2][i] > 0)) {
                            /**
                             * ui*yi=1边界上的点 0 < a[i] < C
                             * 找MAX|E1 - E2|
                             */
                            int j;

                            if (this.boundAlpha.size() > 0) {
                                j = findMax(Ei, this.boundAlpha, id1, id2);
                            } else
                            /**
                             * 如果边界上没有，就随便选一个j != i的aj
                             */
                                j = RandomSelect(i);

                            double Ej = getOffSet(j, id1, id2);

                            /**
                             * 保存当前的ai和aj
                             */
                            double oldAi = a[id1][id2][i];
                            double oldAj = a[id1][id2][j];

                            /**
                             * 计算乘子的范围U, V
                             */
                            double L, H;
                            if (y[i] != y[j]) {
                                L = Math.max(0, a[id1][id2][j] - a[id1][id2][i]);
                                H = Math.min(C, C - a[id1][id2][i] + a[id1][id2][j]);
                            } else {
                                L = Math.max(0, a[id1][id2][i] + a[id1][id2][j] - C);
                                H = Math.min(0, a[id1][id2][i] + a[id1][id2][j]);
                            }

                            /**
                             * 如果eta等于0或者大于0 则表明a最优值应该在L或者U上
                             */
                            double eta = 2 * kernelFunction(i, j) - kernelFunction(i, i) - kernelFunction(j, j);

                            if (eta >= 0)
                                continue;

                            a[id1][id2][j] = a[id1][id2][j] - y[j] * (Ei - Ej) / eta;
                            if (0 < a[id1][id2][j] && a[id1][id2][j] < C)
                                this.boundAlpha.add(j);

                            if (a[id1][id2][j] < L)
                                a[id1][id2][j] = L;
                            else if (a[id1][id2][j] > H)
                                a[id1][id2][j] = H;

                            if (Math.abs(a[id1][id2][j] - oldAj) < 1e-5)
                                continue;
                            a[id1][id2][i] = a[id1][id2][i] + y[i] * y[j] * (oldAj - a[id1][id2][j]);
                            if (0 < a[id1][id2][i] && a[id1][id2][i] < C)
                                this.boundAlpha.add(i);

                            /**
                             * 计算b1， b2
                             */
                            double b1 = b[id1][id2] - Ei - y[i] * (a[id1][id2][i] - oldAi) * kernelFunction(i, i) - y[j] * (a[id1][id2][j] - oldAj) * kernelFunction(i, j);
                            double b2 = b[id1][id2] - Ej - y[i] * (a[id1][id2][i] - oldAi) * kernelFunction(i, j) - y[j] * (a[id1][id2][j] - oldAj) * kernelFunction(j, j);

                            if (0 < a[id1][id2][i] && a[id1][id2][i] < C)
                                b[id1][id2] = b1;
                            else if (0 < a[id1][id2][j] && a[id1][id2][j] < C)
                                b[id1][id2] = b2;
                            else
                                b[id1][id2] = (b1 + b2) / 2;

                            num_changed_alphas = num_changed_alphas + 1;
                        }
                    }
                    if (num_changed_alphas == 0) {
                        passes++;
                    } else
                        passes = 0;
                }
            }
        /**
         * 返回训练完成的拉格朗日乘子a和对应的b
         */
        return new SvmModel(a, b);
    }


    /**
     * @return double 小于0属于隐秘隧道   大于0属于非隐秘隧道
     * @author hanyu
     * @date 2020/10/20 10:01
     * @description model 训练模型
     * x2    测试文本的总特征值矩阵
     * y2    文本各个的所属类别
     * @update [no][date YYYY-MM-DD][name][description]
     */

    public double[] predict(SvmModel model, double[][] x2, int[] y2, SvmTrainData trainData) {
        /**
         *
         *sum 二类判断其属于哪一类时的指标
         *correctCnt 正确的样本数
         *total 总样本数
         *p 当前文本最有可能的所属类
         *q 多类判断时的队首
         */


        double probability = 0;
        double sum = 0;
        int correctCnt = 0;
        int total = y2.length;
        int p, q;
        for (int i = 0; i < total; i++) {
            p = 1;
            q = 2;
            while (q <= trainData.getFeatureVectors().size()) {
                /**
                 * 获取p,q类的文本
                 * 转成数组后来比较
                 */
                Set<Collection<Double>> vectors1 = trainData.get(p);
                Set<Collection<Double>> vectors2 = trainData.get(q);
                n = 0;
                double[][] x3 = new double[vectors1.size() + vectors2.size()][length];
                int[] y3 = new int[vectors1.size() + vectors2.size()];
                this.y = y3;
                this.x = x3;

                for (Collection<Double> v : vectors1) {
                    int j = 0;
                    Iterator<Double> it = v.iterator();
                    while (it.hasNext()) x[n][j++] = it.next();
                    y[n++] = 1;
                }
                for (Collection<Double> v : vectors2) {
                    int j = 0;
                    Iterator<Double> it = v.iterator();
                    while (it.hasNext()) x[n][j++] = it.next();
                    y[n++] = -1;
                }
                sum = 0;
                for (int j = 0; j < n; j++) {
                    sum += y[j] * model.a[p][q][j] * kernaltest(j, x2[i]);
                }
                sum += model.b[p][q];

                /**
                 * sum大于0则属于p类，小于0则属于q类
                 */
                if (sum < 0) {
                    p = q;
                    q++;
                } else q++;
            }
            if (p == y2[i])
                correctCnt++;
//            System.out.println(p + " pk " + y2[i] + " " + sum);

        }
        probability = (double) correctCnt / (double) total;

        return new double[]{probability, sum};
    }

    /**
     * @return 核函数计算后的值
     * @author hanyu
     * @date 2020/10/23 10:12
     * @description 测试专用核函数计算
     * @update [no][date YYYY-MM-DD][name][description]
     */

    private double kernaltest(int i, double x2[]) {
        double sum = 0.0;
        for (int t = 0; t < length; t++) {
            sum += x[i][t] * x2[t];
        }
        return sum;
    }


    /**
     * @return
     * @author hanyu
     * @date 2020/10/23 10:07
     * @description 选择不等于i的j
     * @update [no][date YYYY-MM-DD][name][description]
     */
    private int RandomSelect(int i) {
        int j;
        do {
            j = random.nextInt(n);
        } while (i == j);
        return j;
    }

    /**
     * @return 能得到较优解时的另一个拉格朗日乘子位置
     * @author hanyu
     * @date 2020/10/23 10:58
     * @description svm算法需要——最大化(Ei - Ej)
     * Ei          其中一个拉格朗日乘子所计算出的E值
     * boundAlpha2 表示x点处于边界上所对应的拉格朗日乘子a的集合
     * classone         表示当前训练的类编号
     * classtwo         表示当前训练的类编号
     * @update [no][date YYYY-MM-DD][name][description]
     */
    private int findMax(double Ei, HashSet<Integer> boundAlpha2, int classOne, int classTwo) {
        double max = 0;
        int maxIndex = -1;
        for (Iterator<Integer> iterator = boundAlpha2.iterator(); iterator.hasNext(); ) {
            Integer j = (Integer) iterator.next();
            double Ej = getOffSet(j, classOne, classTwo);
            if (Math.abs(Ei - Ej) > max) {
                max = Math.abs(Ei - Ej);
                maxIndex = j;
            }
        }
        return maxIndex;
    }

    /**
     * @return 偏移量
     * @author hanyu
     * @date 2020/10/23 10:05
     * @description 计算偏移量  第i个拉格朗日乘子，每个文本各有一个
     * @update [no][date YYYY-MM-DD][name][description]
     */
    private double getOffSet(int i, int classOne, int classTwo) {
        return lagrangeFunction(i, classOne, classTwo) - y[i];
    }

    /**
     * @return 函数值
     * @author platinaboy
     * @date 2020/10/23 10:08
     * @description 计算当前拉格朗日乘子时的函数值
     * @update [no][date YYYY-MM-DD][name][description]
     */
    private double lagrangeFunction(int j, int classOne, int classTwo) {
        double sum = 0;
        for (int i = 0; i < n; i++) {
            sum += a[classOne][classTwo][i] * y[i] * kernel[i][j];

        }
        return sum + b[classOne][classTwo];
    }


    /**
     * @return
     * @author hanyu
     * @date 2020/10/23 10:10
     * @description 初始化kernel 函数
     * @update [no][date YYYY-MM-DD][name][description]
     */
    private void initiateKernel(int length) {
        for (int i = 0; i < length; i++) {
            for (int j = 0; j < length; j++) {
                kernel[i][j] = kernelFunction(i, j);
            }
        }


    }

    /**
     * @return
     * @author hanyu
     * @date 2020/10/23 10:21
     * @description 采用多项式核函数
     * kernel （i，j） = x T x
     * @update [no][date YYYY-MM-DD][name][description]
     */
    private double kernelFunction(int i, int j) {
        double sum = 0.0;
        for (int t = 0; t < x[i].length; t++) {
            sum += x[i][t] * x[j][t];

        }
        return sum;
    }


}
