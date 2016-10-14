package com.example.daidaijie.syllabusapplication.login.login;

import com.example.daidaijie.syllabusapplication.ILoginModel;
import com.example.daidaijie.syllabusapplication.bean.UserInfo;
import com.example.daidaijie.syllabusapplication.bean.UserLogin;
import com.example.daidaijie.syllabusapplication.di.qualifier.user.UnLoginUser;
import com.example.daidaijie.syllabusapplication.di.scope.PerActivity;
import com.example.daidaijie.syllabusapplication.user.IUserModel;
import com.example.daidaijie.syllabusapplication.util.LoggerUtil;

import javax.inject.Inject;

import io.realm.RealmObject;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by daidaijie on 2016/10/13.
 */

public class LoginPresenter implements LoginContract.presenter {

    LoginContract.view mView;

    ILoginModel mILoginModel;

    IUserModel mIUserModel;

    @Inject
    @PerActivity
    public LoginPresenter(LoginContract.view view, ILoginModel loginModel, @UnLoginUser IUserModel userModel) {
        mView = view;
        mILoginModel = loginModel;
        mIUserModel = userModel;
    }

    @Override
    public void start() {
        mILoginModel.getUserLoginFromCache()
                .subscribe(new Action1<UserLogin>() {
                    @Override
                    public void call(UserLogin userLogin) {
                        if (userLogin != null) {
                            mView.setLogin(userLogin);
                        }
                    }
                });
    }

    @Override
    public void login(final String username, String password) {
        mView.showLoading(true);
        UserLogin userLogin = new UserLogin(username, password);
        mILoginModel.setUserLogin(userLogin);
        Observable.merge(mIUserModel.getUserInfoFromNet(), mIUserModel.getUserBaseBeanFromNet())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<RealmObject>() {
                    @Override
                    public void onCompleted() {
                        mView.showLoading(false);
                        mView.toMainView();
                    }

                    @Override
                    public void onError(Throwable e) {
                        LoggerUtil.printStack(e);
                        mView.showLoading(false);
                        mView.showFailMessage(e.getMessage().toUpperCase());
                    }

                    @Override
                    public void onNext(RealmObject realmObject) {
                        if (realmObject instanceof UserInfo) {
                            mILoginModel.saveUserLoginToDisk();
                            LoggerUtil.e("UserLogin", "userInfo");
                        } else {
                            LoggerUtil.e("UserLogin", "userBase");
                        }
                    }
                });
    }
}