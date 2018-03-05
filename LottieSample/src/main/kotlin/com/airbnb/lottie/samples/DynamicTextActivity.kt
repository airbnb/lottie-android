package com.airbnb.lottie.samples

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import com.airbnb.lottie.TextDelegate
import kotlinx.android.synthetic.main.activity_dynamic_text.*

class DynamicTextActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dynamic_text)

        val textDelegate = TextDelegate(dynamicTextView)
        nameEditText.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                textDelegate.setText("NAME", s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        dynamicTextView.setTextDelegate(textDelegate)
    }
}
