package com.airbnb.lottie.samples

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.samples.model.CompositionArgs

class PlayerActivity : AppCompatActivity(R.layout.player_activity) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            val args = intent.getParcelableExtra(PlayerFragment.EXTRA_ANIMATION_ARGS) ?:
                    CompositionArgs(fileUri = intent.data)
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
