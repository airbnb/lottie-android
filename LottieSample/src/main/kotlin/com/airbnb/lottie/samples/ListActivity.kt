package com.airbnb.lottie.samples

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.epoxy.EpoxyController
import com.airbnb.epoxy.EpoxyRecyclerView
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

        recyclerView.buildModelsWith(object : EpoxyRecyclerView.ModelBuilderCallback {
            override fun buildModels(controller: EpoxyController) {
                controller.buildModels()
            }
        })
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