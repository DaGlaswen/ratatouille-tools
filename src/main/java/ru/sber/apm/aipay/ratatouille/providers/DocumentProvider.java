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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.modelcontextprotocol.spec.McpSchema.PromptMessage;
import io.modelcontextprotocol.spec.McpSchema.Role;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.mcp.annotation.McpArg;
import org.springframework.ai.mcp.annotation.McpPrompt;
import org.springframework.ai.mcp.annotation.McpResource;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;

import org.springframework.ai.mcp.annotation.context.McpSyncRequestContext;
import org.springframework.stereotype.Service;


@Service
public class DocumentProvider {

    private static final Logger logger = LoggerFactory.getLogger(DocumentProvider.class);

	private static final String DOCS_DIR = Paths.get(
        System.getProperty("user.dir"), "docs"
    ).toString();

	public static String getDocPath(String docId) {
		return Paths.get(DOCS_DIR, docId).toString();    
	}

	@McpTool(description = "Читает содержимое документа и возвращает его в виде строки.", name = "read_doc_contents")
	public String readDocContents(McpSyncRequestContext ctx, @McpToolParam String docId) {

        ctx.info(String.format("Был принят запрос на чтение содержимого документа %s", docId));
        Path docPath = Paths.get(getDocPath(docId));

        try {
            if (!Files.exists(docPath)) {
                throw new IllegalArgumentException("Документ " + docId + " не был найден");
            }
            return Files.readString(docPath);
        } catch (IOException e) {
            String errorMessage = "I/O Ошибка при операции чтения документа " + docId;
            ctx.error(errorMessage);
            throw new RuntimeException(errorMessage, e);
        } catch (Exception e) {
            ctx.error(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

	@McpTool(description = "Редактирует документ, заменяя строку в содержимом документа новой строкой", name = "edit_document")
	public void editDocument(McpSyncRequestContext ctx, @McpToolParam String docId,
                             @McpToolParam String oldContent,
                             @McpToolParam String newContent) {

        ctx.info(String.format("Был принят запрос на редактирование документа %s", docId));
        Path docPath = Paths.get(getDocPath(docId));

        try {
            if (!Files.exists(docPath)) {
                throw new IllegalArgumentException("Документ " + docId + " не был найден");
            }
            String content = Files.readString(docPath);
            content = content.replace(oldContent, newContent);
            Files.writeString(docPath, content);
            ctx.info(String.format("Документ %s успешно отредактирован", docId));
        } catch (IOException e) {
            String errorMessage = "I/O Ошибка при редактировании документа " + docId;
            ctx.error(errorMessage);
            throw new RuntimeException(errorMessage, e);
        } catch (Exception e) {
            ctx.error(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
	}	
	
	@McpResource(uri = "docs://documents", mimeType = "application/json")
	public List<String> listDocs(McpSyncRequestContext ctx) {

        ctx.info("Был принят запрос на получение списка документов");
        try (Stream<Path> stream = Files.list(Paths.get(DOCS_DIR))) {
            List<String> docs = stream
                .map(Path::getFileName)
                .map(Path::toString)
                .collect(Collectors.toList());
            ctx.info(String.format("Список документов получен успешно, всего документов: %d", docs.size()));
            return docs;
        } catch(IOException e) {
            String errorMessage = "I/O Ошибка при получении списка документов";
            ctx.error(errorMessage);
            throw new RuntimeException(errorMessage, e);
        } catch( Exception e) {
            ctx.error(e.getMessage());
			throw new RuntimeException(e.getMessage());
		}
	}

	@McpResource(uri = "docs://documents/{docId}", mimeType = "text/plain")
	public String fetchDoc(McpSyncRequestContext ctx, String docId) {
        ctx.info(String.format("Был принят запрос на чтение документа %s", docId));
        Path docPath = Paths.get(getDocPath(docId));

        try {
            if (!Files.exists(docPath)) {
                throw new IllegalArgumentException("Документ с id " + docId + " не был найден");
            }
            String content = Files.readString(docPath);
            ctx.info(String.format("Документ %s успешно прочитан", docId));
            return content;
        } catch (IOException e) {
            String errorMessage = "I/O Ошибка при чтении документа " + docId;
            ctx.error(errorMessage);
            throw new RuntimeException(errorMessage, e);
        } catch (Exception e) {
            ctx.error(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
	}

	@McpPrompt(name = "format", description = "Переписывает содержимое документа в формате Markdown.")
	public PromptMessage format(McpSyncRequestContext ctx, @McpArg(name = "docId", required = true) String docId) {

        ctx.info(String.format("Был принят запрос на форматирование документа %s", docId));
        try {
	        var prompt = """
			Твоя цель - переформатировать документ, чтобы он был написан с использованием синтаксиса markdown.

			Id документа, который нужно переформатировать:
			<document_id>
			%s
			</document_id>

			Добавь заголовки, маркированные списки, таблицы и т.д. по необходимости. Не стесняйся добавлять дополнительный текст, но не изменяй смысл отчета.
			Используй инструмент 'edit_document' для редактирования документа. После редактирования документа ответь финальной версией документа. Не объясняй свои изменения.
			""".formatted(docId);			

			ctx.info(String.format("Документ %s успешно отформатирован", docId));
			return new PromptMessage(Role.USER, new TextContent(prompt));
        } catch (Exception e) {
            ctx.error(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
	}




}
