package com.chronova.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.lifecycle.lifecycleScope
import com.chronova.app.data.ApiClient
import com.chronova.app.data.ChronovaRepository
import com.chronova.app.databinding.ActivityLoginBinding
import kotlinx.coroutines.launch
import java.util.UUID

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var repository: ChronovaRepository

    private var lastOAuthLaunch = 0L
    private var oauthNonce: String? = null

    companion object {
        private const val OAUTH_REDIRECT_PATH = "/mobile-callback"
        private const val OAUTH_LAUNCH_DEBOUNCE_MS = 1000L
        private const val KEY_OAUTH_NONCE = "oauth_nonce"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = ChronovaRepository(this)
        ApiClient.updateBaseUrl(repository.getServerUrl())
        binding.etServerUrl.setText(repository.getServerUrl())

        // Restore nonce after process death so the deep-link callback still works
        savedInstanceState?.getString(KEY_OAUTH_NONCE)?.let { oauthNonce = it }

        handleOAuthCallback(intent)
        setupClickListeners()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_OAUTH_NONCE, oauthNonce)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        oauthNonce = savedInstanceState.getString(KEY_OAUTH_NONCE)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleOAuthCallback(intent)
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            maybeSaveServerUrl()
            performLogin(email, password)
        }

        binding.btnApiKey.setOnClickListener {
            val apiKey = binding.etApiKey.text.toString().trim()
            if (apiKey.isEmpty()) {
                Toast.makeText(this, "Please enter API key", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            maybeSaveServerUrl()
            repository.saveApiKey(apiKey)
            navigateToMain()
        }

        binding.btnGoogle.setOnClickListener { launchOAuth("google") }
        binding.btnGithub.setOnClickListener { launchOAuth("github") }
    }

    private fun maybeSaveServerUrl() {
        val serverUrl = binding.etServerUrl.text.toString().trim()
        if (serverUrl.isNotEmpty() && serverUrl != "https://chronova.dev/") {
            if (!repository.isValidUrl(serverUrl)) {
                Toast.makeText(this, "Please enter a valid server URL", Toast.LENGTH_SHORT).show()
                return
            }
            repository.saveServerUrl(serverUrl)
        }
    }

    private fun launchOAuth(provider: String) {
        val now = System.currentTimeMillis()
        if (now - lastOAuthLaunch < OAUTH_LAUNCH_DEBOUNCE_MS) return
        lastOAuthLaunch = now

        maybeSaveServerUrl()

        oauthNonce = UUID.randomUUID().toString()
        val redirectUriWithNonce = "$OAUTH_REDIRECT_PATH?nonce=$oauthNonce"

        val baseUrl = repository.getServerUrl()
        val oauthUrl = try {
            Uri.parse(baseUrl).buildUpon()
                .appendEncodedPath("api/auth/$provider/login")
                .appendQueryParameter("redirect_uri", redirectUriWithNonce)
                .build()
        } catch (e: Exception) {
            Toast.makeText(this, "Invalid server URL format", Toast.LENGTH_SHORT).show()
            return
        }

        val customTabsIntent = CustomTabsIntent.Builder().setShowTitle(true).build()
        try {
            customTabsIntent.launchUrl(this, oauthUrl)
        } catch (e: Exception) {
            try {
                startActivity(Intent(Intent.ACTION_VIEW, oauthUrl))
            } catch (e2: Exception) {
                Toast.makeText(this, "No browser available to complete login", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleOAuthCallback(intent: Intent?) {
        if (intent == null) return
        val data = intent.data ?: return
        if (data.scheme != "com.chronova.app" || data.host != "oauth" || data.path != "/callback") {
            return
        }

        // Consume the intent so rotation does not re-trigger the flow.
        intent.data = null
        setIntent(intent)

        val incomingNonce = data.getQueryParameter("nonce")
        if (oauthNonce == null || incomingNonce != oauthNonce) {
            Toast.makeText(this, "Login failed: Invalid session state", Toast.LENGTH_SHORT).show()
            oauthNonce = null
            return
        }
        oauthNonce = null

        val token = data.getQueryParameter("token")
        val error = data.getQueryParameter("error")

        if (token != null) {
            exchangeOAuthToken(token)
        } else {
            showOAuthError(error)
        }
    }

    private fun exchangeOAuthToken(token: String) {
        // Remember if the user had prior auth before the OAuth attempt
        val hadPriorAuth = repository.getApiKey() != null
        showLoading(true)
        lifecycleScope.launch {
            try {
                val result = repository.exchangeMobileToken(token)
                result.fold(
                    onSuccess = { loginResponse ->
                        if (loginResponse.apiKey != null) {
                            // Fetch pro status; degrade gracefully on failure
                            val proResult = repository.checkProSubscription()
                            if (proResult.isFailure) {
                                Toast.makeText(
                                    this@LoginActivity,
                                    "Login successful (pro status unavailable)",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Toast.makeText(this@LoginActivity, "Login successful", Toast.LENGTH_SHORT).show()
                            }
                            navigateToMain()
                        } else {
                            Toast.makeText(this@LoginActivity, loginResponse.message, Toast.LENGTH_SHORT).show()
                        }
                    },
                    onFailure = { exception ->
                        // Only clear auth if user had a prior session — don't wipe for first-time failures
                        if (hadPriorAuth) {
                            repository.clearAuth()
                        }
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

    private fun showOAuthError(error: String?) {
        val safeError = error?.take(80)?.replace(Regex("\\p{C}"), "")
        val message = when (safeError) {
            "invalid_state" -> "Login session expired. Please try again."
            "missing_params" -> "Invalid login response. Please try again."
            "token_exchange_failed" -> "Could not complete login with provider."
            "internal_error" -> "Unexpected error. Please try again."
            else -> "Login failed: ${safeError ?: "Unknown error"}"
        }
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
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
                            Toast.makeText(this@LoginActivity, loginResponse.message, Toast.LENGTH_SHORT).show()
                        }
                    },
                    onFailure = { exception ->
                        Toast.makeText(this@LoginActivity, "Login failed: ${exception.message}", Toast.LENGTH_SHORT).show()
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
        binding.btnGoogle.isEnabled = !show
        binding.btnGithub.isEnabled = !show
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}