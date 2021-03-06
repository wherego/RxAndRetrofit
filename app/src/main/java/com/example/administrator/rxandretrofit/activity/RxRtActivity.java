package com.example.administrator.rxandretrofit.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.rxandretrofit.webservice.ApiService;
import com.example.administrator.rxandretrofit.R;
import com.example.administrator.rxandretrofit.bean.Root;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class RxRtActivity extends AppCompatActivity implements View.OnClickListener {

    private Button mBtnRequest, mBtnRs;
    private TextView mTvShow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rx_rt);
        initView();
    }

    /**
     *
     */
    private void initView() {
        mBtnRequest = (Button) findViewById(R.id.btn_request);
        mBtnRequest.setOnClickListener(this);
        mBtnRs = (Button) findViewById(R.id.btn_requests);
        mBtnRs.setOnClickListener(this);
        mTvShow = (TextView) findViewById(R.id.tv_rxrt_show);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_request:
                sendRequest();
                break;
            case R.id.btn_requests:
                sendRequests();
                break;
        }
    }

    /**
     * rxjava + retrofit 请求数据
     */
    private void sendRequest() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.douban.com/v2/")
                //定义解析工具
                .addConverterFactory(GsonConverterFactory.create())//gson 解析
                //添加回调接口适配器 ，使retrofit回调适配rxjava
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        ApiService apiService = retrofit.create(ApiService.class);
        //被观察者
        Observable<Root> observable = apiService.getSearchBooks3("小王子", "", 0, 3);

        observable
                // 指定 subscribe() 所发生的线程，即 Observable.OnSubscribe 被激活时所处的线程。或者叫做事件产生的线程。
                .subscribeOn(Schedulers.io())
                // observeOn(): 指定 Subscriber 所运行在的线程。或者叫做事件消费的线程。
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Root>() {
                    @Override
                    public void onCompleted() {
                        Toast.makeText(RxRtActivity.this, "完成", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("请求失败",e.toString());
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(Root root) {
                        mTvShow.setText("作者：" + root.getBooks().get(0).getAuthor() + "\n"
                                + "标题：" + root.getBooks().get(0).getTitle() + "\n"
                                + root.getBooks().get(0).getSummary()
                        );
                    }
                });
    }
    /**
     * rxjava + retrofit 请求数据
     */
    private void sendRequests() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.douban.com/v2/")
                .addConverterFactory(new Converter.Factory() {
                    @Override
                    public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
                        return new Converter<ResponseBody, String>() {
                            @Override
                            public String convert(ResponseBody value) throws IOException {
                                return value.string();
                            }
                        };
                    }
                })
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        ApiService apiService = retrofit.create(ApiService.class);
        //被观察者
        Observable<String> observable = apiService.getSearchBooks5("小王子", "", 0, 3);

        observable
                // 指定 subscribe() 所发生的线程，即 Observable.OnSubscribe 被激活时所处的线程。或者叫做事件产生的线程。网络请求
                .subscribeOn(Schedulers.io())
                // observeOn(): 指定 Subscriber 所运行在的线程。或者叫做事件消费的线程。数据处理
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {
                        Toast.makeText(RxRtActivity.this, "完成", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("请求失败",e.toString());
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(String root) {
                        mTvShow.setText(root);
                    }
                });
    }
}
