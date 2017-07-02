package com.airbnb.lottie.samples

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import butterknife.internal.Utils.listOf

import kotlinx.android.synthetic.main.fragment_list.*
import kotlinx.android.synthetic.main.fragment_list.view.*
import kotlinx.android.synthetic.main.view_holder_file.view.*

private data class ListItem(val name: String, val shortcut: String? = null, val onClick: () -> Unit)

class ListFragment : Fragment() {
    private val SHORTCUT_VIEWER = "com.airbnb.lottie.samples.shortcut.VIEWER"
    private val SHORTCUT_TYPOGRAPHY = "com.airbnb.lottie.samples.shortcut.TYPOGRAPHY"
    private val SHORTCUT_TUTORIAL = "com.airbnb.lottie.samples.shortcut.TUTORIAL"
    private val SHORTCUT_FULLSCREEN = "com.airbnb.lottie.samples.shortcut.FULLSCREEN"

    private val items = listOf(
            ListItem("Animation Viewer", SHORTCUT_VIEWER)
                { showFragment(AnimationFragment.newInstance()) },
            ListItem("Animated Typography", SHORTCUT_TYPOGRAPHY)
                { startActivity(TypographyDemoActivity::class.java) },
            ListItem("Animated App Tutorial", SHORTCUT_TUTORIAL)
                { startActivity(AppIntroActivity::class.java) },
            ListItem("Full Screen Animation", SHORTCUT_FULLSCREEN)
                { startActivity(FullScreenActivity::class.java) },
            ListItem("Custom fonts and dynamic text")
                { showFragment(FontFragment.newInstance()) },
            ListItem("Open lottiefiles.com")
                { startActivity("http://www.lottiefiles.com".urlIntent()) }
    )

    private val adapter = FileAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        items.find { it.shortcut == activity.intent.action }?.onClick?.invoke()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = container?.inflate(R.layout.fragment_list, false)
        view!!.recyclerView.adapter = adapter
        return view
    }

    override fun onStart() {
        super.onStart()
        animationView.progress = 0f
        animationView.playAnimation()
    }

    override fun onStop() {
        super.onStop()
        animationView.cancelAnimation()
    }

    private fun showFragment(fragment: Fragment) {
        fragmentManager.beginTransaction()
                .addToBackStack(null)
                .setCustomAnimations(R.anim.slide_in_right, R.anim.hold, R.anim.hold, R.anim.slide_out_right)
                .remove(this)
                .replace(R.id.content_2, fragment)
                .commit()
    }

    private inner class FileAdapter : RecyclerView.Adapter<StringViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StringViewHolder =
            StringViewHolder(parent)

        override fun onBindViewHolder(holder: StringViewHolder, position: Int) =
            holder.bind(items[position])

        override fun getItemCount(): Int = items.size
    }

    private inner class StringViewHolder(parent: ViewGroup) :
            RecyclerView.ViewHolder(parent.inflate(R.layout.view_holder_file, false)) {

        fun bind(item: ListItem) {
            itemView.title.text = item.name
            itemView.setOnClickListener { item.onClick() }
        }
    }

    companion object {
        internal fun newInstance(): ListFragment {
            return ListFragment()
        }
    }
}
