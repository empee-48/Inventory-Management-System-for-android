package com.example.inventory.screens.composable.products

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inventory.data.CategoryCreateDto
import com.example.inventory.data.CategoryResponseDto
import com.example.inventory.service.api.CategoryApiService
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    categoryApiService: CategoryApiService,
    onBack: () -> Unit,
    onAddCategory: () -> Unit = {},
    onCategoryClick: (CategoryResponseDto) -> Unit = {}
) {
    var categories by remember { mutableStateOf<List<CategoryResponseDto>?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var showCreateDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val loadCategories = {
        coroutineScope.launch {
            isLoading = true
            try {
                val response = categoryApiService.getCategories()
                if (response.isSuccessful) {
                    categories = response.body() ?: emptyList()
                    errorMessage = null
                } else {
                    errorMessage = "Failed to load categories: ${response.code()}"
                }
            } catch (e: Exception) {
                errorMessage = "Network error: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadCategories()
    }

    if (showCreateDialog) {
        CreateCategoryDialog(
            onDismiss = { showCreateDialog = false },
            onCreateCategory = { categoryName ->
                coroutineScope.launch {
                    try {
                        val createDto = CategoryCreateDto(name = categoryName)
                        val response = categoryApiService.createCategory(createDto)
                        if (response.isSuccessful) {
                            loadCategories()
                            showCreateDialog = false
                        } else {
                            errorMessage = "Failed to create category: ${response.code()}"
                        }
                    } catch (e: Exception) {
                        errorMessage = "Network error: ${e.message}"
                    }
                }
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Categories",
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFFF8F9FA) // Gray background for top bar
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = Color(0xFF4A90D6),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Category")
            }
        },
        containerColor = Color(0xFFF8F9FA) // Set the scaffold container to gray
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F9FA)) // Ensure the box is gray
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    LoadingCategoriesState()
                }
                errorMessage != null -> {
                    CategoriesErrorState(
                        errorMessage = errorMessage!!,
                        onRetry = {
                            errorMessage = null
                            loadCategories()
                        }
                    )
                }
                categories.isNullOrEmpty() -> {
                    EmptyCategoriesState(
                        onCreateCategory = { showCreateDialog = true }
                    )
                }
                else -> {
                    CategoriesList(
                        categories = categories!!,
                        onCategoryClick = onCategoryClick
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCategoryDialog(
    onDismiss: () -> Unit,
    onCreateCategory: (String) -> Unit
) {
    var categoryName by remember { mutableStateOf("") }
    var isCreating by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = {
            if (!isCreating) {
                onDismiss()
            }
        },
        title = {
            Text(
                text = "Create New Category",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Enter a name for the new category",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF666666)
                )

                OutlinedTextField(
                    value = categoryName,
                    onValueChange = { categoryName = it },
                    label = { Text("Category Name") },
                    placeholder = { Text("e.g., Electronics, Clothing, Food") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isCreating,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color(0xFF4A90D6),
                        focusedLabelColor = Color(0xFF4A90D6)
                    )
                )
            }
        },
        confirmButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(
                    onClick = onDismiss,
                    enabled = !isCreating
                ) {
                    Text("Cancel")
                }
                Button(
                    onClick = {
                        if (categoryName.isNotBlank()) {
                            isCreating = true
                            onCreateCategory(categoryName)
                        }
                    },
                    enabled = categoryName.isNotBlank() && !isCreating,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4A90D6)
                    )
                ) {
                    if (isCreating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Creating...")
                    } else {
                        Text("Create")
                    }
                }
            }
        }
    )
}

@Composable
fun CategoriesList(
    categories: List<CategoryResponseDto>,
    onCategoryClick: (CategoryResponseDto) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA)), // Gray background for the list
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(categories, key = { it.id ?: 0L }) { category ->
            CategoryCard(
                category = category,
                onClick = { onCategoryClick(category) }
            )
        }
    }
}

@Composable
fun CategoryCard(
    category: CategoryResponseDto,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF8F9FA) // Gray background for cards
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .background(Color(0xFFF8F9FA)), // Gray background for row
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Category Icon
            Surface(
                color = Color(0xFF4A90D6).copy(alpha = 0.1f),
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Category,
                        contentDescription = "Category",
                        tint = Color(0xFF4A90D6),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Category Info
            Column(
                modifier = Modifier
                    .weight(1f)
                    .background(Color(0xFFF8F9FA)) // Gray background for text area
            ) {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1A1A1A)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${category.products.size} products",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF666666)
                )
            }

            // Chevron
            Icon(
                imageVector = Icons.Default.ArrowForwardIos,
                contentDescription = "View Details",
                tint = Color(0xFF666666),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun LoadingCategoriesState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA)), // Gray background
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            CircularProgressIndicator(
                color = Color(0xFF4A90D6)
            )
            Text(
                text = "Loading categories...",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF666666)
            )
        }
    }
}

@Composable
fun EmptyCategoriesState(
    onCreateCategory: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA)), // Gray background
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.padding(40.dp)
        ) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = Color(0xFF4A90D6).copy(alpha = 0.1f),
                modifier = Modifier.size(80.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Category,
                        contentDescription = "No Categories",
                        tint = Color(0xFF4A90D6),
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Text(
                text = "No Categories Found",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1A1A1A),
                textAlign = TextAlign.Center
            )

            Text(
                text = "Organize your products by creating categories",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF666666),
                textAlign = TextAlign.Center
            )

            Button(
                onClick = onCreateCategory,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4A90D6)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create Category",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Create First Category")
            }
        }
    }
}

@Composable
fun CategoriesErrorState(
    errorMessage: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA)), // Gray background
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CloudOff,
                contentDescription = "Error",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Text(
                text = "An Error Occurred",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.error
            )
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4A90D6)
                )
            ) {
                Text("Retry")
            }
        }
    }
}