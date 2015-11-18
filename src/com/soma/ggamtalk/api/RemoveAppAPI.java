package com.soma.ggamtalk.api;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;

import com.soma.ggamtalk.utils.Config;
import com.soma.ggamtalk.utils.Util;

public class RemoveAppAPI extends BaseAPI{
	
	public RemoveAppAPI() { }

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
			String query2 = String.format("DELETE FROM app_user_list WHERE user_id='%s' and app_id='%s'", 
					params.getString("user_id"), params.getString("app_id"));
			insertCustomQuery(this, Config.delUserApp, query2);
		    break;
		case Config.delUserApp:
			delRedis(this, Config.delRedis, "app:"+params.getString("user_id"));
			rs.put("result_code", 0);
			rs.put("result_msg", "앱을 등록해지하였습니다.");
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
		if (!params.containsKey("app_id") || params.getString("app_id").isEmpty() || params.getString("app_id").equals("")){
			res.put("result_code",-1);
			res.put("result_msg", "앱을 선택해주세요.");
			return res;
		}
		res.put("result_code", 0);
			return res;
	}
}
