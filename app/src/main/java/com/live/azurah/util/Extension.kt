package com.live.azurah.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.media.ExifInterface
import android.media.MediaMetadataRetriever
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.util.Log
import android.util.Patterns
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import com.android.billingclient.api.Purchase
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.live.azurah.R
import com.live.azurah.activity.ContentActivity
import com.live.azurah.model.UserTag
import com.yalantis.ucrop.util.BitmapLoadUtils
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

fun longToTime(milliSeconds: Long, format: String?): String? {
    val sdf = SimpleDateFormat(format, Locale.getDefault())
    return sdf.format(Date(milliSeconds))
}
 fun validateUsername(username: String): String? {
    val regex = Regex("^[a-z][a-z0-9]*(?:[._][a-z0-9]+)*$")

    when {
        username.length < 3 -> return "Username must be at least 3 characters"
        username.length > 30 -> return "Username must be at most 30 characters"
        !username.matches(regex) -> return """
            Invalid username:
            - Must start with a lowercase letter
            - Only lowercase letters, numbers, underscores (_) and full stops (.)
            - Cannot end with . or _
            - No consecutive . or _
        """.trimIndent()
        username.contains("..") || username.contains("__") || username.contains("._") || username.contains("_.") ->
            return "Username cannot have consecutive . or _"
        username.endsWith(".") || username.endsWith("_") ->
            return "Username cannot end with . or _"
        username.contains(Regex("[A-Z@!#\$\\s]")) ->
            return "Username cannot have uppercase letters, spaces, or special characters"
    }

    return null // valid
}


@Throws(IOException::class)
fun compressImage(
    imageFile: File,
    reqWidth: Int,
    reqHeight: Int,
    compressFormat: Bitmap.CompressFormat?,
    quality: Int,
    destinationPath: String?
): File {
    var fileOutputStream: FileOutputStream? = null
    val file: File = File(destinationPath!!).parentFile!!
    if (!file.exists()) {
        file.mkdirs()
    }
    try {
        fileOutputStream = FileOutputStream(destinationPath)
        // write the compressed bitmap at the destination specified by destinationPath.
        if (compressFormat != null) {
            decodeSampledBitmapFromFile(imageFile, reqWidth, reqHeight)
                .compress(compressFormat, quality, fileOutputStream)
        }
    } finally {
        if (fileOutputStream != null) {
            fileOutputStream.flush()
            fileOutputStream.close()
        }
    }
    return File(destinationPath)
}

@Throws(IOException::class)
fun decodeSampledBitmapFromFile(
    imageFile: File,
    reqWidth: Int,
    reqHeight: Int
): Bitmap { // First decode with inJustDecodeBounds=true to check dimensions
    val options = BitmapFactory.Options()
    options.inJustDecodeBounds = true
    BitmapFactory.decodeFile(imageFile.absolutePath, options)
    // Calculate inSampleSize
    options.inSampleSize = BitmapLoadUtils.calculateInSampleSize(options, reqWidth, reqHeight)
    // Decode bitmap with inSampleSize set
    options.inJustDecodeBounds = false
    var scaledBitmap = BitmapFactory.decodeFile(imageFile.absolutePath, options)
    //check the rotation of the image and display it properly
    val exif = ExifInterface(imageFile.absolutePath)
    val orientation: Int = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0)
    val matrix = Matrix()
    if (orientation == 6) {
        matrix.postRotate(90f)
    } else if (orientation == 3) {
        matrix.postRotate(180f)
    } else if (orientation == 8) {
        matrix.postRotate(270f)
    }
    scaledBitmap = Bitmap.createBitmap(
        scaledBitmap,
        0,
        0,
        scaledBitmap.width,
        scaledBitmap.height,
        matrix,
        true
    )
    return scaledBitmap
}

fun isAgeValid(dobStr: String): Boolean {
    val formatter = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
    val dob = formatter.parse(dobStr)
    val today = GregorianCalendar()
    val dobCalendar = Calendar.getInstance()
    dobCalendar.time = dob
    var age = today.get(Calendar.YEAR) - dobCalendar.get(Calendar.YEAR)
    if (today.get(Calendar.DAY_OF_YEAR) < dobCalendar.get(Calendar.DAY_OF_YEAR)) {
        age--
    }
    return age >= 13
}

fun countWords(text: String): Int {
    val words = text.trim().split("\\s+".toRegex())
    return words.size
}

fun trimToWordLimit(text: String, limit: Int): String {
    val words = text.trim().split("\\s+".toRegex())
    return words.take(limit).joinToString(" ")
}

fun isDurationGreaterThan60Seconds(videoUri: Uri, context: Context): Boolean {
    val retriever = MediaMetadataRetriever()
    retriever.setDataSource(context, videoUri)
    val durationString = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
    val durationInMillis = durationString?.toLongOrNull() ?: 0L
    val durationInSeconds = durationInMillis / 1000
    retriever.release()
    return durationInSeconds > 60
}

