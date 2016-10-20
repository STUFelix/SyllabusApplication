package com.example.daidaijie.syllabusapplication.syllabus.main.fragment;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;

import com.example.daidaijie.syllabusapplication.App;
import com.example.daidaijie.syllabusapplication.ILoginModel;
import com.example.daidaijie.syllabusapplication.R;
import com.example.daidaijie.syllabusapplication.bean.Syllabus;
import com.example.daidaijie.syllabusapplication.di.scope.PerFragment;
import com.example.daidaijie.syllabusapplication.syllabus.ISyllabusModel;
import com.example.daidaijie.syllabusapplication.util.BitmapSaveUtil;
import com.example.daidaijie.syllabusapplication.util.LoggerUtil;

import java.io.File;

import javax.inject.Inject;

import rx.Subscriber;

/**
 * Created by daidaijie on 2016/7/26.
 */
public class SyllabusFragmentPresenter implements SyllabusFragmentContract.presenter {

    ISyllabusModel mISyllabusModel;

    ILoginModel mILoginModel;

    SyllabusFragmentContract.view mView;

    @Inject
    @PerFragment
    public SyllabusFragmentPresenter(ILoginModel loginModel, ISyllabusModel ISyllabusModel, SyllabusFragmentContract.view view) {
        mILoginModel = loginModel;
        mISyllabusModel = ISyllabusModel;
        mView = view;
    }

    @Override
    public void loadData() {
        mView.showLoading(true);
        mView.onLoadStart();
        mISyllabusModel.getSyllabusFromNet()
                .subscribe(new SyllabusSubscriber(true));

    }

    @Override
    public void saveSyllabus(Bitmap syllabusBitmap, Bitmap timeBitmap, Bitmap dayBitmap) {
        String wallPaperName = mILoginModel.getWallPaper();
        Bitmap wallPaperBitmap;

        if (!wallPaperName.isEmpty() && new File(wallPaperName).exists()) {
            wallPaperBitmap = BitmapFactory.decodeFile(wallPaperName);
        } else {
            wallPaperBitmap = BitmapFactory.decodeResource(App.getContext().getResources()
                    , R.drawable.background);
        }

        Matrix matrix = new Matrix();
        float scale;
        float scaleHeight = (dayBitmap.getHeight() + syllabusBitmap.getHeight()) * 1.0f
                / wallPaperBitmap.getHeight();
        float scaleWidht = (timeBitmap.getWidth() + syllabusBitmap.getWidth()) * 1.0f
                / wallPaperBitmap.getWidth();
        scale = Math.max(scaleHeight, scaleWidht);

        matrix.postScale(scale, scale);

        Bitmap result = Bitmap.createBitmap(timeBitmap.getWidth() + syllabusBitmap.getWidth(),
                syllabusBitmap.getHeight() + dayBitmap.getHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);

        Bitmap resizeBmp = Bitmap.createBitmap(wallPaperBitmap, 0, 0, wallPaperBitmap.getWidth(),
                wallPaperBitmap.getHeight(), matrix, true);
        canvas.drawBitmap(resizeBmp, 0, 0, null);

        canvas.drawBitmap(dayBitmap, 0, 0, null);
        canvas.drawBitmap(timeBitmap, 0, dayBitmap.getHeight(), null);
        canvas.drawBitmap(syllabusBitmap, timeBitmap.getWidth(), dayBitmap.getHeight(), null);


        BitmapSaveUtil.saveFile(
                result, "Syllabus" + System.currentTimeMillis() + ".jpg", "STUOA", 100, new BitmapSaveUtil.OnSaveFileCallBack() {
                    @Override
                    public void onSuccess() {
                        mView.showSuccessMessage("已保存课表到图库");
                    }

                    @Override
                    public void onFail(String msg) {
                        mView.showFailMessage(msg);
                    }
                });


        wallPaperBitmap.recycle();
        syllabusBitmap.recycle();
        dayBitmap.recycle();
        timeBitmap.recycle();
    }

    @Override
    public void start() {
        mISyllabusModel.getSyllabusFromCache()
                .subscribe(new SyllabusSubscriber(false));
    }

    private class SyllabusSubscriber extends Subscriber<Syllabus> {

        boolean isShowMsg;

        public SyllabusSubscriber(boolean isShowMsg) {
            this.isShowMsg = isShowMsg;
        }

        @Override
        public void onCompleted() {
            if (isShowMsg) {
                mView.onLoadEnd(true);
                mView.showLoading(false);
                mView.showSuccessMessage("同步成功");
            }
        }

        @Override
        public void onError(Throwable e) {
            LoggerUtil.printStack(e);
            if (isShowMsg) {
                mView.showLoading(false);
                mView.onLoadEnd(false);
                if (e.getMessage() == null) {
                    mView.showFailMessage("同步失败");
                } else {
                    mView.showFailMessage(e.getMessage().toUpperCase());
                }
            }
        }

        @Override
        public void onNext(Syllabus syllabus) {
            mView.showSyllabus(syllabus);
        }
    }

}