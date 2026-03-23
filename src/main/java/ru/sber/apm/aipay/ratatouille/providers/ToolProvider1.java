/* 
* Copyright 2025 - 2025 the original author or authors.
* 
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* 
* https://www.apache.org/licenses/LICENSE-2.0
* 
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package ru.sber.apm.aipay.ratatouille.providers;

import java.util.List;
import java.util.Map;

import org.springframework.ai.mcp.annotation.McpProgressToken;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;

import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CreateMessageResult;
import io.modelcontextprotocol.spec.McpSchema.ElicitResult;
import io.modelcontextprotocol.spec.McpSchema.LoggingMessageNotification;
import io.modelcontextprotocol.spec.McpSchema.ModelHint;
import io.modelcontextprotocol.spec.McpSchema.ModelPreferences;
import io.modelcontextprotocol.spec.McpSchema.ProgressNotification;
import org.springframework.stereotype.Service;

@Service
public class ToolProvider1 {


	@McpTool(description = "Тестовый инструмент 1", name = "test_tool_1", generateOutputSchema = true)
	public String toolLoggingSamplingElicitationProgress(McpSyncServerExchange exchange, @McpToolParam String input,
			@McpProgressToken String progressToken) {

		exchange.loggingNotification(LoggingMessageNotification.builder().data("Test_Tool_1 запущен!").build());

		exchange.progressNotification(
				new ProgressNotification(progressToken, 0.0, 1.0, "начало вызова инструмента"));

		exchange.ping();
			
		// вызываем elicitation
        McpSchema.ElicitRequest elicitationRequest = McpSchema.ElicitRequest.builder()
                .message("Тестовое сообщение")
                .requestedSchema(
                        Map.of("type", "object", "properties", Map.of("name", Map.of("type", "string"), "age", Map.of("type", "integer"))))
                .build();

		ElicitResult elicitationResult = exchange.createElicitation(elicitationRequest);

		exchange.progressNotification(
				new ProgressNotification(progressToken, 0.50, 1.0, "элиситация завершена"));

		// вызываем сэмплирование
        McpSchema.CreateMessageRequest createMessageRequest = McpSchema.CreateMessageRequest.builder()
                .messages(List.of(new McpSchema.SamplingMessage(McpSchema.Role.USER,
                        new McpSchema.TextContent("Тестовое сообщение сэмплирования"))))
                .modelPreferences(ModelPreferences.builder()
                        .hints(List.of(ModelHint.of("OpenAi"), ModelHint.of("Ollama")))
                        .costPriority(1.0)
                        .speedPriority(1.0)
                        .intelligencePriority(1.0)
                        .build())
                .build();

		CreateMessageResult samplingResponse = exchange.createMessage(createMessageRequest);

		exchange.progressNotification(
				new ProgressNotification(progressToken, 1.0, 1.0, "сэмплирование завершено"));

		exchange.loggingNotification(LoggingMessageNotification.builder().data("Tool1 завершен!").build());

		return "ОТВЕТ НА ВЫЗОВ: " + samplingResponse.toString() + ", " + elicitationResult.toString();
	}

}
