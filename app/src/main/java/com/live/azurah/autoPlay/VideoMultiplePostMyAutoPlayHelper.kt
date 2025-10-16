package com.live.azurah.autoPlay

import android.content.Context
import android.graphics.Rect
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import com.google.android.material.tabs.TabLayout
import com.live.azurah.R
import com.live.azurah.adapter.PostDetailAdapter
import com.live.azurah.adapter.PostDetailImagesAdapter
import com.live.azurah.model.PostResponse
import com.live.azurah.player.InstaLikePlayerView2
import com.live.azurah.retrofit.ApiConstants
import com.live.azurah.util.gone
import com.live.azurah.util.visible

/**
 * Create By Saurabh Thakur
 */
class VideoMultiplePostMyAutoPlayHelper(private var recyclerView: RecyclerView, private var list: MutableList<PostResponse.Body.Data>, var cntxt:Context,val listener: ScrollListener) {
    private var lastPlayerView: InstaLikePlayerView2? = null
    private var rvView:RecyclerView? = null
    var pos = -1
    private val TAG="BROADCAST_NOTIFICATION"
    private var oldPos=-1
    private val MIN_LIMIT_VISIBILITY = 20
    private var currentPlayingVideoItemPos = -1

    fun setList(newList:MutableList<PostResponse.Body.Data>){
        list = newList
        Log.d("dffdhdfhf",list.size.toString())
    }
    fun liseSize(fragmentlist: MutableList<PostResponse.Body.Data>) {
//        Log.d("kjfjdgjfg","VideoAutoPlayHelper========"+ this.list.hashCode().toString())
//        Log.d("kjfjdgjfg","fragmentlist========"+ fragmentlist.hashCode().toString())
        if(fragmentlist===list){
            Log.d("kjfjdgjfg", "liseSize: "+"Objects are same")
        }else{
            Log.d("kjfjdgjfg", "liseSize: "+"Objects are not same")
        }
    }

    fun onScrolled(recyclerView: RecyclerView) {

        Log.d("hhhhhhhhhfggf","vcbbcbcbvbbcv")
        val firstVisiblePosition: Int = findFirstVisibleItemPosition()
        val lastVisiblePosition: Int = findLastVisibleItemPosition()

        if (list.isNotEmpty()){
            pos = getMostVisibleItem(firstVisiblePosition, lastVisiblePosition)

            /*for (position in firstVisiblePosition..lastVisiblePosition) {
                val view = recyclerView.layoutManager?.findViewByPosition(position)
                if (view != null) {
                    val rvView: RecyclerView? = view.findViewById(R.id.rvPosts)
                    rvView?.let {
                        val holder = it.findViewHolderForAdapterPosition(position) as? PostDetailImagesAdapter.FeedImageVideoViewHolder
                      Log.d("dfdsdsgsdg",lastPlayerView?.isPlaying().toString())
                        if (holder?.binding?.playerView?.isPlaying() == true){
                            holder.binding.ivSound.visible()
                        }else{
                            holder?.binding?.ivSound?.gone()
                        }
                        holder?.binding?.ivSound?.setImageResource(
                            if (ApiConstants.isMute) R.drawable.volume_off else R.drawable.volume
                        )
                    }
                }
            }*/

          /*  val view55 = recyclerView.layoutManager?.findViewByPosition(pos)
            if (view55 != null) {
                val rvView: RecyclerView? = view55.findViewById(R.id.rvPosts)
                rvView?.let {
                    val holder =
                        it.findViewHolderForAdapterPosition(pos) as? PostDetailImagesAdapter.FeedImageVideoViewHolder
                    Log.d("dfdsdsgsdg", lastPlayerView?.isPlaying().toString())
                    holder?.binding?.ivSound?.visible()
                }
            }*/

            val view = recyclerView.layoutManager?.findViewByPosition(pos)
            rvView = view!!.findViewById(R.id.rvPosts)
            rvView!!.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (recyclerView.adapter is PostDetailImagesAdapter) {
                        onScrolled1()
                        Log.d("lksdjkff","jjjjjjjjjjjjjjjjjjjj")
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


    private fun getMostVisibleItem1(firstVisiblePosition: Int, lastVisiblePosition: Int): Int {
        var maxPercentage = -1
        var pos = 0
        for (i in firstVisiblePosition..lastVisiblePosition) {
            val viewHolder: RecyclerView.ViewHolder =
                rvView?.findViewHolderForAdapterPosition(i)!!

            var currentPercentage = getVisiblePercentage1(viewHolder);
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
    }

    private fun getVisiblePercentage1(
        holder: RecyclerView.ViewHolder
    ): Float {
        val rect_parent = Rect()
        rvView?.getGlobalVisibleRect(rect_parent)
        val location = IntArray(2)
        holder.itemView.getLocationOnScreen(location)
        val rect_child = Rect(location[0], location[1], location[0] + holder.itemView.getWidth(), location[1] + holder.itemView.getHeight())
        val rect_parent_area = ((rect_child.right - rect_child.left) * (rect_child.bottom - rect_child.top)).toFloat()
        val x_overlap = Math.max(0, Math.min(rect_child.right, rect_parent.right) - Math.max(rect_child.left, rect_parent.left)).toFloat()
        val y_overlap = Math.max(0, Math.min(rect_child.bottom, rect_parent.bottom) - Math.max(rect_child.top, rect_parent.top)).toFloat()
        val overlapArea = x_overlap * y_overlap
        val percent = overlapArea / rect_parent_area * 100.0f
        return percent
    }


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