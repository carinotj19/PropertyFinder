package com.example.propertyfinderdashboard.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.propertyfinderdashboard.R
import com.example.propertyfinderdashboard.adapters.ChatAdapter
import com.example.propertyfinderdashboard.models.ChatModel
import io.github.serpro69.kfaker.Faker

class Allmessages : Fragment(), ChatAdapter.OnItemClickListener {

    private lateinit var chatAdapter: ChatAdapter
    private lateinit var chatArrayList: ArrayList<ChatModel>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_allmessages, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dataInitialize()

        val recyclerView: RecyclerView = view.findViewById(R.id.all_messages_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        chatAdapter = ChatAdapter(chatArrayList, this)
        recyclerView.adapter = chatAdapter


        chatAdapter.notifyDataSetChanged()
    }

    private fun dataInitialize() {
        chatArrayList = ArrayList()
        val faker = Faker()

        for (i in 0 until 20) {
            val profilePicture = R.drawable.person  // Replace with your logic for generating a random image
            val profileName = faker.name.name()
            val recentMessage = faker.chuckNorris.fact()
            val timestamp = generateRandomTimestamp()

            val chatModel = ChatModel(
                profilePicture,
                profileName,
                recentMessage,
                timestamp,
                isSentByCurrentUser = false // Assuming it's not sent by the current user
            )
            chatArrayList.add(chatModel)
        }
    }

    // Add a function to generate a random timestamp within the last 7 days
    private fun generateRandomTimestamp(): String {
        // Generate a random number between 3 and 59
        val randomValue = (3..1000).random()

        // Decide whether to use "mins" or "hrs" based on the random value
        val timeUnit = if (randomValue < 60) "mins" else "hrs"

        // Calculate the time difference in minutes or hours
        val timeDifference = if (randomValue < 60) randomValue else randomValue / 60

        return "$timeDifference $timeUnit ago"
    }

    override fun onItemClick(position: Int) {
        val name = chatArrayList[position].profileName
        Toast.makeText(requireContext(), "$name's conversation is currently private", Toast.LENGTH_SHORT).show()
    }
}
