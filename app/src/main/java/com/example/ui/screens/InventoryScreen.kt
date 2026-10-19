package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.ProductEntity
import com.example.data.model.ProductCategory
import com.example.ui.theme.CailingPrimary
import com.example.ui.theme.PastelBackground
import com.example.ui.theme.PastelBorder
import com.example.ui.theme.PastelBorderSoft
import com.example.ui.theme.PastelLavenderAccent
import com.example.ui.theme.PastelLavenderCard
import com.example.ui.theme.PastelMintAccent
import com.example.ui.theme.PastelMintBorder
import com.example.ui.theme.PastelMintCard
import com.example.ui.theme.PastelPeachAccent
import com.example.ui.theme.PastelPeachBorder
import com.example.ui.theme.PastelPeachCard
import com.example.ui.theme.PastelRoseAccent
import com.example.ui.theme.PastelRoseBorder
import com.example.ui.theme.PastelRoseCard
import com.example.ui.theme.PastelSkyAccent
import com.example.ui.theme.PastelSkyBorder
import com.example.ui.theme.PastelSkyCard
import com.example.ui.theme.PastelSurface
import com.example.ui.theme.PastelSurfaceVariant
import com.example.ui.theme.PastelTextPrimary
import com.example.ui.theme.PastelTextSecondary
import com.example.ui.theme.StockLow
import com.example.ui.theme.StockLowBg
import com.example.ui.theme.StockOptimal
import com.example.ui.theme.StockOptimalBg
import com.example.ui.theme.StockWarning
import com.example.ui.theme.StockWarningBg
import java.util.Locale

