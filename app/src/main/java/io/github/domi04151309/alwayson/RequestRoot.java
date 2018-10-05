package io.github.domi04151309.alwayson;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class RequestRoot extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_root);
        Root.shell("echo this is a test");
        Toast toast = Toast.makeText(this, "Requested root access!", Toast.LENGTH_LONG);
        View view = toast.getView();
        view.setBackgroundResource(R.drawable.toast_bg);
        toast.show();
        finish();
    }
}
