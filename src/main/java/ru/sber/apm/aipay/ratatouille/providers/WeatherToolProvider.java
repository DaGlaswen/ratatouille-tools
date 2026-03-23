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

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;


@Service
public class WeatherToolProvider {

	private static final Logger logger = org.slf4j.LoggerFactory.getLogger(WeatherToolProvider.class);

	private final RestClient restClient;

	public WeatherToolProvider() {
		this.restClient = RestClient.create();
	}

	public record WeatherResponse(Current current) {
		public record Current(LocalDateTime time, int interval, double temperature_2m) {
		}
	}

	@McpTool(description = "Получить температуру (в градусах Цельсия) для определенного местоположения")
	public WeatherResponse getTemperature(@McpToolParam(description = "Широта местоположения") double latitude,
			@McpToolParam(description = "Долгота местоположения") double longitude,
			@McpToolParam(description = "Название города") String city) {

		WeatherResponse response = restClient
				.get()
				.uri("https://api.open-meteo.com/v1/forecast?latitude={latitude}&longitude={longitude}&current=temperature_2m",
						latitude, longitude)
				.retrieve()
				.body(WeatherResponse.class);

		logger.info("Проверить температуру для {}. Широта: {}, Долгота: {}. Температура: {}", city, latitude, longitude,
				response.current);

		return response;
	}
	
}
