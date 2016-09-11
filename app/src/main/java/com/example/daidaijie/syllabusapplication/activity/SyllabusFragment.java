package com.example.daidaijie.syllabusapplication.activity;


import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.graphics.ColorUtils;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.balysv.materialripple.MaterialRippleLayout;
import com.example.daidaijie.syllabusapplication.R;
import com.example.daidaijie.syllabusapplication.bean.Lesson;
import com.example.daidaijie.syllabusapplication.bean.Syllabus;
import com.example.daidaijie.syllabusapplication.bean.SyllabusGrid;
import com.example.daidaijie.syllabusapplication.event.SyllabusEvent;
import com.example.daidaijie.syllabusapplication.model.LessonModel;
import com.example.daidaijie.syllabusapplication.model.User;
import com.example.daidaijie.syllabusapplication.presenter.SyllabusFragmentPresenter;
import com.example.daidaijie.syllabusapplication.util.SnackbarUtil;
import com.example.daidaijie.syllabusapplication.view.ISyllabusFragmentView;
import com.example.daidaijie.syllabusapplication.widget.SyllabusScrollView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.codetail.widget.RevealLinearLayout;

public class SyllabusFragment extends Fragment implements ISyllabusFragmentView, SwipeRefreshLayout.OnRefreshListener {

    @BindView(R.id.timeLinearLayout)
    LinearLayout mTimeLinearLayout;
    @BindView(R.id.syllabusGridLayout)
    GridLayout mSyllabusGridLayout;
    @BindView(R.id.syllabusRootLayout)
    LinearLayout mSyllabusRootLayout;
    @BindView(R.id.dateLinearLayout)
    LinearLayout mDateLinearLayout;
    @BindView(R.id.syllabusScrollView)
    SyllabusScrollView mSyllabusScrollView;
    @BindView(R.id.syllabusRefreshLayout)
    SwipeRefreshLayout mSyllabusRefreshLayout;

    private String TAG = "SyllabusFragment";

    private static final String WEEK_DAY = "WeekDate";

    private static final String IS_SWIPE_ENABLE = "isSwipeEnable";

    private SyllabusFragmentPresenter mSyllabusFragmentPresenter = new SyllabusFragmentPresenter();

    //这里除了显示，在程序中皆从0开始，为第一周
    private int mWeek;

    private int deviceWidth;
    private int devideHeight;
    private int timeWidth;
    private int gridWidth;
    private int gridHeight;


    public static SyllabusFragment newInstance(int week) {
        SyllabusFragment fragment = new SyllabusFragment();
        Bundle args = new Bundle();
        args.putInt(WEEK_DAY, week);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mWeek = getArguments().getInt(WEEK_DAY);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mSyllabusFragmentPresenter.attach(this);
        mSyllabusFragmentPresenter.setWeek(mWeek);
        EventBus.getDefault().register(this);
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_syllabus, container, false);
        ButterKnife.bind(this, view);

        deviceWidth = getActivity().getWindowManager().getDefaultDisplay().getWidth();
        devideHeight = getActivity().getWindowManager().getDefaultDisplay().getHeight();
        gridWidth = deviceWidth * 2 / 15;
        timeWidth = deviceWidth - gridWidth * 7;
        gridHeight = getResources().getDimensionPixelOffset(R.dimen.syllabus_grid_height);

        //解决滑动冲突
        mSyllabusScrollView.setSwipeRefreshLayout(mSyllabusRefreshLayout);

        mSyllabusRefreshLayout.setColorSchemeResources(
                android.R.color.holo_blue_light,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light
        );
        mSyllabusRefreshLayout.setOnRefreshListener(this);

        showDate();
        showTime();

        mSyllabusFragmentPresenter.showSyllabus();

