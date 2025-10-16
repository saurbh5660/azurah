package com.live.azurah.activity

import android.app.Dialog
import android.content.Intent
import android.graphics.text.LineBreaker
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Layout
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.text.HtmlCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.SnapHelper
import com.live.azurah.R
import com.live.azurah.adapter.ChallengeImageAdapter
import com.live.azurah.adapter.DayAdapter
import com.live.azurah.adapter.SuggestionUsernameAdapter
import com.live.azurah.databinding.ActivityMarkCahllangeActyivityBinding
import com.live.azurah.databinding.CompleteChallangeDialogBinding
import com.live.azurah.databinding.ConfirmationDialogBinding
import com.live.azurah.databinding.UsernameTakenDialogBinding
import com.live.azurah.model.BibleQuestViewModel
import com.live.azurah.model.CommonResponse
import com.live.azurah.model.InterestModel
import com.live.azurah.retrofit.LoaderDialog
import com.live.azurah.retrofit.Resource
import com.live.azurah.retrofit.Status
import com.live.azurah.util.getCurrentDate
import com.live.azurah.util.getCurrentTime
import com.live.azurah.util.getPreference
import com.live.azurah.util.gone
import com.live.azurah.util.logLong
import com.live.azurah.util.sanitizeHtml
import com.live.azurah.util.showCustomSnackbar
import com.live.azurah.util.visible
import com.live.azurah.viewmodel.CommonViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@AndroidEntryPoint
class MarkChallangeActivity : AppCompatActivity(), Observer<Resource<Any>> {
    private lateinit var binding: ActivityMarkCahllangeActyivityBinding
    private val list = ArrayList<BibleQuestViewModel.Body.BibleQuestChallenge>()
    private val data = BibleQuestViewModel.Body.BibleQuestChallenge()
    private var dayAdapter: DayAdapter? = null
    private var from = ""
    private var praySelected = false
    private var worshipSelected = false
    private var readingSelected = false
    private var exerciseSelected = false
    private var breakSelected = false
    private var dinnerSelected = false
    private var eveningSelected = false
    //   private val loaderDialog by lazy { LoaderDialog(this) }

    private val viewModel by viewModels<CommonViewModel>()
    private var id = ""
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMarkCahllangeActyivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.statusBarColor = getColor(R.color.white)
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
        id = intent.getStringExtra("id") ?: ""

        binding.btnMarkComplete.backgroundTintList =
            ActivityCompat.getColorStateList(this, R.color.blue)
        binding.btnMarkComplete.isEnabled = true

