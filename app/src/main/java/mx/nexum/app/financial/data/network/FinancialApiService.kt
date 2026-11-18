package mx.nexum.app.financial.data.network

import mx.nexum.app.financial.data.dto.BootstrapResponse
import mx.nexum.app.financial.data.dto.CategoryDto
import mx.nexum.app.financial.data.dto.FrequencyDto
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface FinancialApiService {

    @GET("catalogs/categories")
    suspend fun getCategories(
        @Query("locale") locale: String? = null
    ): List<CategoryDto>

    @GET("catalogs/frequencies")
    suspend fun getFrequencies(
        @Query("context") context: String? = null
    ): List<FrequencyDto>

    @GET("bootstrap")
    suspend fun getBootstrap(
        @Header("Authorization") authorization: String,
        @Query("include") include: String? = null
    ): BootstrapResponse
}
