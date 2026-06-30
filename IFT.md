# ИФТ-стенд

Подробная документация: [b2b-agent/IFT.md](https://github.com/yuulkht/b2b-agent/blob/main/IFT.md)

## ratatouille-tools в ИФТ

tools-ift — **host-процесс** (не docker), параллельно с прод-инстансом на :9096.

- **Порт**: `9196` (прод: 9096)
- **Crossover REST**: `https://ift.gate1.spaymentextra.ru/`
- **Spring-профиль**: `ift` (конфиг: `src/main/resources/application-ift.yml`)
- **PID-файл**: `~/ratatouille-debug/ift.pid`

## Запуск

```bash
~/ift.sh tools-start   # запустить (nohup, лог в ~/ratatouille-debug/ift.log)
~/ift.sh tools-stop    # остановить по PID

# вручную:
IFT_CROSSOVER_API_KEY=<key> \
java -jar target/ratatouille-tools-0.0.1-SNAPSHOT.jar \
     --spring.profiles.active=ift \
     --server.port=9196 \
     --crossover.api.base-url=https://ift.gate1.spaymentextra.ru/ \
     --crossover.api.apiKey=${IFT_CROSSOVER_API_KEY}
```

`IFT_CROSSOVER_API_KEY` уже прописан в окружении сервера `cloud-iya`.

Из контейнеров `ratatouille-net-ift` tools-ift недостижим напрямую — агенты ходят через edge:
`https://aiplatilka.ru/test/mcp`.