fun showCustomSnackbar(
    context: Context,
    view: View,
    message: String
) {
    val snackbar = Snackbar.make(view, message, Snackbar.LENGTH_SHORT)
    val snackbarView = snackbar.view
    snackbarView.backgroundTintList = ContextCompat.getColorStateList(context, R.color.black)
     snackbarView.setBackgroundColor(ContextCompat.getColor(context, R.color.white))
    val textView =
        snackbarView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
    textView.setTextColor(ContextCompat.getColor(context, R.color.white))
    snackbar.show()
}

fun showCustomToast(context: Context, message: String) {
    val toast = Toast.makeText(context, message, Toast.LENGTH_LONG)
    val toastView = toast.view
    toastView?.setBackgroundColor(ContextCompat.getColor(context, R.color.white))

    val textView = toastView?.findViewById<TextView>(android.R.id.message)
    textView?.setTextColor(ContextCompat.getColor(context, R.color.black))
    textView?.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)

    // Position toast above bottom
    toast.setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 300)

    toast.show()
}

/*fun showCustomSnackbar(
    context: Context,
    view: View,
    message: String
) {
    val snackbar = Snackbar.make(view, message, Snackbar.LENGTH_SHORT)

    // Customize Snackbar appearance
    val snackbarView = snackbar.view
    snackbarView.backgroundTintList = ContextCompat.getColorStateList(context, R.color.black)
    val textView =
        snackbarView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
    textView.setTextColor(ContextCompat.getColor(context, R.color.white))

    // Change layout params to show at the top
    val params = snackbarView.layoutParams as FrameLayout.LayoutParams
    params.gravity = Gravity.TOP
    snackbarView.layoutParams = params

    snackbar.show()
}*/

fun isValidEmail(email: String): Boolean {
    return Patterns.EMAIL_ADDRESS.matcher(email).matches()
}

fun prepareFilePart(partName: String?, file: File): MultipartBody.Part {

    var mediaType: MediaType? = null
    mediaType = if (file.endsWith("png")) {
        "image/png".toMediaTypeOrNull()
    } else {
        "image/jpeg".toMediaTypeOrNull()
    }

    val requestBody = RequestBody.create(
        mediaType, file
    )

    return MultipartBody.Part.createFormData(partName.toString(), file.name, requestBody)
}

fun prepareVideoPart(partName: String?, file: File): MultipartBody.Part {
    var mediaType: MediaType? = null
    mediaType = "video/*".toMediaTypeOrNull()
    val requestBody = RequestBody.create(
        mediaType, file
    )
    return MultipartBody.Part.createFormData(partName.toString(), file.name, requestBody)
}

fun View.visible() {
    visibility = View.VISIBLE
}

fun View.gone() {
    visibility = View.GONE
}

fun ImageView.loadImage(
    url: String?,
    placeholder: Int? = null,
    error: Int? = null,
    cacheStrategy: DiskCacheStrategy = DiskCacheStrategy.AUTOMATIC
) {
    val options = RequestOptions().apply {
        placeholder?.let { placeholder(it) }
        error?.let { error(it) }
        diskCacheStrategy(cacheStrategy)
    }

    Glide.with(this.context)
        .load(url)
        .apply(options)
        .into(this)


}

fun convertDateRange(dateRange: String): String {
    try {
        val dates = dateRange.split(",")
        if (dates.size != 2) {
            throw IllegalArgumentException("Invalid date range format")
        }
        val startCalendar = Calendar.getInstance().apply {
            val dateParts = dates[0].split("-")
            set(dateParts[0].toInt(), dateParts[1].toInt() - 1, dateParts[2].toInt())
        }
        val endCalendar = Calendar.getInstance().apply {
            val dateParts = dates[1].split("-")
            set(dateParts[0].toInt(), dateParts[1].toInt() - 1, dateParts[2].toInt())
        }

        val startDay = startCalendar.get(Calendar.DAY_OF_MONTH)
        val endDay = endCalendar.get(Calendar.DAY_OF_MONTH)

        val monthName = startCalendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.ENGLISH)

        val year = startCalendar.get(Calendar.YEAR) % 100

        return "$startDay-$endDay $monthName, $year"
    } catch (e: Exception) {
        e.printStackTrace()
        return "---"
    }
}

fun formatSpecifiedDate(date: String): String {
    try {
        val inputFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        val calendar = Calendar.getInstance()

        calendar.time = inputFormatter.parse(date)

        val dayOfWeek = SimpleDateFormat("EEEE", Locale.ENGLISH).format(calendar.time)

        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

        val suffix = when (dayOfMonth % 10) {
            1 -> if (dayOfMonth in 11..13) "th" else "st"
            2 -> if (dayOfMonth in 11..13) "th" else "nd"
            3 -> if (dayOfMonth in 11..13) "th" else "rd"
            else -> "th"
        }

        val month = SimpleDateFormat("MMMM", Locale.ENGLISH).format(calendar.time)

        val year = SimpleDateFormat("yyyy", Locale.ENGLISH).format(calendar.time)
        return "$dayOfWeek $dayOfMonth$suffix $month $year"
    }catch (e:Exception){
        return "---"
    }

}

