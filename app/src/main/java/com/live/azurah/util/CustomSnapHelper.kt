import android.view.View
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.OrientationHelper
import androidx.recyclerview.widget.RecyclerView

class CenterSnapHelper : LinearSnapHelper() {
    override fun calculateDistanceToFinalSnap(
        layoutManager: RecyclerView.LayoutManager,
        targetView: View
    ): IntArray {
        val out = IntArray(2)
        out[0] = calculateDistanceToCenter(layoutManager, targetView, getHorizontalHelper(layoutManager))
        out[1] = calculateDistanceToCenter(layoutManager, targetView, getVerticalHelper(layoutManager))
        return out
    }

    private fun calculateDistanceToCenter(
        layoutManager: RecyclerView.LayoutManager,
        targetView: View,
        helper: OrientationHelper?
    ): Int {
        if (helper == null) return 0
        val childCenter = helper.getDecoratedStart(targetView) + helper.getDecoratedMeasurement(targetView) / 2
        val containerCenter = if (layoutManager.clipToPadding) {
            helper.startAfterPadding + helper.totalSpace / 2
        } else {
            helper.end / 2
        }
        return childCenter - containerCenter
    }

    private fun getHorizontalHelper(layoutManager: RecyclerView.LayoutManager): OrientationHelper? {
        return OrientationHelper.createHorizontalHelper(layoutManager)
    }

    private fun getVerticalHelper(layoutManager: RecyclerView.LayoutManager): OrientationHelper? {
        return OrientationHelper.createVerticalHelper(layoutManager)
    }
}
