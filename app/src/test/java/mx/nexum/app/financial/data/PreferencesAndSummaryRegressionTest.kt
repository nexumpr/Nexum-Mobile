package mx.nexum.app.financial.data

import com.google.gson.Gson
import mx.nexum.app.financial.data.dto.FinancialSummaryResponse
import mx.nexum.app.financial.data.dto.PreferencesDto
import mx.nexum.app.financial.data.dto.UserProfileDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PreferencesAndSummaryRegressionTest {

    private val gson = Gson()

    @Test
    fun localeFromOleadaOneIsResolvedWithoutLanguageField() {
        val json = """{"locale":"es-MX","timezone":"America/Mexico_City","version":1}"""

        val preferences = gson.fromJson(json, PreferencesDto::class.java)

        assertEquals("es-MX", preferences.resolvedLanguage("es"))
        assertEquals("America/Mexico_City", preferences.resolvedTimezone("UTC"))
    }

    @Test
    fun missingFieldsKeepDefaultsInsteadOfThrowing() {
        val preferences = gson.fromJson("""{"locale":"en-US"}""", PreferencesDto::class.java)

        assertEquals("en-US", preferences.resolvedLanguage("es"))
        assertEquals("UTC", preferences.resolvedTimezone("UTC"))
        assertNull(preferences.version)
    }

    @Test
    fun legacyLanguageFieldIsStillAccepted() {
        val preferences = gson.fromJson("""{"language":"en","timezone":"UTC"}""", PreferencesDto::class.java)

        assertEquals("en", preferences.resolvedLanguage("es"))
    }

    @Test
    fun financialSummaryAcceptsDocumentedE026FieldNames() {
        val json = """{
            "incomeEquivalent":"12000.00",
            "recurringFixedEstimate":"6500.00",
            "oneTimeFixed":"0.00",
            "antExpenses":"350.00",
            "goalReservations":"200.00",
            "availableEstimate":"4950.00",
            "currency":"MXN"
        }"""

        val summary = gson.fromJson(json, FinancialSummaryResponse::class.java)

        assertEquals(12000.0, summary.income, 0.0)
        assertEquals(6500.0, summary.fixedExpenses, 0.0)
        assertEquals(350.0, summary.variableExpenses, 0.0)
        assertEquals(200.0, summary.savings, 0.0)
        assertEquals(4950.0, summary.available, 0.0)
        assertEquals("MXN", summary.currency)
    }

    @Test
    fun userProfileToleratesLegacyNameAndMissingFields() {
        val legacy = gson.fromJson(
            """{"id":"u1","email":"test@example.test","name":"Usuario","emailVerified":true}""",
            UserProfileDto::class.java
        )
        val empty = gson.fromJson("{}", UserProfileDto::class.java)

        assertEquals("Usuario", legacy.displayName)
        assertTrue(legacy.emailVerified == true)
        assertNull(empty.displayName)
        assertNull(empty.emailVerified)
    }
}