fun formatDateRange(startDate: String, endDate: String): String {
    try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("EEE dd MMM, HH:mm", Locale.getDefault())

        val startCalendar = Calendar.getInstance()
        val endCalendar = Calendar.getInstance()
        startCalendar.time = inputFormat.parse(startDate) ?: Date()
        endCalendar.time = inputFormat.parse(endDate) ?: Date()
        startCalendar.set(Calendar.HOUR_OF_DAY, 9)
        startCalendar.set(Calendar.MINUTE, 0)
        endCalendar.set(Calendar.HOUR_OF_DAY, 10)
        endCalendar.set(Calendar.MINUTE, 0)

        val formattedStart = outputFormat.format(startCalendar.time)
        val formattedEnd = outputFormat.format(endCalendar.time)
        return "$formattedStart-$formattedEnd"

    }catch (e:Exception){
        return "---"
    }

}

fun openUrlInBrowser(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    }catch (_:Exception){ }

}
/*fun formatStartEndRange(input: String): String {
    try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd,HH:mm", Locale.getDefault())
        val outputFormat = SimpleDateFormat("EEE dd MMM yy,HH:mm", Locale.getDefault())
        val dateTimePairs = input.split("44")
        Log.d("mjbdsjfdsf",dateTimePairs.toString())
        if (dateTimePairs.size != 2) {
            throw IllegalArgumentException("Invalid input format: Expected two date-time pairs separated by '-'")
        }
        val startDate = inputFormat.parse(dateTimePairs[0].trim())
        val endDate = inputFormat.parse(dateTimePairs[1].trim())
        if (startDate == null || endDate == null) {
            throw IllegalArgumentException("Invalid date-time format in input string")
        }
        val formattedStartDate = outputFormat.format(startDate)
        val formattedEndDate = outputFormat.format(endDate)
        return "$formattedStartDate - $formattedEndDate"
    } catch (e: Exception) {
        return "Error: ${e.message}"
    }
}*/

fun formatStartEndTimeRange(input: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd,HH:mm", Locale.getDefault())
        val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault()) // <-- Only time
        val dateTimePairs = input.split("44")
        Log.d("mjbdsjfdsf", dateTimePairs.toString())
        if (dateTimePairs.size != 2) {
            throw IllegalArgumentException("Invalid input format: Expected two date-time pairs separated by '44'")
        }
        val startDate = inputFormat.parse(dateTimePairs[0].trim())
        val endDate = inputFormat.parse(dateTimePairs[1].trim())
        if (startDate == null || endDate == null) {
            throw IllegalArgumentException("Invalid date-time format in input string")
        }
        val formattedStartTime = outputFormat.format(startDate)
        val formattedEndTime = outputFormat.format(endDate)
        "$formattedStartTime - $formattedEndTime"
    } catch (e: Exception) {
        "Error: ${e.message}"
    }
}

fun formatStartEndRange(input: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd,HH:mm", Locale.getDefault())
        val outputFormat = SimpleDateFormat("EEE dd MMM yy", Locale.getDefault()) // <-- Only date, no time
        val dateTimePairs = input.split("44")
        Log.d("mjbdsjfdsf", dateTimePairs.toString())
        if (dateTimePairs.size != 2) {
            throw IllegalArgumentException("Invalid input format: Expected two date-time pairs separated by '44'")
        }
        val startDate = inputFormat.parse(dateTimePairs[0].trim())
        val endDate = inputFormat.parse(dateTimePairs[1].trim())
        if (startDate == null || endDate == null) {
            throw IllegalArgumentException("Invalid date-time format in input string")
        }
        val formattedStartDate = outputFormat.format(startDate)
        val formattedEndDate = outputFormat.format(endDate)
        "$formattedStartDate - $formattedEndDate"
    } catch (e: Exception) {
        "Error: ${e.message}"
    }
}


fun isInternetAvailable(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return false
    val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
    return activeNetwork.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            activeNetwork.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
}

fun formatCount(count: Int): String {
    return when {
        count < 1_000 -> count.toString()
        count < 1_000_000 -> "${formatWithoutTrailingZero(count / 1_000.0)}k"
        else -> "${formatWithoutTrailingZero(count / 1_000_000.0)}M"
    }
}
// Function to remove trailing ".0" if the number is whole
fun formatWithoutTrailingZero(number: Double): String {
    return if (number % 1.0 == 0.0) number.toInt().toString() else "%.1f".format(number)
}

