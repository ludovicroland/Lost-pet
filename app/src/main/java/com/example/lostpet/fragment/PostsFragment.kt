package com.example.lostpet.fragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.lostpet.Constants
import com.example.lostpet.R
import com.example.lostpet.itemAdapter.PostsAnimalItem
import com.example.lostpet.viewmodel.PostsViewModel
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.fragment_posts.*


class PostsFragment : Fragment() {

    private var groupAdapter = GroupAdapter<GroupieViewHolder>()
    private val postsViewModel by viewModels<PostsViewModel>()
    private lateinit var receiver: BroadcastReceiver

    companion object {
        fun newInstance(): PostsFragment {
            return PostsFragment()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.configureOnReceived()
        val intentFilter = IntentFilter(Constants.DELETE_ITEM)
        context?.registerReceiver(receiver, intentFilter)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_posts, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        posts_recyclerview.adapter = groupAdapter
        bindUi()

        postsViewModel.userAnimalList.observe(viewLifecycleOwner, Observer {
            if(it.isEmpty()){
                no_result.visibility = View.VISIBLE
            }

        })
    }

    private fun bindUi() {
        postsViewModel.itemList.observe(viewLifecycleOwner, Observer {
            updateRecyclerView(it)
        })
    }

    private fun updateRecyclerView(item: List<PostsAnimalItem>) {
        groupAdapter.update(item)
    }

    private fun configureOnReceived() {
        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val documentId = intent?.getStringExtra(Constants.ITEM_ID)
                if (documentId != null) {
                    postsViewModel.deleteItem(documentId)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        context?.unregisterReceiver(receiver)
    }
}