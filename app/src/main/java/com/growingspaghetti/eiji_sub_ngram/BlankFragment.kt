package com.growingspaghetti.eiji_sub_ngram

import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.growingspaghetti.eiji_sub_ngram.R
import java.util.*
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString

private const val ARG_KEYWORD = "ARG_KEYWORD"
private const val ARG_DICTIONARY = "ARG_DICTIONARY"


class BlankFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var keyword: String? = null
    private var dictionary: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            keyword = it.getString(ARG_KEYWORD)
            dictionary = it.getString(ARG_DICTIONARY)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_blank, container, false)
        val tv = v.findViewById<View>(R.id.textview) as TextView
        tv.text = Html.fromHtml(search(), Html.FROM_HTML_MODE_COMPACT)
        return v
    }

    companion object {
        @JvmStatic
        fun newInstance(keyword: String, dictionary: String) =
            BlankFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_KEYWORD, keyword)
                    putString(ARG_DICTIONARY, dictionary)
                }
            }
    }

    private fun search(): String {
        val (hits, truncate) = fetch(dict = dictionary!!, keyword = keyword!!)
        val prettyPrint = decorate(keyword!!, hits)
        return truncate.map { i ->
            String.format(
                "<div>%d件の合致候補があったため、500件で打ち切りました。</div>",
                i
            )
        }.orElse("") + prettyPrint.joinToString("<br>")
    }

    private fun reorder(hits: List<String>, input: String): List<String> {
        val a: MutableList<String> = mutableListOf()
        val b: MutableList<String> = mutableListOf()
        for (s in hits) {
            if (s.startsWith(input)) {
                a.add(s)
            } else {
                b.add(s)
            }
        }
        return a.plus(b)
    }

    private fun fetch(dict: String, keyword: String): Pair<List<String>, Optional<Int>> {
        val idx = Path(dict).toFile()
        val segInfo = ngramSearch(
            keyword,
            Path(idx.parent!!, idx.name.replace("_INDEX", "_NGRAM")).absolutePathString(),
            idx.absolutePath
        )
        val toSearch = if (segInfo.size > 500) {
            segInfo.subList(0, 500)
        } else {
            segInfo
        }
        val hits = loadThenFilter(
            keyword,
            toSearch,
            Path(idx.parent!!, idx.name.replace("_INDEX", "_TEXT")).absolutePathString()
        )
        return Pair(
            hits, if (segInfo != toSearch) {
                Optional.of(segInfo.size)
            } else {
                Optional.empty()
            }
        )
    }

    private fun decorate(input: String, hits: List<String>): List<String> {
        val highlightLeft = String.format(
            "</font><font color='olive'><b>%s</b></font><font color='#4a2400'>",
            input.replace("\t", "")
        )
        val highlightRight = String.format("<font color='olive'><b>%s</b></font>", input)

        val ordList = reorder(hits, input)
        return ordList.map {
            val tabIdx = it.indexOf("\t")
            val left = it.substring(0, tabIdx)
            val right = it.substring(tabIdx)
            String.format(
                "<font color='#4a2400'>%s</font>  %s",
                left.replace(input.replace("\t", ""), highlightLeft),
                right.replace("\\n", "<br>")
                    .replace("<ħ>", "<strike>")
                    .replace("</ħ>", "</strike>")
                    .replace(input, highlightRight)
            )
        }.toList()
    }
}