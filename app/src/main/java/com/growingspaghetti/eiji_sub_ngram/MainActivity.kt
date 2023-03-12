package com.growingspaghetti.eiji_sub_ngram

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.EditText
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val searchButton = findViewById<View>(R.id.button2)
        searchButton.setOnClickListener {
            switchActivities();
        }

        if (Environment.getExternalStorageState() != Environment.MEDIA_MOUNTED) {
            val text = findViewById<View>(R.id.textView) as TextView
            text.text = "⚠️SDカードがマウントされていません！この検索ソフトが動作するためには、SDカードにngram索引の付いた辞書そのものを多く必要があります！"
            searchButton.isEnabled = false
            return
        }

        if (!isExternalStorageReadable()) {
            val text = findViewById<View>(R.id.textView) as TextView
            text.text = "⚠️SDカードが読み込めません！この検索ソフトが動作するためには、SDカードに置かれたngram索引の付いた辞書そのものを読み込む必要があります！"
            searchButton.isEnabled = false
        }

        val dirs = this.getExternalFilesDirs(null);
        val dir = dirs[dirs.size - 1];
        dir.mkdirs()
        if (dir.listFiles()!!.none { it.name.endsWith("_INDEX") }) {
            val text = findViewById<View>(R.id.textView) as TextView
            text.text = String.format(
                "⚠️検索する辞書がSDカードのファルダにありません！\n %s に\n\nEDICT_NGRAM, EDICT_INDEX, EDICT_TEXT\n\nSUBTITLE_NGRAM, SUBTITLE_INDEX, SUBTITLE_TEXT\n\nをダウンロードして入れてください。",
                dir
            )
            searchButton.isEnabled = false
        }
    }

    private fun isExternalStorageReadable(): Boolean {
        return Environment.getExternalStorageState() in
                setOf(Environment.MEDIA_MOUNTED, Environment.MEDIA_MOUNTED_READ_ONLY)
    }

    private fun switchActivities() {
        val plainText = findViewById<View>(R.id.editTextTextPersonName2) as EditText
        val keyword = plainText.text.toString()
        if (keyword.isBlank()) {
            return
        }
        val switchActivityIntent = Intent(this, MainActivity2::class.java)
        switchActivityIntent.putExtra("kwd", keyword)
        startActivity(switchActivityIntent)
    }
}
