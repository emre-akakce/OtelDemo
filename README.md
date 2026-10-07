# OtelDemo

Minimal Android app demonstrating a layered OpenTelemetry logger module that exports logs **and** traces via OTLP/HTTP to a local collector.

## Architecture

```
app/                                   Consumer. Only sees Logger / LoggerFactory / LoggerConfigurator.
└── MainActivity                       3 buttons: Root, Work, Error. Each starts a span + emits a log.

logger/                                Android library module.
├── domain/                            Pure data, no dependencies.
│   ├── LogLevel                       Trace…None + parse()
│   ├── LoggingConfig                  logLevel, notificationLevel, endpoint
│   └── LogEntry                       level, template, params, error, category, timestamp
├── application/                       Interfaces the app depends on.
│   ├── Logger                         trace / debug / information / warning / error / critical
│   ├── LoggerFactory                  create(category) → Logger
│   ├── LoggerConfigurator             start / setAppId / flush
│   └── TracerFactory                  get(category) → Tracer
└── infrastructure/                    Concrete OTel implementations (internal).
    ├── OtelLoggerFactory / OtelLogger
    ├── OtelTracerFactory
    ├── OtelLoggerConfigurator         Initializes SDK, drains pending logs
    ├── LoggerState                    Shared state across logger instances
    ├── PendingLogBuffer               Ring buffer (cap 200) for logs before start()
    ├── Masking                        Redacts cookie/email/phone/token → "***"
    ├── OtelSetup                      SdkLoggerProvider + SdkTracerProvider + batch processors + OTLP/HTTP
    ├── ResourceInfo                   service.name, app.version, device.*, app.session.id
    └── ConsoleSink                    android.util.Log, only when debug=true
```

## Call flow

```
Button.onClick
  → MainActivity.doError(...)
    → logger.error(...)                   [application.Logger]
      → OtelLogger.log(Error, ...)        [infrastructure]
        → level filter → masking → alert attribute
        → otelLogger.logRecordBuilder().emit()
          → BatchLogRecordProcessor
            → OtlpHttpLogRecordExporter
              → POST http://10.0.2.2:4318/v1/logs
                → otel-collector
                  → debug exporter (stdout) + Jaeger (traces)
```

The app layer never imports `io.opentelemetry.*`. Swapping OTel for Firebase/Sentry means writing one new adapter in `infrastructure/` and changing `LoggerModule.install()`.

## Running

1. Start the collector stack (sibling repo):
   ```
   cd ../OtelCollector && docker compose up -d
   ```
2. Run the Android app on the emulator (endpoint defaults to `http://10.0.2.2:4318` — emulator → host loopback).
3. Tap buttons. See results in:
   - **Logcat** filtered by `tag:MainActivity` — the console sink output.
   - **Collector stdout** — full log records with attributes: `docker logs -f otel-collector`
   - **Jaeger UI** — http://localhost:16686, service `android-oteldemo`, each tap is a span. Error spans show the recorded exception.

## Overriding the collector endpoint

For a physical device on your LAN, add to `local.properties`:
```
otel.endpoint=http://192.168.1.42:4318
```
Then rebuild.
