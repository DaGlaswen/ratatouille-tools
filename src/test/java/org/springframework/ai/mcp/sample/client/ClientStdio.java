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

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import io.modelcontextprotocol.json.McpJsonMapper;

/**
 * При использовании транспорта stdio сервер MCP автоматически запускается клиентом.
 * Но вы
 * должны сначала собрать jar-файл сервера:
 *
 * <pre>
 * ./mvnw clean install -DskipTests
 * </pre>
 */
public class ClientStdio {

	private static final Logger logger = LoggerFactory.getLogger(ClientStdio.class);

	public static void main(String[] args) {

		logger.info("{}", new File(".").getAbsolutePath());
		
		var stdioParams = ServerParameters.builder("java")
				.args("-Dspring.ai.mcp.server.stdio=true", "-Dspring.main.web-application-type=none",
						"-Dlogging.pattern.console=", "-jar",
						"model-context-protocol/mcp-annotations/mcp-annotations-server/target/mcp-annotations-server-0.0.1-SNAPSHOT.jar")
				.build();

		var transport = new StdioClientTransport(stdioParams, McpJsonMapper.createDefault());

		new SampleClient(transport).run();
	}

}