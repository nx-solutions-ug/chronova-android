package com.chronova.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.chronova.app.data.ApiClient
import com.chronova.app.data.ChronovaRepository
import com.chronova.app.databinding.ActivityLoginBinding
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var repository: ChronovaRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Status bar configuration is handled in themes.xml

        repository = ChronovaRepository(this)

        // Initialize API client with saved server URL
        ApiClient.updateBaseUrl(repository.getServerUrl())

        // Load saved server URL into the input field
        binding.etServerUrl.setText(repository.getServerUrl())

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val serverUrl = binding.etServerUrl.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validate and save server URL if provided
            if (serverUrl.isNotEmpty() && serverUrl != "https://chronova.dev/") {
                if (!repository.isValidUrl(serverUrl)) {
                    Toast.makeText(this, "Please enter a valid server URL", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                repository.saveServerUrl(serverUrl)
            }

            performLogin(email, password)
        }

        binding.btnApiKey.setOnClickListener {
            val apiKey = binding.etApiKey.text.toString().trim()
            val serverUrl = binding.etServerUrl.text.toString().trim()

            if (apiKey.isEmpty()) {
                Toast.makeText(this, "Please enter API key", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validate and save server URL if provided
            if (serverUrl.isNotEmpty() && serverUrl != "https://chronova.dev/") {
                if (!repository.isValidUrl(serverUrl)) {
                    Toast.makeText(this, "Please enter a valid server URL", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                repository.saveServerUrl(serverUrl)
            }

            repository.saveApiKey(apiKey)
            navigateToMain()
        }
    }

    private fun performLogin(email: String, password: String) {
        showLoading(true)

        lifecycleScope.launch {
            try {
                val result = repository.login(email, password)
                result.fold(
                    onSuccess = { loginResponse ->
                        if (loginResponse.apiKey != null) {
                            repository.saveApiKey(loginResponse.apiKey)
                            navigateToMain()
                        } else {
                            Toast.makeText(
                                this@LoginActivity,
                                loginResponse.message,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    onFailure = { exception ->
                        Toast.makeText(
                            this@LoginActivity,
                            "Login failed: ${exception.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            } finally {
                showLoading(false)
            }
        }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled = !show
        binding.btnApiKey.isEnabled = !show
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
