package com.jasontoradler.tictactoe;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.TextView;

public class GameActivity extends Activity implements TicTacToeGrid.OnCellClickListener {

    private static final String TAG = "GameActivity";
    private static final float WIN_MESSAGE_ROTATION = 5f;
    private TicTacToeGrid mGrid;
    private CellState mCellState[] = new CellState[9];
    private TextView mMessage;
    private Button mButton;
    private GameState mGameState = GameState.IN_PROGRESS;
    private int mTurn; // 0 means player 1 or 'O's, 1 means player 2 or 'X's

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mGrid = (TicTacToeGrid) findViewById(R.id.grid);
        mGrid.setOnCellClickListener(this);

        mMessage = (TextView) findViewById(R.id.message);
        mMessage.setText(R.string.player1Turn);
        mMessage.setVisibility(View.VISIBLE);

        mButton = (Button) findViewById(R.id.gameButton);
        mButton.setVisibility(View.INVISIBLE);

        for (int ii = 0; ii < 9; ++ii) {
            mCellState[ii] = CellState.EMPTY;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCellClicked(int position) {
//        Log.v(TAG, "onCellClicked: " + position);

        mButton.setVisibility(View.VISIBLE);

        if (mCellState[position] == CellState.EMPTY) {
            if (mTurn == 1) {
                mGrid.setCellImage(position, R.drawable.ex);
                mCellState[position] = CellState.PLAYER2;
            } else {
                mGrid.setCellImage(position, R.drawable.circle);
                mCellState[position] = CellState.PLAYER1;
            }
            mTurn = (++mTurn) % 2;

            updateGameState();

            displayResult();
        }
    }

    private void displayResult() {
        switch (mGameState) {
            case IN_PROGRESS:
                mMessage.setText(mTurn == 0 ? R.string.player1Turn : R.string.player2Turn);
                mMessage.setScaleX(0.5f);
                mMessage.animate()
                        .setDuration(500)
                        .setInterpolator(new OvershootInterpolator(1.5f))
                        .scaleX(1f)
                        .start();
                break;
            case PLAYER1_WINS:
                mMessage.setText(R.string.player1Wins);
                animateWinMessage();
                break;
            case PLAYER2_WINS:
                mMessage.setText(R.string.player2Wins);
                animateWinMessage();
                break;
            case TIE_GAME:
                mMessage.setText(R.string.tieGame);
                break;
        }
    }

    private void animateWinMessage() {
        ValueAnimator animator = ValueAnimator.ofFloat(
                0f, -WIN_MESSAGE_ROTATION, 0f, WIN_MESSAGE_ROTATION, 0f);
        animator.setDuration(500);
        animator.setInterpolator(null);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                final float value = (float) animation.getAnimatedValue();

                // Scale the text both horizontally and vertically from 1 to 1.15.
                final float scale = (WIN_MESSAGE_ROTATION + 0.15f * Math.abs(value)) /
                        WIN_MESSAGE_ROTATION;
                mMessage.setScaleX(scale);
                mMessage.setScaleY(scale);

                // Rotate the message by the pivot point, which should be in the center.
                mMessage.setRotation(value);

                // Cycle through the existing color to pure red and back.
                final int red = (int) (255 * (1f - (Math.abs(value) / WIN_MESSAGE_ROTATION)));
                final int color = (0xff << 24) + (red << 16);
//                Log.d(TAG, "color: " + Integer.toHexString(color));
                mMessage.setTextColor(color);
            }
        });
        animator.setRepeatMode(ValueAnimator.RESTART);
        animator.setRepeatCount(2);
        animator.start();
    }

    private void updateGameState() {
        CellState matchingCell = CellState.EMPTY;
        // check rows, then columns, and finally diagonals
        if (mCellState[0] == mCellState[1] && mCellState[1] == mCellState[2]) {
            matchingCell = mCellState[0];
            mGrid.displayWinner(0, 2);
        }
        if (matchingCell == CellState.EMPTY && mCellState[3] == mCellState[4] && mCellState[4] == mCellState[5]) {
            matchingCell = mCellState[3];
        }
        if (matchingCell == CellState.EMPTY && mCellState[6] == mCellState[7] && mCellState[7] == mCellState[8]) {
            matchingCell = mCellState[6];
        }
        if (matchingCell == CellState.EMPTY && mCellState[0] == mCellState[3] && mCellState[3] == mCellState[6]) {
            matchingCell = mCellState[0];
        }
        if (matchingCell == CellState.EMPTY && mCellState[1] == mCellState[4] && mCellState[4] == mCellState[7]) {
            matchingCell = mCellState[1];
        }
        if (matchingCell == CellState.EMPTY && mCellState[2] == mCellState[5] && mCellState[5] == mCellState[8]) {
            matchingCell = mCellState[2];
        }
        if (matchingCell == CellState.EMPTY && mCellState[0] == mCellState[4] && mCellState[4] == mCellState[8]) {
            matchingCell = mCellState[0];
        }
        if (matchingCell == CellState.EMPTY && mCellState[2] == mCellState[4] && mCellState[4] == mCellState[6]) {
            matchingCell = mCellState[2];
        }
        if (matchingCell != CellState.EMPTY) {
            mGameState = matchingCell == CellState.PLAYER1
                    ? GameState.PLAYER1_WINS : GameState.PLAYER2_WINS;
        } else {
            boolean isGridFull = true;
            for (int ii = 0; ii < 9; ++ii) {
                if (mCellState[ii] == CellState.EMPTY) {
                    isGridFull = false;
                    break;
                }
            }
            if (isGridFull) {
                mGameState = GameState.TIE_GAME;
            }
        }
        if (mGameState != GameState.IN_PROGRESS) {
            mGrid.setEnabled(false);
            mButton.setText(R.string.playAgain);
        }
    }

    public void onGameButtonClick(View view) {
        // Restart game by resetting game state, grid, turn, message, and button.
        mGameState = GameState.IN_PROGRESS;
        mGrid.reset();
        mGrid.setEnabled(true);
        for (int ii = 0; ii < mCellState.length; ++ii) {
            mCellState[ii] = CellState.EMPTY;
        }
        mTurn = 0;
        mMessage.setText(R.string.player1Turn);
        mMessage.setTextColor(getResources().getColor(android.R.color.primary_text_light));
        mButton.setText(R.string.restart);
        mButton.setVisibility(View.INVISIBLE);
    }

    private enum CellState {
        EMPTY,
        PLAYER1,
        PLAYER2
    }

    private enum GameState {
        IN_PROGRESS,
        PLAYER1_WINS,
        PLAYER2_WINS,
        TIE_GAME,
    }
}
