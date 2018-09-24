package com.airbnb.lottie.samples

import android.os.Bundle
import androidx.browser.customtabs.CustomTabsIntent
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import android.view.MenuItem
import androidx.core.net.toUri
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bottomNavigation.setOnNavigationItemSelectedListener(this)

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
        intent.launchUrl(this, "http://airbnb.io/lottie/android/android.html".toUri())
    }

    private fun showFragment(fragment: androidx.fragment.app.Fragment) {
        supportFragmentManager.beginTransaction()
                .replace(R.id.content, fragment)
                .commit()
    }
}
