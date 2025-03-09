package com.momid.kanda333.ui

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.momid.kanda333.database.data_model.User
import com.momid.kanda333.view_model.AuthViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun RegistrationScreen(authViewModel: AuthViewModel, onRegistrationSuccess: () -> Unit) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var dateOfBirth by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val textFieldShape = RoundedCornerShape(7.dp)
        val textFieldColors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = Color.White,
            focusedContainerColor = Color.LightGray.copy(alpha = 0.1f),
            unfocusedBorderColor = Color.Gray
        )
        Text("Welcome to Kanda", fontSize = 37.sp)
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(
            value = firstName,
            onValueChange = { firstName = it },
            label = { Text("First Name") },
            colors = textFieldColors,
            shape = textFieldShape,
            modifier = Modifier.padding(3.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next)
        )
        OutlinedTextField(
            value = lastName,
            onValueChange = { lastName = it },
            label = { Text("Last Name") },
            colors = textFieldColors,
            shape = textFieldShape,
            modifier = Modifier.padding(3.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next)
        )
        OutlinedTextField(
            value = age,
            onValueChange = { age = it },
            label = { Text("Age") },
            colors = textFieldColors,
            shape = textFieldShape,
            modifier = Modifier.padding(3.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Next,
                keyboardType = KeyboardType.Number
            )
        )
        OutlinedTextField(
            value = dateOfBirth,
            onValueChange = { dateOfBirth = it },
            label = { Text("Date of Birth") },
            colors = textFieldColors,
            shape = textFieldShape,
            modifier = Modifier.padding(3.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Next,
                keyboardType = KeyboardType.Number
            )
        ) // Use DatePicker
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            colors = textFieldColors,
            shape = textFieldShape,
            modifier = Modifier.padding(3.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next)
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            colors = textFieldColors,
            shape = textFieldShape,
            modifier = Modifier.padding(3.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = {
            if (firstName.isBlank() || lastName.isBlank() || age.isBlank() || dateOfBirth.isBlank() || username.isBlank() || password.isBlank()) {
                message = "Please fill all fields"
                return@Button
            }

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    authViewModel.register(
                        User(
                            firstName = firstName,
                            lastName = lastName,
                            age = age.toInt(),
                            dateOfBirth = dateOfBirth,
                            username = username,
                            password = password
                        )
                    )

                    withContext(Dispatchers.Main) {
                        onRegistrationSuccess()
                    }
                } catch (e: NumberFormatException) {
                    message = "Invalid Age"
                } catch (e: Exception) {
                    message = "Registration failed: ${e.message}"
                    Log.e("RegistrationScreen", "Registration error", e)
                }
            }
        }) {
            Text("Register")
        }

        if (message.isNotEmpty()) {
            Text(message, color = MaterialTheme.colorScheme.error)
        }
    }
}
