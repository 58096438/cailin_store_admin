package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.AppSettingsDao
import com.example.data.local.dao.CashShiftDao
import com.example.data.local.dao.CustomerCreditDao
import com.example.data.local.dao.ProductDao
import com.example.data.local.dao.SaleDao
import com.example.data.local.dao.StoreDeviceDao
import com.example.data.local.entity.AppSettingsEntity
import com.example.data.local.entity.CashShiftEntity
import com.example.data.local.entity.CustomerCreditEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.SaleEntity
import com.example.data.local.entity.StoreDeviceInfoEntity

@Database(
  entities = [
    ProductEntity::class,
    SaleEntity::class,
    CustomerCreditEntity::class,
    StoreDeviceInfoEntity::class,
    CashShiftEntity::class,
    AppSettingsEntity::class
  ],
  version = 7,
  exportSchema = false
)
abstract class CailingDatabase : RoomDatabase() {
  abstract fun productDao(): ProductDao
  abstract fun saleDao(): SaleDao
  abstract fun customerCreditDao(): CustomerCreditDao
  abstract fun storeDeviceDao(): StoreDeviceDao
  abstract fun cashShiftDao(): CashShiftDao
  abstract fun appSettingsDao(): AppSettingsDao

  companion object {
    @Volatile
    private var INSTANCE: CailingDatabase? = null

    fun getDatabase(context: Context): CailingDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          CailingDatabase::class.java,
          "cailing_store_admin.db"
        )
        .fallbackToDestructiveMigration()
        .build()
        INSTANCE = instance
        instance
      }
    }
  }
}
