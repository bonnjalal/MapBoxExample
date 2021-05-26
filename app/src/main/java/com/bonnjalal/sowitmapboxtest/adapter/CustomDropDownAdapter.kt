package com.bonnjalal.sowitmapboxtest.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.bonnjalal.sowitmapboxtest.databinding.CustomSpinnerItemBinding
import com.bonnjalal.sowitmapboxtest.model.PolygonModel


class CustomDropDownAdapter(val context: Context) : BaseAdapter() {

    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    lateinit var binding : CustomSpinnerItemBinding
    private var dataSource: ArrayList<PolygonModel> = ArrayList()
    private var onSelectedListener: CustomDropDownAdapter.SpinnerListener? = null

    fun setSpinnerSelectListener(onSelectedListener: CustomDropDownAdapter.SpinnerListener){
        this.onSelectedListener = onSelectedListener
    }
    fun setDataList(dataSource: ArrayList<PolygonModel>){
        this.dataSource = dataSource
    }
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        val view: View
        if (convertView == null) {
            binding = CustomSpinnerItemBinding.inflate(inflater)
            view = binding.root

        } else {
            view = convertView

        }

        binding.polygonName.text = dataSource[position].polygonName
        binding.polygonLocation.text = dataSource[position].polygonLocation
        binding.polygonArea.text = dataSource[position].polygonArea

        if (position == 0) {
            // Set the hint text color gray
            binding.polygonName.setTextColor(Color.GRAY)
            binding.polygonLocation.visibility = View.INVISIBLE
            binding.polygonArea.visibility = View.INVISIBLE
        } else {
            binding.polygonName.setTextColor(Color.WHITE)
            binding.polygonLocation.setTextColor(Color.WHITE)
            binding.polygonArea.setTextColor(Color.WHITE)
        }
        return view
    }

    override fun getItem(position: Int): Any {
        return dataSource[position];
    }

    override fun getCount(): Int {
        return dataSource.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong();
    }
    interface SpinnerListener {
        fun spinnerSelectedItem(v: View?, position: Int)
    }

    /*
    private class ItemHolder(row: View?) {
        init {
        }
    }

     */

    override fun isEnabled(position: Int): Boolean {
        return position != 0
    }
}