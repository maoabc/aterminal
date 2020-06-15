package com.github.maoabc.aterm;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import com.github.maoabc.BaseApp;
import com.github.maoabc.aterm.databinding.DialogCreateSshServerBinding;
import com.github.maoabc.aterm.viewmodel.SshServerOption;
import com.github.maoabc.util.Precondition;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;

public class CreateSshServerDialogFragment extends DialogFragment {

    private static final String TITLE_KEY = "title_key";
    private static final String KEY_OPTIONS = "key_options";

    private DialogCreateSshServerBinding mBinding;

    private String mTitle;

    private SshServerOption mServerOption;

    private ResultCallback mResultCallback;


    public static CreateSshServerDialogFragment newInstance(@StringRes int titleId, SshServerOption options) {

        Bundle args = new Bundle();

        CreateSshServerDialogFragment fragment = new CreateSshServerDialogFragment();
        args.putInt(TITLE_KEY, titleId);
        args.putParcelable(KEY_OPTIONS, options);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        Bundle args = getArguments();
        Precondition.checkNotNull(args);

        mTitle = getString(args.getInt(TITLE_KEY, R.string.new_ssh_server));
        mServerOption = args.getParcelable(KEY_OPTIONS);
    }

    public void setResultCallback(ResultCallback callback) {
        this.mResultCallback = callback;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        FragmentActivity context = getActivity();
        Precondition.checkNotNull(context);

        mBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.dialog_create_ssh_server, null, false);
        mBinding.etHost.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mBinding.tlHost.setError(null);
                mBinding.tlHost.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mBinding.etUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mBinding.tlUsername.setError(null);
                mBinding.tlUsername.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mBinding.setOption(mServerOption);

        AlertDialog alertDialog = new AlertDialog.Builder(context)
                .setTitle(mTitle)
                .setView(mBinding.getRoot())
                .setPositiveButton(R.string.ok, null)
                .setNegativeButton(R.string.cancel, null)
                .create();

        alertDialog.setOnShowListener(dialog -> {
            AlertDialog ad = (AlertDialog) dialog;
            Button positiveButton = ad.getButton(DialogInterface.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {
                checkValid();
            });
        });
        return alertDialog;
    }

    private void checkValid() {
        if (mServerOption == null) {
            return;
        }
        String host = mServerOption.getHost().toString();
        if (TextUtils.isEmpty(host)) {
            mBinding.tlHost.setErrorEnabled(true);
            mBinding.tlHost.setError(getString(R.string.empty_host));
            return;
        }
        String username = mServerOption.getUsername().toString();
        if (TextUtils.isEmpty(username)) {
            mBinding.tlUsername.setErrorEnabled(true);
            mBinding.tlUsername.setError(getString(R.string.empty_username));
            return;
        }
        if (TextUtils.isEmpty(mServerOption.getPassword()) && TextUtils.isEmpty(mServerOption.getPrivateKey())) {
            BaseApp.toast(R.string.need_enter_password_or_key);
            return;
        }
        if (!TextUtils.isEmpty(mServerOption.getPrivateKey())) {
            try {
                JSch jSch = new JSch();
                final byte[] phassphrase;
                CharSequence sequence = mServerOption.getPassphrase();
                if (TextUtils.isEmpty(sequence)) {
                    phassphrase = null;
                } else {
                    phassphrase = sequence.toString().getBytes();
                }
                jSch.addIdentity("", mServerOption.getPrivateKey().toString().getBytes(), null, phassphrase);
            } catch (JSchException e) {
                BaseApp.toast(R.string.key_invalid);
                return;
            }
        }

        if (mResultCallback != null) mResultCallback.onResult(mServerOption);
        dismiss();
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mResultCallback = null;
    }

    public interface ResultCallback {
        void onResult(SshServerOption option);
    }
}
