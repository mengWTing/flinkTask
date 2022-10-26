package cn.ffcs.is.mss.analyzer.utils.druid.entity;

import java.io.Serializable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @Auther chenwei
 * @Description
 * @Date: Created in 2017/11/20 14:03
 * @Modified By
 */
public enum Having implements Serializable {

    //大于
    greaterThan,
    //等于
    equalTo,
    //小于
    lessThan,
    //聚合列等于
    dimSelector,
    //且
    and,
    //或
    or,
    //否
    not;

    /**
     * @Auther chenwei
     * @Description 构建hiving语句greaterThan、equalTo、lessThan
     * @Date: Created in 2017/11/21 18:08
     * @param having
     * @param aggregation
     * @param value
     * @return
     */
    public static JSONObject getHaving(Having having,Aggregation aggregation,double value){

        JSONObject jsonObject = new JSONObject();
        try {

            switch (having){

                case greaterThan: case equalTo: case lessThan:{
                    jsonObject.put("type",having.toString());
                    jsonObject.put("aggregation",aggregation.toString());
                    jsonObject.put("value",value);
                    break;
                }

                default:{
                    break;
                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;

    }

    /**
     * @Auther chenwei
     * @Description 构建hiving语句dimSelector
     * @Date: Created in 2017/11/21 18:09
     * @param having
     * @param dimension
     * @param value
     * @return
     */
    public static JSONObject getHaving(Having having,Dimension dimension,double value){
        JSONObject jsonObject = new JSONObject();
        try {

            switch (having){

                case dimSelector:{
                    jsonObject.put("type",having.toString());
                    jsonObject.put("dimension",dimension.toString());
                    jsonObject.put("value",value);
                    break;
                }

                default:{
                    break;
                }

            }


        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;

    }

    /**
     * @Auther chenwei
     * @Description 构建hiving逻辑语句and、or
     * @Date: Created in 2017/11/21 18:10
     * @param having
     * @param jsonArray
     * @return
     */
    public static JSONObject getHaving(Having having,JSONArray jsonArray){
        JSONObject jsonObject = new JSONObject();

        try {

            switch (having){

                case and: case or: {
                    jsonObject.put("type", having.toString());
                    jsonObject.put("havingSpecs", jsonArray);
                    break;
                }

                default:{

                    break;
                }

            }


        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;

    }

    /**
     * @Auther chenwei
     * @Description 构建hiving逻辑语句not
     * @Date: Created in 2017/11/21 18:10
     * @param having
     * @param jsonObjects
     * @return
     */
    public static JSONObject getHaving(Having having,JSONObject ... jsonObjects) {

        JSONObject havingJsonObject = new JSONObject();
        if (jsonObjects.length > 0) {

            try {
                havingJsonObject.put("type", having.toString());
                switch (having) {

                    case not: {

                        havingJsonObject.put("havingSpecs", jsonObjects[0]);
                        break;
                    }

                    case and: case or: {
                        JSONArray jsonArray = new JSONArray();
                        for (JSONObject jsonObject : jsonObjects) {
                            jsonArray.put(jsonObject);
                        }
                        havingJsonObject.put("havingSpecs", jsonArray);
                        break;
                    }
                    default: {
                        break;
                    }


                }

            } catch(JSONException e){
                e.printStackTrace();
            }

        }
        return havingJsonObject;
    }

}
