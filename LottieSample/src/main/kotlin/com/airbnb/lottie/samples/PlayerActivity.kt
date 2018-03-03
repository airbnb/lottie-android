package com.airbnb.lottie.samples

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity

class PlayerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        if (savedInstanceState == null) {
            val args = intent.getParcelableExtra<CompositionArgs>(PlayerFragment.EXTRA_ANIMATION_ARGS)
            supportFragmentManager.beginTransaction()
                    .add(R.id.content, PlayerFragment.forAsset(args))
                    .commit()
        }
    }

    companion object {
        fun intent(context: Context, args: CompositionArgs): Intent {
            return Intent(context, PlayerActivity::class.java).apply {
                putExtra(PlayerFragment.EXTRA_ANIMATION_ARGS, args)
            }
        }
    }
}
