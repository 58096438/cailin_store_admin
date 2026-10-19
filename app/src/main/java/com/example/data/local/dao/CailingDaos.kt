package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.AppSettingsEntity
import com.example.data.local.entity.CashShiftEntity
import com.example.data.local.entity.CustomerCreditEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.SaleEntity
import com.example.data.local.entity.StoreDeviceInfoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
  @Query("SELECT * FROM products ORDER BY name ASC")
  fun getAllProducts(): Flow<List<ProductEntity>>

  @Query("SELECT * FROM products ORDER BY name ASC")
  suspend fun getAllProductsDirect(): List<ProductEntity>

  @Query("SELECT * FROM products WHERE stock <= minStockAlert ORDER BY stock ASC")
  fun getLowStockProducts(): Flow<List<ProductEntity>>

  @Query("SELECT * FROM products WHERE id = :id")
  suspend fun getProductById(id: Long): ProductEntity?

  @Query("SELECT * FROM products WHERE category = :category ORDER BY name ASC")
  fun getProductsByCategory(category: String): Flow<List<ProductEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(products: List<ProductEntity>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertProduct(product: ProductEntity)

  @Query("UPDATE products SET stock = :newStock, lastSoldTimestamp = :soldTime WHERE id = :productId")
  suspend fun updateStock(productId: Long, newStock: Int, soldTime: Long)

  @Query("SELECT COUNT(*) FROM products")
  suspend fun countProducts(): Int
}

@Dao
interface SaleDao {
  @Query("SELECT * FROM sales ORDER BY timestamp DESC")
  fun getAllSales(): Flow<List<SaleEntity>>

  @Query("SELECT * FROM sales ORDER BY timestamp DESC")
  suspend fun getAllSalesDirect(): List<SaleEntity>

  @Query("SELECT * FROM sales WHERE timestamp >= :startOfDayTimestamp ORDER BY timestamp DESC")
  fun getTodaySales(startOfDayTimestamp: Long): Flow<List<SaleEntity>>

  @Query("SELECT * FROM sales WHERE timestamp >= :startOfDayTimestamp ORDER BY timestamp DESC")
  suspend fun getTodaySalesDirect(startOfDayTimestamp: Long): List<SaleEntity>

  @Query("SELECT * FROM sales WHERE isRiskySale = 1 ORDER BY timestamp DESC")
  fun getRiskySales(): Flow<List<SaleEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertSale(sale: SaleEntity): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertSales(sales: List<SaleEntity>)

  @Query("SELECT COUNT(*) FROM sales")
  suspend fun countSales(): Int
}

@Dao
interface CustomerCreditDao {
  @Query("SELECT * FROM customer_credits ORDER BY dueDateTimestamp ASC")
  fun getAllCredits(): Flow<List<CustomerCreditEntity>>

  @Query("SELECT * FROM customer_credits ORDER BY dueDateTimestamp ASC")
  suspend fun getAllCreditsDirect(): List<CustomerCreditEntity>

  @Query("SELECT * FROM customer_credits WHERE status != 'LIQUIDADO' ORDER BY dueDateTimestamp ASC")
  fun getPendingCredits(): Flow<List<CustomerCreditEntity>>

  @Query("SELECT * FROM customer_credits WHERE status = 'VENCIDO' OR status = 'POR_VENCER' ORDER BY dueDateTimestamp ASC")
  fun getAlertCredits(): Flow<List<CustomerCreditEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertCredit(credit: CustomerCreditEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertCredits(credits: List<CustomerCreditEntity>)

  @Update
  suspend fun updateCredit(credit: CustomerCreditEntity)

  @Query("SELECT COUNT(*) FROM customer_credits")
  suspend fun countCredits(): Int
}

@Dao
interface StoreDeviceDao {
  @Query("SELECT * FROM store_device_info WHERE id = 1 LIMIT 1")
  fun getDeviceInfo(): Flow<StoreDeviceInfoEntity?>

  @Query("SELECT * FROM store_device_info WHERE id = 1 LIMIT 1")
  suspend fun getDeviceInfoDirect(): StoreDeviceInfoEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun setDeviceInfo(info: StoreDeviceInfoEntity)

  @Update
  suspend fun updateDeviceInfo(info: StoreDeviceInfoEntity)
}

@Dao
interface CashShiftDao {
  @Query("SELECT * FROM cash_shifts ORDER BY closedAtTimestamp DESC")
  fun getAllShifts(): Flow<List<CashShiftEntity>>

  @Query("SELECT * FROM cash_shifts ORDER BY closedAtTimestamp DESC")
  suspend fun getAllShiftsDirect(): List<CashShiftEntity>

  @Query("SELECT * FROM cash_shifts ORDER BY closedAtTimestamp DESC LIMIT 1")
  fun getLatestShift(): Flow<CashShiftEntity?>

  @Query("SELECT * FROM cash_shifts WHERE status = 'FALTANTE' ORDER BY closedAtTimestamp DESC")
  fun getDiscrepantShifts(): Flow<List<CashShiftEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertShift(shift: CashShiftEntity): Long

  @Query("SELECT COUNT(*) FROM cash_shifts")
  suspend fun countShifts(): Int
}

@Dao
interface AppSettingsDao {
  @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
  fun getAppSettings(): Flow<AppSettingsEntity?>

  @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
  suspend fun getAppSettingsDirect(): AppSettingsEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertOrUpdate(settings: AppSettingsEntity)

  @Query("UPDATE app_settings SET themeMode = :themeMode WHERE id = 1")
  suspend fun updateThemeMode(themeMode: String)

  @Query("UPDATE app_settings SET accentColorName = :accentColor WHERE id = 1")
  suspend fun updateAccentColor(accentColor: String)

  @Query("UPDATE app_settings SET notificationsEnabled = :enabled WHERE id = 1")
  suspend fun updateNotificationsEnabled(enabled: Boolean)

  @Query("UPDATE app_settings SET alertRiskySales = :enabled, riskyDiscountThreshold = :threshold WHERE id = 1")
  suspend fun updateRiskySaleSettings(enabled: Boolean, threshold: Int)

  @Query("UPDATE app_settings SET currencySymbol = :symbol, targetProfitMargin = :margin WHERE id = 1")
  suspend fun updateBusinessSettings(symbol: String, margin: Double)

  @Query("UPDATE app_settings SET currencySymbol = :symbol WHERE id = 1")
  suspend fun updateCurrencySymbol(symbol: String)

  @Query("UPDATE app_settings SET dailySalesGoal = :goalAmount, dailyPiecesGoal = :goalPieces WHERE id = 1")
  suspend fun updateDailyGoals(goalAmount: Double, goalPieces: Int)
}
