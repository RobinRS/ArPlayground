package com.sap.businesscard;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.ar.core.AugmentedImage;
import com.google.ar.core.AugmentedImageDatabase;
import com.google.ar.core.Camera;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Scene;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

public class MainActivity extends AppCompatActivity implements Scene.OnUpdateListener {

    private ArSceneView arView;
    private Session session;
    private boolean shouldconfigureSession = false;
    private static final int CAMERA = 404;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        arView = (ArSceneView)findViewById(R.id.arView);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.CAMERA)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        CAMERA);
            }
        } else {
            try {
                setupSession();
            } catch (UnavailableSdkTooOldException e) {
                e.printStackTrace();
            } catch (UnavailableDeviceNotCompatibleException e) {
                e.printStackTrace();
            } catch (UnavailableArcoreNotInstalledException e) {
                e.printStackTrace();
            } catch (UnavailableApkTooOldException e) {
                e.printStackTrace();
            }
        }
        initSceneView();
    }

    private void initSceneView() {
        arView.getScene().addOnUpdateListener(this);
    }

    private void setupSession() throws UnavailableSdkTooOldException, UnavailableDeviceNotCompatibleException, UnavailableArcoreNotInstalledException, UnavailableApkTooOldException {
        if(session ==null) {
            session = new Session(this);
            shouldconfigureSession = true;
        }
        if(shouldconfigureSession) {
            configureSession();
            shouldconfigureSession = false;
            arView.setupSession(session);
        }

        try {
            session.resume();
            arView.resume();
        } catch (CameraNotAvailableException e) {
            e.printStackTrace();
            session = null;
            return;
        }
    }

    private void configureSession() {
        Config conf = new Config(session);
        if (!buildDb(conf)) {
            Toast.makeText(this, "Error database", Toast.LENGTH_LONG).show();
        }
        conf.setUpdateMode(Config.UpdateMode.LATEST_CAMERA_IMAGE);
        session.configure(conf);
    }

    private boolean buildDb(Config conf) {
        AugmentedImageDatabase db;
        Bitmap bmp = null;
        try {
            bmp = loadImage();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(bmp == null)
            return false;

        db = new AugmentedImageDatabase(session);
        db.addImage("rob", bmp);
        conf.setAugmentedImageDatabase(db);
        return true;
    }

    private Bitmap loadImage() throws IOException {
        InputStream is = getAssets().open("frame.png");
        return BitmapFactory.decodeStream(is);

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (session != null) {
            arView.pause();
            session.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.CAMERA)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        try {
                            setupSession();
                        } catch (UnavailableSdkTooOldException e) {
                            e.printStackTrace();
                        } catch (UnavailableDeviceNotCompatibleException e) {
                            e.printStackTrace();
                        } catch (UnavailableArcoreNotInstalledException e) {
                            e.printStackTrace();
                        } catch (UnavailableApkTooOldException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toast.makeText(MainActivity.this, "Permission needed!", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                    }
                }).check();
    }

    @Override
    public void onUpdate(FrameTime frameTime) {
        Frame f = arView.getArFrame();
        Collection<AugmentedImage> updateAugmentedImage = f.getUpdatedTrackables(AugmentedImage.class);
        for (AugmentedImage img : updateAugmentedImage) {
            if (img.getTrackingState() == TrackingState.TRACKING) {
                if(img.getName().equals("rob")) {
                    MyArNode node = new MyArNode(this, R.raw.lion);
                    node.setImage(img);
                    arView.getScene().addChild(node);
                    Log.println(Log.ERROR, "Testte", "Tracking IMG" + img.getName());
                }
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    try {
                        setupSession();
                    } catch (UnavailableSdkTooOldException e) {
                        e.printStackTrace();
                    } catch (UnavailableDeviceNotCompatibleException e) {
                        e.printStackTrace();
                    } catch (UnavailableArcoreNotInstalledException e) {
                        e.printStackTrace();
                    } catch (UnavailableApkTooOldException e) {
                        e.printStackTrace();
                    }
                } else {
                }
                return;
            }
        }
    }

}