enum class StockFilterTab(val label: String) {
  TODOS("Todos"),
  OPTIMO("Existencia"),
  BAJO("Por Agotarse"),
  AGOTADO("Agotados")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
  products: List<ProductEntity>,
  selectedCategory: ProductCategory,
  searchQuery: String,
  onCategorySelected: (ProductCategory) -> Unit,
  onSearchQueryChanged: (String) -> Unit,
  modifier: Modifier = Modifier,
  currencySymbol: String = "C$"
) {
  val cur = currencySymbol.ifBlank { "C$" }
  var stockFilter by remember { mutableStateOf(StockFilterTab.TODOS) }
  var selectedProduct by remember { mutableStateOf<ProductEntity?>(null) }

  val totalProducts = products.size
  val totalStockUnits = products.sumOf { it.stock }
  val totalInventoryValue = products.sumOf { it.price * it.stock }

  // Filter products by stock and search query
  val filteredProducts = products.filter { product ->
    val matchesStock = when (stockFilter) {
      StockFilterTab.TODOS -> true
      StockFilterTab.OPTIMO -> product.stock > product.minStockAlert
      StockFilterTab.BAJO -> product.stock in 1..product.minStockAlert
      StockFilterTab.AGOTADO -> product.stock == 0
    }
    val matchesSearch = if (searchQuery.isBlank()) true else {
      product.name.contains(searchQuery, ignoreCase = true) ||
      product.sku.contains(searchQuery, ignoreCase = true) ||
      product.category.contains(searchQuery, ignoreCase = true)
    }
    matchesStock && matchesSearch
  }

  // Unified scrollable page: the whole screen scrolls together smoothly
  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(PastelBackground)
      .testTag("inventory_screen"),
    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    // 1. Header Card (Without the removed "7 Modelos" text)
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = PastelSurface),
        border = BorderStroke(1.2.dp, PastelBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f, fill = false)) {
              Text(
                text = "INVENTARIO Y STOCK",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Bold,
                  letterSpacing = 1.sp,
                  fontSize = 11.sp,
                  color = PastelSkyAccent
                )
              )
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = "Catálogo de Prendas y Existencias",
                style = MaterialTheme.typography.bodySmall.copy(
                  color = PastelTextSecondary,
                  fontWeight = FontWeight.Medium,
                  fontSize = 11.sp
                )
              )
            }

            Surface(
              shape = RoundedCornerShape(12.dp),
              color = PastelSkyCard,
              border = BorderStroke(1.dp, PastelSkyBorder)
            ) {
              Column(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalAlignment = Alignment.End
              ) {
                Text(
                  text = "Valor Tienda",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = PastelSkyAccent
                  )
                )
                Text(
                  text = "$cur ${String.format(Locale.US, "%,.2f", totalInventoryValue)}",
                  style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = PastelSkyAccent
                  )
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Stock quick filter chips
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            StockFilterTab.entries.forEach { tab ->
              val isSelected = stockFilter == tab
              val count = when (tab) {
                StockFilterTab.TODOS -> products.size
                StockFilterTab.OPTIMO -> products.count { it.stock > it.minStockAlert }
                StockFilterTab.BAJO -> products.count { it.stock in 1..it.minStockAlert }
                StockFilterTab.AGOTADO -> products.count { it.stock == 0 }
              }

              Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) CailingPrimary else PastelSurfaceVariant,
                border = BorderStroke(1.dp, if (isSelected) CailingPrimary else PastelBorderSoft),
                modifier = Modifier
                  .weight(1f)
                  .clickable { stockFilter = tab }
              ) {
                Column(
                  modifier = Modifier.padding(vertical = 6.dp),
                  horizontalAlignment = Alignment.CenterHorizontally
                ) {
                  Text(
                    text = tab.label,
                    style = MaterialTheme.typography.labelSmall.copy(
                      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                      fontSize = 10.sp,
                      color = if (isSelected) Color.White else PastelTextSecondary
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                  Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.bodySmall.copy(
                      fontWeight = FontWeight.Bold,
                      fontSize = 11.sp,
                      color = if (isSelected) Color.White else PastelTextPrimary
                    )
                  )
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          // Clean real-time audit note
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(10.dp))
              .background(PastelSurfaceVariant)
              .border(1.dp, PastelBorderSoft, RoundedCornerShape(10.dp))
              .padding(horizontal = 8.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.Lock,
              contentDescription = null,
              tint = PastelTextPrimary,
              modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Auditoría en tiempo real: El stock se descuenta automáticamente con cada venta.",
              style = MaterialTheme.typography.bodySmall.copy(
                color = PastelTextSecondary,
                fontWeight = FontWeight.Medium,
                fontSize = 10.sp
              ),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }
        }
      }
    }

    // 2. Search Bar
    item {
      OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchQueryChanged,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("inventory_search_bar"),
        placeholder = {
          Text("Buscar prenda o SKU...", color = PastelTextSecondary, fontSize = 13.sp)
        },
        leadingIcon = {
          Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Buscar",
            tint = PastelTextPrimary,
            modifier = Modifier.size(18.dp)
          )
        },
        trailingIcon = {
          if (searchQuery.isNotEmpty()) {
            IconButton(onClick = { onSearchQueryChanged("") }) {
              Icon(
                imageVector = Icons.Default.Clear,
                contentDescription = "Limpiar",
                tint = PastelTextPrimary,
                modifier = Modifier.size(18.dp)
              )
            }
          }
        },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        colors = TextFieldDefaults.colors(
          focusedContainerColor = PastelSurface,
          unfocusedContainerColor = PastelSurface,
          focusedTextColor = PastelTextPrimary,
          unfocusedTextColor = PastelTextPrimary,
          focusedIndicatorColor = CailingPrimary,
          unfocusedIndicatorColor = PastelBorder
        )
      )
    }

    // 3. Section Label
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 2.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Artículos en Almacén (${filteredProducts.size})",
          style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            letterSpacing = 1.sp,
            color = PastelTextSecondary
          )
        )
        Text(
          text = "Toca para ver ficha",
          style = MaterialTheme.typography.bodySmall.copy(
            color = PastelTextSecondary,
            fontSize = 11.sp
          )
        )
      }
    }

    // 4. Products List
    if (filteredProducts.isEmpty()) {
      item {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 36.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
              imageVector = Icons.Default.Inventory2,
              contentDescription = null,
              modifier = Modifier.size(44.dp),
              tint = PastelTextSecondary
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
              text = "No hay productos en esta vista",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = PastelTextPrimary
              )
            )
            Text(
              text = "Prueba cambiando el filtro o la búsqueda",
              style = MaterialTheme.typography.bodySmall.copy(
                color = PastelTextSecondary,
                fontSize = 12.sp
              )
            )
          }
        }
      }
    } else {
      items(filteredProducts, key = { it.id }) { product ->
        ProductStockCard(
          product = product,
          onClick = { selectedProduct = product },
          currencySymbol = cur,
          modifier = Modifier.animateItem()
        )
      }
    }

    // Bottom spacing for floating navigation bar
    item {
      Spacer(modifier = Modifier.height(84.dp))
    }
  }

  // Product Full Details Bottom Sheet
  selectedProduct?.let { product ->
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
      onDismissRequest = { selectedProduct = null },
      sheetState = sheetState,
      containerColor = PastelSurface,
      shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
      dragHandle = {
        Surface(
          modifier = Modifier
            .padding(vertical = 12.dp)
            .size(width = 44.dp, height = 4.dp),
          shape = CircleShape,
          color = PastelBorder
        ) {}
      }
    ) {
      ProductDetailSheetContent(
        product = product,
        currencySymbol = cur,
        onDismiss = { selectedProduct = null }
      )
    }
  }
}

