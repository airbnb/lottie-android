package com.airbnb.lottie.samples

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_choose_asset.view.*
import kotlinx.android.synthetic.main.view_holder_file.view.*

class ChooseAssetDialogFragment : DialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = LayoutInflater.from(context)
                .inflate(R.layout.fragment_choose_asset, container, false)
        dialog.setTitle("Choose an Asset")
        view!!.recyclerView.adapter = AssetsAdapter()
        return view
    }

    override fun onStart() {
        super.onStart()
        dialog.window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }

    internal inner class AssetsAdapter : RecyclerView.Adapter<StringViewHolder>() {
        private val files = context.assets.list("").filter { it.toLowerCase().endsWith(".json") }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StringViewHolder =
            StringViewHolder(parent)

        override fun onBindViewHolder(holder: StringViewHolder, position: Int) =
                holder.bind(files[position])

        override fun getItemCount() = files.size
    }

    internal inner class StringViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
            parent.inflate(R.layout.view_holder_file, false)) {

        fun bind(fileName: String) {
            itemView.title.text = fileName
            itemView.setOnClickListener {
                targetFragment.onActivityResult(targetRequestCode, Activity.RESULT_OK,
                        Intent().putExtra(AnimationFragment.EXTRA_ANIMATION_NAME, fileName))
                dismiss()
            }
        }
    }

    companion object {
        internal fun newInstance(): ChooseAssetDialogFragment {
            return ChooseAssetDialogFragment()
        }
    }
}
