package com.tianque.libapi;

import android.app.Activity;

import java.lang.reflect.Constructor;

public class InjectHelper {


    public static void inject(Activity host) {
        String classFullName = host.getClass().getName() + "$$ViewInjector";
        try {
            Class<?> proxy = Class.forName(classFullName);
            if (proxy!=null) {
                Constructor constructor = proxy.getConstructor(host.getClass());
                constructor.newInstance(host);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
