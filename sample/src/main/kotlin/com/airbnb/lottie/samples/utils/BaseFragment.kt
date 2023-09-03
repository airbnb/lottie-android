package com.airbnb.lottie.samples.utils

import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import com.airbnb.mvrx.MavericksView

abstract class BaseFragment(@LayoutRes contentLayoutId: Int) : Fragment(contentLayoutId), MavericksView