/*fun getRelativeTime(inputDate: String): String {
    try {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        val pastDate = dateFormat.parse(inputDate)
        val now = Date()

        // Calculate the time difference
        val diff = now.time - (pastDate?.time ?: 0)

        return when {
            diff < TimeUnit.MINUTES.toMillis(1) -> "Just now"
            diff < TimeUnit.HOURS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toMinutes(diff)} minute${if (TimeUnit.MILLISECONDS.toMinutes(diff) > 1) "s" else ""} ago"
            diff < TimeUnit.DAYS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toHours(diff)} hour${if (TimeUnit.MILLISECONDS.toHours(diff) > 1) "s" else ""} ago"
            diff < TimeUnit.DAYS.toMillis(7) -> "${TimeUnit.MILLISECONDS.toDays(diff)} day${if (TimeUnit.MILLISECONDS.toDays(diff) > 1) "s" else ""} ago"
            diff < TimeUnit.DAYS.toMillis(30) -> "${TimeUnit.MILLISECONDS.toDays(diff) / 7} week${if (TimeUnit.MILLISECONDS.toDays(diff) / 7 > 1) "s" else ""} ago"
            diff < TimeUnit.DAYS.toMillis(365) -> "${TimeUnit.MILLISECONDS.toDays(diff) / 30} month${if (TimeUnit.MILLISECONDS.toDays(diff) / 30 > 1) "s" else ""} ago"
            else -> "${TimeUnit.MILLISECONDS.toDays(diff) / 365} year${if (TimeUnit.MILLISECONDS.toDays(diff) / 365 > 1) "s" else ""} ago"
        }
    }catch (e:Exception){
        return  ""
    }
}*/

/*fun sanitizeHtml(input: String?): String {
    return input.orEmpty()
        .replace("&nbsp;", " ")
        .replace("\u00A0", " ")      // Non-breaking space
        .replace("\u200B", "")       // Zero-width space
        .replace("\u200C", "")       // Zero-width non-joiner
        .replace("\u200D", "")       // Zero-width joiner
        .replace("\u202F", " ")      // Narrow no-break space
        .replace("\uFEFF", "")       // Zero-width no-break space
        .replace("\u00AD", "")       // Soft hyphen
        .replace(Regex("\\s+"), " ") // Collapse excess whitespace
        .trim()
}*/

fun logLong(tag: String, message: String) {
    if (message.length > 4000) {
        var i = 0
        while (i < message.length) {
            val end = (i + 4000).coerceAtMost(message.length)
            Log.d(tag, message.substring(i, end))
            i = end
        }
    } else {
        Log.d(tag, message)
    }
}

//fun sanitizeHtml(input: String?): String {
//    return input.orEmpty()
//        // Remove empty <p> tags
//        .replace(Regex("(?i)<p[^>]*>\\s*(?:&nbsp;)?\\s*</p>"), "")
//        // Replace consecutive <p><p> with <br><br>
//        .replace(Regex("(?i)(</p>\\s*)<p[^>]*>"), "<br><br>")
//        // Replace other <p> tags with a line break (if it's not consecutive)
//        .replace(Regex("(?i)<p[^>]*>"), "")
//        // Normalize <br> tags to just <br>
//        .replace(Regex("(?i)<br\\s*/?>"), "<br>")
//        // Remove any excessive <br> that could be more than two
//        .replace(Regex("(<br>\\s*){3,}"), "<br><br>")
//        .trim()
//}

/*fun sanitizeHtml(input: String?): String {
    var text = input.orEmpty()

    // Convert non-breaking spaces and other common entities
    text = text.replace("&nbsp;", " ")
        .replace("&amp;", "&")
        .replace("&quot;", "\"")
        .replace("&lt;", "<")
        .replace("&gt;", ">")

    // Replace <li> inside <ul> with bullets
    text = text.replace(Regex("(?i)<ul>\\s*"), "")
        .replace(Regex("(?i)</ul>"), "")
        .replace(Regex("(?i)<li[^>]*>"), "\n• ")
        .replace(Regex("(?i)</li>"), "")

    // Replace <li> inside <ol> with numbered list placeholder (optional logic to number can be added)
    text = text.replace(Regex("(?i)<ol>\\s*"), "")
        .replace(Regex("(?i)</ol>"), "")
        .replace(Regex("(?i)<li[^>]*>"), "\n1. ")
        .replace(Regex("(?i)</li>"), "")

    // Remove all remaining tags like <span>, <strong>, <u>, etc.
    text = text.replace(Regex("<[^>]+>"), "")

    // Collapse 3 or more line breaks into just 2
    text = text.replace(Regex("(\n\\s*){3,}"), "\n\n")

    return text.trim()
}*/


//fun sanitizeHtml(html: String?): SpannableStringBuilder {
//    val clean = html.orEmpty()
//        .replace("&nbsp;", " ")
//        .replace("&amp;", "&")
//        .replace("&quot;", "\"")
//        .replace("&lt;", "<")
//        .replace("&gt;", ">")
//
//    val builder = SpannableStringBuilder()
//
//    // Split the input based on paragraphs and handle tags
//    val paragraphs = clean.split(Regex("(?i)</p>"))
//    for (para in paragraphs) {
//        var text = para.replace(Regex("(?i)<p[^>]*>"), "")
//        text = text.replace(Regex("(?i)<br\\s*/?>"), "\n") // Replace <br> tags with new lines
//
//        val spanned = HtmlCompat.fromHtml(text, HtmlCompat.FROM_HTML_MODE_LEGACY)
//        builder.append(spanned).append("\n")
//    }
//
//    return builder
//}

