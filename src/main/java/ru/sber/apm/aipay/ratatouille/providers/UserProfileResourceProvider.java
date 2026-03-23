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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceRequest;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceResult;
import io.modelcontextprotocol.spec.McpSchema.ResourceContents;
import io.modelcontextprotocol.spec.McpSchema.TextResourceContents;
import org.springframework.ai.mcp.annotation.McpResource;

import org.springframework.stereotype.Service;


@Service
public class UserProfileResourceProvider {
	
	private final Map<String, Map<String, String>> userProfiles = new HashMap<>();

	public UserProfileResourceProvider() {
		// Инициализировать с образцом данных
		Map<String, String> johnProfile = new HashMap<>();
		johnProfile.put("name", "John Smith");
		johnProfile.put("email", "john.smith@example.com");
		johnProfile.put("age", "32");
		johnProfile.put("location", "New York");

		Map<String, String> janeProfile = new HashMap<>();
		janeProfile.put("name", "Jane Doe");
		janeProfile.put("email", "jane.doe@example.com");
		janeProfile.put("age", "28");
		janeProfile.put("location", "London");

		Map<String, String> bobProfile = new HashMap<>();
		bobProfile.put("name", "Bob Johnson");
		bobProfile.put("email", "bob.johnson@example.com");
		bobProfile.put("age", "45");
		bobProfile.put("location", "Tokyo");

		Map<String, String> aliceProfile = new HashMap<>();
		aliceProfile.put("name", "Alice Brown");
		aliceProfile.put("email", "alice.brown@example.com");
		aliceProfile.put("age", "36");
		aliceProfile.put("location", "Sydney");

		userProfiles.put("john", johnProfile);
		userProfiles.put("jane", janeProfile);
		userProfiles.put("bob", bobProfile);
		userProfiles.put("alice", aliceProfile);
	}

	@McpResource(uri = "static://hello", name = "Статический ресурс", description = "Пример статического ресурса")
	public String staticResource() { 
		return "Hello World!";
	}


	/**
	 * Метод ресурса, который принимает параметр ReadResourceRequest и переменную URI.
	 */
	@McpResource(uri = "user-profile://{username}", name = "Профиль пользователя", description = "Предоставляет информацию профиля пользователя для конкретного пользователя")
	public ReadResourceResult getUserProfile(ReadResourceRequest request, String username) {
		String profileInfo = formatProfileInfo(userProfiles.getOrDefault(username.toLowerCase(), new HashMap<>()));

		return new ReadResourceResult(List.of(new TextResourceContents(request.uri(), "text/plain", profileInfo)));
	}

	/**
	 * Метод ресурса, который принимает переменные URI непосредственно в качестве параметров. Шаблон URI
	 * в аннотации определяет переменные, которые будут извлечены.
	 */
	@McpResource(uri = "user-profile://{username}", name = "Детали пользователя", description = "Предоставляет детали пользователя для конкретного пользователя с использованием переменных URI")
	public ReadResourceResult getUserDetails(String username) {
		String profileInfo = formatProfileInfo(userProfiles.getOrDefault(username.toLowerCase(), new HashMap<>()));

		return new ReadResourceResult(
				List.of(new TextResourceContents("user-profile://" + username, "text/plain", profileInfo)));
	}

	/**
	 * Метод ресурса, который принимает несколько переменных URI в качестве параметров.
	 */
	@McpResource(uri = "user-attribute://{username}/{attribute}", name = "Атрибут пользователя", description = "Предоставляет конкретный атрибут из профиля пользователя")
	public ReadResourceResult getUserAttribute(String username, String attribute) {
		Map<String, String> profile = userProfiles.getOrDefault(username.toLowerCase(), new HashMap<>());
		String attributeValue = profile.getOrDefault(attribute, "Атрибут не найден");

		return new ReadResourceResult(
				List.of(new TextResourceContents("user-attribute://" + username + "/" + attribute, "text/plain",
						username + "'s " + attribute + ": " + attributeValue)));
	}

