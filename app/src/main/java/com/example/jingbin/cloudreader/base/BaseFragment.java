package com.example.jingbin.cloudreader.base;

import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.example.jingbin.cloudreader.R;
import com.example.jingbin.cloudreader.utils.ClassUtil;
import com.example.jingbin.cloudreader.utils.PerfectClickListener;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

/**
 * 是没有title的Fragment
 *
 * @author jingbin
 */
public abstract class BaseFragment<VM extends AndroidViewModel, SV extends ViewDataBinding> extends Fragment {

    // ViewModel
    protected VM viewModel;
    // 布局view
    protected SV bindingView;
    // fragment是否显示了
    protected boolean mIsVisible = false;
    // 加载中
    private View loadingView;
    // 加载失败
    private View errorView;
    // 动画
    private AnimationDrawable mAnimationDrawable;
    private CompositeDisposable mCompositeDisposable;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View ll = inflater.inflate(R.layout.fragment_base, null);
        bindingView = DataBindingUtil.inflate(getActivity().getLayoutInflater(), setContent(), null, false);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        bindingView.getRoot().setLayoutParams(params);
        RelativeLayout mContainer = ll.findViewById(R.id.container);
        mContainer.addView(bindingView.getRoot());
        return ll;
    }

    /**
     * 在这里实现Fragment数据的缓加载.
     */
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (getUserVisibleHint()) {
            mIsVisible = true;
            onVisible();
        } else {
            mIsVisible = false;
            onInvisible();
        }
    }

    protected void onInvisible() {
    }

    /**
     * 显示时加载数据,需要这样的使用
     * 注意声明 isPrepared，先初始化
     * 生命周期会先执行 setUserVisibleHint 再执行onActivityCreated
     * 在 onActivityCreated 之后第一次显示加载数据，只加载一次
     */
    protected void loadData() {
    }

    protected void onVisible() {
        loadData();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loadingView = ((ViewStub) getView(R.id.vs_loading)).inflate();
        ImageView img = loadingView.findViewById(R.id.img_progress);

        // 加载动画
        mAnimationDrawable = (AnimationDrawable) img.getDrawable();
        // 默认进入页面就开启动画
        if (!mAnimationDrawable.isRunning()) {
            mAnimationDrawable.start();
        }
        bindingView.getRoot().setVisibility(View.GONE);
        initViewModel();
    }

    /**
     * 初始化ViewModel
     */
    private void initViewModel() {
        Class<VM> viewModelClass = ClassUtil.getViewModel(this);
        if (viewModelClass != null) {
            this.viewModel = ViewModelProviders.of(this).get(viewModelClass);
        }
    }

    protected <T extends View> T getView(int id) {
        return (T) getView().findViewById(id);
    }

    /**
     * 布局
     */
    public abstract int setContent();

    /**
     * 加载失败后点击后的操作
     */
    protected void onRefresh() {

    }

    /**
     * 显示加载中状态
     */
    protected void showLoading() {
        if (loadingView != null && loadingView.getVisibility() != View.VISIBLE) {
            loadingView.setVisibility(View.VISIBLE);
        }
        // 开始动画
        if (!mAnimationDrawable.isRunning()) {
            mAnimationDrawable.start();
        }
        if (bindingView.getRoot().getVisibility() != View.GONE) {
            bindingView.getRoot().setVisibility(View.GONE);
        }
        if (errorView != null) {
            errorView.setVisibility(View.GONE);
        }
    }

    /**
     * 加载完成的状态
     */
    protected void showContentView() {
        if (loadingView != null && loadingView.getVisibility() != View.GONE) {
            loadingView.setVisibility(View.GONE);
        }
        // 停止动画
        if (mAnimationDrawable != null && mAnimationDrawable.isRunning()) {
            mAnimationDrawable.stop();
        }
        if (errorView != null) {
            errorView.setVisibility(View.GONE);
        }
        if (bindingView.getRoot().getVisibility() != View.VISIBLE) {
            bindingView.getRoot().setVisibility(View.VISIBLE);
        }
    }

    /**
     * 加载失败点击重新加载的状态
     */
    protected void showError() {
        if (loadingView != null && loadingView.getVisibility() != View.GONE) {
            loadingView.setVisibility(View.GONE);
        }
        // 停止动画
        if (mAnimationDrawable != null && mAnimationDrawable.isRunning()) {
            mAnimationDrawable.stop();
        }
        if (errorView == null) {
            ViewStub viewStub = getView(R.id.vs_error_refresh);
            errorView = viewStub.inflate();
            // 点击加载失败布局
            errorView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showLoading();
                    onRefresh();
                }
            });
        } else {
            errorView.setVisibility(View.VISIBLE);
        }
        if (bindingView.getRoot().getVisibility() != View.GONE) {
            bindingView.getRoot().setVisibility(View.GONE);
        }
    }

    public void addSubscription(Disposable disposable) {
        if (this.mCompositeDisposable == null) {
            this.mCompositeDisposable = new CompositeDisposable();
        }
        this.mCompositeDisposable.add(disposable);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (this.mCompositeDisposable != null && !mCompositeDisposable.isDisposed()) {
            this.mCompositeDisposable.clear();
        }
    }

    public void removeDisposable() {
        if (this.mCompositeDisposable != null && !mCompositeDisposable.isDisposed()) {
            this.mCompositeDisposable.dispose();
        }
    }
}
