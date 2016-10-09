package com.example.daidaijie.syllabusapplication.takeout;

import com.example.daidaijie.syllabusapplication.AppComponent;

import dagger.Component;

/**
 * Created by daidaijie on 2016/10/9.
 */

@PerTakeOut
@Component(dependencies = AppComponent.class, modules = TakeOutModelModule.class)
public abstract class TakeOutModelComponent {

    public abstract ITakeOutModel getTakeOutModel();

    private static TakeOutModelComponent INSTANCE;

    public static TakeOutModelComponent getInstance(AppComponent appComponent) {
        if (INSTANCE == null) {
            INSTANCE = DaggerTakeOutModelComponent.builder().appComponent(appComponent).build();
        }
        return INSTANCE;
    }

    public static void destroy() {
        INSTANCE = null;
    }

}