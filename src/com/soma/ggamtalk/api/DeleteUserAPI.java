package com.soma.ggamtalk.api;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;

import com.soma.ggamtalk.utils.Config;
import com.soma.ggamtalk.utils.Util;

public class DeleteUserAPI extends BaseAPI{
	
	public DeleteUserAPI() { }

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
			String query = String.format("SELECT * FROM user WHERE user_id='%s' and user_pw='%s'", 
					params.getString("user_id"), params.getString("user_pw"));
			selectCustomQuery(this, Config.checkMyPw, query);
		
			break;
		case Config.checkMyPw:
			if(resultJO.containsKey("results")&&resultJO.getJsonArray("results").size()>0){
				String query2 = String.format("DELETE FROM user WHERE user_id='%s' and user_pw='%s'", 
						params.getString("user_id"), params.getString("channel_id"));
				insertCustomQuery(this, Config.delUserChannel, query2);
				break;
			}
			
			rs.put("result_code", -1);
			rs.put("result_msg", "패스워드가 일치하지 않습니다.");
			request.response().end(rs.toString());
			
			break;
			
		case Config.delUserChannel:
//			JsonObject table = new JsonObject();
//			table.put("key", "users:"+params.getString("channel_id"));
//			table.put("value", params.getString("user_id"));
//			sremRedis(this, Config.sremRedis, table );
//			
//			JsonObject table2 = new JsonObject();
//			table2.put("key", "channels:"+params.getString("user_id"));
//			table2.put("value", params.getString("channel_id"));
//			sremRedis(this, Config.sremRedis, table2 );
//			
			rs.put("result_code", 0);
			rs.put("result_msg", "회원탈퇴가 완료되었습니다.");
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
