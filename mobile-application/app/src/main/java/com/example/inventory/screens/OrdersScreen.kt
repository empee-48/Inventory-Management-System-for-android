package com.example.inventory.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.inventory.data.OrderResponseDto
import com.example.inventory.screens.composable.orders.BatchesList
import com.example.inventory.screens.composable.orders.AddOrderScreen
import com.example.inventory.screens.composable.orders.EditOrderScreen
import com.example.inventory.screens.composable.orders.OrderDetailsScreen
import com.example.inventory.screens.composable.orders.OrdersList
import com.example.inventory.service.api.BatchApiService
import com.example.inventory.service.api.OrdersApiService
import com.example.inventory.service.api.ProductApiService
import com.example.inventory.service.api.SuppliersApiService

sealed class OrdersScreenState {
    object List : OrdersScreenState()
    data class Details(val order: OrderResponseDto) : OrdersScreenState()
    object Add : OrdersScreenState()
    data class Edit(val order: OrderResponseDto) : OrdersScreenState()
    object Batches : OrdersScreenState()
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun OrdersScreen(
    orderApiService: OrdersApiService,
    productApiService: ProductApiService,
    suppliersApiService: SuppliersApiService,
    batchesApiService: BatchApiService
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
                onViewBatches = {
                    screenState = OrdersScreenState.Batches
                },
                isRefreshing = isRefreshing,
                batchApiService = batchesApiService,
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
                orderApiService = orderApiService,
                batchApiService = batchesApiService
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
        is OrdersScreenState.Batches -> {
            BatchesList(
                batchesApiService = batchesApiService,
                onBack = { screenState = OrdersScreenState.List }
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun OrdersListScreen(
    orderApiService: OrdersApiService,
    batchApiService: BatchApiService,
    onOrderClick: (OrderResponseDto) -> Unit,
    onAddOrder: () -> Unit,
    onViewBatches: () -> Unit,
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit
) {
    var showBatches by remember { mutableStateOf(false) }
    var swipeProgress by remember { mutableStateOf(0f) }

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = if (showBatches) 1 else 0,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                text = { Text("Orders") },
                selected = !showBatches,
                onClick = { showBatches = false }
            )
            Tab(
                text = { Text("Batches") },
                selected = showBatches,
                onClick = { showBatches = true }
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(showBatches) {
                    detectHorizontalDragGestures { change, dragAmount ->
                        val absoluteDrag = Math.abs(dragAmount)

                        // Only trigger swipe if drag is significant enough
                        if (absoluteDrag > 50f) {
                            if (dragAmount > 0 && showBatches) {
                                // Swipe right - show Orders
                                showBatches = false
                            } else if (dragAmount < 0 && !showBatches) {
                                // Swipe left - show Batches
                                showBatches = true
                            }
                        }
                    }
                }
        ) {
            if (showBatches) {
                BatchesList(
                    batchesApiService = batchApiService,
                    onBack = { showBatches = false }
                )
            } else {
                OrdersListScreenContent(
                    orderApiService = orderApiService,
                    onOrderClick = onOrderClick,
                    onAddOrder = onAddOrder,
                    isRefreshing = isRefreshing,
                    onRefresh = onRefresh
                )
            }
        }
    }
}

// Alternative implementation with horizontal pager-like behavior:
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun OrdersListScreenAlternative(
    orderApiService: OrdersApiService,
    batchApiService: BatchApiService,
    onOrderClick: (OrderResponseDto) -> Unit,
    onAddOrder: () -> Unit,
    onViewBatches: () -> Unit,
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Orders", "Batches")

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier.fillMaxWidth()
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    text = { Text(title) },
                    selected = selectedTab == index,
                    onClick = { selectedTab = index }
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(selectedTab) {
                    detectHorizontalDragGestures { change, dragAmount ->
                        val absoluteDrag = Math.abs(dragAmount)

                        // Only trigger swipe if drag is significant enough
                        if (absoluteDrag > 50f) {
                            if (dragAmount > 0 && selectedTab == 1) {
                                // Swipe right - show Orders
                                selectedTab = 0
                            } else if (dragAmount < 0 && selectedTab == 0) {
                                // Swipe left - show Batches
                                selectedTab = 1
                            }
                        }
                    }
                }
        ) {
            when (selectedTab) {
                0 -> OrdersListScreenContent(
                    orderApiService = orderApiService,
                    onOrderClick = onOrderClick,
                    onAddOrder = onAddOrder,
                    isRefreshing = isRefreshing,
                    onRefresh = onRefresh
                )
                1 -> BatchesList(
                    batchesApiService = batchApiService,
                    onBack = { selectedTab = 0 }
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun OrdersListScreenContent(
    orderApiService: OrdersApiService,
    onOrderClick: (OrderResponseDto) -> Unit,
    onAddOrder: () -> Unit,
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
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