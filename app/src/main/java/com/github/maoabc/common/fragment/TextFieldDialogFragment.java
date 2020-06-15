package com.github.maoabc.common.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;

import com.github.maoabc.BaseApp;
import com.github.maoabc.aterm.R;
import com.github.maoabc.util.Precondition;
import com.google.android.material.textfield.TextInputLayout;


/**
 * 通用文本输入,用来新建或者重命名文件等
 * Created by mao on 17-8-15.
 */

public class TextFieldDialogFragment extends RetainedDialogFragment {
    private static final String TITLE_KEY = "title_key";
    private static final String HINT_KEY = "hint_key";
    private static final String TEXT_KEY = "text_key";
    private static final String SELECT_ALL_KEY = "select_all_key";
    private static final String INPUT_TYPE_KEY = "input_type_key";


    private String mTitle;

    private String mHint;
    private String mText;
    private int mInputType;

    private boolean mSelectAll;

    private EditText mInputText;
    private TextInputLayout inputLayout;

    private ResultCallback mResultCallback;

    private final Handler mHandler = new Handler(Looper.getMainLooper());

    public static TextFieldDialogFragment newInstance(String title, String hint, String initText) {
        return newInstance(title, hint, initText, false, InputType.TYPE_CLASS_TEXT);
    }

    public static TextFieldDialogFragment newInstance(String title, String hint, String initText, boolean selectAll) {
        return newInstance(title, hint, initText, selectAll, InputType.TYPE_CLASS_TEXT);
    }

    public static TextFieldDialogFragment newInstance(String title, String hint, String initText, boolean selectAll, int inputType) {

        Bundle args = new Bundle();

        TextFieldDialogFragment fragment = new TextFieldDialogFragment();
        args.putString(TITLE_KEY, title);
        args.putString(HINT_KEY, hint);
        args.putString(TEXT_KEY, initText);
        args.putBoolean(SELECT_ALL_KEY, selectAll);
        args.putInt(INPUT_TYPE_KEY, inputType);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        Bundle args = getArguments();
        Precondition.checkNotNull(args);

        mTitle = args.getString(TITLE_KEY, "");
        mHint = args.getString(HINT_KEY, "");
        mText = args.getString(TEXT_KEY, "");
        mSelectAll = args.getBoolean(SELECT_ALL_KEY);
        mInputType = args.getInt(INPUT_TYPE_KEY, InputType.TYPE_CLASS_TEXT);

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        LayoutInflater inflater = LayoutInflater.from(getContext());
        @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.fragment_text_field, null);

        inputLayout = view.findViewById(R.id.input_layout);
        mInputText = view.findViewById(R.id.et_input);


        FragmentActivity context = getActivity();
        Precondition.checkNotNull(context);

        AlertDialog alertDialog = new AlertDialog.Builder(context)
                .setTitle(mTitle)
                .setView(view)
                .setPositiveButton(R.string.ok, null)
                .setNegativeButton(R.string.cancel, null)
                .create();

        alertDialog.setOnShowListener(dialog -> {
            initEditText();
            AlertDialog ad = (AlertDialog) dialog;
            Button button = ad.getButton(DialogInterface.BUTTON_POSITIVE);
            button.setOnClickListener(v -> {
                CharSequence text = mInputText.getText().toString().trim();
                if (TextUtils.isEmpty(text)) {
                    BaseApp.toast(R.string.empty_text);
                    return;
                }
                if (!text.equals(mText) && mResultCallback != null) {
                    mResultCallback.onResult(text.toString());
                }
                hideSoftInput();
                dismiss();
            });
        });
        return alertDialog;
    }

    private void initEditText() {
        inputLayout.setHint(mHint);
        mInputText.requestFocus();
        if (!TextUtils.isEmpty(mText)) {
            mInputText.setText(mText);
            if (mSelectAll) {
                mInputText.setSelection(0, mText.length());
            } else {
                int i = mText.lastIndexOf('.');
                mInputText.setSelection(0, i == -1 ? mText.length() : i);
            }
        }
        mInputText.setInputType(mInputType);
        mHandler.postDelayed(() -> {
            Context context = getContext();
            if (context != null) {
                InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) imm.showSoftInput(mInputText, 0);
            }
        }, 100);
    }

    private void hideSoftInput() {
        Activity context = getActivity();
        if (context != null) {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(mInputText.getWindowToken(), 0);
        }
    }


    public void setResultCallback(ResultCallback resultCallback) {
        this.mResultCallback = resultCallback;
    }


}
