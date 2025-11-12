package com.live.azurah.activity

import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.live.azurah.R
import com.live.azurah.adapter.ViewPagerAdapter
import com.live.azurah.databinding.ActivityHomeBinding
import com.live.azurah.databinding.DialogShareBinding
import com.live.azurah.databinding.FaithBuilderDialogBinding
import com.live.azurah.databinding.MenuReportBinding
import com.live.azurah.databinding.SureDialogBinding
import com.live.azurah.fragment.AllShopCategoryFragment
import com.live.azurah.fragment.CategoryDetailFragment
import com.live.azurah.fragment.DashBoardFragment
import com.live.azurah.fragment.EventFragment
import com.live.azurah.fragment.FavouriteFragment
import com.live.azurah.fragment.FollowFollowingFragment
import com.live.azurah.fragment.HomeFragment
import com.live.azurah.fragment.HomeIntroFragment
import com.live.azurah.fragment.ProfileFragment
import com.live.azurah.fragment.SearchHomeFragment
import com.live.azurah.fragment.SearchProductFragment
import com.live.azurah.fragment.ShopFragment
import com.live.azurah.fragment.SongFragment
import com.live.azurah.fragment.SuggetionForYouFragment
import com.live.azurah.fragment.UserLikesFragment
import com.live.azurah.model.CommonResponse
import com.live.azurah.model.CommunityForumResponse
import com.live.azurah.retrofit.LoaderDialog
import com.live.azurah.retrofit.Status
import com.live.azurah.util.getPreference
import com.live.azurah.util.gone
import com.live.azurah.util.showCustomSnackbar
import com.live.azurah.util.visible
import com.live.azurah.viewmodel.SharedViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {
    private lateinit var sharedViewModel: SharedViewModel

    lateinit var binding : ActivityHomeBinding
    private lateinit var fragmentList: MutableList<Fragment>
    private lateinit var viewpagerAdapter: ViewPagerAdapter
    private var searchType = 0
    private var notificationCount = 0
    private var favouriteCount = 0
    private var eventCount = 0
    private var messageCount = 0
    private var currentPos = 0
    private var from = ""
    private var backPressedTime: Long = 0
    private val doubleBackToExitDuration: Long = 2000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
              val systemBars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime()
            )
            view.updatePadding(
                left = systemBars.left,
                bottom = systemBars.bottom,
                right = systemBars.right,
                top = systemBars.top
            )
            insets
        }
        sharedViewModel = ViewModelProvider(this)[SharedViewModel::class.java]
       from = intent.getStringExtra("from") ?: ""
        if (from == "1"){
            window.statusBarColor = getColor(R.color.black_translucent)
            window.navigationBarColor = getColor(R.color.black_translucent)
            replaceIntroFragment(HomeIntroFragment())
        }else{
            window.statusBarColor = getColor(R.color.white)
            window.navigationBarColor = getColor(R.color.white)
        }
        binding.viewPager.isUserInputEnabled = false
        showHideHomeIcon(0)
        initListener()
        initData()
        initFragment()
    }

    private fun initListener() {
        binding.apply {

            sharedViewModel.count.observe(this@HomeActivity){
                it?.let {
                    favouriteCount = it.favouriteProductsCount ?: 0
                    eventCount = it.eventBookmarksCount ?: 0
                    notificationCount = it.notificationUnreadCount ?: 0

                    if (eventCount > 99){
                        ivBookmarkCount.text = "99+"
                    }else{
                        ivBookmarkCount.text = eventCount.toString()
                    }

                    if (favouriteCount > 99){
                        ivFavCount.text = "99+"
                    }else{
                        ivFavCount.text = favouriteCount.toString()
                    }

                    if (notificationCount > 99){
                        ivNotificationCount.text = "99+"
                    }else{
                        ivNotificationCount.text = notificationCount.toString()
                    }
                    showHideHomeIcon(binding.viewPager.currentItem)
                }
            }

            sharedViewModel.getPostData.observe(this@HomeActivity, Observer {
                it?.let {
                    notificationCount = it.unreadNotificationCount ?: 0
                    messageCount = it.unreadMessageCount ?: 0

                    binding.ivNotificationCount.text = if (notificationCount > 99) "99+" else notificationCount.toString()
                    binding.ivChatCount.text = if (messageCount > 99) "99+" else messageCount.toString()
                    if (notificationCount > 0){
                        binding.ivNotificationCount.visible()
                    }else{
                        binding.ivNotificationCount.gone()

                    }
                    if (messageCount > 0){
                        binding.ivChatCount.visible()
                    }else{
                        binding.ivChatCount.gone()

                    }
                }
            })

            supportFragmentManager.addOnBackStackChangedListener {
                if (supportFragmentManager.backStackEntryCount == 0) {
                    val containerFragment =
                        supportFragmentManager.findFragmentById(binding.fragmentContainer.id)
                    val currentItem = binding.viewPager.currentItem
                    val homeFragment = (binding.viewPager.adapter as ViewPagerAdapter).getFragment(currentItem)
                    lifecycleScope.launch(Dispatchers.Main){
                        if (containerFragment == null && homeFragment is HomeFragment) {
                            Log.d("sdgsfgsfg", "fragmentContainer is empty and HomeFragment is visible — resuming video")
                            homeFragment.resumeVideo()
                        }
                    }

                }
            }

           val backPressedCallback = object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val currentFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer)
                    val currentShortFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerShort)
                    if (currentFragment != null){
                        when(currentFragment){
                            is SuggetionForYouFragment, is UserLikesFragment,is CategoryDetailFragment,is SongFragment,is ShopDetailFragment,is FavouriteFragment,is SearchHomeFragment,is SearchProductFragment,is AllShopCategoryFragment->{
                               Log.d("sffdfds","dfdsbfdsbdbsg")
                                LocalBroadcastManager.getInstance(this@HomeActivity).sendBroadcast(
                                    Intent("Shop")
                                )
                                supportFragmentManager.popBackStack()
                            }
                        }
                    }else if (currentShortFragment != null){
                        when(currentShortFragment){
                            is FollowFollowingFragment->{
                                supportFragmentManager.popBackStack()
                            }
                        }
                    }else{

                        when ((viewPager.adapter as ViewPagerAdapter).getFragment(viewPager.currentItem)) {
                            is FavouriteFragment,is SearchProductFragment,is ShopDetailFragment -> {
                                Log.d("sffdfds","dfdsbfdsbdbsg")
                                LocalBroadcastManager.getInstance(this@HomeActivity).sendBroadcast(Intent("Shop"))
                                binding.viewPager.setCurrentItem(3,false)
                            }else->{
                            val currentTime = System.currentTimeMillis()
                            if (currentTime - backPressedTime < doubleBackToExitDuration) {
                                finishAffinity()
                                return
                            }
                            backPressedTime = currentTime
                            lifecycleScope.launch {
                                binding.clTapAgain.visibility = View.VISIBLE
                                delay(2000)
                                binding.clTapAgain.visibility = View.GONE
                            }
                            }
                        }
                    }
                }
            }

            onBackPressedDispatcher.addCallback(this@HomeActivity, backPressedCallback)
            setTabBackgroundView(ivHome,tvHome,R.drawable.selected_home_icon)
            searchType = 0
            binding.viewPager.setCurrentItem(0,false)
            showHideHomeIcon(0)

            ivNotification.setOnClickListener {
                startActivity(Intent(this@HomeActivity,NotificationActivity::class.java))
            }

            ivMore.setOnClickListener {
                setPopUpWindow(it)
            }

            ivSearch.setOnClickListener {
                when(searchType){
                    0->{
                        replaceFragment(SearchHomeFragment())
                    }
                    1->{
                        replaceFragment(SearchHomeFragment())
                    }
                    2->{
                        startActivity(Intent(this@HomeActivity,BookmarkEventActivity::class.java))
                    }
                }
            }

            ivChat.setOnClickListener {
                startActivity(Intent(this@HomeActivity,MessageActivity::class.java))
            }

            ivBookmark.setOnClickListener {
                if (currentPos == 4){
                    startActivity(Intent(this@HomeActivity,BookmarkActivity::class.java))
                }else{
                    startActivity(Intent(this@HomeActivity,BookmarkSavedEventActivity::class.java))
                }
            }
            ivHeart.setOnClickListener {
                viewPager.setCurrentItem(7,false)
            }

            ivSetting.setOnClickListener {
                startActivity(Intent(this@HomeActivity,SettingActivity::class.java))
            }

            ivShare.setOnClickListener {
                val userName = getPreference("username","")

                val shareLink = buildString {
                    append("$userName invited you to join Azrius.\n")
                    append("Azrius is a Christian social media platform where you can grow in faith, find community, join Bible Quests, share prayer requests, and explore uplifting content.\n")
                    append("Download the app to get started.\n")
                    append("https://app.azrius.co.uk/common_api/deepLinking/user?user_id=${getPreference("id","")}")

                }
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, shareLink)
                }

                startActivity(Intent.createChooser(intent, "Share Post"))

