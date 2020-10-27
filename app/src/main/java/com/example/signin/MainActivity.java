package com.example.signin;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import photo.IOUtil;
import photo.ImageCropActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private static final int REQUEST_CODE_SELECT_IMAGE = 1;
    private static final int REQUEST_CODE_TAKE_PICTURE = 2;
    private static final int REQUEST_CODE_CROP = 3;

    public final String IMG_CACHE1 = Environment.getExternalStorageDirectory().getAbsolutePath() + "/img_cache1";
    public final String IMG_CACHE2 =  Environment.getExternalStorageDirectory().getAbsolutePath() + "/img_cache2";
    public final String PUBLIC_CACHE = Environment.getExternalStorageDirectory().getAbsolutePath() + "/temp_icon";

    ImageView icon;
    Dialog mCameraDialog;
    TextView tip;
    ClearEditText editText;
    ImageButton choose;

    int sex = 99;
    String name = null;
    String icon_path = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        System.out.println(IMG_CACHE1);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        //动态申请相机权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

        icon = findViewById(R.id.icon);
        editText = findViewById(R.id.name);
        choose = findViewById(R.id.choose_icon);
        tip = findViewById(R.id.tip);
        RadioGroup rg = (RadioGroup) findViewById(R.id.rg_sex);
//        RadioButton rb_Male = (RadioButton) findViewById(R.id.rb_Male);
//        RadioButton rb_Female = (RadioButton) findViewById(R.id.rb_Female);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        builder.detectFileUriExposure();

