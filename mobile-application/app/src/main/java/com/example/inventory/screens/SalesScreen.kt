package com.example.inventory.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.inventory.data.SalesResponseDto
import com.example.inventory.screens.composable.sales.*
import com.example.inventory.service.api.BatchApiService
import com.example.inventory.service.api.ProductApiService
import com.example.inventory.service.api.SalesApiService

sealed class SalesScreenState {
    object List : SalesScreenState()
    data class Details(val sale: SalesResponseDto) : SalesScreenState()
    object Add : SalesScreenState()
    data class Edit(val sale: SalesResponseDto) : SalesScreenState()
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SalesScreen(
    salesApiService: SalesApiService,
    productApiService: ProductApiService,
    batchApiService: BatchApiService
) {
    var screenState by remember { mutableStateOf<SalesScreenState>(SalesScreenState.List) }
    var isRefreshing by remember { mutableStateOf(false) }

    when (val currentState = screenState) {
        is SalesScreenState.List -> {
            SalesListScreen(
                salesApiService = salesApiService,
                onSaleClick = { sale ->
                    screenState = SalesScreenState.Details(sale)
                },
                onAddSale = {
                    screenState = SalesScreenState.Add
                },
                isRefreshing = isRefreshing,
                onRefresh = { isRefreshing = true }
            )
        }
        is SalesScreenState.Details -> {
            SaleDetailsScreen(
                sale = currentState.sale,
                onBack = { screenState = SalesScreenState.List },
                onEdit = { sale: SalesResponseDto ->
                    screenState = SalesScreenState.Edit(sale)
                },
                onDelete = { sale: SalesResponseDto ->
                    screenState = SalesScreenState.List
                },
                salesApiService = salesApiService
            )
        }
        is SalesScreenState.Add -> {
            AddSaleScreen(
                onBack = { screenState = SalesScreenState.List },
                salesApiService = salesApiService,
                productApiService = productApiService,
                batchApiService = batchApiService,
                onSave = {
                    screenState = SalesScreenState.List
                }
            )
        }
        is SalesScreenState.Edit -> {
            EditSaleScreen(
                sale = currentState.sale,
                onBack = { screenState = SalesScreenState.List },
                onSave = { saleId, saleCreateDto ->
                    screenState = SalesScreenState.List
                },
                salesApiService = salesApiService,
                productApiService = productApiService,
                batchApiService = batchApiService,
                onDeleteSale = {
                    screenState = SalesScreenState.List
                },
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SalesListScreen(
    salesApiService: SalesApiService,
    onSaleClick: (SalesResponseDto) -> Unit,
    onAddSale: () -> Unit,
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        SalesList(
            salesApiService = salesApiService,
            onSaleClick = onSaleClick,
            onRefresh = onRefresh,
            isRefreshing = isRefreshing
        )

        FloatingActionButton(
            onClick = onAddSale,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            containerColor = Color(0xFF4A90D6),
            contentColor = Color.White,
            shape = MaterialTheme.shapes.medium
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Sale",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}