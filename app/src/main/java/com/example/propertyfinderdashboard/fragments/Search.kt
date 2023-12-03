package com.example.propertyfinderdashboard.fragments

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.propertyfinderdashboard.PropertyDetails
import com.example.propertyfinderdashboard.R
import com.example.propertyfinderdashboard.adapters.PropertyAdapter
import com.example.propertyfinderdashboard.helpers.SharedPreferencesHelper
import com.example.propertyfinderdashboard.adapters.SearchAdapter
import com.example.propertyfinderdashboard.models.PropertyModel
import com.example.propertyfinderdashboard.models.SearchModel
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import io.github.serpro69.kfaker.Faker
import kotlin.random.Random

class Search : Fragment(), PropertyAdapter.OnItemClickListener  {

    private lateinit var searchContainer: FrameLayout
    private lateinit var resultsRecyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var progressBar: ProgressBar

    private lateinit var recentSearchesLayout: ViewGroup

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val recentSearches = mutableListOf<SearchModel>()
    private lateinit var searchAdapter: SearchAdapter

    private lateinit var propertyAdapter: PropertyAdapter
    private lateinit var propertyList: List<PropertyModel>

    private var lastQuery: String? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        recentSearchesLayout = view.findViewById(R.id.recent_searches_layout)
        searchContainer = view.findViewById(R.id.search_fragment_container)
        resultsRecyclerView = view.findViewById(R.id.search_results_recycler_view)
        searchView = view.findViewById(R.id.searchView)
        progressBar = view.findViewById(R.id.progress_bar)

        // Load recent searches from SharedPreferences using SharedPreferencesHelper
        loadRecentSearches()
        // Initialize RecyclerView and Adapter for recent searches
        resultsRecyclerView.layoutManager = LinearLayoutManager(context)
        searchAdapter = SearchAdapter(recentSearches)
        resultsRecyclerView.adapter = searchAdapter

        resultsRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        propertyAdapter = PropertyAdapter(emptyList(), this) // Initialize adapter with empty list
        resultsRecyclerView.adapter = propertyAdapter

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

