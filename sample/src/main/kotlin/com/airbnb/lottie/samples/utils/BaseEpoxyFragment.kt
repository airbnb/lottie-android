package com.airbnb.lottie.samples.utils

import android.os.Bundle
import android.view.View
import com.airbnb.epoxy.AsyncEpoxyController
import com.airbnb.epoxy.EpoxyController
import com.airbnb.lottie.samples.R
import com.airbnb.lottie.samples.databinding.BaseFragmentBinding

private class BaseEpoxyController(
    private val buildModelsCallback: EpoxyController.() -> Unit
) : AsyncEpoxyController() {
    override fun buildModels() {
        buildModelsCallback()
    }
}

abstract class BaseEpoxyFragment : BaseFragment(R.layout.base_fragment) {
    protected val binding: BaseFragmentBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.recyclerView.setControllerAndBuildModels(BaseEpoxyController { buildModels() })
    }

    override fun invalidate() {
        binding.recyclerView.requestModelBuild()
    }

    abstract fun EpoxyController.buildModels()
}
