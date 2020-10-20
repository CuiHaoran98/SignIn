package com.example.signin;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final EditText editText = findViewById(R.id.name);
        setEditTextInhibitInputSpeChat(editText);
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
            }

            @Override
            public void afterTextChanged(Editable s) {
                selectionStart = editText.getSelectionStart();
                selectionEnd = editText.getSelectionEnd();
                Log.i("gongbiao1",""+selectionStart);
                if (temp.length() > 10) {
                    Toast.makeText(MainActivity.this,
                            "您输入的的已经超过10个字符", Toast.LENGTH_SHORT)//2 seconds
                            .show();
                    s.delete(selectionStart-1, selectionEnd);
                    int tempSelection = selectionStart;
                    editText.setText(s);
                    editText.setSelection(tempSelection);
                }
            }
        });

        RadioGroup rg = (RadioGroup) findViewById(R.id.rg_sex);
        RadioButton rb_Male = (RadioButton) findViewById(R.id.rb_Male);
        RadioButton rb_Female = (RadioButton) findViewById(R.id.rb_Female);
        //注意是给RadioGroup绑定监视器
        rg.setOnCheckedChangeListener(new MyRadioButtonListener() );
    }

    /**
     * 禁止EditText输入特殊字符
     *
     * @param editText
     */
    public void setEditTextInhibitInputSpeChat(EditText editText) {

        InputFilter filter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
//                String speChat = "[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
                String speChat = "[/?*&^%$#@!~\\\\]";
                Pattern pattern = Pattern.compile(speChat);
                Matcher matcher = pattern.matcher(source.toString());
                if (matcher.find()){
                    Toast.makeText(MainActivity.this,
                            "禁止输入\"[\\\\[\\\\]/?*&^%$#@!~]\"", Toast.LENGTH_SHORT)//2 seconds
                            .show();
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
    public static void setEditTextInhibitInputSpace(EditText editText) {
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

    class MyRadioButtonListener implements RadioGroup.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            // 选中状态改变时被触发
            switch (checkedId) {
                case R.id.rb_Female:
                    // 当用户选择女性时
                    Log.i("sex", "当前用户选择女");
                    break;
                case R.id.rb_Male:
                    // 当用户选择男性时
                    Log.i("sex", "当前用户选择男");
                    break;
            }
        }
    }
}