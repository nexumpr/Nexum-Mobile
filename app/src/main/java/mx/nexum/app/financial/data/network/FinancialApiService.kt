package mx.nexum.app.financial.data.network

import mx.nexum.app.financial.data.dto.BootstrapResponse
import mx.nexum.app.financial.data.dto.CategoryDto
import mx.nexum.app.financial.data.dto.CreateMovementRequest
import mx.nexum.app.financial.data.dto.FinancialSummaryResponse
import mx.nexum.app.financial.data.dto.FrequencyDto
import mx.nexum.app.financial.data.dto.IncomeDto
import mx.nexum.app.financial.data.dto.MovementDto
import mx.nexum.app.financial.data.dto.UpdateIncomeRequest
import mx.nexum.app.financial.data.dto.UpdateMovementRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
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

    @GET("movements")
    suspend fun getMovements(
        @Header("Authorization") authorization: String,
        @Query("page") page: Int? = null,
        @Query("limit") limit: Int? = null
    ): List<MovementDto>

    @POST("movements")
    suspend fun createMovement(
        @Header("Authorization") authorization: String,
        @Body request: CreateMovementRequest
    ): MovementDto

    @GET("movements/{movementId}")
    suspend fun getMovementDetail(
        @Header("Authorization") authorization: String,
        @Path("movementId") movementId: String
    ): MovementDto

    @PATCH("movements/{movementId}")
    suspend fun updateMovement(
        @Header("Authorization") authorization: String,
        @Path("movementId") movementId: String,
        @Body request: UpdateMovementRequest
    ): MovementDto

    @DELETE("movements/{movementId}")
    suspend fun deleteMovement(
        @Header("Authorization") authorization: String,
        @Path("movementId") movementId: String,
        @Header("If-Match") ifMatch: String? = null
    )
}
