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

import io.modelcontextprotocol.spec.McpSchema.CreateMessageResult;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;

import org.springframework.ai.mcp.annotation.context.McpSyncRequestContext;
import org.springframework.ai.mcp.annotation.context.StructuredElicitResult;
import org.springframework.stereotype.Service;


@Service
public class ToolProvider2 {

    public record Person(String name, Number age) {
    }

    @McpTool(description = "Тестовый инструмент 2", name = "test_tool_2", generateOutputSchema = true)
    public String toolLoggingSamplingElicitationProgress(McpSyncRequestContext ctx, @McpToolParam String input) {

        ctx.info("test_tool_2 был вызван"); // Передаем клиенту лог

        ctx.progress(p -> p.percentage(25).message("test_tool_2 отработал на 25%")); // Передаем progress

        ctx.ping(); // Вызываем у клиента ping

        StructuredElicitResult<Person> elicitationResult = ctx.elicit(e -> e.message("Заполните"), Person.class);

        ctx.progress(p -> p.progress(0.50).total(1.0).message("elicitation завершен"));

        CreateMessageResult samplingResponse = ctx.sample(s -> s
                .message("Что происходило в предыдущих сообщениях?")
                .maxTokens(500)
                .modelPreferences(mp -> mp.modelHints("OpenAi", "Ollama")
                        .costPriority(1.0)
                        .speedPriority(1.0)
                        .intelligencePriority(1.0)));

        ctx.progress(p -> p.progress(1.0).total(1.0).message("sampling завершен"));

        ctx.info("Test_Tool_2 успешно выполнен");

        return "РЕЗУЛЬТАТ ВЫЗОВА: " + samplingResponse.toString() + ", " + elicitationResult.toString();
    }

}
