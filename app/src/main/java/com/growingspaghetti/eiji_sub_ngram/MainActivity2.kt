package com.growingspaghetti.eiji_sub_ngram

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import java.io.File

class MainActivity2 : AppCompatActivity() {
    private lateinit var viewPager: ViewPager2
    private lateinit var indices: List<File>
    private lateinit var keyword: String
    private lateinit var fragments: MutableList<BlankFragment?>

    fun getFragmentAt(i: Int): Fragment? {
        if (fragments[i] == null) {
            fragments[i] = BlankFragment.newInstance(keyword, indices[i].absolutePath)
        }
        return fragments[i]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        val dirs = this.getExternalFilesDirs(null);
        val dir = dirs[dirs.size - 1];
        dir.mkdirs()
        keyword = intent.extras!!.getString("kwd")!!
        indices = dir.listFiles()!!.filter { it.name.endsWith("_INDEX") }.toList().sorted()

        viewPager = findViewById(R.id.pager)
        val pagerAdapter = ScreenSlidePagerAdapter(this)
        viewPager.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                title = if (position != indices.size) {
                    indices[position].name.replace("_INDEX", "")
                } else {
                    "ngram検索氏"
                }
            }
        })
        viewPager.adapter = pagerAdapter
        viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        fragments = MutableList(indices.size) { null }
    }

    private inner class ScreenSlidePagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
        override fun getItemCount(): Int {
            return indices.size + 1
        }

        override fun createFragment(position: Int): Fragment {
            if (position != indices.size) {
                return getFragmentAt(position)!!
            }
            return BlankFragment2()
        }
    }
}