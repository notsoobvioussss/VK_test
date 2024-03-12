package com.timur.vk_test

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ProductAdapter
    private lateinit var progressBar: ProgressBar

    private val productService: ProductService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://dummyjson.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(ProductService::class.java)
    }

    private var isLoading = false
    private var currentPage = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)
        progressBar = findViewById(R.id.progressBarBottom)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ProductAdapter()
        recyclerView.adapter = adapter
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
                val totalItemCount = layoutManager.itemCount
                if (!isLoading && lastVisibleItemPosition == totalItemCount - 1) {
                    loadNextPage()
                }
            }
        })

        fetchData()
    }

    private fun fetchData() {
        CoroutineScope(Dispatchers.Main).launch {
            isLoading = true
            progressBar.visibility = View.VISIBLE
            val response = productService.getProducts(skip = currentPage * 20, limit = 20)
            if (response.isSuccessful) {
                val products = response.body()?.products
                products?.let {
                    adapter.setData(it)
                    currentPage++
                }
            }
            isLoading = false
            progressBar.visibility = View.GONE
        }
    }

    private fun loadNextPage() {
        if (!isLoading) {
            fetchData()
        }
    }
}
