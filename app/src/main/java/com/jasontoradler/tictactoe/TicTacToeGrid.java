package com.jasontoradler.tictactoe;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom tic-tac-toe grid which has ImageViews for each cell and the ability
 * to listen to each view click.
 */
public class TicTacToeGrid extends RelativeLayout {

    private static final String TAG = "TicTacToeGrid";
    private static final int DEFAULT_NUM_COLS = 3;
    private static final int DEFAULT_NUM_ROWS = 3;
    private static final int DEFAULT_CELL_MARGIN = 32;
    private static final int DEFAULT_COLOR = 0xff000000;
    private static final float DEFAULT_STROKE_WIDTH = 9f;
    private static final long DEFAULT_GRID_LINE_ANIMATION_DURATION = 500;
    private static final long DEFAULT_GRID_LINE_ANIMATION_START_DELAY = 750;
    private static final long DEFAULT_CELL_IMAGE_ANIMATOR_DURATION = 500;

    private OnCellClickListener mOnCellClickListener = null;
    private Paint mPaint;
    private int mNumCols = DEFAULT_NUM_COLS;
    private int mNumRows = DEFAULT_NUM_ROWS;
    private int mCellMargin = DEFAULT_CELL_MARGIN;
    private List<ImageView> mCells;
    private int mColor = DEFAULT_COLOR;
    private int mWidth;
    private int mHeight;
    private float mVertical1;
    private float mVertical2;
    private float mHorizontal1;
    private float mHorizontal2;
    private float mStrokeWidth = DEFAULT_STROKE_WIDTH;
    private float mVertLineX1;
    private float mVertLineX2;
    private float mHorizLineY1;
    private float mHorizLineY2;
    private long mGridLineAnimationDuration = DEFAULT_GRID_LINE_ANIMATION_DURATION;
    private long mGridLineAnimationStartDelay = DEFAULT_GRID_LINE_ANIMATION_START_DELAY;
    private long mCellImageAnimatorDuration = DEFAULT_CELL_IMAGE_ANIMATOR_DURATION;
    private boolean mIsEnabled = true;
    private boolean mIsGridAnimationDone = false;
    private View.OnClickListener mCellClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            // Handle each click on a specific ImageView.
            int position = (int) v.getTag();
//            Log.v(TAG, "onClick: " + position);
            if (mOnCellClickListener != null && mIsEnabled) {
                mOnCellClickListener.onCellClicked(position);
            }
        }
    };

    public TicTacToeGrid(Context context) {
        super(context);
        init(null, 0);
    }

    public TicTacToeGrid(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public TicTacToeGrid(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    /**
     * Change the ImageView at position to use resourceId.
     *
     * @param position
     * @param resourceId
     */
    public void setCellImage(int position, int resourceId) {
        // Find the ImageView corresponding to the position and set it to the
        // new image resource.
        final ImageView cell = mCells.get(position);
        cell.setImageResource(resourceId);
        // Set initial values of alpha and scale and then animate those
        // properties to their final values.
        cell.setAlpha(0.0f);
        cell.setScaleX(0f);
        cell.setScaleY(0f);
        cell.animate()
                .setDuration(mCellImageAnimatorDuration)
                .setInterpolator(new OvershootInterpolator(3f))
                .alpha(1.0f)
                .scaleX(1f)
                .scaleY(1f)
                .start();
    }

    @Override
    public void setEnabled(boolean enabled) {
        mIsEnabled = enabled;
        super.setEnabled(enabled);
    }

    /**
     * Register a callback to be invoked whenever a cell is clicked.
     *
     * @param listener
     */
    public void setOnCellClickListener(OnCellClickListener listener) {
        mOnCellClickListener = listener;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        mWidth = right - left;
        mHeight = bottom - top;
        final int CELL_WIDTH = mWidth / 3 - (mCellMargin * 2);
        final int CELL_HEIGHT = mHeight / 3 - (mCellMargin * 2);
        for (int ii = 0; ii < getChildCount(); ++ii) {
            final View view = getChildAt(ii);
            final LayoutParams params = (LayoutParams) view.getLayoutParams();
            params.width = CELL_WIDTH;
            params.height = CELL_HEIGHT;
            view.setLayoutParams(params);
        }

        mVertLineX1 = mWidth / 3f;
        mVertLineX2 = 2f * mWidth / 3f;
        mHorizLineY1 = mHeight / 3f;
        mHorizLineY2 = 2f * mHeight / 3f;

        if (!mIsGridAnimationDone) {
            Log.d(TAG, "onLayout: animation not done");
            mHorizontal1 = 0;
            mHorizontal2 = mWidth;
            mVertical1 = 0;
            mVertical2 = mHeight;
            animateGridLines(true);
        }

        super.onLayout(changed, left, top, right, bottom);
    }

    private void init(AttributeSet attrs, int defStyle) {
        Log.v(TAG, "init");

        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.TicTacToeGrid, defStyle, 0);

        mColor = a.getColor(R.styleable.TicTacToeGrid_gridColor, DEFAULT_COLOR);

        a.recycle();

        // Allow our onDraw method to be called.
        setWillNotDraw(false);

        mCells = new ArrayList<>();

        mPaint = new Paint();
        mPaint.setColor(mColor);
        mPaint.setStrokeWidth(mStrokeWidth);

        for (int y = 0; y < mNumRows; ++y) {
            for (int x = 0; x < mNumCols; ++x) {
                ImageView iv = new ImageView(getContext());

                // Create a hopefully unique ID for this view so we can use the
                // RelativeLayout parameters to lay out the view in the correct
                // position.
                iv.setId(1000 + x + y * mNumCols);

                // Set the tag to a zero-based index of the ImageView to aid in
                // identifying which cell was clicked.
                iv.setTag(x + y * mNumCols);

                // Register a listener for clicks on each ImageView.
                iv.setOnClickListener(mCellClickListener);

                // Set the resource to the 'empty' image so that onLayout will
                // calculate the correct size for our grid.
                iv.setImageResource(R.drawable.empty);

                final RelativeLayout.LayoutParams params = new LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(mCellMargin, mCellMargin, mCellMargin, mCellMargin);
                if (x > 0 && y == 0) { // still on first row, past first cell
                    final int leftId = mCells.get(x - 1).getId();
                    params.addRule(RIGHT_OF, leftId);
                } else if (x == 0 && y > 0) { // first column, not first row
                    final int topId = mCells.get((y - 1) * mNumCols).getId();
                    params.addRule(BELOW, topId);
                } else if (x > 0 && y > 0) { // remaining non-edge cases
                    final int leftId = mCells.get(x - 1).getId();
                    params.addRule(RIGHT_OF, leftId);
                    final int topId = mCells.get((y - 1) * mNumCols).getId();
                    params.addRule(BELOW, topId);
                }
                addView(iv, params);
                mCells.add(iv);
            }
        }
    }

    private void animateGridLines(boolean useDelay) {
        Log.v(TAG, "animateGridLines");

        mIsGridAnimationDone = true;

        ValueAnimator animator = ValueAnimator.ofFloat(0f, 2.5f);
        animator.setInterpolator(null);
        animator.setDuration(mGridLineAnimationDuration);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                final float value = (float) animation.getAnimatedValue();
                if (value <= 1.0f) {
                    mHorizontal1 = value * mWidth;
                }
                if (value >= 0.5f && value <= 1.5f) {
                    mVertical1 = (value - 0.5f) * mHeight;
                }
                if (value >= 1.0f && value <= 2.0f) {
                    mHorizontal2 = (2.0f - value) * mWidth;
                }
                if (value >= 1.5f && value <= 2.5f) {
                    mVertical2 = (2.5f - value) * mHeight;
                }
                invalidate();
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                Log.v(TAG, "onAnimationEnd: grid lines");

                // Make sure our lines are complete at the end of the animation.
                mHorizontal1 = mWidth;
                mHorizontal2 = 0;
                mVertical1 = mHeight;
                mVertical2 = 0;
                invalidate();
            }
        });
        if (useDelay) {
            animator.setStartDelay(mGridLineAnimationStartDelay);
        }
        animator.start();
    }

    @Override
    protected void onDraw(final Canvas canvas) {
//        Log.d(TAG, "width/height: " + mWidth + "/" + mHeight);
//        Log.d(TAG, "horiz1=" + mHorizontal1 + ", horiz2=" + mHorizontal2);
//        Log.d(TAG, "vert1=" + mVertical1 + ", vert2=" + mVertical2);

        // Draw the grid lines: two vertical and two horizontal.
        if (mWidth > 0 && mHeight > 0) {
            canvas.drawLine(mVertLineX1, 0, mVertLineX1, mVertical1, mPaint);
            canvas.drawLine(mVertLineX2, mHeight, mVertLineX2, mVertical2, mPaint);
            canvas.drawLine(0, mHorizLineY1, mHorizontal1, mHorizLineY1, mPaint);
            canvas.drawLine(mWidth, mHorizLineY2, mHorizontal2, mHorizLineY2, mPaint);
        }
    }

    /**
     * Override this to allow Android Studio to preview custom component.
     *
     * @return true to allow view to be displayed in preview
     */
    @Override
    public boolean isInEditMode() {
        return true;
    }

    /**
     * Display who won by drawing a line through winning row/column/diagonal. Also, animate
     * cell images in that line.
     *
     * @param start starting position
     * @param end   ending position
     */
    public void displayWinner(int start, int end) {
        // TODO animate values of x & y positions for drawing line through winning row/column/diagonal
        // TODO need to also modify onDraw to draw the winning line when flag is set
    }

    public void reset() {
        for (final ImageView iv : mCells) {
            iv.setImageResource(R.drawable.empty);
        }
//        animateGridLines(false);
    }

    /**
     * Callback which is invoked upon a grid cell click.
     */
    public interface OnCellClickListener {
        /**
         * Called when the grid cell at a specific position is clicked.
         *
         * @param position which cell was clicked (e.g. 0-8)
         */
        void onCellClicked(int position);
    }
}
