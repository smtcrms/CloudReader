package com.example.jingbin.cloudreader.viewmodel.wan;

import android.app.Application;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;

import com.example.jingbin.cloudreader.base.BaseViewModel;
import com.example.jingbin.cloudreader.bean.wanandroid.NaviJsonBean;
import com.example.jingbin.cloudreader.http.HttpClient;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * @author jingbin
 * @data 2018/12/3
 * @Description wanandroid导航数据的ViewModel
 */

public class NaviViewModel extends BaseViewModel {

    public NaviViewModel(@NonNull Application application) {
        super(application);
    }

    public MutableLiveData<NaviJsonBean> getNaviJson() {
        final MutableLiveData<NaviJsonBean> data = new MutableLiveData<>();
        Disposable subscribe = HttpClient.Builder.getWanAndroidServer().getNaviJson()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<NaviJsonBean>() {
                    @Override
                    public void accept(NaviJsonBean naviJsonBean) throws Exception {
                        if (naviJsonBean != null
                                && naviJsonBean.getData() != null
                                && naviJsonBean.getData().size() > 0) {

                            data.setValue(naviJsonBean);
                        } else {
                            data.setValue(null);
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        data.setValue(null);
                    }
                });
        addDisposable(subscribe);
        return data;
    }
}
