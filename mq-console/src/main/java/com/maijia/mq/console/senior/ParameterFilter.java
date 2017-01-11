package com.maijia.mq.console.senior;

import com.maijia.mq.console.AbstractFilter;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ParameterFilter extends AbstractFilter {

	@Override
	public CheckResult filter(Object... inParam) {
		
		MessageEvent e=(MessageEvent)inParam[0];
		
		HttpRequest request=(HttpRequest)e.getMessage();
		ChannelBuffer content=request.getContent();//获得消息体的内容		
		
		String contentStr=null;
		try {
			contentStr = new String(content.toByteBuffer().array(),"UTF-8");
		} catch (UnsupportedEncodingException e1) {
			return CheckResult.OTHER_ERROE;
		}	
		
		QueryStringDecoder decoder=new QueryStringDecoder("/?"+contentStr, Charset.forName("UTF-8"),true, Integer.MAX_VALUE);
		
		//解析JSON字符串为JSON对象
		Map<String,List<String>> tmpMap=null;
		try{
			tmpMap=decoder.getParameters();	
		}catch(Exception ex){
			return CheckResult.IILEGAL_TOKEN;
		}
		if(tmpMap.isEmpty()){//Http消息体为空
			return CheckResult.NULL_PARAM;
		}		
		if(tmpMap.size()>1){//如果传入多个参数，如name=aaaa&password=bbbb
			return CheckResult.NONE_JSON_FORMAT_PARAM;
		}
		
		Map<String,Object> paramMap=parseParam(tmpMap);
		if(paramMap==null){
			return CheckResult.NONE_JSON_FORMAT_PARAM;
		}
		
		return getSuccessor().filter(e,paramMap);//deliver to the next filter
	}
	
	/**
	 * 将临时参数转换为Map<String,Object> paramMap，存放所有的入参信息
	 * @param tmpMap
	 * @return Map<String,Object>
	 */
	private Map<String,Object> parseParam(Map<String,List<String>> tmpMap){
		
		Map<String, Object> paramMap = null;// 存放所有的参数
		Set set = tmpMap.keySet();
		Object[] obj = set.toArray();
	    
		// 取出第一个参数
		List<String> valueList = tmpMap.get(obj[0]);
		if (valueList.size() == 0) {// 如果传入"{name:\"aaa\",password:\"111\"}"的形式
			String value = obj[0].toString();
			if (!"".equals(value) && !"{}".equals(value)) {
//				paramMap = JsonUtil.getMapFromString(value);
				paramMap = new HashMap<>();
			}
		} else if (valueList.size() == 1) {// 如果传入"param={name:\"aaa\",password:\"111\"}"的形式
			String value = (String) valueList.get(0);
			if (!"".equals(value) && !"{}".equals(value)) {
//				paramMap = JsonUtil.getMapFromString(value);
				paramMap = new HashMap<>();
			}else{
//				paramMap = JsonUtil.getMapFromString(obj[0].toString());
				paramMap = new HashMap<>();
			}
		}
	    return paramMap;
	}
}
