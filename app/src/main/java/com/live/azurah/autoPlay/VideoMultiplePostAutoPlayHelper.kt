package com.live.azurah.autoPlay

import android.content.Context
import android.graphics.Rect
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import com.live.azurah.R
import com.live.azurah.adapter.PostDetailAdapter
import com.live.azurah.adapter.PostDetailImagesAdapter
import com.live.azurah.model.PostResponse
import com.live.azurah.player.InstaLikePlayerView2
import com.live.azurah.retrofit.ApiConstants

/**
 * Create By Saurabh Thakur
 */
class VideoMultiplePostAutoPlayHelper(private var recyclerView: RecyclerView, private var list: MutableList<PostResponse.Body.Data>,var cntxt:Context,val listener: ScrollListener) {
    private var lastPlayerView: InstaLikePlayerView2? = null
    private var rvView:RecyclerView? = null
    var pos = -1
    private val TAG="BROADCAST_NOTIFICATION"
    private var oldPos=-1
    private val MIN_LIMIT_VISIBILITY = 20
    private var currentPlayingVideoItemPos = -1

    fun setList(newList:MutableList<PostResponse.Body.Data>){
        list = newList
    }

    fun onScrolled(recyclerView: RecyclerView) {
        val firstVisiblePosition: Int = findFirstVisibleItemPosition()
        val lastVisiblePosition: Int = findLastVisibleItemPosition()

        if (list.isNotEmpty()){
            pos = getMostVisibleItem(firstVisiblePosition, lastVisiblePosition)

            val view = recyclerView.layoutManager?.findViewByPosition(pos)
            rvView = view!!.findViewById(R.id.rvPosts)
            rvView!!.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (recyclerView.adapter is PostDetailImagesAdapter) {
                        onScrolled1()
                    } else {
                        throw IllegalStateException("Adapter should be FeedAdapter or extend FeedAdapter")
                    }
                }

                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    Log.d("positionnn",newState.toString())
                }
            })

            val snapHelper: SnapHelper? = recyclerView.getTag(R.id.snap_helper_tag) as? SnapHelper

            val centerView: View? = snapHelper?.findSnapView(rvView!!.layoutManager)
            Log.d("lksdjkff",centerView.toString())

            try {
                val pos1 = recyclerView.layoutManager!!.getPosition(centerView!!)
                if (pos == -1) {

                    Log.d("kjsdgfnnnn","pos======  "+pos.toString())
                    if (currentPlayingVideoItemPos != - 1) {
                        val viewHolder: RecyclerView.ViewHolder = rvView?.findViewHolderForAdapterPosition(currentPlayingVideoItemPos)!!
                        val currentVisibility = getVisiblePercentage(viewHolder)
                        if (currentVisibility < MIN_LIMIT_VISIBILITY) {
                            lastPlayerView?.removePlayer()
                        }
                        currentPlayingVideoItemPos = -1
                    }
                }
                else {
                    Log.d("kjsdgfnnnn","pos======  "+pos.toString())
                    Log.d("kjsdgfnnn","pos1======  "+pos1.toString())
                    if (pos1 == -1){
                        if (currentPlayingVideoItemPos != -1) {
                            val viewHolder: RecyclerView.ViewHolder = rvView?.findViewHolderForAdapterPosition(currentPlayingVideoItemPos)!!
                            val currentVisibility = getVisiblePercentage(viewHolder);
                            if (currentVisibility < MIN_LIMIT_VISIBILITY) {
                                lastPlayerView?.removePlayer()
                            }
                            currentPlayingVideoItemPos = -1
                        }
                    }else{
                        currentPlayingVideoItemPos = pos1
                        Log.d("khghjdfdsjf",currentPlayingVideoItemPos.toString())
                        attachVideoPlayerAt(pos1);
                    }
                }
            }catch (e:Exception){}

        }
    }

    fun pausePlayer(){
        if (lastPlayerView != null){
            lastPlayerView?.pausePlayer()
        }
    }

    fun restartPlayer(){
        if (lastPlayerView != null){
            lastPlayerView?.resumePlayer()
        }
    }

    fun removePlayer(){
        if (lastPlayerView != null){
            lastPlayerView?.removePlayer()
        }
    }

    fun onScrolled1() {
        val snapHelper: SnapHelper? = recyclerView.getTag(R.id.snap_helper_tag) as? SnapHelper

        val centerView: View? = snapHelper?.findSnapView(rvView!!.layoutManager)
            val pos1 = rvView?.layoutManager!!.getPosition(centerView!!)
            Log.d("nfjdsfdfdg",pos1.toString())
            if (pos == -1) {
                if (currentPlayingVideoItemPos != - 1) {
                    val viewHolder: RecyclerView.ViewHolder = rvView?.findViewHolderForAdapterPosition(currentPlayingVideoItemPos)!!

                    val currentVisibility = getVisiblePercentage(viewHolder);
                    if (currentVisibility < MIN_LIMIT_VISIBILITY) {
                        lastPlayerView?.removePlayer()
                    }
                    currentPlayingVideoItemPos = -1;
                }
            }
            else {
                Log.d("kjsdgf","pos======  "+pos.toString())
                Log.d("kjsdgf","pos1======  "+pos1.toString())
                if (pos1== -1){
                    if (currentPlayingVideoItemPos != -1) {
                        val viewHolder: RecyclerView.ViewHolder =
                            rvView?.findViewHolderForAdapterPosition(currentPlayingVideoItemPos)!!

                        val currentVisibility = getVisiblePercentage(viewHolder);
                        if (currentVisibility < MIN_LIMIT_VISIBILITY) {
                            lastPlayerView?.removePlayer()
                        }
                        currentPlayingVideoItemPos = -1;
                    }
                }else{
                    currentPlayingVideoItemPos = pos1
                    attachVideoPlayerAt(pos1);
                }
            }
    }

    private fun attachVideoPlayerAt(pos1: Int) {

        try {
            val feedViewHolder: PostDetailImagesAdapter.FeedImageVideoViewHolder =
                (rvView?.findViewHolderForAdapterPosition(pos1) as PostDetailImagesAdapter.FeedImageVideoViewHolder?)!!
            if(list[pos].post_images?.get(pos1)?.type == 2) {
                if (lastPlayerView==null || lastPlayerView != feedViewHolder.binding.playerView) {
                    feedViewHolder.binding.playerView.startPlaying()
                    lastPlayerView?.removePlayer()
                    lastPlayerView = null
                }
                lastPlayerView = feedViewHolder.binding.playerView
                lastPlayerView?.setMuted(ApiConstants.isMute)

            } else {
                if (lastPlayerView != null) {
                    lastPlayerView?.removePlayer();
                    lastPlayerView = null
                }

            }
        }catch(e:Exception){
            if (lastPlayerView != null) {
                lastPlayerView?.removePlayer();
                lastPlayerView = null
            }

        }

    }

    private fun getMostVisibleItem(firstVisiblePosition: Int, lastVisiblePosition: Int): Int {
        var pos = 0
        Log.d("kjfjdgjfg",list.size.toString())
            var maxPercentage = -1

            for (i in firstVisiblePosition..lastVisiblePosition) {
                val viewHolder: RecyclerView.ViewHolder =
                    recyclerView.findViewHolderForAdapterPosition(i)!!

                var currentPercentage = getVisiblePercentage(viewHolder)
                if (currentPercentage > maxPercentage) {
                    maxPercentage = currentPercentage.toInt()
                    pos = i
                }
            }

            if (maxPercentage == -1 || maxPercentage < MIN_LIMIT_VISIBILITY) {
                return -1
            }
        return pos
    }

    private fun getVisiblePercentage(holder: RecyclerView.ViewHolder): Float {
        val rect = Rect()
        val isVisible = holder.itemView.getLocalVisibleRect(rect)
        if (!isVisible) return 0f

        val visibleArea = rect.width() * rect.height()
        val totalArea = holder.itemView.width * holder.itemView.height
        return (visibleArea.toFloat() / totalArea) * 100
    }

   /* private fun getVisiblePercentage(holder: RecyclerView.ViewHolder): Float {
        val rect_parent = Rect()
        recyclerView.getGlobalVisibleRect(rect_parent)
        val location = IntArray(2)
        holder.itemView.getLocationOnScreen(location)

        val rect_child = Rect(location[0], location[1], location[0] + holder.itemView.getWidth(), location[1] + holder.itemView.getHeight())
        val rect_parent_area = ((rect_child.right - rect_child.left) * (rect_child.bottom - rect_child.top)).toFloat()
        val x_overlap = Math.max(0, Math.min(rect_child.right, rect_parent.right) - Math.max(rect_child.left, rect_parent.left)).toFloat()
        val y_overlap = Math.max(0, Math.min(rect_child.bottom, rect_parent.bottom) - Math.max(rect_child.top, rect_parent.top)).toFloat()
        val overlapArea = x_overlap * y_overlap
        val percent = overlapArea / rect_parent_area * 100.0f
        return percent
    }*/

    private fun findFirstVisibleItemPosition(): Int {
        if (recyclerView.layoutManager is LinearLayoutManager) {
            return (recyclerView.layoutManager as LinearLayoutManager?)!!.findFirstVisibleItemPosition()
        }
        return -1
    }

    private fun findLastVisibleItemPosition(): Int {
        if (recyclerView.getLayoutManager() is LinearLayoutManager) {
            return (recyclerView.getLayoutManager() as LinearLayoutManager).findLastVisibleItemPosition()
        }
        return -1
    }

    fun startObserving() {
        recyclerView.clearOnScrollListeners()
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!recyclerView.canScrollVertically(1)) {
                    listener.listener()
                }

                if (recyclerView.adapter is PostDetailAdapter) {
                    onScrolled(recyclerView)
                } else {
                    throw IllegalStateException("Adapter should be FeedAdapter or extend FeedAdapter")
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                Log.d("fkjsdfbkjdfd",newState.toString())
                onStateChanged(recyclerView,newState)
            }
        })
    }

    fun onStateChanged(recyclerView: RecyclerView,pos1: Int) {
        val firstVisiblePosition: Int = findFirstVisibleItemPosition()
        val lastVisiblePosition: Int = findLastVisibleItemPosition()

        if (list.isNotEmpty()){

            val viewHolder = rvView?.findViewHolderForAdapterPosition(pos1) as? PostDetailImagesAdapter.FeedImageVideoViewHolder
            viewHolder?.binding?.ivSound?.setImageResource(if(ApiConstants.isMute) R.drawable.volume_off else R.drawable.volume)
            Log.d("fkjgdkfjddgdf",ApiConstants.isMute.toString())
            lastPlayerView?.setMuted(ApiConstants.isMute)
//            optimizeRenderHints()
            for (position in firstVisiblePosition..lastVisiblePosition) {
                val view = recyclerView.layoutManager?.findViewByPosition(position)
                if (view != null) {
                    val rvView: RecyclerView? = view.findViewById(R.id.rvPosts)
                    rvView?.let {
                        val holder = it.findViewHolderForAdapterPosition(position) as? PostDetailImagesAdapter.FeedImageVideoViewHolder
                        holder?.binding?.ivSound?.setImageResource(
                            if (ApiConstants.isMute) R.drawable.volume_off else R.drawable.volume
                        )
                    }
                }
            }
        }
    }


}

interface ScrollListener{
    fun listener()
}