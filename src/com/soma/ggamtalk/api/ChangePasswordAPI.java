package com.soma.ggamtalk.api;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import com.soma.ggamtalk.utils.Config;
import com.soma.ggamtalk.utils.Util;

public class ChangePasswordAPI extends BaseAPI{
	
	public ChangePasswordAPI() { }
	
	
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
			
			String query = String.format("UPDATE user SET user_pw='%s' WHERE user_id='%s'",
					params.getString("user_pw"), params.getString("user_id"));
			insertCustomQuery(this, Config.changePassword, query);

			break;
			
		case Config.changePassword:

			rs.put("result_code", 0);
			rs.put("result_msg", "비밀번호 변경하였습니다.");
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
		if (!params.containsKey("user_pw") || params.getString("user_pw").isEmpty() || params.getString("user_pw").equals("")){
			res.put("result_code",-1);
			res.put("result_msg", "패스워드를 정확히 입력해주세요.");
			return res;
		}
		res.put("result_code", 0);
			return res;
	}

}