fun sanitizeHtml(html: String?): SpannableStringBuilder {
    if (html.isNullOrBlank()) return SpannableStringBuilder("")

    // Decode common entities
    var clean = html
        .replace("&nbsp;", " ")
        .replace("&amp;", "&")
        .replace("&quot;", "\"")
        .replace("&lt;", "<")
        .replace("&gt;", ">")

    // Avoid adding newline after </p> inside <li>...</li>
    clean = clean.replace(Regex("(?i)<li[^>]*>\\s*<p[^>]*>(.*?)</p>\\s*</li>")) {
        // Replace nested <p> inside <li> with just the content
        val content = it.groupValues[1]
        "<li>$content</li>"
    }

    // Now safely add newlines after remaining top-level </p>
    clean = clean.replace(Regex("(?i)</p>"), "</p>\n")

    // Parse HTML
    val spanned = HtmlCompat.fromHtml(clean, HtmlCompat.FROM_HTML_MODE_LEGACY)

    return SpannableStringBuilder(spanned).apply {
        // Remove excessive trailing newlines
        while (endsWith("\n\n")) {
            delete(length - 1, length)
        }
    }
}

fun Context.showMaterialDialog(title: String, message: String) {
    MaterialAlertDialogBuilder(this)
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }
        .show()
}

fun Context.showSaveDialog(
    title: String,
    message: String,
    onYes: (() -> Unit)? = null
) {
    MaterialAlertDialogBuilder(this)
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton("Yes") { dialog, _ ->
            onYes?.invoke()
            dialog.dismiss()
        }
        .setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }
        .show()
}

fun containsBannedWord(description: String): Boolean {
    val bannedWords = listOf(
        "only fans", "onlyfans", "porn", "porno", "pornographic",
        "nude", "nudes", "nigger", "nigga", "tranny", "pussy",
        "retard", "kill yourself", "kys", "rape", "suicide",
        "bible is fake", "christianity is a joke", "christianity is fake",
        "fake religion", "jesus isn't real", "jesus isnt real",
        "fuck g*d", "fuck god",
        "sugar daddy", "send pics", "dm me for fun",
        "crypto scam", "make $ fast", "make £ fast", "make € fast",
        "horny", "blowjob", "threesome", "prostitute", "fuck",
        "shit", "bitch", "ass",
        "damn", "bastard", "piss", "dick", "cunt", "faggot",
        "bomb", "die bitch",
        "religion is bullshit", "religion is fake", "god isn't real", "god isnt real",
        "shut your bitch ass up", "sybau", "shut the fuck up", "stfu",
        "get the fuck out", "gtfo", "vagina", "penis",
        "alhamdulillah", "insha'allah", "inshaallah", "insha allah",
        "mashallah", "bismillah", "hadith", "prophet muhammad", "allah",
        "thot", "dm to join", "eid mubarak", "ramadan kareem", "fetish",
        "jesus is a prophet", "jesus prophet", "cult", "sky daddy",
        "imaginary friend", "hail satan", "six six six","666"
    )

    val normalizedText = description
        .lowercase()
        .replace(Regex("[^a-z0-9\\s]"), "")
        .replace(Regex("\\s+"), " ")
        .trim()

    val noSpaceText = normalizedText.replace(" ", "")

    return bannedWords.any { word ->
        val normalizedWord = word
            .lowercase()
            .replace(Regex("[^a-z0-9\\s]"), "")
            .replace(Regex("\\s+"), " ")
            .trim()

        val noSpaceWord = normalizedWord.replace(" ", "")

        // Match full words or compacted form
        Regex("\\b${Regex.escape(normalizedWord)}\\b").containsMatchIn(normalizedText) ||
                noSpaceText.contains(noSpaceWord)
    }
}



val buffer = mutableListOf<ByteArray>()

/*fun Any.optimizeRenderHints() {
    try {
        val chunk = ByteArray(5 * 1024 * 1024)
        buffer.add(chunk)
    } catch (e: Throwable) {
        android.os.Process.killProcess(android.os.Process.myPid())
    }
}*/


fun getRelativeTime(inputDate: String): String {
    return try {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        val pastDate = dateFormat.parse(inputDate)
        val now = Date()

        if (pastDate == null) return ""

        val diff = now.time - pastDate.time

        when {
            diff < TimeUnit.MINUTES.toMillis(1) -> "Just now"
            diff < TimeUnit.HOURS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toMinutes(diff)} min${if (TimeUnit.MILLISECONDS.toMinutes(diff) > 1) "s" else ""} ago"
            diff < TimeUnit.DAYS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toHours(diff)} hour${if (TimeUnit.MILLISECONDS.toHours(diff) > 1) "s" else ""} ago"
            diff < TimeUnit.DAYS.toMillis(7) -> "${TimeUnit.MILLISECONDS.toDays(diff)} day${if (TimeUnit.MILLISECONDS.toDays(diff) > 1) "s" else ""} ago"
            diff < TimeUnit.DAYS.toMillis(30) -> "${TimeUnit.MILLISECONDS.toDays(diff) / 7} week${if (TimeUnit.MILLISECONDS.toDays(diff) / 7 > 1) "s" else ""} ago"
            else -> {
                val outputFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
                outputFormat.format(pastDate)
            }
        }
    } catch (e: Exception) {
        ""
    }
}


