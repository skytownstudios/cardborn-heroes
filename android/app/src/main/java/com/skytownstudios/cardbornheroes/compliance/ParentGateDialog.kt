package com.skytownstudios.cardbornheroes.compliance

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.random.Random

@Composable
fun ParentGateDialog(onDismiss: () -> Unit, onSuccess: () -> Unit) {
    val a = remember { Random.nextInt(11, 19) }
    val b = remember { Random.nextInt(2, 9) }
    var answer by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Parents only") },
        text = {
            Column {
                Text("Solve to continue: $a + $b = ?")
                OutlinedTextField(value = answer, onValueChange = { answer = it }, label = { Text("Answer") })
                if (error) Text("Incorrect", color = MaterialTheme.colorScheme.error)
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (answer.toIntOrNull() == a + b) { onSuccess(); onDismiss() } else error = true
            }) { Text("Continue") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
