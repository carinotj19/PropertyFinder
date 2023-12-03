package com.example.propertyfinderdashboard.fragments

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.propertyfinderdashboard.R
import com.example.propertyfinderdashboard.helpers.SharedPreferencesHelper
import com.example.propertyfinderdashboard.adapters.SearchAdapter
import com.example.propertyfinderdashboard.models.SearchModel

class Search : Fragment() {

    private lateinit var container: FrameLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView

    private lateinit var recentSearchesLayout: ViewGroup
    private lateinit var searchResultsLayout: ViewGroup

    private val recentSearches = mutableListOf<SearchModel>()
    private lateinit var searchAdapter: SearchAdapter
    private var lastQuery: String? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        recentSearchesLayout = view.findViewById(R.id.recent_searches_layout)
        searchResultsLayout = view.findViewById(R.id.search_results_layout)
        this.container = view.findViewById(R.id.fragment_container)
        recyclerView = view.findViewById(R.id.recent_searches_recycler_view)
        searchView = view.findViewById(R.id.searchView)

        // Load recent searches from SharedPreferences using SharedPreferencesHelper
        loadRecentSearches()
        // Initialize RecyclerView and Adapter for recent searches
        recyclerView.layoutManager = LinearLayoutManager(context)
        searchAdapter = SearchAdapter(recentSearches)
        recyclerView.adapter = searchAdapter

        // Display recent searches by default
        switchToRecentSearches()

        // SearchView listener to switch to search results
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // Handle search query submission
                if (!query.isNullOrBlank() && !query.equals(lastQuery, ignoreCase = true)) {
                    // Add the search query to recent searches
                    addRecentSearch(query)
                    lastQuery = query
                }
                // Implement logic to show search results as needed
                switchToSearchResults(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Switch to search results when the user starts typing
                switchToSearchResults(newText)
                return true
            }
        })

        return view
    }

    private fun switchToRecentSearches() {
        recentSearchesLayout.visibility = View.VISIBLE
        searchResultsLayout.visibility = View.GONE
    }

    private fun switchToSearchResults(query: String?) {
        // Clear existing views in searchResultsLayout
        searchResultsLayout.removeAllViews()

        if (!query.isNullOrBlank()) {
            val dummyResults = listOf("Result 1", "Result 2", "Result 3")

            if (dummyResults.isNotEmpty()) {
                // Create a TextView to display the search query
                val queryTextView = TextView(context)
                queryTextView.text = "Search results for: $query"
                queryTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
                queryTextView.setPadding(16, 16, 16, 16)

                // Add the TextView to the search results layout
                searchResultsLayout.addView(queryTextView)

                // Create TextViews for each result and add them to the search results layout
                for (result in dummyResults) {
                    val resultTextView = TextView(context)
                    resultTextView.text = result
                    resultTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                    resultTextView.setPadding(16, 8, 16, 8)

                    searchResultsLayout.addView(resultTextView)
                }

                // Show searchResultsLayout and hide recentSearchesLayout
                searchResultsLayout.visibility = View.VISIBLE
                recentSearchesLayout.visibility = View.GONE
            } else {
                // If the search results list is empty, show a message
                val noResultsTextView = TextView(context)
                noResultsTextView.text = "No results found."
                noResultsTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
                noResultsTextView.setPadding(16, 16, 16, 16)

                searchResultsLayout.addView(noResultsTextView)

                // Show searchResultsLayout and hide recentSearchesLayout
                searchResultsLayout.visibility = View.VISIBLE
                recentSearchesLayout.visibility = View.GONE
            }
        } else {
            // If the query is blank, switch to recent searches
            switchToRecentSearches()
        }
    }

    private fun loadRecentSearches() {
        // Load recent searches from SharedPreferences using SharedPreferencesHelper
        recentSearches.clear()
        recentSearches.addAll(SharedPreferencesHelper.loadRecentSearches(requireContext()))
    }

    private fun saveRecentSearches() {
        // Save recent searches to SharedPreferences using SharedPreferencesHelper
        SharedPreferencesHelper.saveRecentSearches(requireContext(), recentSearches)
    }

    private fun addRecentSearch(query: String) {
        // Add a new search item to the list
        val searchItem = SearchModel(query)
        recentSearches.add(0, searchItem) // Add to the beginning of the list

        // Limit the list size if needed
        if (recentSearches.size > MAX_RECENT_SEARCHES) {
            recentSearches.removeAt(recentSearches.size - 1)
        }

        // Update the RecyclerView via the adapter
        searchAdapter.notifyDataSetChanged()
        Toast.makeText(context, "Recent Searches Updated", Toast.LENGTH_SHORT).show()

        // Save recent searches to SharedPreferences
        saveRecentSearches()
    }
    companion object {
        private const val MAX_RECENT_SEARCHES = 5 // Adjust the maximum number of recent searches as needed
    }
}
