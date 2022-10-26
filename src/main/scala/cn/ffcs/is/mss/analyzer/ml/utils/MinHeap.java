package cn.ffcs.is.mss.analyzer.ml.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @Auther chenwei
 * @Description 最小堆
 * @Date: Created in 2018/3/9 17:05
 * @Modified By
 */
public class MinHeap {

    private int currentSize;
    private int DEFAULT_CAPACITY;
    private Object[] array;
    private Class clazz;
    /**
     * @Auther chenwei
     * @Description 构建最小堆
     * @Date: Created in 2018/3/9 17:22
     * @param items
     * @param capacity
     */
    public MinHeap(Object[] items,int capacity,Class clazz) {
        this.clazz = clazz;
        //初始化最大容量
        DEFAULT_CAPACITY = capacity;
        //设置当前个数
        currentSize = Math.min(DEFAULT_CAPACITY,items.length);

        //构建最小堆
        array = new Comparable[currentSize + 1];
        for (int i = 0; i < currentSize; i++){
            array[i] = items[i];
        }


        for(int i = currentSize - 1; i >0; i--){
            percolateUp(i);
        }


        for (int i = currentSize; i < items.length; i++){
            insert(items[i]);
        }

    }


    /**
     * @Auther chenwei
     * @Description 获取最小堆的最小值
     * @Date: Created in 2018/3/9 17:26
     * @return
     */
    public Object getMin(){
        return array[0];
    }

    /**
     * @Auther chenwei
     * @Description 插入一个新值
     * @Date: Created in 2018/3/9 17:26
     */
    public void insert(Object value){

        Object min = getMin();
        int compare = compare(value,min);
        if (compare > 0){
            removeMin();
            currentSize++;
            array[currentSize - 1] = value;
            percolateUp(currentSize - 1);
        }

    }

    /**
     * @Auther chenwei
     * @Description 删除最小值
     * @Date: Created in 2018/3/9 17:26
     */
    public void removeMin(){
        array [0] = array[currentSize - 1];
        currentSize--;
        percolateDown(0);
    }

    /**
     * @Auther chenwei
     * @Description 下滤
     * @Date: Created in 2018/3/12 10:31
     * @param position
     */
    private void percolateDown(int position) {

        int leftIndex = 2 * (position + 1) - 1;
        int rightIndex = 2 * (position + 1);
        int compare;
        if (rightIndex < currentSize){
            compare = compare(array[leftIndex], array[rightIndex]);
        }else {
            if (leftIndex < currentSize){
                compare = -1;
            }else {
                return;
            }
        }

        int index;
        if (compare > 0){
            index = rightIndex;
        }else {
            index = leftIndex;
        }

        compare = compare(array[position], array[index]);
        if (compare > 0) {
            Object temp = array[position];
            array[position] = array[index];
            array[index] = temp;
            percolateDown(index);
        }
    }

    /**
     * @Auther chenwei
     * @Description 上浮
     * @Date: Created in 2018/3/12 10:37
     * @param position
     */
    private void percolateUp(int position){
        int parentIndex = (position - 1) / 2;
        int compare = compare(array[position],array[parentIndex]);
        if (compare < 0){
            Object temp = array[position];
            array[position] = array[parentIndex];
            array[parentIndex] = temp;
            percolateUp(parentIndex);
        }

    }

    /**
     * @Auther chenwei
     * @Description 该最小堆是否为空
     * @Date: Created in 2018/3/9 17:33
     * @return
     */
    public boolean isEmpty() {
        return currentSize == 0;
    }

    /**
     * @Auther chenwei
     * @Description 比较两个值的大小
     * object1 大于 object2返回1
     * object1 等于 object2返回0
     * object1 小于 object2返回-1
     * @Date: Created in 2018/3/9 18:30
     * @param object1
     * @param object2
     * @return
     */
    private int compare(Object object1, Object object2){

        try {
            Class clazzWarp = getBasicClass(clazz);
            Method method = clazz.getMethod("compare",clazzWarp,clazzWarp);
            Integer result = (Integer)method.invoke(clazz,object1,object2);
            return result;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        return 0;
    }


    /**
     * @Auther chenwei
     * @Description 根据包装类型获取基本类型
     * @Date: Created in 2018/3/9 18:29
     * @param clazz
     * @return
     */
    private static Class getBasicClass(Class clazz){
        return BasicClass.valueOf(clazz.getSimpleName()).clazz;
    }

    enum BasicClass{

        Integer(int.class),
        Double(double.class),
        Float(long.class),
        Long(short.class),
        Short(byte.class),
        Byte(boolean.class),
        Boolean(char.class),
        Character(float.class);
        private Class clazz;
        BasicClass(Class clazz){
            this.clazz = clazz;
        }

    }



}
