package com.soma.ggamtalk.api;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import com.soma.ggamtalk.utils.Config;
import com.soma.ggamtalk.utils.Util;

public class ChangeUserColorAPI extends BaseAPI{
	
	public ChangeUserColorAPI() { }
	
	
	@Override
	public void execute(Vertx vertx, HttpServerRequest request) {
		init(vertx, request);
		
		if(params.isEmpty() || checkValidation(params).getInteger("result_code")==-1){
			request.response().end(checkValidation(params).toString());
			return;
		}
		
		getRedis(this, Config.getSession, params.getString("token"));
		
	}


	@Override
	public void onExecute(int what, JsonObject resultJO) {
		
		if(resultJO.containsKey("result_code") && resultJO.getInteger("result_code")==-1){
			request.response().end(resultJO.toString());
			return;
		}
		JsonObject rs = new JsonObject();

		switch (what) {
		case Config.getSession:
			if(!resultJO.containsKey("result")||resultJO.getString("result")==null){
				rs.put("result_code", -1);
				rs.put("result_msg", "로그인이 필요합니다.");
				request.response().end(rs.toString());
				break;
			}
			String user_info[]= resultJO.getString("result").split(",");
			String user_id=user_info[0];
			
			params.put("user_id", user_id);
			
			String query = String.format("UPDATE app_user_list SET user_color='%s' WHERE user_id='%s' and app_id='%s'",
					params.getString("user_color"), params.getString("user_id"), params.getString("app_id"));
			insertCustomQuery(this, Config.setNickApp, query);

			break;
			
		case Config.setNickApp:
			String query2 = String.format("UPDATE channel_user_list SET user_color='%s' WHERE user_id='%s' and app_id='%s'",
					params.getString("user_color"), params.getString("user_id"), params.getString("app_id"));
			insertCustomQuery(this, 100, query2);
			
			rs.put("result_code", 0);
			rs.put("result_msg", "사용자 색상을 변경하였습니다.");
			request.response().end(rs.toString());
			
			getRedis(this, Config.getRedis, "app:" + params.getString("user_id"));
			break;
		
		case Config.getRedis:
			JsonArray ja = new JsonArray(resultJO.getString("result"));
			for(int i=0; i<ja.size();i++){
				if(params.getString("app_id").equals(ja.getJsonObject(i).getString("app_id"))){
					JsonObject jo = ja.getJsonObject(i);
					jo.put("user_color", params.getString("user_color"));
					ja.remove(i);
					ja.add(jo);
				}
			}
			JsonObject table = new JsonObject();
			table.put("key", "app:"+Util.getUserId(params.getString("token")));
			table.put("value", ja.toString());
			setRedis(this, Config.setRedis, table);
			
			break;
		}
		
	}
	@Override
	public JsonObject checkValidation(JsonObject params) {
		JsonObject res = new JsonObject(); 
		if (!params.containsKey("token") || params.getString("token").isEmpty() || params.getString("token").equals("")){
			res.put("result_code",-1);
			res.put("result_msg", "로그인 후 이용해주세요.");
			return res;
		}
		if (!params.containsKey("user_color") || params.getString("user_color").isEmpty() || params.getString("user_color").equals("")){
			res.put("result_code",-1);
			res.put("result_msg", "로그인 후 이용해주세요.");
			return res;
		}
		if (!params.containsKey("app_id") || params.getString("app_id").isEmpty() || params.getString("app_id").equals("")){
			res.put("result_code",-1);
			res.put("result_msg", "로그인 후 이용해주세요.");
			return res;
		}
		res.put("result_code", 0);
			return res;
	}

}
