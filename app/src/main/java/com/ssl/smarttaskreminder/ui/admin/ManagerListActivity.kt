package com.ssl.smarttaskreminder.ui.admin

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.ssl.smarttaskreminder.AppConstants
import com.ssl.smarttaskreminder.databinding.ActivityManagerListBinding
import com.ssl.smarttaskreminder.ui.adapters.ManagerAdapter
import com.ssl.smarttaskreminder.viewmodel.AdminViewModel

class ManagerListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityManagerListBinding
    private val viewModel: AdminViewModel by viewModels()
    private lateinit var adapter: ManagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManagerListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = "Managers"

        adapter = ManagerAdapter(
            onEditClick = { manager ->
                val intent = Intent(this, AddEditManagerActivity::class.java)
                intent.putExtra(AppConstants.EXTRA_IS_EDIT_MODE, true)
                intent.putExtra(AppConstants.EXTRA_MANAGER_ID, manager.documentId)
                startActivity(intent)
            },
            onDeleteClick = { manager ->
                MaterialAlertDialogBuilder(this)
                    .setTitle("Delete Manager")
                    .setMessage("Remove ${manager.name} from the system?")
                    .setPositiveButton("Delete") { _, _ -> viewModel.deleteManager(manager.documentId) }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        )

        binding.rvManagers.layoutManager = LinearLayoutManager(this)
        binding.rvManagers.adapter = adapter

        binding.fabAddManager.setOnClickListener {
            startActivity(Intent(this, AddEditManagerActivity::class.java))
        }

        observeViewModel()
        viewModel.loadManagers()
    }

    private fun observeViewModel() {
        viewModel.managers.observe(this) { managers ->
            adapter.submitList(managers)
            binding.tvEmpty.visibility     = if (managers.isEmpty()) View.VISIBLE else View.GONE
            binding.rvManagers.visibility  = if (managers.isEmpty()) View.GONE else View.VISIBLE
        }

        viewModel.loading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.success.observe(this) { msg ->
            msg?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_SHORT).show()
                viewModel.clearMessages()
            }
        }

        viewModel.error.observe(this) { err ->
            err?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                viewModel.clearMessages()
            }
        }
    }

    override fun onResume() { super.onResume(); viewModel.loadManagers() }
    override fun onSupportNavigateUp(): Boolean { finish(); return true }
}
