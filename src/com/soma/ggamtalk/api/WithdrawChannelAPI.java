package com.soma.ggamtalk.api;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;

import com.soma.ggamtalk.utils.Config;
import com.soma.ggamtalk.utils.Util;

public class WithdrawChannelAPI extends BaseAPI{
	
	public WithdrawChannelAPI() { }

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
			String gcm_id=user_info[1];
			
			params.put("user_id", user_id);
			params.put("gcm_id", gcm_id);
			
			String query = String.format("DELETE FROM channel_user_list WHERE user_id='%s' and channel_id='%s'", 
					params.getString("user_id"),params.getString("channel_id"));
			insertCustomQuery(this, Config.delUserChannel, query);
			
		case Config.delUserChannel:
//			JsonObject table = new JsonObject();
//			table.put("key", "users:"+params.getString("channel_id"));
//			table.put("value", params.getString("user_id")+","+params.getString("gcm_id"));
//			sremRedis(this, Config.sremRedis, table );
			
			rs.put("result_code", 0);
			rs.put("result_msg", "채널에서 퇴장하였습니다.");
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
		if (!params.containsKey("channel_id") || params.getString("channel_id").isEmpty() || params.getString("channel_id").equals("")){
			res.put("result_code",-1);
			res.put("result_msg", "채널을 선택해주세요.");
			return res;
		}
		res.put("result_code", 0);
			return res;
	}
}
