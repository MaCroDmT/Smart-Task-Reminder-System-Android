package com.ssl.smarttaskreminder.ui.manager

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.ssl.smarttaskreminder.databinding.ActivityUserListBinding
import com.ssl.smarttaskreminder.ui.adapters.UserAdapter
import com.ssl.smarttaskreminder.viewmodel.ManagerViewModel

class TeamUserListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserListBinding
    private val viewModel: ManagerViewModel by viewModels()
    private lateinit var adapter: UserAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = "My Team"

        // Manager can view but not edit users here (read-only)
        adapter = UserAdapter(
            onEditClick   = {},
            onDeleteClick = {}
        )

        binding.rvUsers.layoutManager = LinearLayoutManager(this)
        binding.rvUsers.adapter = adapter
        binding.fabAddUser.visibility = View.GONE  // Manager cannot add users

        viewModel.teamUsers.observe(this) { users ->
            adapter.submitList(users)
            binding.tvEmpty.visibility = if (users.isEmpty()) View.VISIBLE else View.GONE
            binding.rvUsers.visibility = if (users.isEmpty()) View.GONE else View.VISIBLE
        }

        viewModel.loadTeamUsers()
    }

    override fun onSupportNavigateUp(): Boolean { finish(); return true }
}
