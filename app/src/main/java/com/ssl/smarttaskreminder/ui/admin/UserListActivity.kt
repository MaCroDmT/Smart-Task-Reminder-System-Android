package com.ssl.smarttaskreminder.ui.admin

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.ssl.smarttaskreminder.AppConstants
import com.ssl.smarttaskreminder.data.model.Manager
import com.ssl.smarttaskreminder.databinding.ActivityUserListBinding
import com.ssl.smarttaskreminder.ui.adapters.UserAdapter
import com.ssl.smarttaskreminder.viewmodel.AdminViewModel
import androidx.recyclerview.widget.LinearLayoutManager

class UserListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserListBinding
    private val viewModel: AdminViewModel by viewModels()
    private lateinit var adapter: UserAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = "Users"

        adapter = UserAdapter(
            onEditClick = { user ->
                val intent = Intent(this, AddEditUserActivity::class.java)
                intent.putExtra(AppConstants.EXTRA_IS_EDIT_MODE, true)
                intent.putExtra(AppConstants.EXTRA_USER_ID, user.documentId)
                startActivity(intent)
            },
            onDeleteClick = { user ->
                MaterialAlertDialogBuilder(this)
                    .setTitle("Delete User")
                    .setMessage("Remove ${user.name} from the system?")
                    .setPositiveButton("Delete") { _, _ -> viewModel.deleteUser(user.documentId) }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        )

        binding.rvUsers.layoutManager = LinearLayoutManager(this)
        binding.rvUsers.adapter = adapter

        binding.fabAddUser.setOnClickListener {
            startActivity(Intent(this, AddEditUserActivity::class.java))
        }

        observeViewModel()
        viewModel.loadUsers()
        viewModel.loadManagers() // Needed for assignment in add user
    }

    private fun observeViewModel() {
        viewModel.users.observe(this) { users ->
            adapter.submitList(users)
            binding.tvEmpty.visibility = if (users.isEmpty()) View.VISIBLE else View.GONE
            binding.rvUsers.visibility = if (users.isEmpty()) View.GONE else View.VISIBLE
        }

        viewModel.loading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.success.observe(this) { msg ->
            msg?.let { Snackbar.make(binding.root, it, Snackbar.LENGTH_SHORT).show(); viewModel.clearMessages() }
        }

        viewModel.error.observe(this) { err ->
            err?.let { Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show(); viewModel.clearMessages() }
        }
    }

    override fun onResume() { super.onResume(); viewModel.loadUsers() }
    override fun onSupportNavigateUp(): Boolean { finish(); return true }
}
