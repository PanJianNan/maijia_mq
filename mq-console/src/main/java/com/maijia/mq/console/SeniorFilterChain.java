package com.maijia.mq.console;

import org.jboss.netty.channel.MessageEvent;

public class SeniorFilterChain extends FilterChain {

	@Override
	public void createChain() {
	}
	
	@Override
	public void processReturn(MessageEvent e,AbstractFilter.CheckResult type){
		
		String returnMsg="";
		switch(type){
		case OK:
			return;
		case NONE_JSON_FORMAT_PARAM:
//			returnMsg=JsonUtil.stringToJson(Constants.NONE_JSON_STRING,Constants.msgMap.get(Constants.NONE_JSON_STRING), false);
			returnMsg="安全验证失败！";
			break;
		case NULL_SYSTEMID:
//			returnMsg=JsonUtil.stringToJson(Constants.NULL_SYSTEMIN,"安全验证失败！", false);
			returnMsg="安全验证失败！";
			break;
		case NULL_UUID_UUIDKEY_SYSTEMID:
//			returnMsg=JsonUtil.stringToJson(Constants.NULL_UUID_UUIDKEY_SYSTEMID,"安全验证失败！", false);
			returnMsg="安全验证失败！";
			break;
		case CHECK_FAILURE:
//			returnMsg=JsonUtil.stringToJson(Constants.DECRYPT_FAILURE,"安全验证失败！", false);
			returnMsg="安全验证失败！";
			break;
		case NULL_PARAM:
//			returnMsg=JsonUtil.stringToJson(Constants.NULL_PARAM,Constants.msgMap.get(Constants.NULL_PARAM), false);
			returnMsg="安全验证失败！";
			break;
		case DUPLICATED_UUID:
//			returnMsg=JsonUtil.stringToJson(Constants.DUPLICATED_UUID,"安全验证失败！", false);
			returnMsg="安全验证失败！";
			break;
		case IILEGAL_TOKEN:
//			returnMsg=JsonUtil.stringToJson(Constants.IILEGAL_TOKEN,"所传参数含有非法字符！", false);
			returnMsg="安全验证失败！";
			break;
		default:
//			returnMsg=JsonUtil.stringToJson("","出现错误！", false);
			returnMsg="安全验证失败！";
			break;	
		}
		HttpServer.writeResponse(e, returnMsg);
	}

}
