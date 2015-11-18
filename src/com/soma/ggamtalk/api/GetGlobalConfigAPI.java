package com.soma.ggamtalk.api;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;

import com.soma.ggamtalk.utils.Config;
import com.soma.ggamtalk.utils.Util;

public class GetGlobalConfigAPI extends BaseAPI{
	
	public GetGlobalConfigAPI() { }
	

	@Override
	public void execute(Vertx vertx, HttpServerRequest request) {
		init(vertx, request);
		
//		if(params.isEmpty() || checkValidation(params).getInteger("result_code")==-1){
//			request.response().end(checkValidation(params).toString());
//		}
		
//		getRedis(this, Config.getSession, params.getString("token"));
		
		String query = String.format("SELECT * FROM global_setting");
		selectCustomQuery(this, Config.getGlobalConfig, query);
		
	}


	@Override
	public void onExecute(int what, JsonObject resultJO) {

		if(resultJO.containsKey("result_code") && resultJO.getInteger("result_code")==-1){
			request.response().end(resultJO.toString());
			return;
		}
		JsonObject rs = new JsonObject();

		switch (what) {
		case Config.getGlobalConfig:
			rs.put("result_code", 0);
			rs.put("result_msg", "앱의 정보입니다.");
			rs.put("config", resultJO.getJsonArray("results").getJsonObject(0));
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
