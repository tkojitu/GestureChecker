package org.jitu.gesturechecker;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.Prediction;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends ActionBarActivity
        implements GestureOverlayView.OnGesturePerformedListener{
    private static final int REQUEST_ACTION_GET_CONTENT = 11;
    private GestureLibrary library;
    private String filepath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        GestureOverlayView gestures = (GestureOverlayView) findViewById(R.id.gestures);
        gestures.addOnGesturePerformedListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_load) {
            onOpen();
            return true;
        } else if (id == R.id.action_reload) {
            onReload();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean onOpen() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("file/*");
        try {
            startActivityForResult(intent, REQUEST_ACTION_GET_CONTENT);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
        case REQUEST_ACTION_GET_CONTENT:
            onReplyActionGetContent(resultCode, data);
            break;
        default:
            break;
        }
    }

    private void onReload() {
        if (filepath == null) {
            return;
        }
        loadLibrary(filepath);
    }

    private void onReplyActionGetContent(int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }
        String path = data.getData().getPath();
        loadLibrary(path);
    }

    private void loadLibrary(String path) {
        library = GestureLibraries.fromFile(path);
        if (library == null) {
            Toast.makeText(this, "GestureLibraries.fromFile failed", Toast.LENGTH_LONG).show();
            return;
        }
        if (!library.load()) {
            Toast.makeText(this, "GestureLibraries#load failed", Toast.LENGTH_LONG).show();
            return;
        }
        filepath = path;
    }

    public void onGesturePerformed(GestureOverlayView gestureView, Gesture gesture) {
        if (library == null) {
            return;
        }
        ArrayList<Prediction> predictions = library.recognize(gesture);
        if (predictions.isEmpty()) {
            Toast.makeText(this, "no predictions", Toast.LENGTH_LONG).show();
            return;
        }
        Prediction prediction = predictions.get(0);
        if (prediction.score > 1.0) {
            Toast.makeText(this, "hit: " + prediction.name, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "unhit: " + prediction.score + ": " + prediction.name,
                    Toast.LENGTH_SHORT).show();
        }
    }
}
