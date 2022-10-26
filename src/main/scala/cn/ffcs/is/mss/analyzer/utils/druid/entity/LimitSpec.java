package cn.ffcs.is.mss.analyzer.utils.druid.entity;

import java.io.Serializable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @Auther chenwei
 * @Description
 * @Date: Created in 2017/12/12 19:31
 * @Modified By
 */
public class LimitSpec implements Serializable {


    /**
     * @Auther chenwei
     * @Description 获取topN的json串
     * @Date: Created in 2018/1/8 14:17
     * @param dimension
     * @param direction
     * @param dimensionOrder
     * @param limit
     * @return
     */
    public static JSONObject getLimitSpec(Dimension dimension,Direction direction,DimensionOrder dimensionOrder,int limit){


        return getLimitSpec(dimension.toString(),direction,dimensionOrder,limit);

    }

    /**
     * @Auther chenwei
     * @Description 获取topN的json串
     * @Date: Created in 2018/1/8 14:17
     * @param aggregation
     * @param direction
     * @param dimensionOrder
     * @param limit
     * @return
     */
    public static JSONObject getLimitSpec(Aggregation aggregation,Direction direction,DimensionOrder dimensionOrder,int limit){

        return getLimitSpec(aggregation.toString(),direction,dimensionOrder,limit);

    }


    /**
     * @Auther chenwei
     * @Description 获取topN的json串
     * @Date: Created in 2018/1/8 14:13
     * @param dimension
     * @param direction
     * @param dimensionOrder
     * @param limit
     * @return
     */
    private static JSONObject getLimitSpec(String dimension,Direction direction,DimensionOrder dimensionOrder,int limit){

        JSONObject limitSpecJSONObject = new JSONObject();

        try {
            JSONObject columnsJsonObject = new JSONObject();

            //设置排序列
            columnsJsonObject.put("dimension",dimension);
            //设置正序还是逆序
            columnsJsonObject.put("direction",direction.toString());
            //设置排序依据
            columnsJsonObject.put("dimensionOrder",dimensionOrder.toString());

            JSONArray jsonArray = new JSONArray();
            jsonArray.put(columnsJsonObject);


            limitSpecJSONObject.put("columns",jsonArray);
            limitSpecJSONObject.put("type","default");
            //设置TopN个数
            limitSpecJSONObject.put("limit",limit);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return limitSpecJSONObject;

    }


    /**
     * @Auther chenwei
     * @Description 正序还是逆序
     * @Date: Created in 2018/1/8 14:12
     */
    public enum Direction{
        //递增
        ascending,
        //递减
        descending
    }




    /**
     * @Auther chenwei
     * @Description 排序依据
     * @Date: Created in 2018/1/8 14:12
     */
    public enum DimensionOrder{

        //字典顺序
        lexicographic,
        //包括文字与数字的的顺序
        alphanumeric,
        //字符串长度
        strlen,
        //数字
        numeric
    }

}
