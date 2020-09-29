package com.airbnb.lottie.samples

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.epoxy.EpoxyController
import com.airbnb.epoxy.EpoxyRecyclerView
import com.airbnb.lottie.samples.databinding.ListActivityBinding
import com.airbnb.lottie.samples.utils.viewBinding
import com.airbnb.lottie.samples.views.listingCard
import com.airbnb.lottie.samples.views.marquee

class WishListActivity : AppCompatActivity() {
    private val binding: ListActivityBinding by viewBinding()

    private val wishListedItems = mutableSetOf<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.recyclerView.buildModelsWith(object : EpoxyRecyclerView.ModelBuilderCallback {
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

        repeat(100) { index ->
            listingCard {
                id(index)
                isWishListed(wishListedItems.contains(index))
                onToggled { isWishListed ->
                    if (isWishListed) wishListedItems.add(index)
                    else wishListedItems.remove(index)
                    binding.recyclerView.requestModelBuild()
                }
            }
        }
    }
}