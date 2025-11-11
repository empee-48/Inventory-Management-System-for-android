package com.example.inventory

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.inventory.service.AuthRepository
import com.example.inventory.screens.GetStartedScreen
import com.example.inventory.screens.LoginScreen
import com.example.inventory.screens.MainScreen
import com.example.inventory.screens.composable.common.PermissionManager
import com.example.inventory.service.RetrofitInstance
import com.example.inventory.ui.theme.InventoryTheme

class MainActivity : ComponentActivity() {

    // Single permission launcher for storage permission
    private val storagePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted - trigger the pending download
            onStoragePermissionGranted?.invoke()
        } else {
            // Permission denied - show error
            onStoragePermissionDenied?.invoke()
        }
        // Clear the callbacks
        onStoragePermissionGranted = null
        onStoragePermissionDenied = null
    }

    // Callbacks for permission results
    private var onStoragePermissionGranted: (() -> Unit)? = null
    private var onStoragePermissionDenied: (() -> Unit)? = null

    // Function to request storage permission from other composables
    fun requestStoragePermission(
        onGranted: () -> Unit,
        onDenied: () -> Unit
    ) {
        this.onStoragePermissionGranted = onGranted
        this.onStoragePermissionDenied = onDenied

        storagePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            InventoryTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(
                        onRequestStoragePermission = { onGranted, onDenied ->
                            requestStoragePermission(onGranted, onDenied)
                        }
                    )
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavigation(
    onRequestStoragePermission: ((() -> Unit, () -> Unit) -> Unit)? = null
) {
    val context = LocalContext.current
    val inventoryApp = context.applicationContext as InventoryApplication
    val tokenManager = inventoryApp.tokenManager
    val authRepository = AuthRepository(tokenManager)
    val retrofit = RetrofitInstance(tokenManager)
    val productApiService = retrofit.productsApiService
    val categoryApiService = retrofit.categoriesApiService
    val salesApiService = retrofit.salesApiService
    val ordersApiService = retrofit.ordersApiService
    val suppliersApiService = retrofit.suppliersApiService
    val batchApiService = retrofit.batchApiService
    val userApiService = retrofit.userApiService

    var showGetStarted by remember { mutableStateOf(true) }
    var isLoggedIn by remember { mutableStateOf(authRepository.isLoggedIn()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    // Permission state management
    val permissionManager = remember { PermissionManager() }
    var needsPermission by remember { mutableStateOf(false) }
    var pendingDownloadAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    // Check permission status when composable launches
    LaunchedEffect(Unit) {
        needsPermission = !permissionManager.hasStoragePermission(context)
    }

    // Function to execute download with permission check
    fun executeDownloadWithPermissionCheck(downloadAction: () -> Unit) {
        if (needsPermission && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            // Store the download action and request permission
            pendingDownloadAction = downloadAction
            onRequestStoragePermission?.invoke(
                {
                    // Permission granted
                    needsPermission = false
                    pendingDownloadAction?.invoke()
                    pendingDownloadAction = null
                    successMessage = "Storage permission granted - downloading PDF"
                },
                {
                    // Permission denied
                    errorMessage = "Storage permission denied. Cannot save PDF to Downloads folder."
                    pendingDownloadAction = null
                }
            )
        } else {
            // No permission needed (Android 10+) or already granted
            downloadAction()
        }
    }

    if (showGetStarted) {
        GetStartedScreen(
            onGetStarted = {
                showGetStarted = false
            }
        )
    } else {
        if (isLoggedIn) {
            MainScreen(
                tokenManager = tokenManager,
                userApiService = userApiService,
                productApiService = productApiService,
                categoryApiService = categoryApiService,
                salesApiService = salesApiService,
                ordersApiService = ordersApiService,
                suppliersApiService = suppliersApiService,
                batchApiService = batchApiService,
                onLogout = {
                    authRepository.logout()
                    isLoggedIn = false
                },
                onError = { error ->
                    errorMessage = error
                },
                onShowMessage = { message ->
                    successMessage = message
                },
                onDownloadRequest = { downloadAction ->
                    executeDownloadWithPermissionCheck(downloadAction)
                }
            )
        } else {
            LoginScreen(
                onLoginSuccess = {
                    isLoggedIn = true
                }
            )
        }
    }

    // Show error messages
    LaunchedEffect(errorMessage) {
        errorMessage?.let { message ->
            // You can show a snackbar here
            println("Error: $message")
            errorMessage = null
        }
    }

    // Show success messages
    LaunchedEffect(successMessage) {
        successMessage?.let { message ->
            // You can show a snackbar here
            println("Success: $message")
            successMessage = null
        }
    }
}