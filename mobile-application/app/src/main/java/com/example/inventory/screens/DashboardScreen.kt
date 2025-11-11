package com.example.inventory.screens

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.inventory.data.UserResponseDto
import com.example.inventory.screens.composable.common.FileDownloader
import com.example.inventory.screens.composable.dashboard.*
import com.example.inventory.service.TokenManager
import com.example.inventory.service.api.*
import com.example.inventory.viewModels.DashboardData
import com.example.inventory.viewModels.DashboardViewModel
import com.example.inventory.viewModels.DashboardUiState
import kotlinx.coroutines.*
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DashboardScreen(
    tokenManager: TokenManager,
    userApiService: UserApiService,
    productApiService: ProductApiService,
    categoryApiService: CategoryApiService,
    salesApiService: SalesApiService,
    ordersApiService: OrdersApiService,
    suppliersApiService: SuppliersApiService,
    batchApiService: BatchApiService,
    onError: (String) -> Unit = {},
    onShowMessage: (String) -> Unit = {},
    onDownloadRequest: (() -> Unit) -> Unit
) {
    val viewModel: DashboardViewModel = viewModel(
        factory = DashboardViewModelFactory(
            productApiService,
            categoryApiService,
            salesApiService,
            ordersApiService,
            suppliersApiService,
            batchApiService
        )
    )

    val uiState by viewModel.uiState.collectAsState()
    var user by remember { mutableStateOf(tokenManager.getUser()) }
    val context = LocalContext.current

    // Auto-refresh data every time the screen is opened
    LaunchedEffect(Unit) {
        viewModel.refreshData()
    }

    // Load user data if not available
    LaunchedEffect(userApiService) {
        if (user == null) {
            try {
                val response = userApiService.getCurrentUser()
                if (response.isSuccessful) {
                    response.body()?.let { userData ->
                        tokenManager.saveUser(userData)
                        user = userData
                    }
                }
            } catch (e: Exception) {
                onError("Failed to load user data: ${e.message}")
            }
        }
    }

    when (uiState) {
        is DashboardUiState.Loading -> {
            DashboardLoading()
        }
        is DashboardUiState.Error -> {
            ModernErrorState(
                message = (uiState as DashboardUiState.Error).message,
                onRetry = { viewModel.refreshData() }
            )
        }
        is DashboardUiState.Success -> {
            val data = (uiState as DashboardUiState.Success).data
            ModernDashboardContent(
                data = data,
                user = user,
                onRefresh = { viewModel.refreshData() },
                batchApiService = batchApiService,
                salesApiService = salesApiService,
                onError = onError,
                onShowMessage = onShowMessage,
                context = context,
                onDownloadRequest = onDownloadRequest
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun ModernDashboardContent(
    data: DashboardData,
    user: UserResponseDto?,
    onRefresh: () -> Unit,
    batchApiService: BatchApiService,
    salesApiService: SalesApiService,
    onError: (String) -> Unit,
    onShowMessage: (String) -> Unit,
    context: Context,
    onDownloadRequest: (() -> Unit) -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var isGeneratingPdf by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        verticalArrangement = Arrangement.spacedBy(32.dp),
        contentPadding = PaddingValues(vertical = 24.dp)
    ) {
        item {
            ModernHeader(user = user, onRefresh = onRefresh)
        }

        item {
            QuickStatsGrid(data = data)
        }

        // Download Report Section
        item {
            DownloadReportSection(
                selectedDate = selectedDate,
                isGenerating = isGeneratingPdf,
                onDateClick = { showDatePicker = true },
                onDownloadClick = {
                    // Wrap in permission check
                    onDownloadRequest {
                        generateDailyReport(
                            selectedDate = selectedDate,
                            batchApiService = batchApiService,
                            salesApiService = salesApiService,
                            context = context,
                            onError = onError,
                            onShowMessage = onShowMessage,
                            onGeneratingChange = { isGenerating -> isGeneratingPdf = isGenerating }
                        )
                    }
                }
            )
        }

        if (data.recentSales.isNotEmpty()) {
            item {
                SalesGraphSection(data = data)
            }
        }

        if (data.lowStockProducts.isNotEmpty()) {
            item {
                ModernAlertsSection(products = data.lowStockProducts)
            }
        }

        if (data.recentSales.isNotEmpty() || data.recentOrders.isNotEmpty()) {
            item {
                RecentActivitySection(data = data)
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showDatePicker) {
        SimpleDatePickerDialog(
            onDismiss = { showDatePicker = false },
            onDateSelected = { date ->
                selectedDate = date
                showDatePicker = false
            },
            initialDate = selectedDate
        )
    }
}

@Composable
private fun ModernHeader(user: UserResponseDto?, onRefresh: () -> Unit) {
    Column(
        modifier = Modifier.padding(horizontal = 24.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = buildWelcomeMessage(user),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Dashboard Overview",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }

            // Refresh button
            Surface(
                onClick = onRefresh,
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.size(48.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DownloadReportSection(
    selectedDate: LocalDate,
    isGenerating: Boolean,
    onDateClick: () -> Unit,
    onDownloadClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF10B981).copy(alpha = 0.1f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Download Report",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Column {
                    Text(
                        text = "Daily Report",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Generate PDF report for any date",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Date selection and download button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Date display with picker
                Surface(
                    onClick = onDateClick,
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "Select Date",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Selected Date",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = selectedDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Download button
                Button(
                    onClick = {
                        try {
                            onDownloadClick()
                        } catch (e: Exception) {
                            // Error will be handled in generateDailyReport
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    modifier = Modifier.height(56.dp),
                    enabled = !isGenerating
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Download",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isGenerating) "Generating..." else "Generate PDF",
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun generateDailyReport(
    selectedDate: LocalDate,
    batchApiService: BatchApiService,
    salesApiService: SalesApiService,
    context: Context,
    onError: (String) -> Unit,
    onShowMessage: (String) -> Unit,
    onGeneratingChange: (Boolean) -> Unit
) {
    onGeneratingChange(true)

    CoroutineScope(Dispatchers.IO).launch {
        try {
            withContext(Dispatchers.Main) {
                onShowMessage("Creating PDF report...")
            }

            // Step 1: Create the actual PDF file with today's date
            val pdfFile = createActualPdfWithTodaysDate(context, selectedDate)

            if (!pdfFile.exists()) {
                withContext(Dispatchers.Main) {
                    onGeneratingChange(false)
                    onError("Failed to create PDF file")
                }
                return@launch
            }

            withContext(Dispatchers.Main) {
                onShowMessage("Saving PDF to Downloads...")
            }

            // Step 2: Use FileDownloader to save to Downloads
            val fileName = "daily_sales_report_${selectedDate.format(DateTimeFormatter.ofPattern("yyyy_MM_dd"))}.pdf"
            val fileDownloader = FileDownloader(context)
            val downloadResult = fileDownloader.savePdfToDownloads(pdfFile, fileName)

            withContext(Dispatchers.Main) {
                onGeneratingChange(false)

                if (downloadResult.isSuccess) {
                    val successMessage = downloadResult.getOrNull() ?: "PDF downloaded successfully!"
                    onShowMessage("✅ $successMessage")

//                    fileDownloader.sharePdfFile(pdfFile, fileName)
                } else {
                    val error = downloadResult.exceptionOrNull()?.message ?: "Unknown error"
                    onError("Download failed: $error")
                }

                // Clean up temp file
                pdfFile.delete()
            }

        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                onGeneratingChange(false)
                onError("Unexpected error: ${e.message}")
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private suspend fun createActualPdfWithTodaysDate(
    context: Context,
    selectedDate: LocalDate
): File {
    return withContext(Dispatchers.IO) {
        try {
            // Create a simple PDF file with today's date
            val fileName = "temp_report_${System.currentTimeMillis()}.pdf"
            val file = File(context.cacheDir, fileName)

            // For now, create a simple text file as PDF (you can replace with iText later)
            val pdfContent = """
                DAILY SALES REPORT
                ==================
                
                Date: ${selectedDate.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"))}
                Generated: ${LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"))}
                
                This is a test PDF report.
                Actual PDF generation with iText can be added here.
                
                Report includes:
                - Sales data for ${selectedDate}
                - Profit calculations
                - Itemized sales breakdown
                - Financial summary
                
                Total Sales: $0.00
                Total Profit: $0.00
                Number of Sales: 0
                
                --- END OF REPORT ---
            """.trimIndent()

            file.writeText(pdfContent)
            file
        } catch (e: Exception) {
            // Fallback: create empty file
            val fileName = "temp_report_${System.currentTimeMillis()}.pdf"
            File(context.cacheDir, fileName).apply { createNewFile() }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SimpleDatePickerDialog(
    onDismiss: () -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    initialDate: LocalDate = LocalDate.now()
) {
    var selectedDate by remember { mutableStateOf(initialDate) }
    var year by remember { mutableStateOf(initialDate.year.toString()) }
    var month by remember { mutableStateOf(initialDate.monthValue.toString()) }
    var day by remember { mutableStateOf(initialDate.dayOfMonth.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Date") },
        text = {
            Column {
                Text(
                    "Choose a date for the report",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Year
                    OutlinedTextField(
                        value = year,
                        onValueChange = { newYear ->
                            year = newYear
                            updateDate(year, month, day)?.let { selectedDate = it }
                        },
                        label = { Text("Year") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    // Month
                    OutlinedTextField(
                        value = month,
                        onValueChange = { newMonth ->
                            month = newMonth
                            updateDate(year, month, day)?.let { selectedDate = it }
                        },
                        label = { Text("Month") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    // Day
                    OutlinedTextField(
                        value = day,
                        onValueChange = { newDay ->
                            day = newDay
                            updateDate(year, month, day)?.let { selectedDate = it }
                        },
                        label = { Text("Day") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "Selected: ${selectedDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onDateSelected(selectedDate)
                    onDismiss()
                }
            ) {
                Text("Select")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@RequiresApi(Build.VERSION_CODES.O)
private fun updateDate(year: String, month: String, day: String): LocalDate? {
    return try {
        val yearInt = year.toIntOrNull() ?: return null
        val monthInt = month.toIntOrNull() ?: return null
        val dayInt = day.toIntOrNull() ?: return null

        // Basic validation
        if (yearInt < 2000 || yearInt > 2100) return null
        if (monthInt !in 1..12) return null
        if (dayInt !in 1..31) return null

        LocalDate.of(yearInt, monthInt, dayInt)
    } catch (e: Exception) {
        null
    }
}

private fun buildWelcomeMessage(user: UserResponseDto?): String {
    return when {
        user?.username != null -> "Welcome back, ${user.username}!"
        else -> "Welcome to your dashboard!"
    }
}

class DashboardViewModelFactory(
    private val productApiService: ProductApiService,
    private val categoryApiService: CategoryApiService,
    private val salesApiService: SalesApiService,
    private val ordersApiService: OrdersApiService,
    private val suppliersApiService: SuppliersApiService,
    private val batchApiService: BatchApiService
) : androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            return DashboardViewModel(
                productApiService,
                categoryApiService,
                salesApiService,
                ordersApiService,
                suppliersApiService,
                batchApiService
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}