package com.example.data.local.dao

import com.example.data.local.entity.*
import kotlinx.coroutines.flow.Flow

interface ProductDao {
  fun getAllProducts(): Flow<List<ProductEntity>>
  suspend fun getProductById(id: Long): ProductEntity?
  suspend fun insertProduct(product: ProductEntity): Long
  suspend fun insertAll(products: List<ProductEntity>)
  suspend fun updateProduct(product: ProductEntity)
  suspend fun deleteProduct(product: ProductEntity)
}

interface SaleDao {
  fun getAllSales(): Flow<List<SaleEntity>>
  suspend fun insertSale(sale: SaleEntity): Long
  suspend fun insertAll(sales: List<SaleEntity>)
  suspend fun deleteSale(sale: SaleEntity)
}

interface CustomerCreditDao {
  fun getAllCredits(): Flow<List<CustomerCreditEntity>>
  suspend fun insertCredit(credit: CustomerCreditEntity): Long
  suspend fun insertAll(credits: List<CustomerCreditEntity>)
  suspend fun updateCredit(credit: CustomerCreditEntity)
  suspend fun deleteCredit(credit: CustomerCreditEntity)
}

interface StoreDeviceDao {
  fun getDeviceInfo(): Flow<StoreDeviceInfoEntity?>
  suspend fun insertOrUpdate(info: StoreDeviceInfoEntity)
}

interface CashShiftDao {
  fun getAllShifts(): Flow<List<CashShiftEntity>>
  suspend fun insertShift(shift: CashShiftEntity): Long
  suspend fun insertAll(shifts: List<CashShiftEntity>)
}

interface AppSettingsDao {
  fun getSettings(): Flow<AppSettingsEntity?>
  suspend fun insertOrUpdate(settings: AppSettingsEntity)
}
