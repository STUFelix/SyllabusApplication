package com.example.daidaijie.syllabusapplication.main;

import com.example.daidaijie.syllabusapplication.base.BasePresenter;
import com.example.daidaijie.syllabusapplication.base.BaseView;
import com.example.daidaijie.syllabusapplication.bean.Banner;
import com.example.daidaijie.syllabusapplication.bean.Semester;
import com.example.daidaijie.syllabusapplication.bean.UserInfo;

import java.util.List;

/**
 * Created by daidaijie on 2016/10/13.
 */

public interface MainContract {

    interface presenter extends BasePresenter {
        void setCurrentSemester(Semester semester);

        void showSemesterSelect();
    }

    interface view extends BaseView<presenter> {
        void setBannerPage(List<Banner> banners);

        void showUserInfo(UserInfo mUserInfo);

        void showSemester(Semester semester);

        void showInfoMessage(String msg);

        void setCurrentSemester(Semester semester);
    }

}