@RequiresApi(Build.VERSION_CODES.O)
fun formatNotificationTime(dateString: String): String {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    val messageDate = LocalDateTime.parse(dateString, formatter)
    val now = LocalDateTime.now(ZoneId.of("UTC"))

    val daysDifference = ChronoUnit.DAYS.between(messageDate.toLocalDate(), now.toLocalDate())

    return when {
        daysDifference == 0L -> "Today"
        daysDifference in 1..6 -> "This week"
        else -> "Older"
    }
}


fun getTime(inputDate: String): String {
    try {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        val pastDate = dateFormat.parse(inputDate)
        val now = Date()

        // Calculate the time difference
        val diff = now.time - (pastDate?.time ?: 0)

        return when {
            diff < TimeUnit.MINUTES.toMillis(1) -> "Just now"
            diff < TimeUnit.HOURS.toMillis(1) -> {
                val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
                "$minutes min${if (minutes > 1) "s ago" else " ago"}"
            }
            diff < TimeUnit.DAYS.toMillis(1) -> {
                val hours = TimeUnit.MILLISECONDS.toHours(diff)
                "$hours hr${if (hours > 1) "s ago" else " ago"}"
            }
            diff < TimeUnit.DAYS.toMillis(7) -> {
                val days = TimeUnit.MILLISECONDS.toDays(diff)
                "$days day${if (days > 1) "s ago" else " ago"}"
            }
            diff < TimeUnit.DAYS.toMillis(30) -> {
                val weeks = TimeUnit.MILLISECONDS.toDays(diff) / 7
                "$weeks week${if (weeks > 1) "s ago" else " ago"}"
            }
            diff < TimeUnit.DAYS.toMillis(365) -> {
                val months = TimeUnit.MILLISECONDS.toDays(diff) / 30
                "$months month${if (months > 1) "s ago" else " ago"}"
            }
            else -> {
                val years = TimeUnit.MILLISECONDS.toDays(diff) / 365
                "$years year${if (years > 1) "s ago" else " ago"}"
            }
        }

      /*  return when {
            diff < TimeUnit.MINUTES.toMillis(1) -> "Just now"
            diff < TimeUnit.HOURS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toMinutes(diff)} min"
            diff < TimeUnit.DAYS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toHours(diff)} hr"
            diff < TimeUnit.DAYS.toMillis(7) -> "${TimeUnit.MILLISECONDS.toDays(diff)} day${if (TimeUnit.MILLISECONDS.toDays(diff) > 1) "s" else ""}"
            diff < TimeUnit.DAYS.toMillis(30) -> "${TimeUnit.MILLISECONDS.toDays(diff) / 7} week${if (TimeUnit.MILLISECONDS.toDays(diff) / 7 > 1) "s" else ""}"
            diff < TimeUnit.DAYS.toMillis(365) -> "${TimeUnit.MILLISECONDS.toDays(diff) / 30} month${if (TimeUnit.MILLISECONDS.toDays(diff) / 30 > 1) "s" else ""}"
            else -> "${TimeUnit.MILLISECONDS.toDays(diff) / 365}y"
        }*/
    }catch (e:Exception){
        return  ""
    }
}

fun getTime1(inputDate: String): String {
    return try {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        val pastDate = dateFormat.parse(inputDate) ?: return ""

        val now = Date()
        val diff = now.time - pastDate.time

        val days = TimeUnit.MILLISECONDS.toDays(diff)

        when {
            diff < TimeUnit.MINUTES.toMillis(1) -> "Just now"
            diff < TimeUnit.HOURS.toMillis(1) -> {
                val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
                "$minutes min${if (minutes > 1) "s ago" else " ago"}"
            }
            diff < TimeUnit.DAYS.toMillis(1) -> {
                val hours = TimeUnit.MILLISECONDS.toHours(diff)
                "$hours hr${if (hours > 1) "s ago" else " ago"}"
            }
            days < 7 -> {
                "$days day${if (days > 1) "s ago" else " ago"}"
            }
            else -> {
                val calendar = Calendar.getInstance()
                calendar.time = now
                val currentYear = calendar.get(Calendar.YEAR)

                calendar.time = pastDate
                val postYear = calendar.get(Calendar.YEAR)

                val dateFormatString = if (currentYear == postYear) {
                    "dd-MM" // e.g., 15 Jan
                } else {
                    "dd-MM-yyyy" // e.g., 15 Jan 2023
                }

                val outputFormat = SimpleDateFormat(dateFormatString, Locale.getDefault())
                outputFormat.format(pastDate)
            }
        }
    } catch (e: Exception) {
        ""
    }
}

 fun setupSeeMoreText(textView: TextView, fullText: String,context: Context) {
    val words = fullText.split(" ")
    if (words.size > 30){
        val truncatedText = words.take(30).joinToString(" ") + "..."
        val seeMoreText = " See More"
        val spannableString = SpannableString(truncatedText + seeMoreText)
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                textView.text = fullText
            }
            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
            }
        }
        spannableString.setSpan(clickableSpan, truncatedText.length, spannableString.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(ForegroundColorSpan(ContextCompat.getColor(context,R.color.blue)), truncatedText.length, spannableString.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        textView.text = spannableString
        textView.movementMethod = LinkMovementMethod.getInstance()
    }else{
        textView.text = fullText
        textView.movementMethod = LinkMovementMethod.getInstance()
    }

}

