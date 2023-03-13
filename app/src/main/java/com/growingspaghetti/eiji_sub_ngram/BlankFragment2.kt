package com.growingspaghetti.eiji_sub_ngram

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText

class BlankFragment2 : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_blank2, container, false)
        val searchButton = v.findViewById<View>(R.id.button2)
        searchButton.setOnClickListener{
            switchActivities(v);
        }
        return v
    }

    private fun switchActivities(v: View) {
        val plainText = v.findViewById<View>(R.id.editTextTextPersonName2) as EditText
        val keyword = plainText.text.toString()
        if (keyword.isBlank()) {
            return
        }
        val switchActivityIntent = Intent(context, MainActivity2::class.java)
        switchActivityIntent.putExtra("kwd", keyword)
        startActivity(switchActivityIntent)
    }
}