/**
 * Product Card:
 * - NO icon next to product name (per user request)
 * - Distinct, harmonious color scheme matching stock level
 * - Spacious, clean Liquid Glass presentation
 */
@Composable
fun ProductStockCard(
  product: ProductEntity,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  currencySymbol: String = "C$"
) {
  val isOutOfStock = product.stock == 0
  val isLowStock = product.stock in 1..product.minStockAlert

  // Cohesive styling depending on stock level
  val (cardBorderColor, cardBackgroundColor, badgeTextColor, badgeBgColor, stockBadgeLabel) = when {
    isOutOfStock -> ColorGroup(
      borderColor = PastelRoseBorder,
      backgroundColor = PastelRoseCard,
      textColor = StockLow,
      badgeBg = StockLowBg,
      label = "Agotado (0 pzas)"
    )
    isLowStock -> ColorGroup(
      borderColor = PastelPeachBorder,
      backgroundColor = PastelPeachCard,
      textColor = StockWarning,
      badgeBg = StockWarningBg,
      label = "Por agotarse (${product.stock} pzas)"
    )
    else -> ColorGroup(
      borderColor = PastelMintBorder,
      backgroundColor = PastelMintCard,
      textColor = StockOptimal,
      badgeBg = StockOptimalBg,
      label = "En existencia (${product.stock} pzas)"
    )
  }

  val profitMargin = if (product.price > 0) {
    ((product.price - product.costPrice) / product.price) * 100
  } else 0.0

  val profitAmount = (product.price - product.costPrice).coerceAtLeast(0.0)
  val stockRatio = (product.stock / 20f).coerceIn(0f, 1f)

  Card(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(18.dp))
      .clickable { onClick() },
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
    border = BorderStroke(1.2.dp, cardBorderColor),
    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp)
    ) {
      // Top row: Product Name + Stock Badge (NO icon)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f, fill = false)) {
          Text(
            text = product.name,
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp,
              color = PastelTextPrimary
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
          Spacer(modifier = Modifier.height(2.dp))
          Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
              shape = RoundedCornerShape(4.dp),
              color = PastelSurfaceVariant,
              border = BorderStroke(0.8.dp, PastelBorderSoft)
            ) {
              Text(
                text = product.sku,
                style = MaterialTheme.typography.labelSmall.copy(
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  color = PastelSkyAccent
                ),
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
              )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = product.category,
              style = MaterialTheme.typography.bodySmall.copy(
                color = PastelTextSecondary,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp
              ),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }
        }

        Surface(
          shape = RoundedCornerShape(8.dp),
          color = badgeBgColor,
          border = BorderStroke(1.dp, badgeTextColor.copy(alpha = 0.4f))
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            if (isLowStock) {
              Icon(
                imageVector = Icons.Default.WarningAmber,
                contentDescription = null,
                tint = badgeTextColor,
                modifier = Modifier.size(12.dp)
              )
              Spacer(modifier = Modifier.width(3.dp))
            }
            Text(
              text = stockBadgeLabel,
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = badgeTextColor
              )
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Visual Stock Level Bar
      LinearProgressIndicator(
        progress = { stockRatio },
        modifier = Modifier
          .fillMaxWidth()
          .height(4.dp)
          .clip(RoundedCornerShape(2.dp)),
        color = badgeTextColor,
        trackColor = PastelSurfaceVariant
      )

      Spacer(modifier = Modifier.height(10.dp))

      // Bottom Metrics: Clean, uncluttered price and margin presentation
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(10.dp))
          .background(PastelSurface)
          .border(1.dp, PastelBorderSoft, RoundedCornerShape(10.dp))
          .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Precio de venta",
            style = MaterialTheme.typography.labelSmall.copy(
              color = PastelTextSecondary,
              fontSize = 10.sp
            )
          )
          Text(
            text = "$currencySymbol ${String.format(Locale.US, "%,.2f", product.price)}",
            style = MaterialTheme.typography.bodyLarge.copy(
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
              color = PastelTextPrimary
            )
          )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text(
            text = "Precio Costo",
            style = MaterialTheme.typography.labelSmall.copy(
              color = PastelTextSecondary,
              fontSize = 10.sp
            )
          )
          Text(
            text = "$currencySymbol ${String.format(Locale.US, "%,.2f", product.costPrice)}",
            style = MaterialTheme.typography.bodyMedium.copy(
              fontWeight = FontWeight.SemiBold,
              fontSize = 13.sp,
              color = PastelTextSecondary
            )
          )
        }

        Column(horizontalAlignment = Alignment.End) {
          Text(
            text = "Margen utilidad",
            style = MaterialTheme.typography.labelSmall.copy(
              color = PastelTextSecondary,
              fontSize = 10.sp
            )
          )
          Text(
            text = "+${String.format(Locale.US, "%.0f", profitMargin)}% (+$${String.format(Locale.US, "%.1f", profitAmount)})",
            style = MaterialTheme.typography.bodyMedium.copy(
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp,
              color = StockOptimal
            )
          )
        }
      }
    }
  }
}

