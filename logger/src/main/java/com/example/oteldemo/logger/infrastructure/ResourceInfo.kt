package com.example.oteldemo.logger.infrastructure

import android.content.Context
import android.os.Build
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.semconv.ServiceAttributes
import java.util.UUID

/**
 * FMT-02: Logger oluşturulurken bir kez hesaplanan resource attribute'ları.
 */
internal class ResourceInfo private constructor(val attributes: Attributes) {

    companion object {
        fun build(context: Context, serviceName: String, environment: String): ResourceInfo {
            val pkg = context.packageName
            val versionName = runCatching {
                context.packageManager.getPackageInfo(pkg, 0).versionName
            }.getOrNull() ?: "unknown"

            val attrs = Attributes.builder()
                .put(ServiceAttributes.SERVICE_NAME, serviceName)
                .put("deployment.environment", environment)
                .put("app.version", versionName)
                .put("app.package", pkg)
                .put("device.manufacturer", Build.MANUFACTURER)
                .put("device.model", Build.MODEL)
                .put("os.name", "Android")
                .put("os.version", Build.VERSION.RELEASE)
                .put("app.session.id", UUID.randomUUID().toString())
                .put("platform", "android")
                .build()

            return ResourceInfo(attrs)
        }
    }
}
