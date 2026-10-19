package com.example.data.model

enum class PaymentMethod(val displayName: String, val code: String) {
  EFECTIVO("Efectivo", "CASH"),
  TRANSFERENCIA("Transferencia", "TRANSFER"),
  TARJETA("Tarjeta Débito/Crédito", "CARD"),
  CREDITO("Crédito Tienda", "CREDIT");

  companion object {
    fun fromCode(code: String): PaymentMethod {
      return entries.find { it.name.equals(code, ignoreCase = true) || it.code.equals(code, ignoreCase = true) }
        ?: EFECTIVO
    }
  }
}

enum class ProductCategory(val displayName: String) {
  TODOS("Todos"),
  ARETES("Aretes"),
  COLLARES("Collares"),
  PULSERAS("Pulseras"),
  ANILLOS("Anillos"),
  CABELLO("Cabello y Broches"),
  BOLSOS("Bolsos y Carteras")
}

enum class CreditStatus(val displayName: String) {
  AL_CORRIENTE("Sin mora"),
  POR_VENCER("Por vencer"),
  VENCIDO("Vencido"),
  LIQUIDADO("Liquidado")
}

data class DailySummary(
  val totalSalesAmount: Double,
  val salesCount: Int,
  val cashAmount: Double,
  val transferAmount: Double,
  val cardAmount: Double,
  val creditAmount: Double,
  val averageTicket: Double
)
