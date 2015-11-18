package com.soma.ggamtalk.api;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;

import com.soma.ggamtalk.utils.Config;
import com.soma.ggamtalk.utils.Util;

public class ListAppAPI extends BaseAPI{
	
	public ListAppAPI() { }
	

	@Override
	public void execute(Vertx vertx, HttpServerRequest request) {
		init(vertx, request);
			
		getRedis(this, Config.getSession, params.getString("token"));
		
		if(params.isEmpty() || checkValidation(params).getInteger("result_code")==-1){
			request.response().end(checkValidation(params).toString());
			return;
		}
		
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
			getRedis(this, Config.getRedis, "app:"+params.getString("user_id"));

			
			break;
		case Config.getRedis:
			if(resultJO.containsKey("result") && new JsonArray(resultJO.getString("result")).size()>0){
				rs.put("result_code", 0);
				rs.put("result_msg", "등록된 앱 리스트입니다.");
				rs.put("list_app", new JsonArray(resultJO.getString("result")));
				request.response().end(rs.toString());
			}else{
				String query = String.format("SELECT * FROM app_user_list WHERE user_id='%s'", params.getString("user_id"));
				selectCustomQuery(this, Config.getApp, query);
			}
			
			break;
		
		case Config.getApp:
			
			JsonObject table = new JsonObject();
			table.put("key","app:"+params.getString("user_id"));
			table.put("value", resultJO.getJsonArray("results").toString());
			setRedis(this, Config.setRedis, table);
			
			rs.put("result_code", 0);
			rs.put("result_msg", "등록된 앱 리스트입니다.");
			rs.put("list_app", resultJO.getJsonArray("results"));
			
			request.response().end(rs.toString());
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
		res.put("result_code", 0);
			return res;
	}

}
