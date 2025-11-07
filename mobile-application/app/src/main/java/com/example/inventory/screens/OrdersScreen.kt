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
import com.example.inventory.data.OrderResponseDto
import com.example.inventory.screens.composable.orders.AddOrderScreen
import com.example.inventory.screens.composable.orders.EditOrderScreen
import com.example.inventory.screens.composable.orders.OrderDetailsScreen
import com.example.inventory.screens.composable.orders.OrdersList
import com.example.inventory.service.api.OrdersApiService
import com.example.inventory.service.api.ProductApiService
import com.example.inventory.service.api.SuppliersApiService

sealed class OrdersScreenState {
    object List : OrdersScreenState()
    data class Details(val order: OrderResponseDto) : OrdersScreenState()
    object Add : OrdersScreenState()
    data class Edit(val order: OrderResponseDto) : OrdersScreenState()
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun OrdersScreen(
    orderApiService: OrdersApiService,
    productApiService: ProductApiService,
    suppliersApiService: SuppliersApiService
) {
    var screenState by remember { mutableStateOf<OrdersScreenState>(OrdersScreenState.List) }
    var isRefreshing by remember { mutableStateOf(false) }

    when (val currentState = screenState) {
        is OrdersScreenState.List -> {
            OrdersListScreen(
                orderApiService = orderApiService,
                onOrderClick = { order ->
                    screenState = OrdersScreenState.Details(order)
                },
                onAddOrder = {
                    screenState = OrdersScreenState.Add
                },
                isRefreshing = isRefreshing,
                onRefresh = { isRefreshing = true }
            )
        }
        is OrdersScreenState.Details -> {
            OrderDetailsScreen(
                order = currentState.order,
                onBack = { screenState = OrdersScreenState.List },
                onEdit = { order: OrderResponseDto ->
                    screenState = OrdersScreenState.Edit(order)
                },
                onDelete = { order: OrderResponseDto ->
                    screenState = OrdersScreenState.List
                },
                orderApiService = orderApiService
            )
        }
        is OrdersScreenState.Add -> {
            AddOrderScreen(
                onBack = { screenState = OrdersScreenState.List },
                orderApiService = orderApiService,
                productApiService = productApiService,
                suppliersApiService = suppliersApiService,
                onSave = {
                    screenState = OrdersScreenState.List
                }
            )
        }
        is OrdersScreenState.Edit -> {
            EditOrderScreen(
                order = currentState.order,
                onBack = { screenState = OrdersScreenState.List },
                onSave = { orderId, orderCreateDto ->
                    screenState = OrdersScreenState.List
                },
                orderApiService = orderApiService,
                productApiService = productApiService,
                onDeleteOrder = {
                    screenState = OrdersScreenState.List
                },
            )
        }
    }
}

@Composable
fun OrdersListScreen(
    orderApiService: OrdersApiService,
    onOrderClick: (OrderResponseDto) -> Unit,
    onAddOrder: () -> Unit,
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        OrdersList(
            orderApiService = orderApiService,
            onOrderClick = onOrderClick,
            onRefresh = onRefresh,
            isRefreshing = isRefreshing
        )

        FloatingActionButton(
            onClick = onAddOrder,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            containerColor = Color(0xFF4A90D6),
            contentColor = Color.White,
            shape = MaterialTheme.shapes.medium
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Order",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}