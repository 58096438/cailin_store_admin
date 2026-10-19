package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.PaymentMethod
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Cailin Store", appName)
  }

  @Test
  fun `verify payment methods resolution`() {
    assertEquals(PaymentMethod.EFECTIVO, PaymentMethod.fromCode("EFECTIVO"))
    assertEquals(PaymentMethod.TRANSFERENCIA, PaymentMethod.fromCode("TRANSFER"))
    assertEquals(PaymentMethod.TARJETA, PaymentMethod.fromCode("CARD"))
    assertEquals(PaymentMethod.CREDITO, PaymentMethod.fromCode("CREDIT"))
  }
}
