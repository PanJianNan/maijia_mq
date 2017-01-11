package com.maijia.mq.console.pipelinefactory;

import com.maijia.mq.console.handler.ApiHandler;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.jboss.netty.handler.stream.ChunkedWriteHandler;


public class HttpChannelPipelineFactory implements ChannelPipelineFactory{

	public ChannelPipeline getPipeline() throws Exception {

		ChannelPipeline pipeline = Channels.pipeline();
		pipeline.addLast("decoder", new HttpRequestDecoder());//upstream
		pipeline.addLast("aggregator",new HttpChunkAggregator(1024*1024*1024));//upstream
		pipeline.addLast("encoder", new HttpResponseEncoder());//downstream
		pipeline.addLast("streamer", new ChunkedWriteHandler());//upstream and downstream
		pipeline.addLast("api", new ApiHandler());//upstream
//		pipeline.addLast("handler", new ControlHandler());//upstream
		return pipeline;
	}

}
