package com.airbnb.lottie.samples

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import com.airbnb.lottie.samples.databinding.MainActivityBinding
import com.airbnb.lottie.samples.utils.viewBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {
    private val binding: MainActivityBinding by viewBinding()

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        super.onCreate(savedInstanceState)
        binding.bottomNavigation.setOnNavigationItemSelectedListener(this)

        savedInstanceState ?: showFragment(ShowcaseFragment())
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.showcase -> showFragment(ShowcaseFragment())
            R.id.preview -> showFragment(PreviewFragment())
            R.id.lottiefiles -> showFragment(LottiefilesFragment())
            R.id.learn -> showShowcase()
            else -> return false
        }
        return true
    }

    private fun showShowcase() {
        val intent = CustomTabsIntent.Builder().build()
        intent.launchUrl(this, "http://airbnb.io/lottie/#/android".toUri())
    }

    private fun showFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
                .replace(R.id.content, fragment)
                .commit()
    }
}
