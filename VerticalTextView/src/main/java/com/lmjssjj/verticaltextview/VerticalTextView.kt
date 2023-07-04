package com.lmjssjj.verticaltextview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.text.TextPaint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import kotlin.math.absoluteValue

/**
 * @author : lmjssjj
 * @since  :
 */
class VerticalTextView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    companion object {
        const val LOG_TAG = "VerticalTextView"

    }

    //显示文本
    var mText: String? = ""

    //文本颜色
    var mTextColor = 0x000000

    //文本大小
    var mTextSize = sp2px(context, 14F)

    var mRowSpacing = 0

    var mColumnSpacing = dp2px(context, 4F)

    var mMaxColumns = 0

    var mActualColumns = 0

    //文字对齐方式
    var mTextAlign = TextAlign.TOP

    var isShowEllipsis = false

    var mTextStyle = 0

    var mTypeface: Typeface? = null

    var mMaxCharWidth = 0

    var mCharHeight = 0

    var mLineHeight = 0

    private var mMeasureWidth = 0

    private var mMeasureHeight = 0

    private var mFontMetrics: Paint.FontMetrics? = null

    //文本画笔
    private var mTextPaint: TextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)

    //一列显示的字符数
    var mColumnCharCount = 0

    private var mTextGravity = Gravity.LEFT

    private var isRtl = false

    //显示列的字符串
    private val mColumnTexts = mutableListOf<String>()

    init {

        val a = getContext().obtainStyledAttributes(
            attrs, R.styleable.VerticalTextView, defStyleAttr, 0
        )

        mText = a.getString(R.styleable.VerticalTextView_text)
        mTextColor = a.getColor(R.styleable.VerticalTextView_textColor, Color.BLACK)
        mTextSize = a.getDimensionPixelSize(R.styleable.VerticalTextView_textSize, mTextSize)
        mRowSpacing = a.getDimensionPixelSize(R.styleable.VerticalTextView_rowSpacing, mRowSpacing)
        mColumnSpacing =
            a.getDimensionPixelSize(R.styleable.VerticalTextView_columnSpacing, mColumnSpacing)
        mColumnCharCount = a.getInteger(R.styleable.VerticalTextView_columnCharCount, -1)
        mMaxColumns = a.getInteger(R.styleable.VerticalTextView_maxColumns, -1)
        mTextGravity = Gravity.values()[a.getInt(R.styleable.VerticalTextView_textGravity, 0)]
//        includeFontPadding = a.getBoolean(R.styleable.VerticalTextView_includeFontPadding, true)
        mTextStyle = a.getInt(R.styleable.VerticalTextView_textStyle, mTextStyle)
        isRtl = a.getBoolean(R.styleable.VerticalTextView_isRtl, isRtl)
        a.recycle()

        initTextPaint()
        invalidateTextMeasure()
    }

    private fun initTextPaint() {
        val res = resources
        mTextPaint.density = res.displayMetrics.density
        if (mTypeface != null) {
            mTextPaint.setTypeface(mTypeface)
        }
        mTextPaint.textSize = mTextSize.toFloat()
        mTextPaint.color = mTextColor
        mTextPaint.isFakeBoldText = mTextStyle and Typeface.BOLD != 0
        mTextPaint.textSkewX = if (mTextStyle and Typeface.ITALIC != 0) -0.25F else 0F
        mFontMetrics = mTextPaint?.fontMetrics
    }

    private fun invalidateTextMeasure() {
        if (mText.isNullOrEmpty()) {
            return
        }
        mText?.forEach { c ->
            val width = mTextPaint!!.measureText("$c")
            if (mMaxCharWidth < width) {
                mMaxCharWidth = width.toInt()
            }
        }
        mCharHeight =
            (Math.abs(mFontMetrics!!.bottom - mFontMetrics!!.top) + Math.abs(
                mFontMetrics!!.leading
            )).toInt()

//        Log.i(LOG_TAG, "mMaxCharWidth:${mMaxCharWidth}")
//        Log.i(LOG_TAG, "mCharHeight:${mCharHeight}")

    }

    private fun updateColumnTexts(columnCount: Int) {
        mColumnTexts.clear()
        var i = columnCount
        while (i < mText!!.length) {
            mColumnTexts.add(mText!!.substring(i - columnCount, i))
            i += columnCount
        }
        if (i - columnCount < mText!!.length) {
            mColumnTexts.add(mText!!.substring(i - columnCount))
        }
//        mColumnTexts.forEach {
//            Log.i(LOG_TAG, "text:${it}")
//        }
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val height = measuredHeight(heightMeasureSpec)
        val width = measuredWidth(widthMeasureSpec)
//        Log.i(LOG_TAG, "width:${width}")
//        Log.i(LOG_TAG, "height:${height}")
        setMeasuredDimension(width, height);
    }

    private fun measuredWidth(widthMeasureSpec: Int): Int {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        if (widthMode == MeasureSpec.EXACTLY) {
            mMeasureWidth = widthSize// - paddingLeft - paddingRight
            if (mCharHeight > 0) {
                val charCount =
                    (mMeasureHeight - mCharHeight) / (mCharHeight + mRowSpacing) + 1//一列的字符个数
                updateColumnTexts(charCount)
            }
        } else {
            if (mText.isNullOrEmpty()) {
                mMeasureWidth = 0
            } else {
                if (mCharHeight > 0) {
                    var charCount = 1
                    if (mColumnCharCount > 0) {
                        charCount = mColumnCharCount
                    } else if (mMeasureHeight > 0) {
                        charCount =
                            (mMeasureHeight - mCharHeight) / (mCharHeight + mRowSpacing) + 1//一列的字符个数
                    }
                    updateColumnTexts(charCount)
                    var column = mColumnTexts.size
                    if (mMaxColumns > 0) {
                        if (column > mMaxColumns) {
                            isShowEllipsis = true
                            mActualColumns = mMaxColumns
                        } else {
                            mActualColumns = column
                        }
                    } else {
                        mActualColumns = column
                    }
                    mMeasureWidth =
                        (mMaxCharWidth + mColumnSpacing) * (mActualColumns - 1) + mMaxCharWidth + paddingRight + paddingLeft
                } else {
                    mMeasureWidth = suggestedMinimumWidth
                }
            }
        }
        return mMeasureWidth;
    }

    private fun measuredHeight(heightMeasureSpec: Int): Int {
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        if (heightMode == MeasureSpec.EXACTLY) {
            mMeasureHeight = heightSize// - paddingTop - paddingBottom
        } else {
            if (mText.isNullOrEmpty()) {
                mMeasureHeight = 0
            } else {
                mMeasureHeight = heightSize// - paddingTop - paddingBottom
                /*
                 * bug fix 当parent是RelativeLayout时，RelativeLayout onMeasure会测量两次，
                 * 当自定义view宽或高设置为wrap_content时，会出现计算出错，显示异常。这是由于
                 * 第一次调用时宽高mode默认是wrap_content类型，size会是parent size。这将导致
                 * 自定义view第一次计算出的size不是我们需要的值，影响第二次正常计算。
                 */
                if ((layoutParams?.height ?: 0) > 0) {
                    mMeasureHeight = layoutParams.height
                }
                if (mColumnCharCount > 0) {
                    mMeasureHeight = Int.MIN_VALUE
                    updateColumnTexts(mColumnCharCount)
                    for (i in 0 until mColumnTexts.size) {
                        //获取最高一列作为高度
                        mMeasureHeight =
                            mMeasureHeight.coerceAtLeast((mCharHeight + mRowSpacing) * (mColumnTexts[i].length - 1) + mCharHeight)
                    }
                    mMeasureHeight = mMeasureHeight + paddingBottom + paddingTop
                } else {
                    mMeasureHeight =
                        mMeasureHeight.coerceAtMost(mCharHeight * mText!!.length) + paddingBottom + paddingTop
                }
            }
        }
        return mMeasureHeight;
    }

    override fun onDraw(canvas: Canvas?) {
//        canvas?.drawARGB(55, 55, 55, 55)

        var startX = 0
        var startY = 0
        if (mColumnTexts.isEmpty()) {
            return
        }
        for (i in 0 until mColumnTexts.size) {
            startX = if (i == 0) paddingLeft else startX + mMaxCharWidth + mColumnSpacing//每一列的起始位置
            startX = if (i == 0) {
                if (isRtl) mMeasureWidth - paddingRight - mMaxCharWidth else paddingLeft
            } else {
                if (isRtl) mMeasureWidth - paddingRight - (mMaxCharWidth) * (i + 1) - mColumnSpacing * i else startX + mMaxCharWidth + mColumnSpacing
            }
//            Log.i(LOG_TAG, "startX:${startX}");
            val chars = mColumnTexts[i]
            val isLastColumn = i == mActualColumns - 1
            for (j in 0 until chars.length) {
                startY =
                    if (j == 0) paddingTop + mFontMetrics!!.ascent.absoluteValue.toInt() else startY + mCharHeight + mRowSpacing
//                Log.i(LOG_TAG, "startY:${startY}");
                if (mActualColumns == mMaxColumns && isShowEllipsis && j == chars.length - 1 && isLastColumn) {
                    canvas?.drawText(
                        "\u00B7",
                        (if (mTextGravity.isCenter()) startX + mMaxCharWidth / 2 + 1 else startX).toFloat(),
                        startY.toFloat(),
                        mTextPaint!!
                    )
                    return
                } else {
                    canvas?.drawText(
                        "${chars[j]}",
                        (if (mTextGravity.isCenter()) startX + mMaxCharWidth / 2 + 1 else startX).toFloat(),
                        startY.toFloat(),
                        mTextPaint
                    )
                }
            }
        }
    }

    //设置字体
    fun setTypeface(typeface: Typeface) {
        if (mTextPaint?.getTypeface() !== typeface) {
            mTypeface = typeface
            mTextPaint?.setTypeface(typeface)
            mFontMetrics = mTextPaint.fontMetrics;
            postInvalidate()
        }
    }

    fun setText(text: String) {
        mText = text;
        invalidateTextMeasure();
        requestLayout()
    }

    fun setTextSize(size: Int) {
        mTextSize = size
        mTextPaint.textSize = mTextSize.toFloat()
        mFontMetrics = mTextPaint.fontMetrics;
        invalidateTextMeasure()
        requestLayout()
    }

    fun setTextColor(color: Int) {
        mTextColor = color
        mTextPaint.color = mTextColor
        postInvalidate()
    }

    fun setRowSpacing(spacing: Int) {
        mRowSpacing = spacing
        requestLayout()
    }

    fun setColumnSpacing(spacing: Int) {
        mColumnSpacing = spacing
        requestLayout()
    }

    fun isRtl(): Boolean {
        return isRtl
    }

    private fun sp2px(context: Context, sp: Float): Int = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_SP, sp, context.resources.displayMetrics
    ).toInt()

    private fun dp2px(context: Context, dp: Float): Int = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, dp, context.resources.displayMetrics
    ).toInt()

    enum class Gravity {
        LEFT, CENTER;

        fun isCenter(): Boolean = this == CENTER
    }

    enum class TextAlign {
        TOP, CENTER, BOTTOM;
    }
}