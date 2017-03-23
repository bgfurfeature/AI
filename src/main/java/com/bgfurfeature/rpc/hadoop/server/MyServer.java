package com.bgfurfeature.rpc.hadoop.server;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.ipc.RPC;
import org.apache.hadoop.ipc.RPC.Builder;
import org.apache.hadoop.ipc.Server;

import com.bgfurfeature.rpc.hadoop.protocol.SomeService;

/**
 * hadoop 自带的rpc 框架，直接拿来使用
 */
public class MyServer {

	public static void main(String[] args) throws Exception {

		Builder builder = new Builder(new Configuration());
		builder.setBindAddress("localhost");
		builder.setPort(5555);
		builder.setProtocol(SomeService.class);  // 遵守的协议
		builder.setInstance(new SomeServiceImpl()); // 具体的实现
		Server server = builder.build();

		server.start();
	}

}
