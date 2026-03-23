/*
* Copyright 2024 - 2024 оригинальный автор или авторы.
*
* Лицензировано в соответствии с Apache License, версия 2.0 (the "License");
* вы можете использовать этот файл только в соответствии с лицензией.
* Вы можете получить копию лицензии по адресу
*
* https://www.apache.org/licenses/LICENSE-2.0
*
* Если не указано иное в соответствии с действующим законодательством или согласовано в письменной форме,
* программное обеспечение, распространяемое по лицензии,
* распространяется на условиях "КАК ЕСТЬ", БЕЗ ГАРАНТИЙ ИЛИ УСЛОВИЙ ЛЮБОГО РОДА, 
* явных или подразумеваемых. См. лицензию для получения информации об условиях использования, 
* а также об ограничениях, установленных лицензией.
*/
package org.springframework.ai.mcp.sample.client;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.spec.McpClientTransport;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.CompleteRequest;
import io.modelcontextprotocol.spec.McpSchema.CompleteResult;
import io.modelcontextprotocol.spec.McpSchema.GetPromptRequest;
import io.modelcontextprotocol.spec.McpSchema.GetPromptResult;
import io.modelcontextprotocol.spec.McpSchema.ListToolsResult;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceRequest;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceResult;
import io.modelcontextprotocol.spec.McpSchema.ResourceReference;
import io.modelcontextprotocol.spec.McpSchema.PromptReference;


public class SampleClient {

	private static final Logger logger = LoggerFactory.getLogger(SampleClient.class);

	private final McpClientTransport transport;

	public SampleClient(McpClientTransport transport) {
		this.transport = transport;
	}

	public void run() {

		var client = McpClient.sync(this.transport)
				.loggingConsumer(message -> {
					logger.info(">> Client Logging: {}", message);
				})
				.build();

		client.initialize();

		client.ping();

		// List and demonstrate tools
		ListToolsResult toolsList = client.listTools();
		logger.info("Available Tools = {}", toolsList);
		toolsList.tools().stream().forEach(tool -> {
			logger.info("Tool: {}, description: {}, schema: {}", tool.name(), tool.description(), tool.inputSchema());
		});

		// CallToolResult weatherForcastResult = client.callTool(new CallToolRequest("getWeatherForecastByLocation",
		// 		Map.of("latitude", "47.6062", "longitude", "-122.3321")));
		// logger.info("Weather Forcast: {}", weatherForcastResult);

		CallToolResult weatherForcastResult2 = client.callTool(new CallToolRequest("getTemperature",
				Map.of("latitude", "47.6062", "longitude", "-122.3321", "city", "Seattle")));
		logger.info("Weather Forcast: {}", weatherForcastResult2);


		// // Resources
		ReadResourceResult resource = client.readResource(new ReadResourceRequest("user-status://alice"));

		logger.info("Resource = {}", resource);

		// Prompts
		GetPromptResult prompt = client.getPrompt(
				new GetPromptRequest("personalized-message", Map.of("name", "Alice", "age", "14", "interests", "AI")));

		logger.info("Prompt = {}", prompt);

		// Completions
		CompleteResult completion = client.completeCompletion(new CompleteRequest(new ResourceReference("user-status://{username}"),
				new CompleteRequest.CompleteArgument("username", "a")));

		logger.info("Completion = {}", completion);

		CompleteResult completion2 = client.completeCompletion(new CompleteRequest(new PromptReference("personalized-message"),
				new CompleteRequest.CompleteArgument("name", "a")));

		logger.info("Completion2 = {}", completion2);

		client.closeGracefully();

	}

}
