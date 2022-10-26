package cn.ffcs.is.mss.analyzer.utils.druid.entity;

import java.io.Serializable;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @Auther chenwei
 * @Description
 * @Date: Created in 2017/10/30 下午5:27
 * @Modified By
 */
public enum  Filter implements Serializable {

    //选择
    selector,
    //正则
    regex,
    //且
    and,
    //或
    or,
    //否
    not;

    @Override
    public String toString() {
        return super.toString();
    }


    /**
     * @Auther chenwei
     * @Description 构建选择过滤器|正则过滤器
     * @Date: Created in 2017/10/30 下午5:40
     * @param filter
     * @param dimension
     * @param value
     * @return
     */
    public static JSONObject getFilter (Filter filter,Dimension dimension,Object value) {


        JSONObject filterJsonObject = new JSONObject();

        try {

            filterJsonObject.put("type", filter.toString());
            filterJsonObject.put("dimension", dimension.toString());

            switch (filter) {
                case selector: {
                    filterJsonObject.put("value", value);
                    break;
                }

                case regex: {

                    filterJsonObject.put("pattern", value.toString());
                    break;
                }

                default: {
                    break;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return filterJsonObject;
    }


    /**
     * @Auther chenwei
     * @Description 构建逻辑选择器and|or|not
     * @Date: Created in 2017/10/30 下午5:43
     * @param filter
     * @param jsonObjects
     * @return
     */
    public static JSONObject getFilter(Filter filter,JSONObject ... jsonObjects){

        JSONObject filterJsonObject = new JSONObject();


        try {
            filterJsonObject.put("type",filter.toString());

            switch (filter) {

                case and: case or: {

                    JSONArray jsonArray = new JSONArray();
                    for (JSONObject jsonObject : jsonObjects){
                        jsonArray.put(jsonObject);
                    }

                    filterJsonObject.put("fields", jsonArray);

                    break;
                }
                case not: {

                    filterJsonObject.put("field", jsonObjects[0]);
                    break;

                }

                default: {
                    break;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }


        return filterJsonObject;

    }

    /**
     * @Auther chenwei
     * @Description 构建逻辑选择器and|or
     * @Date: Created in 2017/10/30 下午5:43
     * @param filter
     * @param dimension
     * @param set
     * @return
     */
    public static JSONObject getFilters(Filter filter,Dimension dimension,Set<Object> set, Filter valueFilter){


        try {

            switch (filter) {

                case and:
                case or: {
                    JSONArray jsonArray = new JSONArray();
                    for (Object object: set){
                        jsonArray.put(getFilter(valueFilter,dimension,object));
                    }

                    return getFilter(filter,jsonArray);
                }

                default: {

                    break;
                }

            }
        }catch (Exception e){
            e.printStackTrace();
        }


        return null;

    }

    /**
     * @Auther chenwei
     * @Description 构建逻辑选择器and|or
     * @Date: Created in 2017/10/30 下午5:43
     * @param filter
     * @param jsonArray
     * @return
     */
    public static JSONObject getFilter(Filter filter,JSONArray jsonArray){

        JSONObject filterJsonObject = new JSONObject();

        try {
            filterJsonObject.put("type",filter.toString());

            switch (filter) {

                case and: case or: {

                    filterJsonObject.put("fields", jsonArray);

                    break;
                }

                default: {
                    break;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }


        return filterJsonObject;

    }


}
