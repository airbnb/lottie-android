package com.airbnb.lottie.samples

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.airbnb.epoxy.AsyncEpoxyController
import com.airbnb.epoxy.EpoxyController
import com.airbnb.mvrx.BaseMvRxFragment
import kotlinx.android.synthetic.main.fragment_base.*
import kotlinx.android.synthetic.main.fragment_base.view.*


private class BaseEpoxyController(
        private val buildModelsCallback: EpoxyController.() -> Unit
) : AsyncEpoxyController() {
    override fun buildModels() {
        buildModelsCallback()
    }
}

abstract class BaseEpoxyFragment : BaseMvRxFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_base, container, false).apply {
                recyclerView.setController(BaseEpoxyController { buildModels() })
            }

    override fun invalidate() {
        recyclerView.requestModelBuild()
    }

    abstract fun EpoxyController.buildModels()
}