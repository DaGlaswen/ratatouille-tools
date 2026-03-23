///*
//* Copyright 2025 - 2025 the original author or authors.
//*
//* Licensed under the Apache License, Version 2.0 (the "License");
//* you may not use this file except in compliance with the License.
//* You may obtain a copy of the License at
//*
//* https://www.apache.org/licenses/LICENSE-2.0
//*
//* Unless required by applicable law or agreed to in writing, software
//* distributed under the License is distributed on an "AS IS" BASIS,
//* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//* See the License for the specific language governing permissions and
//* limitations under the License.
//*/
//package org.springframework.ai.mcp.sample.server.providers;
//
//import io.modelcontextprotocol.spec.McpSchema.CreateMessageResult;
//import org.springframework.ai.mcp.annotation.McpTool;
//import org.springframework.ai.mcp.annotation.McpToolParam;
//import org.springaicommunity.mcp.context.McpAsyncRequestContext;
//import org.springaicommunity.mcp.context.StructuredElicitResult;
//import reactor.core.publisher.Mono;
//
//import org.springframework.stereotype.Service;
//
//@Service
//public class AsyncToolProvider {
//
//	// Примечание: Elicitation не поддерживает некоторые типы данных в JSON Schema. Необходимо использовать Number вместо int/Integer!
//	public record Person(String name, Number age) {}
//
//	@McpTool(description = "Тестовый инструмент", name = "tool4", generateOutputSchema = true)
//	public Mono<String> toolLoggingSamplingElicitationProgress(McpAsyncRequestContext mcpCtx, @McpToolParam String input) {
//
//		return Mono.defer(() -> Mono.just(input))
//			// Логирование + инициализация токена прогресса
//			.doOnNext(i -> {
//				mcpCtx.info("Tool4 вызван с входными данными: " + i);
//				mcpCtx.progress(p -> p.progress(0.0).total(1.0).message("начало вызова инструмента"));
//				mcpCtx.ping();
//			})
//			// Выполняем elicitation
//			.flatMap(i -> mcpCtx.elicit(Person.class)
//				.doOnSuccess(result ->
//					mcpCtx.progress(p -> p.progress(0.50).total(1.0).message("элиситация завершена"))
//				)
//				.map(elicitResult -> new Object[] { i, elicitResult })
//			)
//			// Выполняем sampling
//			.flatMap(data -> {
//				String originalInput = (String) data[0];
//				StructuredElicitResult<Person> person = (StructuredElicitResult<Person>) data[1];
//
//				return mcpCtx.sample(s -> s
//					.message("Тестовое сообщение сэмплирования")
//					.maxTokens(500)
//					.modelPreferences(mp -> mp.modelHints("OpenAi","Ollama")
//							.costPriority(1.0)
//							.speedPriority(1.0)
//							.intelligencePriority(1.0)))
//					.doOnSuccess(result ->
//						mcpCtx.progress(p -> p.progress(1.0).total(1.0).message("сэмплирование завершено"))
//					)
//					.map(samplingResult -> new Object[] { originalInput, person, samplingResult });
//			})
//			// Составляем ответ
//			.map(results -> {
//				String originalInput = (String) results[0];
//				StructuredElicitResult<Person> person = (StructuredElicitResult<Person>) results[1];
//				CreateMessageResult samplingResult = (CreateMessageResult) results[2];
//
//				String elicitResponse = person.structuredContent() != null ? person.structuredContent().toString() : "нет ответа на элиситацию";
//				String samplingResponse = samplingResult != null ? samplingResult.toString() : "нет ответа на сэмплирование";
//
//				return String.format("ОТВЕТ НА ВЫЗОВ: %s, %s", samplingResponse, elicitResponse);
//			})
//			// Логируем результат
//			.doOnSuccess(response -> mcpCtx.info("Tool4 завершен"))
//			.doOnError(error -> mcpCtx.info("Ошибка Tool4: " + error.getMessage()));
//	}
//
//}
