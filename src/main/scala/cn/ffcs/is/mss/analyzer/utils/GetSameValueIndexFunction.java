package cn.ffcs.is.mss.analyzer.utils;

import java.util.*;

/**
 * @author hanyu
 * @ClassName GetSameValueIndexFunction
 * @date 2023/2/6 11:49
 * @description
 * @update [no][date YYYY-MM-DD][name][description]
 **/
public class GetSameValueIndexFunction {
    public static void main(String[] args) {
        Map<String, Integer> map = new HashMap<String, Integer>();
        String[] array = new String[]{"ff", "BB", "cc", "dd", "AA", "BB", "cc", "dd", "AA", "BB", "AA"};

        for (String str : array) {
            Integer num = map.get(str);
            num = null == num ? 1 : num + 1;
            map.put(str, num);
        }

        if (array.length != map.size()) {
            System.out.println("存在相同的元素及重复个数!");
        }

        Set set = map.entrySet();
        Iterator it = set.iterator();
        List<String> sList = new ArrayList<String>();
        while (it.hasNext()) {
            Map.Entry<String, Integer> entry = (Map.Entry<String, Integer>) it.next();
            String key = entry.getKey();
            Integer value = entry.getValue();
//            System.out.println("String :" + key + " num :" + value);
            if (value > 1)
                sList.add(key);
        }

//        System.out.println("============相同元素的下标======================");
        List<ArrayList<Integer>> indexArr = new ArrayList<ArrayList<Integer>>();
        for (String s : sList) {
            ArrayList<Integer> aIntegers = new ArrayList<Integer>();
            for (int i = 0; i < array.length; i++) {
                if (array[i].equals(s)) {
                    aIntegers.add(i);
                }
            }
            if (aIntegers.size() > 0)
                indexArr.add(aIntegers);
        }
        System.out.println(indexArr);
    }

}
