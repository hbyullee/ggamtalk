package com.soma.ggamtalk.api;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;

import com.soma.ggamtalk.utils.Config;
import com.soma.ggamtalk.utils.Util;

import java.util.ArrayList;
import java.util.List;

public class ListChannelAPI extends BaseAPI{
	
	JsonArray ja;
	JsonArray list;
	public ListChannelAPI() { }
	

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
			params.put("table_name", "channel_user_list");
			
			params.remove("token");
			selectQuery(this, Config.getChannel, params);
	
			break;
	
		case Config.getChannel:
			list = new JsonArray();
			ja = resultJO.getJsonArray("results");
			if(ja.size()<1){
				rs.put("result_code", -1);
				rs.put("result_msg", "가입한 채널이 없습니다.");
				request.response().end(rs.toString());
				break;
				
			}
				
			for(int i=0; i<ja.size(); i++){
				list.add("channelList:"+ja.getJsonObject(i).getString("app_id")+":"+ja.getJsonObject(i).getString("channel_id"));
			}
			
			mgetManyRedis(this, Config.mgetManyRedis, list.toString());
			
			
			break;
		case Config.mgetManyRedis:
			System.out.println("mgetManyRedis : "+ resultJO.toString());
			if(!resultJO.containsKey("result") || resultJO.getJsonArray("result") ==null || resultJO.getJsonArray("result").size()<1){
				StringBuilder sb = new StringBuilder();
				for(int i = 0; i<list.size(); i++){
					if(sb.length() > 0)
		                sb.append(" AND ");
					sb.append("channel_id =");
					sb.append("'");
					String a[] = list.getString(i).split(":");
					sb.append(a[2]);
					sb.append("'");
					  
				}
				String query2 = String.format("SELECT * FROM channel WHERE "+sb.toString());
				
				selectCustomQuery(this, Config.getUser, query2);
				break;
			}
			
			for(int i=0; i<ja.size(); i++){
				if(resultJO.getJsonArray("result").getString(i)==null)
					continue;
				ja.getJsonObject(i).put("channel_name", new JsonObject(resultJO.getJsonArray("result").getString(i)).getString("channel_name"));
				ja.getJsonObject(i).put("chief_id", new JsonObject(resultJO.getJsonArray("result").getString(i)).getString("chief_id"));
				ja.getJsonObject(i).put("channel_cate", new JsonObject(resultJO.getJsonArray("result").getString(i)).getString("channel_cate"));
			}
			
			rs.put("result_code", 0);
			rs.put("result_msg", "채널 리스트 입니다.");
			rs.put("list_channel", ja);
			request.response().end(rs.toString());
			
			break;
			
		case Config.getUser:
			for(int i=0; i<ja.size(); i++){
				ja.getJsonObject(i).put("channel_name", resultJO.getJsonArray("results").getJsonObject(i).getString("channel_name"));
				ja.getJsonObject(i).put("channel_cate", resultJO.getJsonArray("results").getJsonObject(i).getString("channel_cate"));
			}
			
			rs.put("result_code", 0);
			rs.put("result_msg", "채널 리스트 입니다.");
			rs.put("list_channel", ja);
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
		res.put("result_code", 0);
			return res;
	}

}
