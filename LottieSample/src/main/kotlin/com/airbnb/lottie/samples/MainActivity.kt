package com.airbnb.lottie.samples

import android.content.Intent
import android.os.Bundle
import android.support.customtabs.CustomTabsIntent
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.AppCompatDelegate
import android.view.MenuItem
import androidx.net.toUri
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
            R.id.learn -> startActivity(Intent(this, DebugActivity::class.java))
            else -> return false
        }
        return true
    }

    private fun showShowcase() {
        val intent = CustomTabsIntent.Builder().build()
        intent.launchUrl(this, "http://airbnb.io/lottie/android/android.html".toUri())
    }

    private fun showFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
                .replace(R.id.content, fragment)
                .commit()
    }
}
