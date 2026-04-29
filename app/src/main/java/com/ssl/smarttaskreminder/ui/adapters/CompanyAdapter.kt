package com.ssl.smarttaskreminder.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ssl.smarttaskreminder.AppConstants
import com.ssl.smarttaskreminder.data.model.Company
import com.ssl.smarttaskreminder.databinding.ItemCompanyBinding

class CompanyAdapter(
    private val onItemClick: (Company) -> Unit,
    private val onToggleStatus: (Company) -> Unit
) : ListAdapter<Company, CompanyAdapter.ViewHolder>(DiffCallback) {

    inner class ViewHolder(private val binding: ItemCompanyBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(company: Company) {
            binding.tvCompanyName.text = company.name
            binding.tvCompanyCid.text  = "CID: ${company.cid}"
            binding.tvCompanySlug.text = company.slug
            binding.tvStatus.text      = company.status.uppercase()

            val isActive = company.status == AppConstants.COMPANY_ACTIVE
            binding.tvStatus.setBackgroundResource(
                if (isActive) com.ssl.smarttaskreminder.R.drawable.bg_button_success
                else com.ssl.smarttaskreminder.R.drawable.bg_button_danger
            )

            binding.root.setOnClickListener { onItemClick(company) }
            binding.btnToggleStatus.setOnClickListener { onToggleStatus(company) }
            binding.btnToggleStatus.text = if (isActive) "Deactivate" else "Activate"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemCompanyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))

    companion object {
        val DiffCallback = object : DiffUtil.ItemCallback<Company>() {
            override fun areItemsTheSame(old: Company, new: Company) = old.cid == new.cid
            override fun areContentsTheSame(old: Company, new: Company) = old == new
        }
    }
}
