package com.airbnb.lottie.samples

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.airbnb.lottie.samples.databinding.LottiefilesFragmentBinding
import com.airbnb.lottie.samples.utils.viewBinding


class LottiefilesFragment : Fragment(R.layout.lottiefiles_fragment) {
    private val binding: LottiefilesFragmentBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.getItOnPlay.setOnClickListener {
            val uri = Uri.parse("https://play.google.com/store/apps/details?id=com.lottiefiles.LottiePreview")
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        }
    }
}
