package com.baway.yuwentao;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.Poi;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

/**
 * 类用途 :主页面
 * 类的说明：本类是主页面，显示图文混排的视图
 * 作者 : 郁文涛
 * 时间 : 2017/8/21 8:49
 */
public class MainActivity extends AppCompatActivity {
    //调用系统相册-选择图片
    private static final int IMAGE = 1;
    private static final int IMAGES = 10;
    private SpannableString msp;
    private SpannableString msp1;
    private EditText editText;
    private EditText editText1;
    private ImageView imageView;
    private ImageView imageView1;
    private Button button;

    //定位
    public LocationClient mLocationClient = null;
    public BDLocationListener myListener = new MyLocationListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startLocate();
        initView();
    }

    //专门制造异常的按钮
    public void errorBtn(View v) {
        TextView viewById = (TextView) findViewById(R.id.text_test);
        viewById.setText("错误");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //获取图片路径
        if (requestCode == IMAGES && resultCode == Activity.RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            String[] filePathColumns = {MediaStore.Images.Media.DATA};
            Cursor c = getContentResolver().query(selectedImage, filePathColumns, null, null, null);
            c.moveToFirst();
            int columnIndex = c.getColumnIndex(filePathColumns[0]);
            String imagePath = c.getString(columnIndex);
            appendImage1(imagePath);
            c.close();
        }
        //获取图片路径
        if (requestCode == IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            String[] filePathColumns = {MediaStore.Images.Media.DATA};
            Cursor c = getContentResolver().query(selectedImage, filePathColumns, null, null, null);
            c.moveToFirst();
            int columnIndex = c.getColumnIndex(filePathColumns[0]);
            String imagePath = c.getString(columnIndex);
            appendImage(imagePath);
            c.close();
        }

    }

    private void appendImage1(String imgUrl) {
        // 用imgUrl获取Bitmap对象
        Bitmap bitmap = ImageLoader.getInstance().loadImageSync(
                "file://" + imgUrl);
        // 将Bitmap对象转换成Drawable对象
        Resources res = getApplicationContext().getResources();
        Drawable drawable = new BitmapDrawable(res, bitmap);

        // 用imgUrl初始化SpannableString对象
        msp1 = new SpannableString(imgUrl);
        // 设置图片宽高
        drawable.setBounds(0, 0, 300, 300);
        msp1.setSpan(new ImageSpan(drawable), 0, imgUrl.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        // 获取Editable的对象
        Editable edit1 = editText1.getEditableText();
        // 获取光标位置
        int index1 = editText1.getSelectionStart();
        // 光标所在位置插入文字
        edit1.insert(index1, msp1);
    }

    private void appendImage(String imgUrl) {
        // 用imgUrl获取Bitmap对象
        Bitmap bitmap = ImageLoader.getInstance().loadImageSync(
                "file://" + imgUrl);
        // 将Bitmap对象转换成Drawable对象
        Resources res = getApplicationContext().getResources();
        Drawable drawable = new BitmapDrawable(res, bitmap);

        // 用imgUrl初始化SpannableString对象
        msp = new SpannableString(imgUrl);
        // 设置图片宽高
        drawable.setBounds(0, 0, 300, 300);
        msp.setSpan(new ImageSpan(drawable), 0, imgUrl.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        // 获取Editable的对象
        Editable edit = editText.getEditableText();
        // 获取光标位置
        int index = editText.getSelectionStart();
        // 光标所在位置插入文字
        edit.insert(index, msp);
    }

    private void initView() {
        button = (Button) findViewById(R.id.btn_location);
        imageView = (ImageView) findViewById(R.id.image_onclick);
        imageView1 = (ImageView) findViewById(R.id.image_onclick1);
        editText = (EditText) findViewById(R.id.edittext_image);
        editText1 = (EditText) findViewById(R.id.edittext_image1);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //调用相册
                Intent intent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, IMAGE);
            }
        });

        imageView1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //调用相册
                Intent intent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, IMAGES);
            }
        });
    }


    /**
     * 定位
     */
    private void startLocate() {
        mLocationClient = new LocationClient(getApplicationContext());     //声明LocationClient类
        mLocationClient.registerLocationListener(myListener);    //注册监听函数
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Battery_Saving
        );//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
        int span = 1000;
        option.setScanSpan(span);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        option.setLocationNotify(true);//可选，默认false，设置是否当GPS有效时按照1S/1次频率输出GPS结果
        option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIgnoreKillProcess(false);//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤GPS仿真结果，默认需要
        mLocationClient.setLocOption(option);
        //开启定位
        mLocationClient.start();
    }

    private class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            final StringBuffer sb = new StringBuffer(256);
            sb.append("time : ");
            sb.append(location.getTime());
//            sb.append("\nerror code : ");
//            sb.append(location.getLocType());
            sb.append("\nlatitude : ");
            sb.append(location.getLatitude());
            sb.append("\nlontitude : ");
            sb.append(location.getLongitude());
//            sb.append("\nradius : ");
//            sb.append(location.getRadius());
            if (location.getLocType() == BDLocation.TypeGpsLocation) {// GPS定位结果
                sb.append("\nspeed : ");
                sb.append(location.getSpeed());// 单位：公里每小时
                sb.append("\nsatellite : ");
                sb.append(location.getSatelliteNumber());
                sb.append("\nheight : ");
                sb.append(location.getAltitude());// 单位：米
                sb.append("\ndirection : ");
                sb.append(location.getDirection());// 单位度
                sb.append("\naddr : ");
                sb.append(location.getAddrStr());
                sb.append("\ndescribe : ");
                sb.append("gps定位成功");

            } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {// 网络定位结果
                sb.append("\naddr : ");
                sb.append(location.getAddrStr());
                //运营商信息
//                sb.append("\noperationers : ");
                sb.append(location.getOperators());
//                sb.append("\ndescribe : ");
//                sb.append("网络定位成功");
            } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {// 离线定位结果
                sb.append("\ndescribe : ");
                sb.append("离线定位成功，离线定位结果也是有效的");
            } else if (location.getLocType() == BDLocation.TypeServerError) {
                sb.append("\ndescribe : ");
                sb.append("服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因");
            } else if (location.getLocType() == BDLocation.TypeNetWorkException) {
                sb.append("\ndescribe : ");
                sb.append("网络不同导致定位失败，请检查网络是否通畅");
            } else if (location.getLocType() == BDLocation.TypeCriteriaException) {
                sb.append("\ndescribe : ");
                sb.append("无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机");
            }
            sb.append("\nlocationdescribe : ");
            sb.append(location.getLocationDescribe());// 位置语义化信息
            List<Poi> list = location.getPoiList();// POI数据
            if (list != null) {
//                sb.append("\npoilist size = : ");
//                sb.append(list.size());
                sb.append(" " + list.get(1).getName());
//                for (Poi p : list) {
//                    sb.append("\npoi= : ");
//                    sb.append(p.getId() + " " + p.getName() + " " + p.getRank());
//                }
            }
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(MainActivity.this, sb.toString(), Toast.LENGTH_LONG).show();
                }
            });

        }
    }

}