	/**
	 * Метод ресурса, который принимает обмен и переменные URI.
	 */
	@McpResource(uri = "user-profile-exchange://{username}", name = "Профиль пользователя с обменом", description = "Предоставляет информацию профиля пользователя с контекстом обмена сервера")
	public ReadResourceResult getProfileWithExchange(McpSyncServerExchange exchange, String username) {
		String profileInfo = formatProfileInfo(userProfiles.getOrDefault(username.toLowerCase(), new HashMap<>()));

		return new ReadResourceResult(List.of(new TextResourceContents("user-profile-exchange://" + username,
				"text/plain", "Профиль с обменом для " + username + ": " + profileInfo)));
	}

	/**
	 * Метод ресурса, который принимает строковый параметр переменной URI.
	 */
	@McpResource(uri = "user-connections://{username}", name = "Соединения пользователя", description = "Предоставляет список соединений для конкретного пользователя")
	public List<String> getUserConnections(String username) {
		// Сгенерировать простой список соединений на основе имени пользователя
		return List.of(username + " связан с Alice", username + " связан с Bob",
				username + " связан с Charlie");
	}

	/**
	 * Метод ресурса, который принимает как McpSyncServerExchange, ReadResourceRequest,
	 * так и
	 * параметры переменной URI.
	 */
	@McpResource(uri = "user-notifications://{username}", name = "Уведомления пользователя", description = "Предоставляет уведомления для конкретного пользователя")
	public List<ResourceContents> getUserNotifications(ReadResourceRequest request, String username) {
		// Сгенерировать уведомления на основе имени пользователя
		String notifications = generateNotifications(username);

		return List.of(new TextResourceContents(request.uri(), "text/plain", notifications));
	}

	/**
	 * Метод ресурса, который возвращает одиночный ResourceContents с типом
	 * содержимого TEXT.
	 */
	@McpResource(uri = "user-status://{username}", name = "Статус пользователя", description = "Предоставляет текущий статус для конкретного пользователя")
	public ResourceContents getUserStatus(ReadResourceRequest request, String username) {
		// Сгенерировать простой статус на основе имени пользователя
		String status = generateUserStatus(username);

		return new TextResourceContents(request.uri(), "text/plain", status);
	}

	/**
	 * Метод ресурса, который возвращает одиночную строку с типом содержимого TEXT.
	 */
	@McpResource(uri = "user-location://{username}", name = "Местоположение пользователя", description = "Предоставляет текущее местоположение для конкретного пользователя")
	public String getUserLocation(String username) {
		Map<String, String> profile = userProfiles.getOrDefault(username.toLowerCase(), new HashMap<>());

		// Извлечь местоположение из данных профиля
		return profile.getOrDefault("location", "Местоположение недоступно");
	}

	/**
	 * Метод ресурса, который возвращает одиночную строку с типом содержимого BLOB. Это
	 * демонстрирует, как строка может быть обработана как двоичные данные.
	 */
	@McpResource(uri = "user-avatar://{username}", name = "Аватар пользователя", description = "Предоставляет изображение аватара, закодированное в base64, для конкретного пользователя", mimeType = "image/png")
	public String getUserAvatar(ReadResourceRequest request, String username) {
		// В реальной реализации это будет изображение в формате base64
		// В этом примере мы просто возвращаем строку-заполнитель
		return "base64-encoded-avatar-image-for-" + username;
	}

	private String formatProfileInfo(Map<String, String> profile) {
		if (profile.isEmpty()) {
			return "Профиль пользователя не найден";
		}

		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, String> entry : profile.entrySet()) {
			sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
		}
		return sb.toString().trim();
	}

	private String generateNotifications(String username) {
		// Простая логика для генерации уведомлений
		return "У вас 3 новых сообщения\n" + "2 человека посмотрели ваш профиль\n" + "У вас 1 новый запрос на соединение";
	}

	private String generateUserStatus(String username) {
		// Simple logic to generate a status
		if (username.equals("john")) {
			return "🟢 В сети";
		} else if (username.equals("jane")) {
			return "🟠 Отошел";
		} else if (username.equals("bob")) {
			return "⚪ Не в сети";
		} else if (username.equals("alice")) {
			return "🔴 Занят";
		} else {
			return "⚪ Не в сети";
		}
	}

}
