package mx.nexum.app.financial.data.network

import mx.nexum.app.financial.data.dto.BootstrapResponse
import mx.nexum.app.financial.data.dto.CategoryDto
import mx.nexum.app.financial.data.dto.FinancialSummaryResponse
import mx.nexum.app.financial.data.dto.FrequencyDto
import mx.nexum.app.financial.data.dto.IncomeDto
import mx.nexum.app.financial.data.dto.UpdateIncomeRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
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

    @GET("users/me/income")
    suspend fun getIncome(
        @Header("Authorization") authorization: String
    ): IncomeDto

    @PATCH("users/me/income")
    suspend fun updateIncome(
        @Header("Authorization") authorization: String,
        @Body request: UpdateIncomeRequest
    ): IncomeDto

    @GET("summary")
    suspend fun getFinancialSummary(
        @Header("Authorization") authorization: String
    ): FinancialSummaryResponse
}
