package com.soma.ggamtalk.api;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;

import com.soma.ggamtalk.utils.Config;
import com.soma.ggamtalk.utils.Util;

public class GetAppConfigAPI extends BaseAPI{
	
	public GetAppConfigAPI() { }

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
			params.remove("token");
			params.put("table_name","app_user_list");
			selectQuery(this, Config.getAppConfig, params);
//			selectQuery(this, Config.getAppConfig, params.getString(""));
//			Config.getAppConfig(this, Util.getUserId(params.getString("token")), params.getString("app_id"));
			break;
	
		case Config.getAppConfig:
			rs.put("result_code", 0);
			rs.put("result_msg", "success to get appconfig");
			rs.put("config", resultJO.getJsonArray("results"));
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
