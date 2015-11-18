package com.soma.ggamtalk.api;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;

import com.soma.ggamtalk.utils.Config;
import com.soma.ggamtalk.utils.Util;

public class JoinChannelAPI extends BaseAPI{
	
	public JoinChannelAPI() { }
	
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
			String gcm_id =user_info[1];
			params.put("user_id", user_id);
			params.put("gcm_id", gcm_id);
			
			if(params.containsKey("channel_pw")){
				String query = String.format("SELECT * FROM channel WHERE channel_id='%s' and channel_pw='%s'",
						params.getString("channel_id"), params.getString("channel_pw"));
				selectCustomQuery(this, Config.checkChannelpw, query);
			}else{
				String query = String.format("INSERT INTO channel_user_list SET user_id='%s', channel_id='%s', app_id='%s', user_nick='%s'",
						params.getString("user_id"), params.getString("channel_id"), params.getString("app_id"), params.getString("user_nick"));
				insertCustomQuery(this, Config.setUserChannel, query);
			}
			
			break;
			
		case Config.checkChannelpw:
			if(resultJO.getJsonArray("results").size()<1){
				rs.put("result_code", -1);
				rs.put("result_msg", "채널 비밀번호가 올바르지 않습니다.");
				request.response().end(rs.toString());
				break;
			}
			
			String query2 = String.format("INSERT INTO channel_user_list SET user_id='%s', channel_id='%s', app_id='%s', user_nick='%s'",
					params.getString("user_id"), params.getString("channel_id"), params.getString("app_id"), params.getString("user_nick"));
			insertCustomQuery(this, Config.setUserChannel, query2);
			break;
			
		case Config.setUserChannel:
			
//		 	JsonObject user_info = new JsonObject();
//		 	user_info.put("user_id", params.getString("user_id"));
//		 	user_info.put("gcm_id", params.getString("gcm_id"));
//		 	user_info.put("user_nick", params.getString("user_nick"));
//		 	user_info.put("user_color", "#FFE400");
//	
//			JsonObject jo2 = new JsonObject();
//			jo2.put("key", "users:"+params.getString("channel_id"));
//			jo2.put("value", params.getString("user_id")+","+params.getString("gcm_id")+","+params.getString("user_nick")+",#FFE400");
//			saddRedis(this, Config.saddRedis, jo2);
//			
//			JsonObject table2 = new JsonObject();
//		 	table2.put("key","ch:"+user_id+":"+channel_id);
//		 	table2.put("value",user_info);
//		 	setRedis(this, Config.setRedis, table2);
//			
			rs.put("result_code", 0);
			rs.put("result_msg", "채널에 입장하였습니다.");
			
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
		if (!params.containsKey("channel_id") || params.getString("channel_id").isEmpty() || params.getString("channel_id").equals("")){
			res.put("result_code",-1);
			res.put("result_msg", "채널을 선택해주세요.");
			return res;
		}
		if (!params.containsKey("app_id") || params.getString("app_id").isEmpty() || params.getString("app_id").equals("")){
			res.put("result_code",-1);
			res.put("result_msg", "앱 아이디가 없습니다.");
			return res;
		}
		if (!params.containsKey("user_nick") || params.getString("user_nick").isEmpty() || params.getString("user_nick").equals("")){
			res.put("result_code",-1);
			res.put("result_msg", "유저 닉네임을 입력해주세요.");
			return res;
		}
		
		res.put("result_code", 0);
			return res;
	}

}
