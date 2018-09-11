package com.ylli.api.base.mybatis;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

/**
 * Created by ylli on 2017/4/19.
 */
@MappedTypes(value = {List.class})
public class ListTypeHandler extends BaseTypeHandler<List> {

    private GsonBuilder gsonBuilder;

    public ListTypeHandler() {
        super();
        gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(new TypeToken<List>(){}.getType(),
                new DeserializerDoubleAsIntFix());
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int index,
                                    List parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(index, new Gson().toJson(parameter));
    }

    @Override
    public List getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return getList(rs.getString(columnName));
    }

    @Override
    public List getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return getList(rs.getString(columnIndex));
    }

    @Override
    public List getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return getList(cs.getString(columnIndex));
    }

    private List getList(String value) {
        Gson gson = gsonBuilder.create();
        return gson.fromJson(value, List.class);
    }

    public static class DeserializerDoubleAsIntFix implements JsonDeserializer<List> {

        @Override
        @SuppressWarnings("unchecked")
        public List deserialize(JsonElement json, Type typeOfT,
                                JsonDeserializationContext context) throws JsonParseException {
            return (List) read(json);
        }

        private Object read(JsonElement in) {
            if (in.isJsonArray()) {
                List<Object> list = new ArrayList<>();
                JsonArray arr = in.getAsJsonArray();
                for (JsonElement anArr : arr) {
                    list.add(read(anArr));
                }
                return list;
            } else if (in.isJsonPrimitive()) {
                JsonPrimitive prim = in.getAsJsonPrimitive();
                if (prim.isBoolean()) {
                    return prim.getAsBoolean();
                } else if (prim.isString()) {
                    return prim.getAsString();
                } else if (prim.isNumber()) {
                    Number num = prim.getAsNumber();
                    // here you can handle double int/long values
                    // and return any type you want
                    // this solution will transform 3.0 float to long values
                    if (Math.ceil(num.doubleValue()) == num.longValue()) {
                        return num.longValue();
                    } else {
                        return num.doubleValue();
                    }
                }
            }
            return null;
        }
    }
}
