package com.chronova.app

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.chronova.app.data.ApiClient
import com.chronova.app.data.ChronovaRepository
import com.chronova.app.databinding.ActivityMainBinding
import com.chronova.app.ui.ProjectsContainerFragment
import com.chronova.app.ui.GoalsFragment
import com.chronova.app.ui.LeaderboardFragment
import com.chronova.app.ui.InsightsPagerFragment
import com.chronova.app.ui.main.MainPagerFragment
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var repository: ChronovaRepository
    private var isProUser: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Status bar configuration is handled in themes.xml

        repository = ChronovaRepository(this)

        // Initialize API client with saved server URL
        ApiClient.updateBaseUrl(repository.getServerUrl())

        // Check authentication
        if (!repository.isAuthenticated()) {
            navigateToLogin()
            return
        }

        // Check PRO subscription status
        checkProSubscription()

        setSupportActionBar(binding.toolbar)
        setupBottomNavigation()

        // Start with main dashboard (with tabs)
        if (savedInstanceState == null) {
            val mainPagerFragment = MainPagerFragment().apply {
                arguments = Bundle().apply {
                    putBoolean("is_pro_user", isProUser)
                }
            }
            replaceFragment(mainPagerFragment)
            updateTitleWithProBadge("Dashboard")
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> {
                    val frag = MainPagerFragment().apply {
                        arguments = Bundle().apply { putBoolean("is_pro_user", isProUser) }
                    }
                    replaceFragment(frag)
                    updateTitleWithProBadge("Dashboard")
                    true
                }
                R.id.nav_projects -> {
                    replaceFragment(ProjectsContainerFragment())
                    updateTitleWithProBadge("Projects")
                    true
                }
                R.id.nav_goals -> {
                    replaceFragment(GoalsFragment())
                    updateTitleWithProBadge("Goals")
                    true
                }
                R.id.nav_leaderboard -> {
                    val frag = LeaderboardFragment().apply {
                        arguments = Bundle().apply { putBoolean("is_pro_user", isProUser) }
                    }
                    replaceFragment(frag)
                    updateTitleWithProBadge("Leaderboard")
                    true
                }
                R.id.nav_insights -> {
                    val frag = InsightsPagerFragment().apply {
                        arguments = Bundle().apply { putBoolean("is_pro_user", isProUser) }
                    }
                    replaceFragment(frag)
                    updateTitleWithProBadge("Insights")
                    true
                }
                else -> false
            }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                repository.clearAuth()
                navigateToLogin()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun checkProSubscription() {
        lifecycleScope.launch {
            try {
                val result = repository.checkProSubscription()
                result.fold(
                    onSuccess = { isPro ->
                        isProUser = isPro
                        // Update the title with PRO badge
                        updateTitleWithProBadge("Dashboard")
                        // Notify fragment about PRO status change
                        notifyFragmentOfProStatus(isPro)
                    },
                    onFailure = { exception ->
                        // Default to false on error
                        isProUser = false
                        println("Error checking PRO subscription: ${exception.message}")
                    }
                )
            } catch (e: Exception) {
                isProUser = false
                println("Error checking PRO subscription: ${e.message}")
            }
        }
    }

    private fun notifyFragmentOfProStatus(isPro: Boolean) {
        // Find the MainPagerFragment and update its PRO status
        val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (fragment is MainPagerFragment) {
            fragment.updateProStatus(isPro)
        }
    }

    private fun updateTitleWithProBadge(title: String) {
        val displayTitle = if (isProUser) {
            "$title ⭐ PRO"
        } else {
            title
        }
        supportActionBar?.title = displayTitle
    }
}
