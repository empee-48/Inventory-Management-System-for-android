package com.example.inventory.screens.composable.dashboard

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.inventory.screens.composable.common.LoadingComponent

@Composable
fun DashboardLoading() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        LoadingComponent(
            message = "Loading Data...",
            modifier = Modifier.align(Alignment.Center)
        )
    }
}