//                showShareDialog()
            }

            backIcon.setOnClickListener {
                if (viewPager.currentItem == 5 || viewPager.currentItem == 6 || viewPager.currentItem == 7){
                    viewPager.setCurrentItem(3,false)
                }
            }

            viewPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
                ) {
                    super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                }

                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    currentPos= position
                    showHideHomeIcon(position)
                }

                override fun onPageScrollStateChanged(state: Int) {
                    super.onPageScrollStateChanged(state)
                }
            })

            llHome.setOnClickListener {
                setTabBackgroundView(ivHome,tvHome,R.drawable.selected_home_icon)
                searchType = 0
                binding.viewPager.setCurrentItem(0,false)
                showHideHomeIcon(0)
            }

            llDash.setOnClickListener {
                setTabBackgroundView(ivDash,tvDash,R.drawable.selected_bookmark)
                searchType = 1
                showHideHomeIcon(1)
                binding.viewPager.setCurrentItem(1,false)

            }

            llEvent.setOnClickListener {
                setTabBackgroundView(ivEvents,tvEvents,R.drawable.selected_event)
                searchType = 2
                showHideHomeIcon(2)
                binding.viewPager.setCurrentItem(2,false)
            }

            llShop.setOnClickListener {
                setTabBackgroundView(ivShop,tvShop,R.drawable.selected_shop)
                showHideHomeIcon(3)
                binding.viewPager.setCurrentItem(3,false)

            }

            llProfile.setOnClickListener {
                setTabBackgroundView(ivProfile,tvProfile,R.drawable.selected_profile)
                showHideHomeIcon(4)
                binding.viewPager.setCurrentItem(4,false)
            }
        }
    }

    private fun initData() {
        fragmentList = ArrayList()
        fragmentList.add(HomeFragment())
        fragmentList.add(DashBoardFragment())
        fragmentList.add(EventFragment())
        fragmentList.add(ShopFragment())
        fragmentList.add(ProfileFragment())
        fragmentList.add(SearchProductFragment())
        fragmentList.add(ShopDetailFragment())
        fragmentList.add(FavouriteFragment())
    }

    private fun initFragment() {
        viewpagerAdapter = ViewPagerAdapter(fragmentList, this)
        binding.viewPager.adapter = viewpagerAdapter
    }

    private fun showHideHomeIcon(type:Int){
        with(binding){
            when(type){
                0->{
                    ivNotification.visibility = View.VISIBLE
                    ivChat.visibility = View.VISIBLE
                    ivSearch.visibility = View.VISIBLE
                    ivBookmark.visibility = View.GONE
                    ivBookmarkCount.visibility = View.GONE
                    ivHeart.visibility = View.GONE
                    ivFavCount.visibility = View.GONE
                    ivSetting.visibility = View.GONE
                    ivMore.visibility = View.GONE
                    ivShare.visibility = View.GONE
                    tvCenterTitle.visibility = View.GONE
                    backIcon.visibility = View.GONE
                    tvTitle.visibility = View.VISIBLE
                    bottomNav.visibility = View.VISIBLE
                    removeShortFragment()

                  /*  binding.ivNotificationCount.visible()
                    binding.ivChatCount.visible()*/
                    if (notificationCount > 0){
                        binding.ivNotificationCount.visible()
                    }else{
                        binding.ivNotificationCount.gone()
                    }
                    if (messageCount > 0){
                        binding.ivChatCount.visible()
                    }else{
                        binding.ivChatCount.gone()

                    }
                }
                1->{
                    ivNotification.visibility = View.VISIBLE
                    ivChat.visibility = View.VISIBLE
                    ivSearch.visibility = View.VISIBLE
                    ivBookmark.visibility = View.GONE
                    ivBookmarkCount.visibility = View.GONE
                    ivHeart.visibility = View.GONE
                    ivFavCount.visibility = View.GONE
                    ivMore.visibility = View.GONE
                    ivSetting.visibility = View.GONE
                    ivShare.visibility = View.GONE
                    tvCenterTitle.visibility = View.GONE
                    backIcon.visibility = View.GONE
                    tvTitle.visibility = View.VISIBLE
                    bottomNav.visibility = View.VISIBLE
                    removeShortFragment()

               /*     binding.ivNotificationCount.visible()
                    binding.ivChatCount.visible()*/

                    if (notificationCount > 0){
                        binding.ivNotificationCount.visible()
                    }else{
                        binding.ivNotificationCount.gone()
                    }
                    if (messageCount > 0){
                        binding.ivChatCount.visible()
                    }else{
                        binding.ivChatCount.gone()
                    }
                }
                2->{
                    ivNotification.visibility = View.GONE
                    ivNotificationCount.visibility = View.GONE
                    ivChat.visibility = View.GONE
                    ivChatCount.visibility = View.GONE
                    ivSearch.visibility = View.GONE
                    ivBookmark.visibility = View.VISIBLE
                    ivHeart.visibility = View.GONE
                    ivMore.visibility = View.GONE
                    ivFavCount.visibility = View.GONE
                    ivSetting.visibility = View.GONE
                    ivShare.visibility = View.GONE
                    tvCenterTitle.visibility = View.GONE
                    backIcon.visibility = View.GONE
                    tvTitle.visibility = View.VISIBLE
                    bottomNav.visibility = View.VISIBLE

                    if (eventCount > 0){
                        binding.ivBookmarkCount.visible()
                    }else{
                        binding.ivBookmarkCount.gone()
                    }
                    removeShortFragment()
                }
                3->{
                    ivNotification.visibility = View.GONE
                    ivNotificationCount.visibility = View.GONE
                    ivChat.visibility = View.GONE
                    ivChatCount.visibility = View.GONE
                    ivSearch.visibility = View.GONE
                    ivBookmark.visibility = View.GONE
                    ivBookmarkCount.visibility = View.GONE
                    ivHeart.visibility = View.VISIBLE
                    ivSetting.visibility = View.GONE
                    ivShare.visibility = View.GONE
                    ivMore.visibility = View.GONE
                    tvCenterTitle.visibility = View.GONE
                    backIcon.visibility = View.GONE
                    tvTitle.visibility = View.VISIBLE
                    bottomNav.visibility = View.VISIBLE

                    if (favouriteCount > 0){
                        binding.ivFavCount.visible()
                    }else{
                        binding.ivFavCount.gone()
                    }
                    removeShortFragment()

                }
                4->{
                    ivNotification.visibility = View.GONE
                    ivNotificationCount.visibility = View.GONE
                    ivChat.visibility = View.GONE
                    ivChatCount.visibility = View.GONE
                    ivSearch.visibility = View.GONE
                    ivBookmark.visibility = View.GONE
                    ivBookmarkCount.visibility = View.GONE
                    ivMore.visibility = View.VISIBLE
                    ivHeart.visibility = View.GONE
                    ivFavCount.visibility = View.GONE
                    ivSetting.visibility = View.GONE
                    ivShare.visibility = View.GONE
                    tvCenterTitle.visibility = View.GONE
                    backIcon.visibility = View.GONE
                    tvTitle.visibility = View.VISIBLE
                    bottomNav.visibility = View.VISIBLE
                    removeShortFragment()


                }
                5->{
                    ivNotification.visibility = View.GONE
                    ivNotificationCount.visibility = View.GONE
                    ivChat.visibility = View.GONE
                    ivChatCount.visibility = View.GONE
                    ivSearch.visibility = View.GONE
                    ivBookmark.visibility = View.GONE
                    ivBookmarkCount.visibility = View.GONE
                    ivHeart.visibility = View.VISIBLE
                    ivSetting.visibility = View.GONE
                    ivShare.visibility = View.GONE
                    ivMore.visibility = View.GONE
                    tvCenterTitle.visibility = View.VISIBLE
                    tvTitle.visibility = View.GONE
                    backIcon.visibility = View.VISIBLE
                    bottomNav.visibility = View.VISIBLE

                    if (favouriteCount > 0){
                        binding.ivFavCount.visible()
                    }else{
                        binding.ivFavCount.gone()
                    }
                    removeShortFragment()

                }
                6->{
                    ivNotification.visibility = View.GONE
                    ivNotificationCount.visibility = View.GONE
                    ivChat.visibility = View.GONE
                    ivChatCount.visibility = View.GONE
                    ivSearch.visibility = View.GONE
                    ivBookmark.visibility = View.GONE
                    ivBookmarkCount.visibility = View.GONE
                    ivHeart.visibility = View.VISIBLE
                    ivFavCount.visibility = View.GONE
                    ivSetting.visibility = View.GONE
                    ivShare.visibility = View.GONE
                    tvCenterTitle.visibility = View.VISIBLE
                    tvTitle.visibility = View.GONE
                    backIcon.visibility = View.VISIBLE
                    bottomNav.visibility = View.GONE
                    removeShortFragment()

                }
                7->{
                    ivNotification.visibility = View.GONE
                    ivNotificationCount.visibility = View.GONE
                    ivChatCount.visibility = View.GONE
                    ivSearch.visibility = View.GONE
                    ivBookmark.visibility = View.GONE
                    ivBookmarkCount.visibility = View.GONE
                    ivHeart.visibility = View.GONE
                    ivFavCount.visibility = View.GONE
                    ivSetting.visibility = View.GONE
                    ivShare.visibility = View.GONE
                    tvCenterTitle.visibility = View.VISIBLE
                    tvTitle.visibility = View.GONE
                    backIcon.visibility = View.VISIBLE
                    bottomNav.visibility = View.VISIBLE
                    removeShortFragment()
                }
        }
        }
    }

    fun replaceFragment(fragment: Fragment){
        when(val homeFragment = (binding.viewPager.adapter as ViewPagerAdapter).getFragment(binding.viewPager.currentItem)){
            is HomeFragment->{
                Log.d("sdgsdgdg","sdgsdgdg")
                homeFragment.pauseVideo()
            }
        }
        supportFragmentManager.beginTransaction().replace(binding.fragmentContainer.id, fragment).addToBackStack(null).commit()
    }

    fun replaceIntroFragment(fragment: Fragment){
        supportFragmentManager.beginTransaction().replace(binding.fragmentContainer.id, fragment).commit()
    }

    fun removeFragment(){
        val fragment: Fragment? = supportFragmentManager.findFragmentById(binding.fragmentContainer.id)
        if (fragment != null){
            window.statusBarColor = getColor(R.color.white)
            window.navigationBarColor = getColor(R.color.white)
            supportFragmentManager.beginTransaction().remove(fragment).commit()
        }
    }

    fun replaceShortFragment(fragment: Fragment){
        supportFragmentManager.beginTransaction().replace(binding.fragmentContainerShort.id, fragment).addToBackStack(null).commit()
    }

    fun removeShortFragment(){
        val fragment: Fragment? = supportFragmentManager.findFragmentById(binding.fragmentContainerShort.id)
        if (fragment != null){
            supportFragmentManager.beginTransaction().remove(fragment).commit()
        }
    }

    private fun showShareDialog(){
        val customDialog = Dialog(this)
        customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val resetBinding = DialogShareBinding.inflate(layoutInflater)
        customDialog.setContentView(resetBinding.root)
        customDialog.window?.setGravity(Gravity.CENTER)
        customDialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        customDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        resetBinding.btnLink.text = buildString {
            append("https://app.azrius.co.uk/common_api/deepLinking/user?user_id=")
            append(getPreference("id", ""))
        }
        resetBinding.btnCopy.setOnClickListener {
            customDialog.dismiss()
            copyToClipboard("https://app.azrius.co.uk/common_api/deepLinking/user?user_id="+ getPreference("id",""))

        }

        resetBinding.ivCross.setOnClickListener {
            customDialog.dismiss()
        }
        customDialog.show()

    }


    private fun setTabBackgroundView(imageView: ImageView, textView: TextView,selectedIcon:Int){
        with(binding){
            ivHome.setImageResource(R.drawable.unselected_home)
            ivDash.setImageResource(R.drawable.unselected_bookmark_icon)
            ivEvents.setImageResource(R.drawable.unselected_event)
            ivShop.setImageResource(R.drawable.unselected_shop)
            ivProfile.setImageResource(R.drawable.unselected_profile)

            val typeface = ResourcesCompat.getFont(this@HomeActivity,R.font.poppins)
            tvHome.typeface = typeface
            tvDash.typeface = typeface
            tvEvents.typeface = typeface
            tvShop.typeface = typeface
            tvProfile.typeface = typeface

            tvHome.setTextColor(ContextCompat.getColor(this@HomeActivity, R.color.menu_item_unselected))
            tvDash.setTextColor(ContextCompat.getColor(this@HomeActivity, R.color.menu_item_unselected))
            tvEvents.setTextColor(ContextCompat.getColor(this@HomeActivity, R.color.menu_item_unselected))
            tvShop.setTextColor(ContextCompat.getColor(this@HomeActivity, R.color.menu_item_unselected))
            tvProfile.setTextColor(ContextCompat.getColor(this@HomeActivity, R.color.menu_item_unselected))

            imageView.setImageResource(selectedIcon)
            textView.setTextColor(ContextCompat.getColor(this@HomeActivity, R.color.black))

            val typeface1 = ResourcesCompat.getFont(this@HomeActivity,R.font.poppins_semibold)
            textView.typeface = typeface1
        }

    }

    private fun copyToClipboard(text: String) {
        val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("label", text)
        clipboardManager.setPrimaryClip(clipData)
    }

    private fun setPopUpWindow(view1: View) {
        val menuBinding = MenuReportBinding.inflate(layoutInflater)

        val myPopupWindow = PopupWindow(
            menuBinding.root,
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            true
        )

        with(menuBinding){
            ivUser.setImageResource(R.drawable.share_icon)
            ivReportUser.setImageResource(R.drawable.bookmark_icon)
            ivBlockUser.setImageResource(R.drawable.setting_icon)

            tvNotDone.text = "Share Profile"
            tvHaveDone.text = "Saved Posts"
            tvBlockUser.text = "Settings"

            ivReward.visible()
            tvReward.visible()
            divider18.visible()

            tvNotDone.setOnClickListener {
                myPopupWindow.dismiss()
                val userName = getPreference("username","")

                val shareLink = buildString {
                    append("$userName invited you to join Azrius.\n")
                    append("Azrius is a Christian social media platform where you can grow in faith, find community, join Bible Quests, share prayer requests, and explore uplifting content.\n")
                    append("Download the app to get started.\n")
                    append("https://app.azrius.co.uk/common_api/deepLinking/user?user_id=${getPreference("id","")}")
                }
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, shareLink)
                }

                startActivity(Intent.createChooser(intent, "Share Post"))

//                showShareDialog()
            }

            tvHaveDone.setOnClickListener {
                myPopupWindow.dismiss()
                startActivity(Intent(this@HomeActivity,BookmarkActivity::class.java))
            }

            tvBlockUser.setOnClickListener {
                myPopupWindow.dismiss()
                startActivity(Intent(this@HomeActivity,SettingActivity::class.java))
            }

            tvReward.setOnClickListener {
                myPopupWindow.dismiss()
                startActivity(Intent(this@HomeActivity, ReferralActivity::class.java))
            }
            ivReward.setOnClickListener {
                myPopupWindow.dismiss()
                startActivity(Intent(this@HomeActivity, ReferralActivity::class.java))
            }
        }

        myPopupWindow.showAsDropDown(view1, 0, -60)

    }

    fun faithBuilderDialog(){
        val customDialog = Dialog(this)
        customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val faithBuilderDialogBinding = FaithBuilderDialogBinding.inflate(layoutInflater)
        customDialog.setContentView(faithBuilderDialogBinding.root)
        customDialog.window?.setGravity(Gravity.CENTER)
        customDialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        customDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        faithBuilderDialogBinding.ivClose.setOnClickListener {
            customDialog.dismiss()
        }
        faithBuilderDialogBinding.btnInviteFriends.setOnClickListener {
            customDialog.dismiss()
            startActivity(Intent(this,ReferralActivity::class.java))
        }
        customDialog.show()
    }

}