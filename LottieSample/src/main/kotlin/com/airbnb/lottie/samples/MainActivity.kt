package com.airbnb.lottie.samples

import android.os.Bundle
import android.support.customtabs.CustomTabsIntent
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.AppCompatDelegate
import android.view.KeyEvent
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
            R.id.learn -> showShowcase()
            else -> return false
        }
        return true
    }


    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            System.exit(0)
            return true
        }
        return super.onKeyUp(keyCode, event)
    }

    private fun showShowcase() {
        CustomTabsIntent.Builder().build().run {
            launchUrl(this@MainActivity, "http://airbnb.io/lottie/android/android.html".toUri())
        }
    }

    private fun showFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
                .replace(R.id.content, fragment)
                .commit()
    }
}
