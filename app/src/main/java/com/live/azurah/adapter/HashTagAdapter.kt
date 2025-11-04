import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.live.azurah.databinding.ItemHashtagBinding
import com.live.azurah.model.HashTagResponse
import com.live.azurah.model.Hashtag

class HashtagAdapter(
    private var hashtagList: ArrayList<HashTagResponse.Body.Data>,
    private val onHashtagClickListener: (HashTagResponse.Body.Data) -> Unit
) : RecyclerView.Adapter<HashtagAdapter.HashtagViewHolder>() {

    inner class HashtagViewHolder(private val binding: ItemHashtagBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(hashtag: HashTagResponse.Body.Data) {
            binding.tvHashTag.text = "#${hashtag.name}"
            binding.tvHashPost.text = "${hashtag.hashCount} public posts"

            binding.root.setOnClickListener {
                onHashtagClickListener(hashtag)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HashtagViewHolder {
        val binding = ItemHashtagBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HashtagViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HashtagViewHolder, position: Int) {
        holder.bind(hashtagList[position])
    }

    override fun getItemCount(): Int = hashtagList.size

    fun updateList(newList: List<HashTagResponse.Body.Data>) {
        hashtagList.clear()
        hashtagList.addAll(newList)
        notifyDataSetChanged()
    }

    fun addData(newList: List<HashTagResponse.Body.Data>) {
        val startPosition = hashtagList.size
        hashtagList.addAll(newList)
        notifyItemRangeInserted(startPosition, newList.size)
    }

    fun clearData() {
        hashtagList.clear()
        notifyDataSetChanged()
    }
}