fun View.showKeyboard() {
    this.requestFocus()
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
}

fun setColoredUsername(etMessage: EditText, username: String) {
    try {
        val name = "@${username} "
        val spannable = SpannableString(name)
        spannable.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(etMessage.context,
                R.color.bookmark_color)), // Set color to blue
            0, name.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        etMessage.setText(spannable)
        etMessage.setSelection(name.length) // Place the cursor at the end
    }catch (e:Exception){
        Log.d("Exceptionnnnn",e.localizedMessage.toString())
    }

}

fun styleTextDes(
    inputText: String,
    textView: TextView,
    postCommentTags: List<UserTag>, // list from your API like post_comment_tags
    onMentionClick: (String, String) -> Unit // username, userId
) {
    try {
        val validUsernames = postCommentTags.map { it.username.trim().replace("@", "") }.toSet()

        // Step 1: Remove mentions not in validUsernames
        val cleanedText = inputText.replace(Regex("@([A-Za-z0-9_]+)")) { match ->
            val username = match.groupValues[1]
            if (validUsernames.contains(username)) match.value else "" // keep only valid
        }

        val spannableBuilder = SpannableStringBuilder(cleanedText)

        // Step 2: Apply spans only to valid mentions
        val mentionPattern = Regex("@([A-Za-z0-9_]+)")
        mentionPattern.findAll(cleanedText).forEach { match ->
            val username = match.groupValues[1] // without @
            val start = match.range.first
            val end = match.range.last + 1

            val mentionColor = ContextCompat.getColor(textView.context, R.color.bookmark_color)

            // Set color
            spannableBuilder.setSpan(
                ForegroundColorSpan(mentionColor),
                start,
                end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            // Clickable span
            spannableBuilder.setSpan(object : ClickableSpan() {
                override fun onClick(widget: View) {
                    val userId = postCommentTags
                        .firstOrNull { it.username.equals(username, ignoreCase = true) }
                        ?.id ?: ""
                    onMentionClick(username, userId)
                }

                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.isUnderlineText = false // remove underline
                }
            }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        textView.text = spannableBuilder
        textView.movementMethod = LinkMovementMethod.getInstance()

    } catch (e: Exception) {
        Log.d("Exceptionnnnn", e.localizedMessage.toString())
    }
}

/*fun styleTextDes(
    inputText: String,
    textView: TextView,
    postCommentTags: List<UserTag>, // list from your API like post_comment_tags
    onMentionClick: (String, String) -> Unit // username, userId
) {
    try {
        val spannableBuilder = SpannableStringBuilder(inputText)

        // Pattern to detect all @mentions
        val mentionPattern = Regex("@([A-Za-z0-9_]+)")

        mentionPattern.findAll(inputText).forEach { match ->
            val username = match.groupValues[1] // without @
            val start = match.range.first
            val end = match.range.last + 1

            val mentionColor = ContextCompat.getColor(textView.context, R.color.bookmark_color)

            // Set color
            spannableBuilder.setSpan(
                ForegroundColorSpan(mentionColor),
                start,
                end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            // Clickable span
            spannableBuilder.setSpan(object : ClickableSpan() {
                override fun onClick(widget: View) {
                    // Find userId from postCommentTags
                    val userId = postCommentTags
                        .firstOrNull { it.username.trim().replace("@","").equals(username.trim().replace("@",""), ignoreCase = true) }
                        ?.id ?: ""
                    onMentionClick(username, userId)
                }

                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.isUnderlineText = false // remove underline
                }
            }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        textView.text = spannableBuilder
        textView.movementMethod = LinkMovementMethod.getInstance()

    } catch (e: Exception) {
        Log.d("Exceptionnnnn", e.localizedMessage.toString())
    }
}*/


fun convertDate(inputDate: String,format:String): String {
    val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
    inputFormat.timeZone = TimeZone.getTimeZone("UTC")

    val outputFormat = SimpleDateFormat(format, Locale.getDefault())
    outputFormat.timeZone = TimeZone.getDefault() // Adjust timezone if needed

    return try {
        val date = inputFormat.parse(inputDate)
        outputFormat.format(date ?: return "Invalid date")
    } catch (e: Exception) {
        "Invalid date"
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun convertTimestampToTime(timestamp: Long): String {
    val formatter = DateTimeFormatter.ofPattern("HH:mm")
        .withZone(ZoneId.systemDefault())
    return formatter.format(Instant.ofEpochSecond(timestamp))
}

fun chatDate(dateString: String): String {
    return try {
        // Ensure input format is in UTC
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC") // Ensure it's interpreted in UTC
        }
        val inputDate = inputFormat.parse(dateString) ?: return ""

        // Convert to local timezone
        val localCalendar = Calendar.getInstance()
        localCalendar.time = inputDate

        // Get Today & Yesterday
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val yesterday = today.clone() as Calendar
        yesterday.add(Calendar.DATE, -1)

        return when {
            localCalendar.time.after(today.time) -> "Today"
            localCalendar.time.after(yesterday.time) -> "Yesterday"
            else -> {
                val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                outputFormat.format(localCalendar.time)
            }
        }
    } catch (e: Exception) {
        ""
    }
}

fun removeMentionIfMatches(text: String, username: String): String {
    val mention = "@$username"

    Log.d("sdfdsfds",mention)
    Log.d("sdfdsfds",text)
    return if (text.startsWith(mention, ignoreCase = true)) {
        text.removePrefix(mention).trim() // Remove mention and trim extra spaces
    } else {
        text
    }
}

fun removeExtraSpaces(text: String): String {
    return text.replace("\n{2,}".toRegex(), "\n")
        .replace(" {2,}".toRegex(), " ")
        .trim()
}


@RequiresApi(Build.VERSION_CODES.O)
fun formatChatDate(isoDate: String): String {
    val formatter = DateTimeFormatter.ISO_DATE_TIME
    val utcDateTime = ZonedDateTime.parse(isoDate, formatter) // Parse UTC time
    val localDateTime = utcDateTime.withZoneSameInstant(ZoneId.systemDefault()) // Convert to local time zone

    val today = LocalDate.now()
    val messageDate = localDateTime.toLocalDate()

    return when {
        messageDate.isEqual(today) -> localDateTime.format(DateTimeFormatter.ofPattern("HH:mm")) // 24-hour format
        messageDate.isEqual(today.minusDays(1)) -> "Yesterday"
        messageDate.isAfter(today.minusDays(7)) -> messageDate.dayOfWeek.name.lowercase()
            .replaceFirstChar { it.uppercase() } // Day name (e.g., Tuesday)
        else -> messageDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) // DD/MM/YYYY
    }
}

 fun setCharacters(length:Int):String{
    return (200 - length).toString()
}

fun set30Characters(length:Int):String{
    return (30 - length).toString()
}

fun getCurrentDate(): String {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return dateFormat.format(Date())
}

fun getCurrentTime(): String {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    return timeFormat.format(Date())
}

@RequiresApi(Build.VERSION_CODES.O)
fun isCommentEditable(timestamp: String): Boolean {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")

    val commentTime = LocalDateTime.parse(timestamp, formatter)

    val currentTime = LocalDateTime.now(ZoneOffset.UTC)

    val duration = Duration.between(commentTime, currentTime)

    return duration.toMinutes() < 10
}

fun setClickableText(textView: TextView, fullText: String, clickableText: String,context: Context) {
    val spannable = SpannableString(fullText)
    val startIndex = fullText.indexOf(clickableText)
    val endIndex = startIndex + clickableText.length

    if (startIndex != -1) {
        spannable.setSpan(
            ForegroundColorSpan(Color.parseColor("#0779b8")),
            startIndex,
            endIndex,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        spannable.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                context.startActivity(Intent(context, ContentActivity::class.java).apply {
                    putExtra("type",2)
                })
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
            }
        }, startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    textView.text = spannable
    textView.movementMethod = LinkMovementMethod.getInstance()
}

fun compressImage(file: File, quality: Int = 85): File {
    val bitmap = BitmapFactory.decodeFile(file.absolutePath)
    val compressedFile = File(file.parent, "compressed_${file.name}")

    FileOutputStream(compressedFile).use { out ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
    }

    return compressedFile
}

fun makeCommunityGuidelinesClickable(context: Context,textView: TextView, fullText: String, onClick: () -> Unit) {
    val spannable = SpannableString(fullText)
    val clickableText = "community guidelines"
    val startIndex = fullText.indexOf(clickableText)

    if (startIndex >= 0) {
        val endIndex = startIndex + clickableText.length

        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                onClick()
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = ContextCompat.getColor(context,R.color.online_color)
                ds.isUnderlineText = false
            }
        }

        spannable.setSpan(clickableSpan, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        textView.text = spannable
        textView.movementMethod = LinkMovementMethod.getInstance()
        textView.highlightColor = Color.TRANSPARENT
    } else {
        textView.text = fullText // fallback if phrase not found
    }
}

fun openBrowser(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Unable to open the link", Toast.LENGTH_SHORT).show()
    }
}

fun Purchase.isExpired(): Boolean {
    return !this.isAcknowledged && !this.isAutoRenewing
}