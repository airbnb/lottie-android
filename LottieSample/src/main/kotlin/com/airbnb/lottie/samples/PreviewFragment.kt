package com.airbnb.lottie.samples

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_preview.*

class PreviewFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_preview, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        qr.apply {
            addIcon(R.drawable.ic_device)
            setOnClickListener {

            }
        }

        file.apply {
            addIcon(R.drawable.ic_device)
            setOnClickListener {

            }
        }

        url.apply {
            addIcon(R.drawable.ic_device)
            setOnClickListener {

            }
        }
    }
}