                progressBar.visibility = View.GONE
                searchContainer.visibility = View.VISIBLE

                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {

                progressBar.visibility = View.VISIBLE
                searchContainer.visibility = View.GONE
                propertyAdapter.updateData(emptyList())
                resultsRecyclerView.visibility = View.GONE

                return true
            }
        })

        return view
    }

    private fun switchToRecentSearches() {
        recentSearchesLayout.visibility = View.VISIBLE
        resultsRecyclerView.visibility = View.GONE
    }

    private fun switchToSearchResults(query: String?) {
        // Clear existing views in searchResultsLayout
        resultsRecyclerView.removeAllViews()
        propertyAdapter.updateData(emptyList())

        if (!query.isNullOrBlank()) {
            // Perform a Firestore query based on the search query
            performFirestoreQuery(query)
        } else {
            // If the query is blank, switch to recent searches
            switchToRecentSearches()
        }
    }

    private fun isUserAdminOrSeller(uid: String?, callback: (Boolean) -> Unit) {
        val userRef = FirebaseFirestore.getInstance().collection("Users").document(uid ?: "")
        userRef.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    when (documentSnapshot.getString("Role")) {
                        "admin", "seller" -> {
                            // User is an admin or seller
                            callback(true)
                        }
                        else -> {
                            // User is not an admin or seller
                            callback(false)
                        }
                    }
                } else {
                    // User document does not exist
                    callback(false)
                }
            }
            .addOnFailureListener {
                // Error occurred during the Firestore operation
                callback(false)
            }
    }

    private fun performFirestoreQuery(query: String) {
            // Reference to the "properties" collection in Firestore
            val propertiesCollection = db.collection("Properties")

            // Reference to the "pendingproperties" collection in Firestore
            val pendingPropertiesCollection = db.collection("PendingProperties")

            // Get the current user's UID
            val uid = auth.currentUser?.uid

            isUserAdminOrSeller(uid) { isAdminOrSeller ->
                if (isAdminOrSeller) {
                    // If the user is an admin or seller, search both "properties" and "pendingproperties"
                    performRoleSpecificQuery(propertiesCollection, query)
                    performRoleSpecificQuery(pendingPropertiesCollection, query)
                } else {
                    // If the user is a regular user, only search "properties"
                    performRoleSpecificQuery(propertiesCollection, query)
                }
            }

    }

    private fun performRoleSpecificQuery(propertiesCollection: CollectionReference, query: String) {
        // Query for properties where any substring of length 3 or more is in the 'propertyName' field
        val queryTask: Task<QuerySnapshot> =
            propertiesCollection.orderBy("propertyName")
                .startAt(query)
                .endAt(query + "\uf8ff")
                .get()


        Log.d("Firebase Query", "Field: propertyName, Query: $query")

        queryTask.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Handle the query results
                val queryResults = task.result?.documents

                Log.d("Firebase Query", "Results: $queryResults")
                if (!queryResults.isNullOrEmpty()) {
                    propertyList = queryResults.map { document ->
                        PropertyModel(
                            document.id,
                            document.getString("propertyName") ?: "",
                            document.getString("propertyImage") ?: "",
                            document.getString("location") ?: "",
                            document.getDouble("price") ?: 0.0,
                            document.getDouble("rating") ?: 0.0
                        )
                    }
                    // Update the RecyclerView with the search results
                    propertyAdapter.updateData(propertyList)

                    // Show searchResultsLayout and hide recentSearchesLayout
                    resultsRecyclerView.visibility = View.VISIBLE
                    recentSearchesLayout.visibility = View.GONE
                } else {
                    // If no results found, show a message
                    val noResultsTextView = TextView(context)
                    noResultsTextView.text = "No results found."
                    noResultsTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
                    noResultsTextView.setPadding(16, 16, 16, 16)

                    resultsRecyclerView.addView(noResultsTextView)

                    // Show searchResultsLayout and hide recentSearchesLayout
                    resultsRecyclerView.visibility = View.VISIBLE
                    recentSearchesLayout.visibility = View.GONE
                }
            } else {
                // Handle the error
                val exception = task.exception
                // Log or show an error message
                Log.e("Firebase Query", "Error: $exception")
            }
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

    private fun addToRecentViewed(clickedItem: PropertyModel) {
        val db = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        currentUser?.let { user ->
            val userId = user.uid
            val recentlyViewedCollection = db.collection("Users").document(userId).collection("RecentlyViewed")

            val viewed: MutableMap<String, Any> = HashMap()
            viewed["propertyName"] = clickedItem.propertyName
            viewed["location"] = clickedItem.location
            viewed["price"] = clickedItem.price
            viewed["propertyImage"] = clickedItem.propertyImage
            viewed["rating"] = clickedItem.rating
            viewed["timestamp"] = FieldValue.serverTimestamp()

            recentlyViewedCollection
                .add(viewed)
                .addOnSuccessListener { documentReference ->
                    Log.d(ContentValues.TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
                }
                .addOnFailureListener { e ->
                    Log.w(ContentValues.TAG, "Error adding document", e)
                }
        }
    }

    override fun onItemClick(position: Int) {
        val clickedItem = propertyList[position]
        addToRecentViewed(clickedItem)

        val faker = Faker()

        val landmark = faker.address.streetName()
        val ownerName = faker.name.name()
        val description = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."
        val availability = Random.nextBoolean()

        val intent = Intent(requireContext(), PropertyDetails::class.java)
        intent.putExtra("PROPERTY_IMAGE", clickedItem.propertyImage)
        intent.putExtra("PROPERTY_NAME", clickedItem.propertyName)
        intent.putExtra("PROPERTY_PRICING", clickedItem.price)
        intent.putExtra("PROPERTY_RATING", clickedItem.rating)

        intent.putExtra("PROPERTY_LOCATION", clickedItem.location)

        intent.putExtra("PROPERTY_LANDMARKS", landmark)
        intent.putExtra("PROPERTY_OWNER_NAME", ownerName)
        intent.putExtra("PROPERTY_DESCRIPTION", description)
        intent.putExtra("PROPERTY_AVAILABILITY", availability)
        startActivity(intent)
    }
    companion object {
        private const val MAX_RECENT_SEARCHES = 5 // Adjust the maximum number of recent searches as needed
    }
}
