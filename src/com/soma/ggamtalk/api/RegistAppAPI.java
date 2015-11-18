package com.soma.ggamtalk.api;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;

import com.soma.ggamtalk.utils.Config;
import com.soma.ggamtalk.utils.Util;

public class RegistAppAPI extends BaseAPI{
	
	public RegistAppAPI() { }
	
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
		{
			if(!resultJO.containsKey("result")||resultJO.getString("result")==null){
				rs.put("result_code", -1);
				rs.put("result_msg", "로그인 후 이용해주세요.");
				request.response().end(rs.toString());
				break;
			}
			String user_info[]= resultJO.getString("result").split(",");
			String user_id=user_info[0];
			
			params.put("user_id", user_id);
			
			String query = String.format("INSERT INTO app_user_list SET user_id='%s', app_id='%s', user_nick='%s', app_cate='%s'", 
					params.getString("user_id"), params.getString("app_id"), params.getString("user_nick"), params.getString("app_cate"), "on");
			insertCustomQuery(this, Config.setApp, query);
			
			break;
		}
		case Config.setApp:
		{
			setPermission(this, Config.setPermission, params.getString("app_id")+"_default");
			rs.put("result_code", 0);
			rs.put("result_msg", "앱을 성공적으로 등록하였습니다.");
			request.response().end(rs.toString());
			delRedis(this, Config.delRedis, "app:"+params.getString("user_id"));
			break;
		}	
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
			res.put("result_msg", "앱 아이디를 정확히 입력해주세요.");
			return res;
		}
		res.put("result_code", 0);
			return res;
	}
	
}
