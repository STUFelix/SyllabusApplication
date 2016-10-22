package com.example.daidaijie.syllabusapplication.schoolDynamatic.personal;

import com.example.daidaijie.syllabusapplication.base.BasePresenter;
import com.example.daidaijie.syllabusapplication.base.BaseView;
import com.example.daidaijie.syllabusapplication.bean.UserBaseBean;

/**
 * Created by daidaijie on 2016/10/22.
 */

public interface PersonalContract {

    interface presenter extends BasePresenter {
    }

    interface view extends BaseView<presenter> {

        void showUserBase(UserBaseBean userBaseBean);
    }

}