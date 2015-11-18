package com.soma.ggamtalk.api;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;

import com.soma.ggamtalk.utils.Config;
import com.soma.ggamtalk.utils.Util;

public class RemoveChannelAPI extends BaseAPI{
	
	public RemoveChannelAPI() { }

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
		//유저가 chief맞는지 확인!
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
			String query = String.format("DELETE FROM channel WHERE channel_id='%s'", 
					params.getString("channel_id"));
			insertCustomQuery(this, Config.delUserChannel, query);
		    break;
		case Config.delUserChannel:
			String query2 = String.format("DELETE FROM channel_user_list WHERE channel_id='%s'", 
					params.getString("channel_id"));
			
			insertCustomQuery(this, Config.delUserApp, query2);
//			delRedis(this, Config.delRedis, "users:"+params.getString("channel_id"));
			delRedis(this, Config.delRedis, "channelList:"+params.getString("app_id")+":"+params.getString("channel_id"));
		case Config.delUserApp:
			
			rs.put("result_code", -1);
			rs.put("result_msg", "채널을 삭제하였습니다.");
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
			res.put("result_msg", "앱을 선택해주세요.");
			return res;
		}
		res.put("result_code", 0);
			return res;
	}
}
