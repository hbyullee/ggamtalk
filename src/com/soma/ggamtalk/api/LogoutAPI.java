package com.soma.ggamtalk.api;


import com.soma.ggamtalk.utils.Config;
import com.soma.ggamtalk.utils.Util;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class LogoutAPI extends BaseAPI{

	public LogoutAPI(){ }
	
	@Override
	public void execute(Vertx vertx, HttpServerRequest request){
		init(vertx, request);
		
		if(params.isEmpty() || checkValidation(params).getInteger("result_code")==-1){
			request.response().end(checkValidation(params).toString());
			return;
		}
		
		getRedis(this, Config.getSession, params.getString("token"));
		
		delRedis(this, Config.logout, params.getString("token"));
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
			
			delRedis(this, Config.logout, params.getString("token"));
			
			
		case Config.logout:
			Util.getCache().remove(params.getString("user_id"));
			rs.put("result_code", 0);
			rs.put("result_msg", "로그아웃하였습니다.");
			request.response().end(rs.toString());
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
		res.put("result_code", 0);
			return res;
	}

}
