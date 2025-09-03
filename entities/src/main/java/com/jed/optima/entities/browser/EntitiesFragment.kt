package com.jed.optima.entities.browser

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.jed.optima.entities.databinding.ListLayoutBinding
import com.jed.optima.entities.storage.Entity
import com.jed.optima.lists.RecyclerViewUtils
import com.jed.optima.lists.RecyclerViewUtils.matchParentWidth

class EntitiesFragment(private val viewModelFactory: ViewModelProvider.Factory) : Fragment() {

    private val entitiesViewModel by viewModels<EntitiesViewModel> { viewModelFactory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ListLayoutBinding.inflate(inflater, container, false).root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = ListLayoutBinding.bind(view)
        binding.list.layoutManager = LinearLayoutManager(requireContext())
        binding.list.addItemDecoration(RecyclerViewUtils.verticalLineDivider(requireContext()))

        val list = EntitiesFragmentArgs.fromBundle(requireArguments()).list
        entitiesViewModel.getEntities(list).observe(viewLifecycleOwner) {
            binding.list.adapter = EntitiesAdapter(it)
        }
    }
}

private class EntitiesAdapter(private val data: List<Entity.Saved>) :
    RecyclerView.Adapter<EntityViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): EntityViewHolder {
        return EntityViewHolder(parent.context)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(viewHolder: EntityViewHolder, position: Int) {
        val entity = data[position]
        viewHolder.setEntity(entity)
    }
}

private class EntityViewHolder(context: Context) : ViewHolder(EntityItemView(context)) {

    init {
        matchParentWidth()
    }

    fun setEntity(entity: Entity.Saved) {
        (itemView as EntityItemView).setEntity(entity)
    }
}
