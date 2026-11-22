package mx.nexum.app.core.contract

import okhttp3.Headers
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.net.SocketTimeoutException
import javax.net.ssl.SSLException

data class RecordedRequest(
    val method: String,
    val path: String,
    val headers: Headers,
    val contentType: String?,
    val contentLength: Long = 0L,
    val body: String = ""
)

data class FixtureScenario(
    val statusCode: Int,
    val fixture: String? = null,
    val headers: Map<String, String> = emptyMap(),
    val timeout: Boolean = false,
    val sslFailure: Boolean = false,
    val mediaType: String = "application/json"
)

class FixtureHttpSimulator(private val scenarios: Map<String, FixtureScenario>) {
    val recorded = mutableListOf<RecordedRequest>()

    fun client(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain -> handle(chain.request()) }
        .build()

    fun retrofit(): Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client())
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private fun handle(request: Request): Response {
        val bodyBuffer = Buffer()
        request.body?.writeTo(bodyBuffer)
        recorded += RecordedRequest(
            method = request.method,
            path = request.url.encodedPath,
            headers = request.headers,
            contentType = request.body?.contentType()?.toString(),
            contentLength = request.body?.contentLength() ?: 0L,
            body = bodyBuffer.readUtf8()
        )

        val key = key(request.method, request.url.encodedPath)
        val scenario = scenarios[key]
            ?: throw IOException("Escenario sintético no disponible: $key")

        if (scenario.timeout) {
            throw SocketTimeoutException("Tiempo de espera sintético agotado")
        }

        if (scenario.sslFailure) {
            throw SSLException("TLS sintético inválido")
        }

        val headers = Headers.Builder().apply {
            scenario.headers.forEach { (name, value) -> add(name, value) }
        }.build()

        val body = scenario.fixture?.let { name ->
            fixture(name).toResponseBody(scenario.mediaType.toMediaType())
        }

        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(scenario.statusCode)
            .message("synthetic")
            .headers(headers)
            .body(body)
            .build()
    }

    companion object {
        const val BASE_URL = "https://contract.invalid/api/v1/"

        fun key(method: String, path: String): String = "$method $path"

        fun fixture(name: String): String = FixtureHttpSimulator::class.java.classLoader
            ?.getResourceAsStream("contract/$name")
            ?.bufferedReader()
            ?.use { it.readText() }
            ?: error("Fixture sintética no encontrada: $name")
    }
}
