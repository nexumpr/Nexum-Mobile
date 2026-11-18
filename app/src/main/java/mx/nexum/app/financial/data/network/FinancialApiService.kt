package mx.nexum.app.financial.data.network

import mx.nexum.app.financial.data.dto.BootstrapResponse
import mx.nexum.app.financial.data.dto.CategoryDto
import mx.nexum.app.financial.data.dto.CreateGoalRequest
import mx.nexum.app.financial.data.dto.CreateGoalResponse
import mx.nexum.app.financial.data.dto.CreateMovementRequest
import mx.nexum.app.financial.data.dto.CreateRecurringPaymentRequest
import mx.nexum.app.financial.data.dto.FinancialSummaryResponse
import mx.nexum.app.financial.data.dto.FrequencyDto
import mx.nexum.app.financial.data.dto.GoalDto
import mx.nexum.app.financial.data.dto.IncomeDto
import mx.nexum.app.financial.data.dto.MovementDto
import mx.nexum.app.financial.data.dto.RecordPaymentRequest
import mx.nexum.app.financial.data.dto.RecordPaymentResponse
import mx.nexum.app.financial.data.dto.RecurringPaymentDto
import mx.nexum.app.financial.data.dto.UpdateIncomeRequest
import mx.nexum.app.financial.data.dto.UpdateMovementRequest
import mx.nexum.app.financial.data.dto.UpdateRecurringPaymentRequest
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

    @GET("recurring-payments")
    suspend fun getRecurringPayments(
        @Header("Authorization") authorization: String,
        @Query("active") active: Boolean? = null,
        @Query("page") page: Int? = null,
        @Query("limit") limit: Int? = null
    ): List<RecurringPaymentDto>

    @POST("recurring-payments")
    suspend fun createRecurringPayment(
        @Header("Authorization") authorization: String,
        @Body request: CreateRecurringPaymentRequest
    ): RecurringPaymentDto

    @GET("recurring-payments/{recurringPaymentId}")
    suspend fun getRecurringPaymentDetail(
        @Header("Authorization") authorization: String,
        @Path("recurringPaymentId") recurringPaymentId: String
    ): RecurringPaymentDto

    @PATCH("recurring-payments/{recurringPaymentId}")
    suspend fun updateRecurringPayment(
        @Header("Authorization") authorization: String,
        @Path("recurringPaymentId") recurringPaymentId: String,
        @Body request: UpdateRecurringPaymentRequest
    ): RecurringPaymentDto

    @DELETE("recurring-payments/{recurringPaymentId}")
    suspend fun deleteRecurringPayment(
        @Header("Authorization") authorization: String,
        @Path("recurringPaymentId") recurringPaymentId: String
    )

    @POST("recurring-payments/{recurringPaymentId}/payments")
    suspend fun recordRecurringPaymentPayment(
        @Header("Authorization") authorization: String,
        @Path("recurringPaymentId") recurringPaymentId: String,
        @Body request: RecordPaymentRequest,
        @Header("Idempotency-Key") idempotencyKey: String? = null
    ): RecordPaymentResponse

    @GET("goals")
    suspend fun getGoals(
        @Header("Authorization") authorization: String,
        @Query("status") status: String? = null,
        @Query("page") page: Int? = null,
        @Query("limit") limit: Int? = null
    ): List<GoalDto>

    @POST("goals")
    suspend fun createGoal(
        @Header("Authorization") authorization: String,
        @Body request: CreateGoalRequest
    ): CreateGoalResponse

    @GET("goals/{goalId}")
    suspend fun getGoalDetail(
        @Header("Authorization") authorization: String,
        @Path("goalId") goalId: String
    ): GoalDto
}
