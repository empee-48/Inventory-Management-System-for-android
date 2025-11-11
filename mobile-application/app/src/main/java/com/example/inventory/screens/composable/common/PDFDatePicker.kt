package com.example.inventory.screens.composable.common

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DatePickerDialog(
    onDismiss: () -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    initialDate: LocalDate = LocalDate.now()
) {
    var selectedDate by remember { mutableStateOf(initialDate) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = 8.dp,
            modifier = Modifier
                .width(320.dp)
                .wrapContentHeight()
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "Select Date",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 20.dp)
                ) {
                    // Year
                    OutlinedTextField(
                        value = selectedDate.year.toString(),
                        onValueChange = { year ->
                            year.toIntOrNull()?.let {
                                selectedDate = selectedDate.withYear(it)
                            }
                        },
                        label = { Text("Year") },
                        modifier = Modifier.weight(1f)
                    )

                    // Month
                    OutlinedTextField(
                        value = selectedDate.monthValue.toString(),
                        onValueChange = { month ->
                            month.toIntOrNull()?.takeIf { it in 1..12 }?.let {
                                selectedDate = selectedDate.withMonth(it)
                            }
                        },
                        label = { Text("Month") },
                        modifier = Modifier.weight(1f)
                    )

                    // Day
                    OutlinedTextField(
                        value = selectedDate.dayOfMonth.toString(),
                        onValueChange = { day ->
                            day.toIntOrNull()?.takeIf { it in 1..31 }?.let {
                                selectedDate = selectedDate.withDayOfMonth(it)
                            }
                        },
                        label = { Text("Day") },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Selected date preview
                Text(
                    text = "Selected: ${selectedDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Buttons
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { onDateSelected(selectedDate) }) {
                        Text("Select")
                    }
                }
            }
        }
    }
}