//        setEditTextInhibitInputSpeChat(editText);
        editText.addTextChangedListener(new TextWatcher() {
            private CharSequence temp;
            private boolean isEdit = true;
            private int selectionStart ;
            private int selectionEnd ;
            @Override
            public void beforeTextChanged(CharSequence s, int arg1, int arg2,
                                          int arg3) {
                temp = s;
            }

            @Override
            public void onTextChanged(CharSequence s, int arg1, int arg2,
                                      int arg3) {
                if (s.length() > 10){
                    tip.setText("最多可输入10个字符");
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    // 隐藏软键盘
                    imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
                }else if (isIllegal(s)){
                    tip.setText("禁止输入[\\\\[\\\\]/?*&^%$#@!~]");
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    // 隐藏软键盘
                    imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
                }else {
                    tip.setText("");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
//                selectionStart = editText.getSelectionStart();
//                selectionEnd = editText.getSelectionEnd();
////                Log.i("gongbiao1",""+selectionStart);
//                if (temp.length() > 10) {
////                    Toast.makeText(MainActivity.this,
////                            "您输入的的已经超过10个字符", Toast.LENGTH_SHORT)//2 seconds
////                            .show();
//                    tip.setText("最多可输入10个字符");
//                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//                    // 隐藏软键盘
//                    imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
////                    s.delete(selectionStart-1, selectionEnd);
//                    System.out.println(s);
//                    int tempSelection = selectionStart;
//                    editText.setText(s);
//                    editText.setSelection(tempSelection);
//                }else{
//                    tip.setText("");
//                }
            }
        });

        choose.setOnClickListener(this);

        //注意是给RadioGroup绑定监视器
        rg.setOnCheckedChangeListener(new MyRadioButtonListener() );

        Button login = findViewById(R.id.login);
        login.setOnClickListener(this);
    }

    private boolean isIllegal(CharSequence s) {
        String speChat = "[/?*&^%$#@!~\\\\]";
        Pattern pattern = Pattern.compile(speChat);
        Matcher matcher = pattern.matcher(s.toString());
        return matcher.find();
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()){

            case R.id.choose_icon:
                System.out.println("clicked");
                setDialog();
                break;
            case R.id.btn_choose_img:
                selectImage();
                mCameraDialog.dismiss();
                break;
            case R.id.btn_open_camera:
                takePicture();
                mCameraDialog.dismiss();
                break;
            case R.id.btn_cancel:
                mCameraDialog.dismiss();
                break;
            case R.id.login:
                if(loginCheck()){
                    //输入数据到数据库
                    Toast.makeText(MainActivity.this,
                            "登录成功", Toast.LENGTH_SHORT)//2 seconds
                            .show();
//                    icon_path = choose.getImageMatrix()
                }
                break;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_CODE_SELECT_IMAGE:
                if (resultCode == RESULT_OK) {
                    Uri uri = data.getData();
                    ContentResolver cr = getContentResolver();
                    InputStream is = null;
                    FileOutputStream fos = null;
                    boolean writeSucceed = true;

                    try {
                        is = cr.openInputStream(uri);
                        fos = new FileOutputStream(IMG_CACHE1);
                        int read = 0;
                        byte[] buffer = new byte[4096];
                        while ((read = is.read(buffer)) > 0) {
                            fos.write(buffer);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        writeSucceed = false;
                    } finally {
                        IOUtil.closeQuietly(is);
                        IOUtil.closeQuietly(fos);
                    }

                    if (writeSucceed) {
                        Intent intent = ImageCropActivity.createIntent(
                                MainActivity.this, IMG_CACHE1, IMG_CACHE2, getCropAreaStr(), false);
                        startActivityForResult(intent, REQUEST_CODE_CROP);
                    } else {
                        Toast.makeText(MainActivity.this, "无法打开图片文件，您的sd卡是否已满？", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    // do nothing
                }
                break;

            case REQUEST_CODE_TAKE_PICTURE:
                if (resultCode == RESULT_OK) {
                    Intent intent = ImageCropActivity.createIntent(
                                MainActivity.this, PUBLIC_CACHE, IMG_CACHE2, getCropAreaStr(), false);
                    startActivityForResult(intent, REQUEST_CODE_CROP);
                } else {
                    // do nothing
                }
                break;

            case REQUEST_CODE_CROP:
                if (resultCode == RESULT_OK) {
                    Bitmap bitmap = BitmapFactory.decodeFile(IMG_CACHE2);
                    ImageButton image = findViewById(R.id.choose_icon);
                    image.setImageBitmap(bitmap);
                    //button周围没有空白
                    image.setBackgroundColor(0);
                    image.setVisibility(View.VISIBLE);
                } else {
                    // do nothing
                }
                break;
        }

    }

    private String getCropAreaStr() {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        int screenWidth = dm.widthPixels;
        int screenHeight = dm.heightPixels;
        int rectWidth = screenWidth / 2;
        int left = screenWidth / 2 - rectWidth / 2;
        int right = screenWidth / 2 + rectWidth / 2;
        int top = screenHeight / 2 - rectWidth / 2;
        int bottom = screenHeight / 2 + rectWidth / 2;
        return left + ", " + top + ", " + right + ", " + bottom;
    }

    /**
     * 底部弹出菜单
     */
    private void setDialog(){
        mCameraDialog = new Dialog(this, R.style.BottomDialog);
        LinearLayout root = (LinearLayout) LayoutInflater.from(this).inflate(
                R.layout.botton_dialog, null);
        //初始化视图
        root.findViewById(R.id.btn_choose_img).setOnClickListener(this);
        root.findViewById(R.id.btn_open_camera).setOnClickListener(this);
        root.findViewById(R.id.btn_cancel).setOnClickListener(this);
        mCameraDialog.setContentView(root);
        Window dialogWindow = mCameraDialog.getWindow();
        dialogWindow.setGravity(Gravity.BOTTOM);
//        dialogWindow.setWindowAnimations(R.style.dialogstyle); // 添加动画
        WindowManager.LayoutParams lp = dialogWindow.getAttributes(); // 获取对话框当前的参数值
        lp.x = 0; // 新位置X坐标
        lp.y = 0; // 新位置Y坐标
        lp.width = (int) getResources().getDisplayMetrics().widthPixels; // 宽度
        root.measure(0, 0);
        lp.height = root.getMeasuredHeight();

        lp.alpha = 9f; // 透明度
        dialogWindow.setAttributes(lp);
        System.out.println(mCameraDialog);
        mCameraDialog.show();
    }

    public void selectImage(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra("crop", false);
        intent.putExtra("return-data", true);
        startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE);
    }

    public void takePicture(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Uri tempUri = Uri.fromFile(new File(PUBLIC_CACHE));
//        if (file.exists()) {
//            file.delete();
//        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT,tempUri); // set the image file name
        startActivityForResult(intent, REQUEST_CODE_TAKE_PICTURE);
    }

    /**
     * 禁止EditText输入特殊字符
     *
     * @param editText
     */
    public void setEditTextInhibitInputSpeChat(ClearEditText editText) {

        InputFilter filter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
//                String speChat = "[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
                String speChat = "[/?*&^%$#@!~\\\\]";
                Pattern pattern = Pattern.compile(speChat);
                Matcher matcher = pattern.matcher(source.toString());
                if (matcher.find()){
//                    Toast.makeText(MainActivity.this,
//                            "禁止输入\"[\\\\[\\\\]/?*&^%$#@!~]\"", Toast.LENGTH_SHORT)//2 seconds
//                            .show();
                    tip.setText("禁止输入[\\\\[\\\\]/?*&^%$#@!~]");
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    // 隐藏软键盘
                    imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
                    return "";
                }
                else return null;
            }
        };
        editText.setFilters(new InputFilter[]{filter});
    }

    /**
     * 禁止EditText输入空格
     *
     * @param editText
     */
    public static void setEditTextInhibitInputSpace(ClearEditText editText) {
        InputFilter filter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                if (source.equals(" ")) {
                    return "";
                } else {
                    return null;
                }
            }
        };
        editText.setFilters(new InputFilter[]{filter});
    }

    /**
     * 性别选择
     */
    class MyRadioButtonListener implements RadioGroup.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            // 选中状态改变时被触发
            switch (checkedId) {
                case R.id.rb_Female:
                    // 当用户选择女性时
                    sex = 0;
                    Log.i("sex", "当前用户选择女");
                    break;
                case R.id.rb_Male:
                    // 当用户选择男性时
                    Log.i("sex", "当前用户选择男");
                    sex = 1;
                    break;
            }
        }
    }

    /**
     * 检查登录各项
     * @return
     */
    private boolean loginCheck() {

        if(TextUtils.isEmpty(editText.getText())){
            Toast.makeText(MainActivity.this,
                            "您未输入昵称", Toast.LENGTH_SHORT)//2 seconds
                            .show();
            return false;
        }else if(sex == 99){
            Toast.makeText(MainActivity.this,
                    "您未选择性别", Toast.LENGTH_SHORT)//2 seconds
                    .show();
            return false;
        }else{
            return true;
        }
    }
}