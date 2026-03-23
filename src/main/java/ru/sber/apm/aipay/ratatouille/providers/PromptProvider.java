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

import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema.GetPromptRequest;
import io.modelcontextprotocol.spec.McpSchema.GetPromptResult;
import io.modelcontextprotocol.spec.McpSchema.LoggingMessageNotification;
import io.modelcontextprotocol.spec.McpSchema.PromptMessage;
import io.modelcontextprotocol.spec.McpSchema.Role;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import org.springframework.ai.mcp.annotation.McpArg;
import org.springframework.ai.mcp.annotation.McpPrompt;

import org.springframework.stereotype.Service;


@Service
public class PromptProvider {
    /**
     * Простой приветственный запрос, который принимает параметр имени.
     * @param name Имя для приветствия
     * @return Приветственное сообщение
     */
    @McpPrompt(name = "greeting", description = "Простой приветственный запрос")
    public GetPromptResult greetingPrompt(
            @McpArg(name = "name", description = "Имя для приветствия", required = true) String name) {
        return new GetPromptResult("Приветствие", List.of(new PromptMessage(Role.ASSISTANT,
                new TextContent("Привет, " + name + "! Добро пожаловать в систему MCP."))));
    }

    /**
     * Более сложный запрос, который генерирует персонализированное сообщение.
     * @param exchange Обмен с сервером
     * @param name Имя пользователя
     * @param age Возраст пользователя
     * @param interests Интересы пользователя
     * @return Персонализированное сообщение
     */
    @McpPrompt(name = "personalized-message",
            description = "Генерирует персонализированное сообщение на основе информации о пользователе")
    public GetPromptResult personalizedMessage(McpSyncServerExchange exchange,
            @McpArg(name = "name", description = "Имя пользователя", required = true) String name,
            @McpArg(name = "age", description = "Возраст пользователя", required = false) Integer age,
            @McpArg(name = "interests", description = "Интересы пользователя", required = false) String interests) {

        exchange.loggingNotification(LoggingMessageNotification.builder()
            // .level(LoggingLevel.INFO)
            .data("personalized-message event")
            .build());

        StringBuilder message = new StringBuilder();
        message.append("\nПривет, ").append(name).append("!\n");

        if (age != null) {
            message.append("В возрасте ").append(age).append(" лет у вас ");
            if (age < 30) {
                message.append("еще так много впереди.\n\n");
            }
            else if (age < 60) {
                message.append("накоплен ценный жизненный опыт.\n\n");
            }
            else {
                message.append("накоплена мудрость, которой можно делиться с другими.\n\n");
            }
        }

        if (interests != null && !interests.isEmpty()) {
            message.append("Ваш интерес к ")
                .append(interests)
                .append(" показывает вашу любознательность и страсть к обучению.\n\n");
        }

        message
            .append("Я здесь, чтобы помочь вам с любыми вопросами, которые у вас могут возникнуть об MCP.");

        return new GetPromptResult("Персонализированное сообщение",
                List.of(new PromptMessage(Role.ASSISTANT, new TextContent(message.toString()))));
    }

    /**
     * Запрос, который возвращает список сообщений, формирующих разговор.
     * @param request Запрос запроса
     * @return Список сообщений
     */
    @McpPrompt(name = "conversation-starter", description = "Обеспечивает начало разговора с системой")
    public List<PromptMessage> conversationStarter(GetPromptRequest request) {
        return List.of(
                new PromptMessage(Role.ASSISTANT,
                        new TextContent("Привет! Я помощник MCP. Как я могу помочь вам сегодня?")),
                new PromptMessage(Role.USER,
                        new TextContent("Я хотел бы узнать больше о Протоколе Контекста Модели.")),
                new PromptMessage(Role.ASSISTANT, new TextContent(
                        "Отличный выбор! MCP - это стандартизированный способ для серверов "
                                + "взаимодействовать с языковыми моделями. Он обеспечивает структурированный подход для "
                                + "обмена информацией, отправки запросов и обработки ответов. "
                                + "Какой аспект вы хотели бы изучить в первую очередь?")));
    }

    /**
     * Запрос, который принимает аргументы в виде карты.
     * @param arguments Карта аргументов
     * @return Результат запроса
     */
    @McpPrompt(name = "map-arguments", description = "Демонстрирует использование карты для аргументов")
    public GetPromptResult mapArguments(Map<String, Object> arguments) {
        StringBuilder message = new StringBuilder("Я получил следующие аргументы:\n\n");

        if (arguments != null && !arguments.isEmpty()) {
            for (Map.Entry<String, Object> entry : arguments.entrySet()) {
                message.append("- ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
            }
        }
        else {
            message.append("Аргументы не были предоставлены.");
        }

        return new GetPromptResult("Демонстрация аргументов карты",
                List.of(new PromptMessage(Role.ASSISTANT, new TextContent(message.toString()))));
    }

    /**
     * Запрос, который возвращает один PromptMessage.
     * @param name Имя пользователя
     * @return один PromptMessage
     */
    @McpPrompt(name = "single-message", description = "Демонстрирует возврат одного PromptMessage")
    public PromptMessage singleMessagePrompt(
            @McpArg(name = "name", description = "Имя пользователя", required = true) String name) {
        return new PromptMessage(Role.ASSISTANT,
                new TextContent("Привет, " + name + "! Это ответ с одним сообщением."));
    }

    /**
     * Запрос, который возвращает список строк.
     * @param topic Тема, о которой предоставить информацию
     * @return Список строк с информацией о теме
     */
    @McpPrompt(name = "string-list", description = "Демонстрирует возврат списка строк")
    public List<String> stringListPrompt(@McpArg(name = "topic",
            description = "Тема, о которой предоставить информацию", required = true) String topic) {
        if ("MCP".equalsIgnoreCase(topic)) {
            return List.of(
                    "MCP - это стандартизированный способ для серверов взаимодействовать с языковыми моделями.",
                    "Он обеспечивает структурированный подход для обмена информацией, отправки запросов и обработки ответов.",
                    "MCP позволяет серверам предоставлять ресурсы, инструменты и запросы клиентам единообразным способом.");
        }
        else {
            return List.of("У меня нет конкретной информации о " + topic + ".",
                    "Попробуйте другую тему или задайте более конкретный вопрос.");
        }
    }

}
