package com.airbnb.lottie.samples

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.airbnb.epoxy.EpoxyController
import com.airbnb.lottie.samples.views.ListingCard
import com.airbnb.lottie.samples.views.WishListIconView
import com.airbnb.lottie.samples.views.listingCard
import com.airbnb.lottie.samples.views.marquee
import kotlinx.android.synthetic.main.activity_list.*

class ListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.setNavigationOnClickListener { finish() }

        recyclerView.buildModelsWith { it.buildModels() }
    }

    private fun EpoxyController.buildModels() {
        marquee {
            id("marquee")
            title("List")
            subtitle("Loading the same animation many times in a list")
        }

        repeat(100) {
            listingCard {
                id(it)
                clickListener { view -> (view as WishListIconView).toggleWishlisted() }
            }
        }
    }
}