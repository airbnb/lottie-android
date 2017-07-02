package com.airbnb.lottie.samples

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_warnings.view.*
import kotlinx.android.synthetic.main.view_holder_warning.view.*

class WarningsDialogFragment : DialogFragment() {

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_warnings, container, false)
        view!!.okButton.setOnClickListener { dismiss() }
        view.recyclerView.adapter = Adapter(arguments.getStringArrayList(ARG_WARNINGS))
        return view
    }

    private class Adapter(private val warnings: List<String>) : RecyclerView.Adapter<VH>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH = VH(parent)

        override fun onBindViewHolder(holder: VH, position: Int) =
                holder.bind(warnings[position], position != itemCount - 1)

        override fun getItemCount(): Int = warnings.size
    }

    internal class VH(parent: ViewGroup) : RecyclerView.ViewHolder(
            parent.inflate(R.layout.view_holder_warning, false)) {
        fun bind(warning: String, showDivider: Boolean) {
            itemView.warning.text = warning
            itemView.divider.visibility = if (showDivider) View.VISIBLE else View.GONE
        }
    }

    companion object {
        private val ARG_WARNINGS = "warnings"

        internal fun newInstance(warnings: ArrayList<String>): WarningsDialogFragment {
            val args = Bundle()
            args.putStringArrayList(ARG_WARNINGS, warnings)
            val frag = WarningsDialogFragment()
            frag.arguments = args
            return frag
        }
    }
}
