package com.soma.ggamtalk.api;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import com.soma.ggamtalk.utils.Config;

public class SearchChannelAPI extends BaseAPI{
	
	public SearchChannelAPI() { }
	

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
			
			keysRedis(this, Config.keysRedis, "channelList:"+params.getString("app_id")+":*");
			
			break;
		case Config.keysRedis:
			mgetManyRedis(this, Config.mgetManyRedis, resultJO.getJsonArray("result").toString());
			
			break;
		case Config.mgetManyRedis:
			JsonArray channel_list = new JsonArray();
			System.out.println("list뜨니? :"+ resultJO.getJsonArray("result"));
			for(int i=0; i<resultJO.getJsonArray("result").size();i++){
				if(new JsonObject(resultJO.getJsonArray("result").getString(i)).getString("channel_name").indexOf(params.getString("channel_name"))>-1){
					channel_list.add(new JsonObject(resultJO.getJsonArray("result").getString(i)));
				}
			}
			rs.put("result_code", 0);
			rs.put("result_msg", "success to search channel list");
			rs.put("list_channel", channel_list);
			request.response().end(rs.toString());
//			String query = String.format("SELECT * FROM channel WHERE app_id='%s' and channel_name='%s'", params.getString("app_id"), params.getString("channel_name"));
//			
//			selectCustomQuery(this, Config.searchChannel, query);
			
			break;
		case Config.searchChannel:
			rs.put("result_code", 0);
			rs.put("result_msg", "success to search channel list");
			rs.put("list_channel", resultJO.getJsonArray("results"));
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
		if (!params.containsKey("channel_name") || params.getString("channel_name").isEmpty() || params.getString("channel_name").equals("")){
			res.put("result_code",-1);
			res.put("result_msg", "채널 정보가 없습니다.");
			return res;
		}
		if (!params.containsKey("app_id") || params.getString("app_id").isEmpty() || params.getString("app_id").equals("")){
			res.put("result_code",-1);
			res.put("result_msg", "앱 아이디 정보가 없습니다.");
			return res;
		}
		res.put("result_code", 0);
			return res;
	}

}
