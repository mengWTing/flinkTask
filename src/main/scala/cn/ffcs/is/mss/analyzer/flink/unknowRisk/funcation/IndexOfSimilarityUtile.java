package cn.ffcs.is.mss.analyzer.flink.unknowRisk.funcation;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author hanyu
 * @ClassName IndexOfSimilarityUtile
 * @date 2021/11/18 17:37
 * @description
 * @update [no][date YYYY-MM-DD][name][description]
 **/
public class IndexOfSimilarityUtile {
    static Map<Character, int[]> vectorMap = new HashMap<Character, int[]>();
    static int[] tempArray = null;
    static IndexOfSimilarityUtile IndexOfSimilarity = new IndexOfSimilarityUtile();

    public static IndexOfSimilarityUtile IndexOfSimilarity(String string1, String string2) {

            for (Character character1 : string1.toCharArray()) {
                if (vectorMap.containsKey(character1)) {
                    vectorMap.get(character1)[0]++;
                } else {
                    tempArray = new int[2];
                    tempArray[0] = 1;
                    tempArray[1] = 0;
                    vectorMap.put(character1, tempArray);
                }

            }

            for (Character character2 : string2.toCharArray()) {
                if (vectorMap.containsKey(character2)) {
                    vectorMap.get(character2)[1]++;
                } else {
                    tempArray = new int[2];
                    tempArray[0] = 0;
                    tempArray[1] = 1;
                    vectorMap.put(character2, tempArray);
                }
            }

            return IndexOfSimilarity;


    }

    public double sim() {
        double result = 0;
        result = pointMulti(vectorMap) / sqrtMulti(vectorMap);
        return result;
    }

    private double sqrtMulti(Map<Character, int[]> paramMap) {
        double result = 0;
        result = squares(paramMap);
        result = Math.sqrt(result);
        return result;
    }


    private double squares(Map<Character, int[]> paramMap) {
        double result1 = 0;
        double result2 = 0;
        Set<Character> keySet = paramMap.keySet();
        for (Character character : keySet) {
            int[] temp = paramMap.get(character);
            result1 += (temp[0] * temp[0]);
            result2 += (temp[1] * temp[1]);
        }
        return result1 * result2;
    }

    private double pointMulti(Map<Character, int[]> paramMap) {
        double result = 0;
        Set<Character> keySet = paramMap.keySet();
        for (Character character : keySet) {
            int[] temp = paramMap.get(character);
            result += (temp[0] * temp[1]);
        }
        return result;
    }
}