/**
 * Product Detail Modal Bottom Sheet
 */
@Composable
fun ProductDetailSheetContent(
  product: ProductEntity,
  currencySymbol: String = "C$",
  onDismiss: () -> Unit
) {
  val isOutOfStock = product.stock == 0
  val isLowStock = product.stock in 1..product.minStockAlert

  val (statusLabel, statusColor, statusBg) = when {
    isOutOfStock -> Triple("Agotado", StockLow, StockLowBg)
    isLowStock -> Triple("Por agotarse", StockWarning, StockWarningBg)
    else -> Triple("En existencia", StockOptimal, StockOptimalBg)
  }

  val profitMargin = if (product.price > 0) {
    ((product.price - product.costPrice) / product.price) * 100
  } else 0.0

  val profitPerUnit = (product.price - product.costPrice).coerceAtLeast(0.0)
  val totalStockValue = product.price * product.stock

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 20.dp, vertical = 8.dp)
      .padding(bottom = 36.dp)
  ) {
    // Header
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(modifier = Modifier.weight(1f, fill = false)) {
        Text(
          text = product.name,
          style = MaterialTheme.typography.headlineSmall.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 19.sp,
            color = PastelTextPrimary
          ),
          maxLines = 2,
          overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = "SKU: ${product.sku} • ${product.category}",
          style = MaterialTheme.typography.bodySmall.copy(
            color = PastelTextSecondary,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp
          )
        )
      }

      Surface(
        shape = RoundedCornerShape(10.dp),
        color = statusBg,
        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.5f))
      ) {
        Text(
          text = statusLabel,
          style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            color = statusColor,
            fontSize = 11.sp
          ),
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Section 1: Precios y Rentabilidad (Liquid Glass Card)
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = PastelSurface),
      border = BorderStroke(1.2.dp, PastelBorder),
      elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(28.dp)
              .clip(CircleShape)
              .background(PastelMintCard),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.MonetizationOn,
              contentDescription = null,
              tint = PastelMintAccent,
              modifier = Modifier.size(16.dp)
            )
          }
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Precios y Rentabilidad",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
              color = PastelTextPrimary
            )
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          ProductMetricBox(
            label = "Precio Venta",
            value = "$currencySymbol ${String.format(Locale.US, "%,.2f", product.price)}",
            tint = PastelTextPrimary,
            modifier = Modifier.weight(1f)
          )
          ProductMetricBox(
            label = "Precio Costo",
            value = "$currencySymbol ${String.format(Locale.US, "%,.2f", product.costPrice)}",
            tint = PastelTextSecondary,
            modifier = Modifier.weight(1f)
          )
          ProductMetricBox(
            label = "Ganancia Unidad",
            value = "+$currencySymbol ${String.format(Locale.US, "%,.2f", profitPerUnit)}",
            tint = StockOptimal,
            modifier = Modifier.weight(1f)
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Margen de Ganancia (Espacioso y con diseño Liquid Glass)
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = PastelSurfaceVariant,
          border = BorderStroke(1.dp, PastelBorderSoft),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(32.dp)
                  .clip(CircleShape)
                  .background(StockOptimalBg),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.MonetizationOn,
                  contentDescription = null,
                  tint = StockOptimal,
                  modifier = Modifier.size(18.dp)
                )
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = "Margen de Ganancia",
                  style = MaterialTheme.typography.bodyMedium.copy(
                    color = PastelTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                  )
                )
                Text(
                  text = "Rentabilidad sobre costo",
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = PastelTextSecondary,
                    fontSize = 10.sp
                  )
                )
              }
            }

            Surface(
              shape = RoundedCornerShape(10.dp),
              color = StockOptimalBg,
              border = BorderStroke(1.dp, StockOptimal.copy(alpha = 0.35f))
            ) {
              Text(
                text = "+${String.format(Locale.US, "%.1f", profitMargin)}%",
                style = MaterialTheme.typography.titleMedium.copy(
                  color = StockOptimal,
                  fontWeight = FontWeight.Bold,
                  fontSize = 13.sp
                ),
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Valor Total en Stock (Separado con respiro visual amplio)
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = PastelSurfaceVariant,
          border = BorderStroke(1.dp, PastelBorderSoft),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(32.dp)
                  .clip(CircleShape)
                  .background(PastelSkyCard),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Inventory2,
                  contentDescription = null,
                  tint = PastelSkyAccent,
                  modifier = Modifier.size(18.dp)
                )
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = "Valor Total en Stock",
                  style = MaterialTheme.typography.bodyMedium.copy(
                    color = PastelTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                  )
                )
                Text(
                  text = "${product.stock} unidades en existencia",
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = PastelTextSecondary,
                    fontSize = 10.sp
                  )
                )
              }
            }

            Text(
              text = "$currencySymbol ${String.format(Locale.US, "%,.2f", totalStockValue)}",
              style = MaterialTheme.typography.titleMedium.copy(
                color = PastelSkyAccent,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
              )
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Section 2: Existencias y Almacén (Liquid Glass Card)
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = PastelSurface),
      border = BorderStroke(1.2.dp, PastelBorder),
      elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(28.dp)
              .clip(CircleShape)
              .background(PastelSkyCard),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Inventory2,
              contentDescription = null,
              tint = PastelSkyAccent,
              modifier = Modifier.size(16.dp)
            )
          }
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Existencias y Almacén",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
              color = PastelTextPrimary
            )
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "Unidades en tienda",
              style = MaterialTheme.typography.bodySmall.copy(color = PastelTextSecondary, fontSize = 11.sp)
            )
            Text(
              text = "${product.stock} piezas",
              style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = PastelTextPrimary
              )
            )
          }

          Column(horizontalAlignment = Alignment.End) {
            Text(
              text = "Mínimo alerta",
              style = MaterialTheme.typography.bodySmall.copy(color = PastelTextSecondary, fontSize = 11.sp)
            )
            Text(
              text = "${product.minStockAlert} piezas",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = PastelTextSecondary
              )
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        val stockRatio = (product.stock / 20f).coerceIn(0f, 1f)
        LinearProgressIndicator(
          progress = { stockRatio },
          modifier = Modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(3.dp)),
          color = statusColor,
          trackColor = PastelSurfaceVariant
        )
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Section 3: Ficha Técnica (Liquid Glass Card)
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = PastelSurface),
      border = BorderStroke(1.2.dp, PastelBorder),
      elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(28.dp)
              .clip(CircleShape)
              .background(PastelLavenderCard),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Info,
              contentDescription = null,
              tint = PastelLavenderAccent,
              modifier = Modifier.size(16.dp)
            )
          }
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Ficha Técnica",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
              color = PastelTextPrimary
            )
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        ProductDetailRow(label = "Categoría", value = product.category)
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = PastelBorderSoft)
        ProductDetailRow(label = "Código SKU", value = product.sku)
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = PastelBorderSoft)
        ProductDetailRow(label = "ID de Catálogo", value = "#${product.id.toString().padStart(4, '0')}")
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = PastelBorderSoft)
        ProductDetailRow(
          label = "Descripción",
          value = product.description.ifBlank { "Prenda boutique Cailin de alta calidad, corte contemporáneo y acabados finos." }
        )
      }
    }

    Spacer(modifier = Modifier.height(18.dp))

    OutlinedButton(
      onClick = onDismiss,
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(14.dp),
      border = BorderStroke(1.dp, PastelBorder)
    ) {
      Text("Cerrar Ficha del Producto", color = PastelTextPrimary, fontWeight = FontWeight.SemiBold)
    }
  }
}

@Composable
fun ProductDetailRow(label: String, value: String) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.bodySmall.copy(color = PastelTextSecondary, fontSize = 12.sp)
    )
    Text(
      text = value,
      style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = PastelTextPrimary, fontSize = 13.sp),
      maxLines = 2,
      overflow = TextOverflow.Ellipsis
    )
  }
}

@Composable
fun ProductMetricBox(
  label: String,
  value: String,
  tint: Color,
  modifier: Modifier = Modifier
) {
  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(12.dp),
    color = PastelSurfaceVariant,
    border = BorderStroke(1.dp, PastelBorderSoft)
  ) {
    Column(
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(
        text = label,
        style = MaterialTheme.typography.bodySmall.copy(
          color = PastelTextSecondary,
          fontSize = 10.sp,
          fontWeight = FontWeight.Medium
        ),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = value,
        style = MaterialTheme.typography.bodyMedium.copy(
          fontWeight = FontWeight.Bold,
          color = tint,
          fontSize = 13.sp
        ),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }
  }
}

private data class ColorGroup(
  val borderColor: Color,
  val backgroundColor: Color,
  val textColor: Color,
  val badgeBg: Color,
  val label: String
)
