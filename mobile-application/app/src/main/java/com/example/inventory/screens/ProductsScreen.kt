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
import com.example.inventory.data.CategoryResponseDto
import com.example.inventory.data.ProductResponseDto
import com.example.inventory.screens.composable.products.*
import com.example.inventory.service.api.CategoryApiService
import com.example.inventory.service.api.ProductApiService
import com.example.inventory.service.api.SalesApiService

sealed class ProductsScreenState {
    object List : ProductsScreenState()
    data class Details(val product: ProductResponseDto) : ProductsScreenState()
    object Add : ProductsScreenState()
    data class Edit(val product: ProductResponseDto) : ProductsScreenState()
    data class CategoryDetails(val category: CategoryResponseDto) : ProductsScreenState()
    object Categories : ProductsScreenState()
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ProductsScreen(
    productApiService: ProductApiService,
    categoryApiService: CategoryApiService,
    salesApiService: SalesApiService
) {
    var screenState by remember { mutableStateOf<ProductsScreenState>(ProductsScreenState.List) }
    var isRefreshing by remember { mutableStateOf(false) }

    when (val currentState = screenState) {
        is ProductsScreenState.List -> {
            ProductsListScreen(
                productApiService = productApiService,
                categoryApiService = categoryApiService,
                onProductClick = { product ->
                    screenState = ProductsScreenState.Details(product)
                },
                onAddProduct = {
                    screenState = ProductsScreenState.Add
                },
                onNavigateToCategories = {
                    screenState = ProductsScreenState.Categories
                },
                isRefreshing = isRefreshing,
                onRefresh = { isRefreshing = true }
            )
        }
        is ProductsScreenState.Details -> {
            ProductDetailsScreen(
                product = currentState.product,
                onBack = { screenState = ProductsScreenState.List },
                onEdit = { product: ProductResponseDto ->
                    screenState = ProductsScreenState.Edit(product)
                },
                onDelete = { product: ProductResponseDto ->
                    screenState = ProductsScreenState.List
                },
                productApiService = productApiService,
                salesApiService = salesApiService
            )
        }
        is ProductsScreenState.Add -> {
            AddProductScreen(
                onBack = { screenState = ProductsScreenState.List },
                categoryApiService = categoryApiService,
                productApiService = productApiService,
                onSave = {
                    screenState = ProductsScreenState.List
                }
            )
        }
        is ProductsScreenState.Edit -> {
            EditProductScreen(
                product = currentState.product,
                onBack = { screenState = ProductsScreenState.List },
                onSave = { productId, productCreateDto ->
                    screenState = ProductsScreenState.List
                },
                categoryApiService = categoryApiService,
                productApiService = productApiService
            )
        }
        is ProductsScreenState.Categories -> {
            CategoriesScreen(
                categoryApiService = categoryApiService,
                onBack = { screenState = ProductsScreenState.List },
                onCategoryClick = { category ->
                    screenState = ProductsScreenState.CategoryDetails(category)
                }
            )
        }
        is ProductsScreenState.CategoryDetails -> {
            CategoryDetailsScreen(
                category = currentState.category,
                onBack = {
                    screenState = ProductsScreenState.Categories
                },
                categoryApiService = categoryApiService,
                onEditCategory = { category ->
                    screenState = ProductsScreenState.Categories
                },
                onProductClick = { product ->
                    screenState = ProductsScreenState.Details(product)
                },
                onDeleteCategory = {
                    screenState = ProductsScreenState.Categories
                }
            )
        }
    }
}

@Composable
fun ProductsListScreen(
    productApiService: ProductApiService,
    categoryApiService: CategoryApiService,
    onProductClick: (ProductResponseDto) -> Unit,
    onAddProduct: () -> Unit,
    onNavigateToCategories: () -> Unit,
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        ProductsList(
            productApiService = productApiService,
            categoryApiService = categoryApiService,
            onProductClick = onProductClick,
            onNavigateToCategories = onNavigateToCategories,
            onRefresh = onRefresh,
            isRefreshing = isRefreshing
        )

        FloatingActionButton(
            onClick = onAddProduct,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            containerColor = Color(0xFF4A90D6),
            contentColor = Color.White,
            shape = MaterialTheme.shapes.medium
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Product",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}