        getDetail()
        initListener()
        setAdapter()
    }

    private fun getDetail() {
        viewModel.getBibleView(id, this).observe(this, this)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun setAdapter() {
        dayAdapter = DayAdapter(this, list)
        binding.rvCategory.adapter = dayAdapter
        dayAdapter?.onClick = { pos: Int ->
            Log.d("csdfdsfdsf",pos.toString())

            val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val firstNonEmptyDate = list
                .map { it.completedDate }
                .firstOrNull { !it.isNullOrEmpty() }
                ?.let { LocalDate.parse(it, dateFormatter) }

            if (list[pos].isCompleted == 1){
                binding.btnMarkComplete.backgroundTintList =
                    ActivityCompat.getColorStateList(
                        this@MarkChallangeActivity,
                        R.color.button_grey
                    )
                binding.btnMarkComplete.isEnabled = false
            }else{
                binding.btnMarkComplete.backgroundTintList =
                    ActivityCompat.getColorStateList(
                        this@MarkChallangeActivity,
                        R.color.blue
                    )
                binding.btnMarkComplete.isEnabled = true
            }


          /*  if (firstNonEmptyDate != null) {
                Log.d("csdfdsfdsf",list[pos].isCompleted.toString())

                val daysBetween = ChronoUnit.DAYS.between(
                    firstNonEmptyDate,
                    LocalDate.parse(getCurrentDate(), dateFormatter)
                )
                if ((list[pos].dayNo ?: 0) <= daysBetween.toInt()+1) {
                    if (pos != 0) {
                        if (list[pos - 1].isCompleted == 0) {
                            binding.btnMarkComplete.backgroundTintList =
                                ActivityCompat.getColorStateList(
                                    this@MarkChallangeActivity,
                                    R.color.button_grey
                                )
                            binding.btnMarkComplete.isEnabled = false
                        } else if (list[pos].isCompleted == 1) {

                            binding.btnMarkComplete.backgroundTintList =
                                ActivityCompat.getColorStateList(
                                    this@MarkChallangeActivity,
                                    R.color.button_grey
                                )
                            binding.btnMarkComplete.isEnabled = false

                        }else{
                            binding.btnMarkComplete.backgroundTintList =
                                ActivityCompat.getColorStateList(
                                    this@MarkChallangeActivity,
                                    R.color.blue
                                )
                            binding.btnMarkComplete.isEnabled = true
                        }
                    } else {
                        if (list[pos].isCompleted == 1) {
                            binding.btnMarkComplete.backgroundTintList =
                                ActivityCompat.getColorStateList(
                                    this@MarkChallangeActivity,
                                    R.color.button_grey
                                )
                            binding.btnMarkComplete.isEnabled = false
                        } else {
                            binding.btnMarkComplete.backgroundTintList =
                                ActivityCompat.getColorStateList(
                                    this@MarkChallangeActivity,
                                    R.color.blue
                                )
                            binding.btnMarkComplete.isEnabled = true
                        }
                    }
                }else{
                    binding.btnMarkComplete.backgroundTintList =
                        ActivityCompat.getColorStateList(
                            this@MarkChallangeActivity,
                            R.color.button_grey
                        )
                    binding.btnMarkComplete.isEnabled = false
                }
            }
            else{
                if (pos != 0) {
                    if (list[pos - 1].isCompleted == 0) {
                        binding.btnMarkComplete.backgroundTintList =
                            ActivityCompat.getColorStateList(this@MarkChallangeActivity, R.color.button_grey)
                        binding.btnMarkComplete.isEnabled = false
                    }else{
                        binding.btnMarkComplete.backgroundTintList =
                            ActivityCompat.getColorStateList(this@MarkChallangeActivity, R.color.blue)
                        binding.btnMarkComplete.isEnabled = true
                    }
                }else{
                    if (list[pos].isCompleted == 1){
                        binding.btnMarkComplete.backgroundTintList =
                            ActivityCompat.getColorStateList(this@MarkChallangeActivity, R.color.button_grey)
                        binding.btnMarkComplete.isEnabled = false
                    }else{
                        binding.btnMarkComplete.backgroundTintList =
                            ActivityCompat.getColorStateList(this@MarkChallangeActivity, R.color.blue)
                        binding.btnMarkComplete.isEnabled = true
                    }
                }
            }*/


            binding.tvTitle.text = list[pos].title ?: ""

            val cleanedHtml = sanitizeHtml(list[pos].description ?: "")
            binding.txtReadingDesc.apply {
                text = cleanedHtml
                hyphenationFrequency = Layout.HYPHENATION_FREQUENCY_NONE
                justificationMode = LineBreaker.JUSTIFICATION_MODE_NONE  // API 26+
            }

            logLong("DescriptionHTML", list[pos].description.toString())
        }
        dayAdapter?.onToastClick={pos,type->
            if (type == 1){
                showCustomSnackbar(this, binding.root, "Please complete today's session before moving on to the next.")

            }else{
                showCustomSnackbar(this, binding.root, "Awesome work today! The next lesson will be ready for you tomorrow.")
            }
        }
    }

    private fun initListener() {
        with(binding) {
            backIcon.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }

            btnMarkComplete.setOnClickListener {
                markAsComplete()
            }

           /* btnRestart.setOnClickListener {
                confirmationDialog()
            }*/

            /*   ivPraySelected.setOnClickListener {
                   praySelected = true
                   ivPraySelected.setImageResource(R.drawable.green_tick)
                   enableButton()
               }

               ivWorshipSelected.setOnClickListener {
                   worshipSelected = true
                   ivWorshipSelected.setImageResource(R.drawable.green_tick)
                   enableButton()
               }

               ivReadingBible.setOnClickListener {
                   readingSelected = true
                   ivReadingBible.setImageResource(R.drawable.green_tick)
                   enableButton()
               }

               ivExercise.setOnClickListener {
                   exerciseSelected = true
                   ivExercise.setImageResource(R.drawable.green_tick)
                   enableButton()
               }
               ivBreak.setOnClickListener {
                   breakSelected = true
                   ivBreak.setImageResource(R.drawable.green_tick)
                   enableButton()
               }
               ivDinner.setOnClickListener {
                   dinnerSelected = true
                   ivDinner.setImageResource(R.drawable.green_tick)
                   enableButton()
               }
               ivEveningRef.setOnClickListener {
                   eveningSelected = true
                   ivEveningRef.setImageResource(R.drawable.green_tick)
                   enableButton()

               }*/
        }
    }

    fun enableButton() {
        if (praySelected && worshipSelected && readingSelected && exerciseSelected && breakSelected && dinnerSelected && eveningSelected) {
            binding.btnMarkComplete.backgroundTintList =
                ActivityCompat.getColorStateList(this@MarkChallangeActivity, R.color.blue)
            binding.btnMarkComplete.isEnabled = true
        }
    }

    private fun confirmationDialog() {
        val customDialog = Dialog(this)
        customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val confirmationBinding = ConfirmationDialogBinding.inflate(layoutInflater)
        customDialog.setContentView(confirmationBinding.root)
        customDialog.window?.setGravity(Gravity.CENTER)
        customDialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        customDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        confirmationBinding.tvNo.setOnClickListener {
            customDialog.dismiss()
        }

        confirmationBinding.tvYes.setOnClickListener {
            customDialog.dismiss()
            markAsRestart()
        }
        customDialog.show()
    }

    private fun completeChallengeDialog(day:Int) {
        val customDialog = Dialog(this)
        customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val confirmationBinding = CompleteChallangeDialogBinding.inflate(layoutInflater)
        customDialog.setContentView(confirmationBinding.root)
        customDialog.setCancelable(false)
        customDialog.window?.setGravity(Gravity.CENTER)
        customDialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        customDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        confirmationBinding.tvUsernameTaken.text  = buildString {
            append("Congrats on completing Day ")
            append(day)
            append("!")
        }
        confirmationBinding.tvYes.setOnClickListener {
            customDialog.dismiss()
            if(day == list.size) {
                startActivity(
                    Intent(
                        this@MarkChallangeActivity,
                        ChallangeCompletedActivity::class.java
                    ).apply {
                        putExtra("id",id)
                    }
                )
                finish()
            }
        }
        customDialog.show()
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onChanged(value: Resource<Any>) {
        when (value.status) {
            Status.SUCCESS -> {
               LoaderDialog.dismiss()
                when (value.data) {
                    is BibleQuestViewModel -> {
                        val res = value.data.body
                        with(binding) {
                            val readingChallengeSize = res?.bibleQuestChallenges ?: ArrayList()
                            list.clear()
                            for (i in readingChallengeSize.indices) {
                                val challenge = readingChallengeSize[i]
                                challenge?.dayNo = i+1
                                challenge?.isSelected = false
                                list.add(challenge ?: BibleQuestViewModel.Body.BibleQuestChallenge())
                            }

                            if (list.isNotEmpty()){
                                nsChallengeDetail.visible()
                            }

                            val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                            val lastNonEmptyDate = list
                                .map { it.completedDate }
                                .lastOrNull { !it.isNullOrEmpty() }
                                ?.let { LocalDate.parse(it, dateFormatter) }

                            val lastCompletedDate = list.lastOrNull { !it.completedDate.isNullOrEmpty() }

                            if (lastCompletedDate != null){
                                val index = list.indexOf(lastCompletedDate)
                                if (index != -1){
                                    if ((index+1) < list.size){
                                        if (lastNonEmptyDate != null){
                                            if (LocalDate.parse(getCurrentDate(), dateFormatter) == lastNonEmptyDate){
                                                binding.tvTitle.text =  list[index].title ?: ""
                                                list[index].isSelected = true
                                                list[index].currentDay = 1
                                                val cleanedHtml = sanitizeHtml(list[index].description ?: "")

                                                binding.txtReadingDesc.apply {
                                                    text = cleanedHtml
                                                    hyphenationFrequency = Layout.HYPHENATION_FREQUENCY_NONE
                                                    justificationMode = LineBreaker.JUSTIFICATION_MODE_NONE
                                                }
                                            }else{
                                                binding.tvTitle.text =  list[index+1].title ?: ""
                                                list[index+1].isSelected = true
                                                list[index+1].currentDay = 1
                                                val cleanedHtml = sanitizeHtml(list[index+1].description ?: "")

                                                binding.txtReadingDesc.apply {
                                                    text = cleanedHtml
                                                    hyphenationFrequency = Layout.HYPHENATION_FREQUENCY_NONE
                                                    justificationMode = LineBreaker.JUSTIFICATION_MODE_NONE
                                                }
                                            }
                                        }

                                    }else{
                                        binding.tvTitle.text =  list[index].title ?: ""
                                        list[index].isSelected = true
                                        list[index].currentDay = 1
                                        val cleanedHtml = sanitizeHtml(list[index].description ?: "")

                                        binding.txtReadingDesc.apply {
                                            text = cleanedHtml
                                            hyphenationFrequency = Layout.HYPHENATION_FREQUENCY_NONE
                                            justificationMode = LineBreaker.JUSTIFICATION_MODE_NONE
                                        }
                                    }
                                }else{
                                   if (list.isNotEmpty()){
                                       binding.tvTitle.text =  list[0].title ?: ""
                                       list[0].isSelected = true
                                       list[0].currentDay = 1
                                       val cleanedHtml = sanitizeHtml(list[0].description ?: "")

                                       binding.txtReadingDesc.apply {
                                           text = cleanedHtml
                                           hyphenationFrequency = Layout.HYPHENATION_FREQUENCY_NONE
                                           justificationMode = LineBreaker.JUSTIFICATION_MODE_NONE
                                       }
                                   }
                                }
                            }else{
                                if (list.isNotEmpty()){
                                    binding.tvTitle.text =  list[0].title ?: ""
                                    list[0].isSelected = true
                                    list[0].currentDay = 1
                                    val cleanedHtml = sanitizeHtml(list[0].description ?: "")

                                    binding.txtReadingDesc.apply {
                                        text = cleanedHtml
                                        hyphenationFrequency = Layout.HYPHENATION_FREQUENCY_NONE
                                        justificationMode = LineBreaker.JUSTIFICATION_MODE_NONE
                                    }
                                }
                            }



                           /* if (firstNonEmptyDate != null) {
                                // Calculate the number of days between first date and current date
                                val daysBetween = ChronoUnit.DAYS.between(firstNonEmptyDate,  LocalDate.parse(getCurrentDate(), dateFormatter))
//                                val daysBetween = ChronoUnit.DAYS.between(firstNonEmptyDate,  LocalDate.parse("2025-02-17", dateFormatter))
                                println("First non-empty date: $firstNonEmptyDate")
                                println("Current date: ${getCurrentDate()}")
                                println("Days between: $daysBetween")
                                   for (i in 0 until daysBetween.toInt()){
                                       if (i < list.size){
                                           if (list[i].completedDate.isNullOrEmpty()){
                                               list[i].isPassed = true
                                           }
                                       }
                                   }

                                if (daysBetween.toInt()+1 <= list.size) {
                                    binding.tvTitle.text =  list[daysBetween.toInt()].title ?: ""
                                    list[daysBetween.toInt()].isSelected = true
                                    list[daysBetween.toInt()].currentDay = 1
                                    val cleanedHtml = sanitizeHtml(list[daysBetween.toInt()].description ?: "")

                                    binding.txtReadingDesc.apply {
                                        text = cleanedHtml
                                        hyphenationFrequency = Layout.HYPHENATION_FREQUENCY_NONE
                                        justificationMode = LineBreaker.JUSTIFICATION_MODE_NONE
                                    }
                                }else{
                                    binding.tvTitle.text = list.firstOrNull()?.title ?: ""
                                    list.firstOrNull()?.isSelected = true
                                    list.firstOrNull()?.currentDay = 1

                                    val cleanedHtml = sanitizeHtml(list.firstOrNull()?.description ?: "")
                                    binding.txtReadingDesc.apply {
                                        text = cleanedHtml
                                        hyphenationFrequency = Layout.HYPHENATION_FREQUENCY_NONE
                                        justificationMode = LineBreaker.JUSTIFICATION_MODE_NONE
                                    }
                                }

                            }
                            else{
                                binding.tvTitle.text = list.firstOrNull()?.title ?: ""
                                list.firstOrNull()?.isSelected = true
                                list.firstOrNull()?.currentDay = 1

                                val cleanedHtml = sanitizeHtml(list.firstOrNull()?.description ?: "")
                                binding.txtReadingDesc.apply {
                                    text = cleanedHtml
                                    hyphenationFrequency = Layout.HYPHENATION_FREQUENCY_NONE
                                    justificationMode = LineBreaker.JUSTIFICATION_MODE_NONE  // API 26+
                                }
                            }*/

                            dayAdapter?.notifyDataSetChanged()
                           /* if ((res?.isChallengeStarted ?:0) > 0){
                                binding.btnRestart.visible()
                            }else{
                                binding.btnRestart.gone()
                            }*/
                        }
                    }
                }
            }

            Status.LOADING -> {
                LoaderDialog.show(this)
            }

            Status.ERROR -> {
               LoaderDialog.dismiss()
                showCustomSnackbar(this, binding.root, value.message.toString())
            }
        }
    }


    private fun markAsComplete() {
        val selectedDay = list.filter { it.isSelected == true }
        val selectedIndex = list.indexOfFirst { it.isSelected == true }
        if (selectedDay.isEmpty()) {
            showCustomSnackbar(this, binding.root, "Please select day.")
            return
        }
        if (selectedIndex != 0) {
            if (list[selectedIndex - 1].isCompleted == 0) {
                showCustomSnackbar(this, binding.root, "Complete previous day challenge.")
                return
            }
        }
        val map = HashMap<String, String>()
        map["user_id"] = getPreference("id", "")
        map["bible_quest_id"] = id
        map["bible_quest_challenge_id"] = selectedDay.firstOrNull()?.id.toString()
        map["day_no"] = selectedDay.firstOrNull()?.dayNo.toString()
        map["date"] = getCurrentDate()
        map["time"] = getCurrentTime()

        viewModel.markAsComplete(map, this).observe(this) { value ->
            when (value.status) {
                Status.SUCCESS -> {
                   LoaderDialog.dismiss()
                    when (value.data) {
                        is CommonResponse -> {
                            val res = value.data.body
                            list[selectedIndex].isCompleted = 1
                            list[selectedIndex].completedDate = getCurrentDate()
//                            binding.btnRestart.visible()
                            dayAdapter?.notifyItemChanged(selectedIndex)
                            completeChallengeDialog((selectedIndex+1))
                            binding.btnMarkComplete.backgroundTintList =
                                ActivityCompat.getColorStateList(this, R.color.button_grey)
                            binding.btnMarkComplete.isEnabled = false

                        }
                    }
                }
                Status.LOADING -> {
                    LoaderDialog.show(this)
                }

                Status.ERROR -> {
                   LoaderDialog.dismiss()
                    showCustomSnackbar(this, binding.root, value.message.toString())
                }
            }
        }
    }

    private fun markAsRestart() {
        val map = HashMap<String, String>()
        map["user_id"] = getPreference("id", "")
        map["bible_quest_id"] = id

        viewModel.markAsRestart(map, this).observe(this) { value ->
            when (value.status) {
                Status.SUCCESS -> {
                   LoaderDialog.dismiss()
                    when (value.data) {
                        is CommonResponse -> {
                            getDetail()
                        }
                    }
                }

                Status.LOADING -> {
                    LoaderDialog.show(this)
                }

                Status.ERROR -> {
                   LoaderDialog.dismiss()
                    showCustomSnackbar(this, binding.root, value.message.toString())
                }
            }
        }
    }

}