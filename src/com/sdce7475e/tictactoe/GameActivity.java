package com.sdce7475e.tictactoe;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

public class GameActivity extends Activity implements OnClickListener {

	// state storage/retrieval keys
//	private final String[] KEY_CELL_STATES = {
//			"cell0", "cell1", "cell2", 
//			"cell3", "cell4", "cell5", 
//			"cell6", "cell7", "cell8", 
//	};
	private final String KEY_CELL_STATES = "cell_states";
	private final String KEY_GAME_STATE = "game_state";
	private final String KEY_TURN = "turn";

	private enum CellState {
		NONE,
		CIRCLE,
		EX
	}

	private enum GameState {
		IN_PROGRESS,
		CIRCLE_WINS,
		EX_WINS,
		TIE_GAME,
	}

	private GameState mGameState;
	private ImageView[] mCells;
	private TextView mMessage;
	private CellState[] mCellState;
	private final int[] CELL_IDS = {
			R.id.cell0, R.id.cell1, R.id.cell2,
			R.id.cell3, R.id.cell4, R.id.cell5,
			R.id.cell6, R.id.cell7, R.id.cell8 };
	private int mTurn = 0; // 0 = circle, 1 = ex

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_game);

		mMessage = (TextView) findViewById(R.id.message);
		mMessage.setText(mTurn == 0 ? R.string.player1Turn : R.string.player2Turn);

		mCellState = new CellState[9];
		mCells = new ImageView[9];

		mGameState = GameState.IN_PROGRESS;

		for (int ii = 0; ii < 9; ++ii) {
			mCellState[ii] = CellState.NONE;

			mCells[ii] = (ImageView) findViewById(CELL_IDS[ii]);
			mCells[ii].setOnClickListener(this);
			mCells[ii].setTag(ii);
		}
	}

	@Override
	protected void onRestoreInstanceState(Bundle inState) {
		super.onRestoreInstanceState(inState);
		mGameState = GameState.values()[inState.getInt(KEY_GAME_STATE)];
		mTurn = inState.getBoolean(KEY_TURN) ? 0 : 1;
		int[] states = inState.getIntArray(KEY_CELL_STATES);
		for (int ii = 0; ii < 9; ++ii) {
			mCellState[ii] = CellState.values()[states[ii]];
			switch (mCellState[ii]) {
			case NONE:
				mCells[ii].setImageResource(R.drawable.empty);
				break;
			case CIRCLE:
				mCells[ii].setImageResource(R.drawable.circle);
				break;
			case EX:
				mCells[ii].setImageResource(R.drawable.ex);
				break;
			}
		}
		mMessage.setText(mTurn == 0 ? R.string.player1Turn : R.string.player2Turn);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(KEY_GAME_STATE, mGameState.ordinal());
		int[] states = new int[9];
		for (int ii = 0; ii < 9; ++ii) {
			states[ii] = mCellState[ii].ordinal();
		}
		outState.putIntArray(KEY_CELL_STATES, states);
		outState.putBoolean(KEY_TURN, mTurn == 0);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.game_activity_actions, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.newGame:
			resetGame();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onClick(View v) {
		final int index = (Integer) v.getTag();
		if (mGameState == GameState.IN_PROGRESS
				&& index >= 0 && index < 9 && mCellState[index] == CellState.NONE) {
			final int id = mTurn == 0 ? R.drawable.circle : R.drawable.ex;
			mCellState[index] = mTurn == 0 ? CellState.CIRCLE : CellState.EX;
			mCells[index].setImageResource(id);
			updateGameState();
			if (mGameState == GameState.IN_PROGRESS) {
				nextTurn();
			} else {
				displayResult();
			}
		}
	}

	private void displayResult() {
		if (mGameState == GameState.CIRCLE_WINS) {
			mMessage.setText(R.string.player1Wins);
		} else if (mGameState == GameState.EX_WINS) {
			mMessage.setText(R.string.player2Wins);
		} else {
			mMessage.setText(R.string.tieGame);
		}
	}

	private void resetGame() {
		mTurn = 0;
		mGameState = GameState.IN_PROGRESS;
		mMessage.setText(R.string.player1Turn);
		for (int ii = 0; ii < 9; ++ii) {
			mCellState[ii] = CellState.NONE;
			mCells[ii].setImageResource(R.drawable.empty);
		}
	}

	private void nextTurn() {
		mTurn = (mTurn == 0) ? 1 : 0;
		mMessage.setText(mTurn == 0 ? R.string.player1Turn : R.string.player2Turn);
	}

	private void updateGameState() {
		CellState matchingCell = CellState.NONE;
		// check rows, then columns, and finally diagonals
		if (mCellState[0] == mCellState[1] && mCellState[1] == mCellState[2]) {
			matchingCell = mCellState[0];
		}
		if (matchingCell == CellState.NONE && mCellState[3] == mCellState[4] && mCellState[4] == mCellState[5]) {
			matchingCell = mCellState[3];
		}
		if (matchingCell == CellState.NONE && mCellState[6] == mCellState[7] && mCellState[7] == mCellState[8]) {
			matchingCell = mCellState[6];
		}
		if (matchingCell == CellState.NONE && mCellState[0] == mCellState[3] && mCellState[3] == mCellState[6]) {
			matchingCell = mCellState[0];
		}
		if (matchingCell == CellState.NONE && mCellState[1] == mCellState[4] && mCellState[4] == mCellState[7]) {
			matchingCell = mCellState[1];
		}
		if (matchingCell == CellState.NONE && mCellState[2] == mCellState[5] && mCellState[5] == mCellState[8]) {
			matchingCell = mCellState[2];
		}
		if (matchingCell == CellState.NONE && mCellState[0] == mCellState[4] && mCellState[4] == mCellState[8]) {
			matchingCell = mCellState[0];
		}
		if (matchingCell == CellState.NONE && mCellState[2] == mCellState[4] && mCellState[4] == mCellState[6]) {
			matchingCell = mCellState[2];
		}
		if (matchingCell != CellState.NONE) {
			mGameState = matchingCell == CellState.CIRCLE
					? GameState.CIRCLE_WINS : GameState.EX_WINS;
		} else {
			boolean isGridFull = true;
			for (int ii = 0; ii < 9; ++ii) {
				if (mCellState[ii] == CellState.NONE) {
					isGridFull = false;
					break;
				}
			}
			if (isGridFull) {
				mGameState = GameState.TIE_GAME;
			}
		}
	}
}
