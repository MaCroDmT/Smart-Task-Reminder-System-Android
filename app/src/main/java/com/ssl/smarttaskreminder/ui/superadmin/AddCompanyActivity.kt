package com.ssl.smarttaskreminder.ui.superadmin

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.ssl.smarttaskreminder.databinding.ActivityAddCompanyBinding
import com.ssl.smarttaskreminder.viewmodel.CompanyViewModel

class AddCompanyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddCompanyBinding
    private val viewModel: CompanyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddCompanyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = "Add Company"

        binding.btnSave.setOnClickListener {
            val name     = binding.etCompanyName.text.toString().trim()
            val industry = binding.etIndustry.text.toString().trim()
            val status   = if (binding.switchStatus.isChecked) "active" else "inactive"

            viewModel.createCompany(name, industry, status)
        }

        viewModel.loading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnSave.isEnabled      = !isLoading
        }

        viewModel.success.observe(this) { msg ->
            msg?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_SHORT).show()
                viewModel.clearMessages()
                finish()
            }
        }

        viewModel.error.observe(this) { err ->
            err?.let {
                binding.tvError.text       = it
                binding.tvError.visibility = View.VISIBLE
                viewModel.clearMessages()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish(); return true
    }
}
