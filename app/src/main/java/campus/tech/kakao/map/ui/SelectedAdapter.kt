package campus.tech.kakao.map.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import campus.tech.kakao.map.databinding.ItemSelectedBinding
import campus.tech.kakao.map.model.MapItem

class SelectedAdapter(

    private val onItemRemoved: (MapItem) -> Unit,
    private val onItemClicked: (MapItem) -> Unit

) : RecyclerView.Adapter<SelectedAdapter.SelectedViewHolder>() {

    private var items: List<MapItem> = emptyList()


    fun submitList(newItems: List<MapItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    //viewHolder 생성 메서드
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectedViewHolder {

        val binding = ItemSelectedBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SelectedViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SelectedViewHolder, position: Int) {
        val currentItem = items[position]
        holder.bind(currentItem)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class SelectedViewHolder(private val binding: ItemSelectedBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MapItem) {
            binding.apply {
                selectedItemName.text = item.place_name //상단에 이름만 표시
                deleteButton.setOnClickListener { onItemRemoved(item) }

                root.setOnClickListener { onItemClicked(item) } // 클릭 시 검색
            }
        }
    }
}