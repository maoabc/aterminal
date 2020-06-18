package com.github.maoabc.aterm;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

public class ATermIntentHandler extends AppCompatActivity {
    public static final String TAG = "HandlerIntent";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (intent == null) {
            return;
        }
//        Log.d(TAG, "handlerIntent: " + intent);
        String action = intent.getAction();
        if (ATermService.SERVICE_ACTION_CHANGE_DIRECTORY.equals(action)) {
            Intent intent1 = new Intent(this, ATermService.class);
            intent1.setAction(action);
            intent1.putExtras(intent);
            startService(intent1);
        } else if (ATermService.SERVICE_ACTION_EXEC_COMMAND.equals(action)) {
            Intent intent1 = new Intent(this, ATermService.class);
            intent1.setAction(action);
            intent1.putExtras(intent);
            startService(intent1);
        } else {
            try {
                Intent newIntent = new Intent(this, ATermService.class);
                newIntent.setDataAndType(intent.getData(), intent.getType());
                if (intent.getExtras() != null) {
                    newIntent.putExtras(intent);
                }
                startService(newIntent);
            } catch (Exception e) {
                Log.e(TAG, "handleIntent: ", e);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        finish();
    }
}