        if (mWeek == 0 && User.getInstance().getSyllabus(User.getInstance().getCurrentSemester()) == null) {
            mSyllabusRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    mSyllabusRefreshLayout.setRefreshing(true);
                    mSyllabusFragmentPresenter.updateSyllabus();
                }
            });
        }

        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mSyllabusFragmentPresenter.detach();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(IS_SWIPE_ENABLE, mSyllabusRefreshLayout.isEnabled());
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            mSyllabusRefreshLayout.setEnabled(savedInstanceState.getBoolean(IS_SWIPE_ENABLE));
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleUpdateSyllabus(SyllabusEvent event) {
        if (event.messageWeek != mWeek) {
            Log.d(TAG, "handleUpdateSyllabus: " + mWeek);
            if (mSyllabusFragmentPresenter != null) {
                mSyllabusFragmentPresenter.reloadSyllabus();
                mSyllabusFragmentPresenter.showSyllabus();
            }
        }
    }

    private void showDate() {
        {
            TextView blankTextView = (TextView) getActivity().getLayoutInflater()
                    .inflate(R.layout.week_grid, null, false);

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    timeWidth, ViewGroup.LayoutParams.MATCH_PARENT
            );
            mDateLinearLayout.addView(blankTextView, layoutParams);
        }
        for (int i = 0; i < 7; i++) {
            String[] weekString = new String[]{"周日", "周一", "周二", "周三", "周四", "周五", "周六"};

            TextView weekTextView = (TextView) getActivity().getLayoutInflater()
                    .inflate(R.layout.week_grid, null, false);
            weekTextView.setText(weekString[i]);
            if (i + 1 == 7) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    weekTextView.setBackground(getResources().getDrawable(R.drawable.bg_grid_week_end));
                } else {
                    weekTextView.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_grid_week_end));
                }
            }
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    gridWidth, ViewGroup.LayoutParams.MATCH_PARENT);
            mDateLinearLayout.addView(weekTextView, layoutParams);
        }
    }

    /**
     * 显示时间
     */
    private void showTime() {
        for (int i = 1; i <= 13; i++) {
            TextView timeTextView = (TextView) LayoutInflater
                    .from(getActivity()).inflate(R.layout.week_grid, null, false);
            timeTextView.setText(Syllabus.time2char(i) + "");
            if (i == 13) {
                timeTextView.setBackground(getResources().getDrawable(R.drawable.bg_grid_time_end));
            }
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    timeWidth, gridHeight);
            mTimeLinearLayout.addView(timeTextView, layoutParams);
        }

    }

    @Override
    public void showSyllabus(Syllabus syllabus) {
        if (syllabus == null) {
            return;
        }
        mSyllabusGridLayout.removeAllViews();
        for (int i = 0; i < 7; i++) {
            for (int j = 0; j < 13; j++) {
                SyllabusGrid syllabusGrid = syllabus.getSyllabusGrids().get(i).get(j);
                Log.e(TAG, "showSyllabus: " + i + " , " + j + " " + syllabusGrid.getLessons().size());
                Lesson lesson = null;
                for (Integer lessonID : syllabusGrid.getLessons()) {
                    Lesson tmpLesson = LessonModel.getInstance().getLesson(lessonID);
                    boolean flag = false;
                    for (Lesson.TimeGird timeGird : tmpLesson.getTimeGirds()) {
                        if (timeGird.getWeekDate() == i) {
                            long weekOfTime = timeGird.getWeekOfTime();
                            if (((weekOfTime >> mWeek) & 1) == 1) {
                                flag = true;
                            }
                        }
                    }
                    if (flag) {
                        lesson = tmpLesson;
                        break;
                    }
                }
                if (lesson != null)
                    Log.e(TAG, "showSyllabus: " + i + ", " + j + ", " + lesson.getName());
                final RevealLinearLayout lessonLinearLayout = (RevealLinearLayout) LayoutInflater.from(getActivity()).inflate(R.layout.lesson_grid, null, false);
                MaterialRippleLayout lessonRippleLayout = (MaterialRippleLayout) lessonLinearLayout.findViewById(R.id.lessonRipple);
                TextView lessonTextView = (TextView) lessonLinearLayout.findViewById(R.id.lessonTextView);
                int span = 1;

                if (lesson != null) {
                    GradientDrawable shape = (GradientDrawable) getResources().getDrawable(R.drawable.grid_background);

                    shape.setColor(ColorUtils.setAlphaComponent(getResources().getColor(
                            lesson.getBgColor()), 192));
                    lessonTextView.setText(lesson.getTrueName() + "\n@" + lesson.getRoom());

                    lessonTextView.setBackgroundDrawable(shape);
                    for (int k = j + 1; k < 13; k++) {
                        SyllabusGrid nextSyllabusGrid = syllabus.getSyllabusGrids().get(i).get(k);
                        if (nextSyllabusGrid.getLessons().size() == 0) break;

                        Lesson nextlesson = null;
                        for (Integer lessonID : nextSyllabusGrid.getLessons()) {
                            Lesson tmpLesson = LessonModel.getInstance().getLesson(lessonID);
                            boolean flag = false;
                            for (Lesson.TimeGird timeGird : tmpLesson.getTimeGirds()) {
                                if (timeGird.getWeekDate() == i) {
                                    long weekOfTime = timeGird.getWeekOfTime();
                                    if (((weekOfTime >> mWeek) & 1) == 1) {
                                        flag = true;
                                    }
                                }
                            }
                            if (flag) {
                                nextlesson = tmpLesson;
                                break;
                            }
                        }

                        if (nextlesson == null) break;

                        if (nextlesson.getId().equals(lesson.getId())) {
                            span++;
                        }
                    }
                    final Lesson finalLesson = lesson;
                    lessonRippleLayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            SyllabusActivity activity = (SyllabusActivity) getActivity();
                            if (!activity.isSingleLock()) {
                                activity.setSingleLock(true);
                                activity.showSelectWeekLayout(false);
                                Intent intent = LessonInfoActivity.getIntent(
                                        getActivity(), finalLesson.getIntID()
                                );
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(activity,
                                            lessonLinearLayout, "lesson_grid");
                                    activity.startActivityForResult(intent, 200, options.toBundle());
                                } else {
                                    activity.startActivityForResult(intent, 200);
                                }
                            }
                        }
                    });

                } else {
                    lessonLinearLayout.setVisibility(View.INVISIBLE);
                    lessonRippleLayout.setEnabled(false);
                }
                lessonTextView.setWidth(gridWidth);
                lessonTextView.setHeight(gridHeight * span);
                GridLayout.Spec rowSpec = GridLayout.spec(j, span);
                GridLayout.Spec columnSpec = GridLayout.spec(i);
                mSyllabusGridLayout.addView(lessonLinearLayout, new GridLayout.LayoutParams(rowSpec, columnSpec));
                j += span - 1;

            }

        }
    }

    @Override
    public void showSuccessBanner() {
        SnackbarUtil.ShortSnackbar(
                mSyllabusRootLayout,
                "课表同步成功",
                SnackbarUtil.Confirm
        ).show();
    }

    @Override
    public void showFailBannner() {
        SnackbarUtil.LongSnackbar(
                mSyllabusRootLayout,
                "课表同步失败",
                SnackbarUtil.Alert
        ).setAction("再次同步", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSyllabusRefreshLayout.setRefreshing(true);
                mSyllabusFragmentPresenter.updateSyllabus();
            }
        }).show();
    }

    @Override
    public void showLoading() {
        mSyllabusRefreshLayout.setRefreshing(true);
    }

    @Override
    public void hideLoading() {
        mSyllabusRefreshLayout.setRefreshing(false);
    }

    @Override
    public void setViewPagerEnable(boolean enable) {
        SyllabusActivity fatherActivity = (SyllabusActivity) getActivity();
        fatherActivity.setViewPagerEnable(enable);
    }

    @Override
    public void onRefresh() {
        mSyllabusFragmentPresenter.updateSyllabus();
    }

    @Override
    public void setHeadImageView(Uri uri) {
        SyllabusActivity fatherActivity = (SyllabusActivity) getActivity();
        fatherActivity.setHeadImageView(uri);
    }

    @Override
    public void setNickName(String nickName) {
        SyllabusActivity fatherActivity = (SyllabusActivity) getActivity();
        fatherActivity.setNickName(nickName);
    }

}
