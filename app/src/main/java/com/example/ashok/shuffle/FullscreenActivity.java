package com.example.ashok.shuffle;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.Map;
import java.util.Random;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenActivity extends AppCompatActivity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    Intent intent = getIntent();

    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;
    private int MAXSQUARES;
    private int TOTALSQUARES;
    private int MAXSHUFFLE = 100;
    private TextView[] v;
    private ArrayList<Integer> list;
    private Deque<Integer> stackUndo;
    private PopupWindow popInfo = null;
    private TextView txtInfo;

    private int returnZero(int index) {
        if ((index + 1) < TOTALSQUARES && list.get(index + 1) == 0)
            return index + 1;
        if ((index - 1) >= 0 && list.get(index - 1) == 0)
            return index - 1;
        if ((index + MAXSQUARES) < TOTALSQUARES && list.get(index + MAXSQUARES) == 0)
            return index + MAXSQUARES;
        if ((index - MAXSQUARES) >= 0 && list.get(index - MAXSQUARES) == 0)
            return index - MAXSQUARES;

        return -1;
    }

    private boolean shuffleList(int num, int zero) {
        if (num < 0 || num >= TOTALSQUARES)
            return false;
        list.set(zero, list.get(num));
        list.set(num, 0);
        return true;
    }
    /* y param should always correspond to zero element */
    private void swapCells(int x, int y) {
        int tmp;
        Drawable t;

        tmp = list.get(x);
        list.set(x, list.get(y));
        list.set(y, tmp);

        t = v[x].getBackground();
        v[x].setBackground(v[y].getBackground());
        v[y].setBackground(t);
        v[x].setText(String.valueOf(list.get(x)));
        v[x].setTextColor(0xffffffff);
        v[y].setText(String.valueOf(list.get(y)));
        v[y].setTextColor(0xff000000);
        v[x].refreshDrawableState();
        v[y].refreshDrawableState();
    }

    private void checkFinish(int zero) {
        for (int i = 0; i < (TOTALSQUARES - 1); i++)
            if ((i + 1) != list.get(i))
                return;
        v[zero].setText("Completed!");
        v[zero].setTextColor(0xff000000);
        v[zero].refreshDrawableState();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        intent = getIntent();
        MAXSQUARES = intent.getIntExtra(ConfigScreen.NSQUARES, 4);
        TOTALSQUARES = MAXSQUARES * MAXSQUARES;
        v = new TextView[TOTALSQUARES];
        list = new ArrayList<Integer>();
        stackUndo = new ArrayDeque<Integer>();
        TableRow tr = new TableRow(this);
        int side;
        int sides[] = {1, -1, MAXSQUARES, -MAXSQUARES};

        if (list.size() == 0) {
            for (int i = 1; i < TOTALSQUARES; i++) {
                list.add(i);
            }
            list.add(0);
        }

        Random n = new Random();

        for (int i = 0;i < MAXSHUFFLE;)
            if (shuffleList(list.indexOf(0)+sides[n.nextInt(4)], list.indexOf(0)))
                i++;
        //Collections.shuffle(list);



        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen);
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        side = size.x / MAXSQUARES;
        mVisible = true;
        //mControlsView = findViewById(R.id.fullscreen_content_controls);
        //mContentView = findViewById(R.id.fullscreen_content);

        ((TableLayout) (findViewById(R.id.tbl))).setShrinkAllColumns(true);
        mContentView = mControlsView = findViewById(R.id.tbl);
        for (int i = 0; i < TOTALSQUARES; i++) {

            if ((i % MAXSQUARES) == 0) {
                tr = new TableRow(this);
                ((TableLayout) (findViewById(R.id.tbl))).addView(tr);
            }

            v[i] = new TextView(findViewById(R.id.tbl).getContext());
            v[i].setHeight(side);
            v[i].setWidth(side);

            tr.addView(v[i]);

            v[i].setText(String.valueOf(list.get(i)));
            if (list.get(i) != 0) {
                v[i].setBackgroundColor((n.nextInt() << 16) | (n.nextInt() << 8) | n.nextInt());
                v[i].setTextColor(0xff000000);
            } else {
                v[i].setBackgroundColor(0xffffffff);
                v[i].setTextColor(0xffffffff);
            }
            v[i].setTypeface(null, Typeface.BOLD);
            v[i].setGravity(Gravity.CENTER);

            v[i].refreshDrawableState();
            // Set up the user interaction to manually show or hide the system UI.
            v[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int val = Integer.parseInt(((TextView) view).getText().toString());
                    int zero = returnZero(list.indexOf(val));

                    if (zero == -1)
                        return;

                    stackUndo.push(list.indexOf(val));
                    stackUndo.push(zero);
                    swapCells(list.indexOf(val), zero);
                    checkFinish(list.indexOf(0));
                }
            });
        }

        findViewById(R.id.ibUndo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (stackUndo.isEmpty())
                    return;
                int nZero = stackUndo.pop();
                int zero = stackUndo.pop();
                swapCells(nZero, zero);
            }
        });
        findViewById(R.id.ibInfo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Resources res = getResources();
                Drawable shape = res.getDrawable(R.drawable.roundedborder, getTheme());

                if (popInfo != null)
                    return;
                popInfo = new PopupWindow(v.getContext());
                txtInfo = new TextView(v.getContext());
                txtInfo.setText("Arrange them in order by clicking squares around the white box to move!");
                txtInfo.setTextColor(0xff000000);
                txtInfo.setBackgroundColor(0xFFFDDC02);
                txtInfo.setBackground(shape);
                popInfo.setAnimationStyle(R.style.Animation);
                popInfo.setContentView(txtInfo);
                popInfo.setHeight(150);
                popInfo.setWidth(700);
                popInfo.showAtLocation(v, Gravity.BOTTOM, 12, 12);

                txtInfo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popInfo.dismiss();
                        popInfo = null;
                    }
                });


            }
        });
        findViewById(R.id.tbl).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        //findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };


    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        //mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }
}
