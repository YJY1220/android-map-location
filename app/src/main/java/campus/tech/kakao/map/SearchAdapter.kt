// SearchAdapter.kt

package campus.tech.kakao.map

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import campus.tech.kakao.map.databinding.ItemSearchResultBinding

class SearchAdapter(
    private val onItemClicked: (MapItem) -> Unit  // 아이템 클릭 시 호출되는 함수
) : RecyclerView.Adapter<SearchAdapter.SearchViewHolder>() {

    private var items: List<MapItem> = emptyList()  // 현재 목록을 저장하는 리스트

    // 데이터를 갱신
    fun submitList(newItems: List<MapItem>) {
        items = newItems
        notifyDataSetChanged()  // 전체 데이터 갱신
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {

        val binding = ItemSearchResultBinding.inflate(
            LayoutInflater.from(parent.context),  // LayoutInflater 생성
            parent,
            false
        )
        return SearchViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        val currentItem = items[position]  // 지금 데이터 가져오기
        holder.bind(currentItem)
    }

    // RecyclerView 데이터 개수 반환
    override fun getItemCount(): Int {
        return items.size
    }

    inner class SearchViewHolder(private val binding: ItemSearchResultBinding) :
        RecyclerView.ViewHolder(binding.root) {  // 루트 view 초기화
        // 데이터 view에 바인딩
        fun bind(item: MapItem) {
            binding.apply {
                itemName.text = item.place_name
                itemAddress.text = item.road_address_name
                itemCategory.text = item.category_group_name
                root.setOnClickListener { onItemClicked(item) }
            }
        }
    }
}
