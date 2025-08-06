package com.airbnb.lottie.samples

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.airbnb.lottie.samples.databinding.MainActivityBinding
import com.airbnb.lottie.samples.utils.viewBinding

class MainActivity : AppCompatActivity() {
    private val binding: MainActivityBinding by viewBinding()

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        super.onCreate(savedInstanceState)
        binding.bottomNavigation.setOnItemSelectedListener listener@{ item ->
            when (item.itemId) {
                R.id.preview -> showFragment(PreviewFragment())
                R.id.lottiefiles -> showFragment(LottiefilesFragment())
                R.id.learn -> showShowcase()
            }
            true
        }
        binding.bottomNavigation.itemIconTintList = null

        updateContentPaddingForSystemWindowInsets()

        if (savedInstanceState == null) {
            showFragment(PreviewFragment())
        }
    }

    private fun updateContentPaddingForSystemWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.content) { v, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
                        or WindowInsetsCompat.Type.displayCutout(),
            )
            v.updatePadding(
                left = bars.left,
                top = bars.top,
                right = bars.right,
                bottom = bars.bottom,
            )
            WindowInsetsCompat.CONSUMED
        }
    }

    private fun showShowcase() {
        val intent = CustomTabsIntent.Builder().build()
        intent.launchUrl(this, "http://airbnb.io/lottie/#/android".toUri())
    }

    private fun showFragment(fragment: Fragment) {
        supportFragmentManager.commit {
            replace(R.id.content, fragment)
        }
    }
}
