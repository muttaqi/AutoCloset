package com.autocloset.mobile;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.autocloset.mobile.Data.UserContract;
import com.autocloset.mobile.Data.UserDbHelper;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.DriveScopes;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.api.client.http.HttpTransport;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends FragmentActivity implements MainFragment.OnFragmentInteractionListener {

    private static final int NUM_PAGES = 2;

    private ViewPager mPager;

    private Camera mCamera;

    private PagerAdapter pagerAdapter;
    private boolean deviceHasCameraHardware;

    private String TAG;

    private static final String APPLICATION_NAME = "Google Drive API Java Quickstart";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    /** Global instance of the {@link FileDataStoreFactory}. */
    private static FileDataStoreFactory DATA_STORE_FACTORY;

    /** Global instance of the HTTP transport. */
    private static HttpTransport HTTP_TRANSPORT;

    /** Directory to store user credentials for this application. */
    private static final java.io.File DATA_STORE_DIR = new java.io.File(
            System.getProperty("user.home"), ".credentials/drive-java-quickstart");

    private static final int REQUEST_CODE_SIGN_IN = 0;
    private static final int REQUEST_CODE_OPEN_ITEM = 1;
    private static final int REQUEST_WRITE_STORAGE = 112;

    private GoogleSignInAccount signInAccount;
    private Set<Scope> requiredScopes;
    private DriveClient mDriveClient;
    private DriveResourceClient mDriveResourceClient;

    private TaskCompletionSource<DriveId> mOpenItemTaskSource;
    private File storageDir;

    com.google.api.services.drive.Drive driveService;

    Context context;
    File dir;

    private DocumentReference mDocRef;

    private SQLiteDatabase mUserDb;
    private Cursor userCursor;

    public long userID;

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE_METADATA_READONLY);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        TAG = this.getClass().getSimpleName();

        // get user id
        try {
            mDocRef = FirebaseFirestore.getInstance().document("autocloset/num_users");
        } catch (Exception e) {
            Log.d(TAG, "DEBUG MA 177 " + e);
        }
        final UserDbHelper userDbHelper = new UserDbHelper(this);
        mUserDb = userDbHelper.getWritableDatabase();
        userCursor = mUserDb.query(UserContract.UserEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null);

        
        // get id locally
        if (userCursor.moveToFirst() && userCursor != null) {
            userID = userCursor.getLong(userCursor.getColumnIndex(UserContract.UserEntry.USER_ID));
        }

        // else get from firebase
        else {
            mDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                    if (task.isSuccessful() && task.getResult().exists()) {

                        userID = task.getResult().getLong("value");

                        // cache id locally
                        ContentValues cv = new ContentValues();
                        cv.put(UserContract.UserEntry.USER_ID, userID);
                        mUserDb.insert(UserContract.UserEntry.TABLE_NAME,
                                null,
                                cv);

                        Map<String, Object> data = new HashMap<>();
                        data.put("value", userID + 1);
                        mDocRef.set(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                            }
                        });
                    }
                }
            });
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            createPermissions(this, this);
        }

        if (this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // this device has a camera
            deviceHasCameraHardware = true;
        } else {
            // no camera on this device
            deviceHasCameraHardware = false;
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.pager, new MainFragment())
                    .commitNow();
        }

        if (deviceHasCameraHardware) {

            mPager = (ViewPager) findViewById(R.id.pager);
            pagerAdapter = new MainActivity.ScreenSlidePagerAdapter(getSupportFragmentManager());
            mPager.setAdapter(pagerAdapter);
        }

        context = getApplicationContext();

        // download model from google drive
        try {
            DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
            HTTP_TRANSPORT = new NetHttpTransport();

            driveService = getDriveService();

            String fileId = "1XyM8UcvCbmubeWVdWa_u05BGieGpjFiJ";
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            driveService.files().get(fileId)
                    .executeMediaAndDownloadTo(outputStream);

            String fileName = "trained_clothing_model.zip";
            FileOutputStream fos = openFileOutput(fileName, Context.MODE_PRIVATE);

            fos.write(outputStream.toByteArray());
            fos.close();

        } catch (IOException e) {Log.d(TAG, "DEBUG MA 301 " + e);}
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    // handle sliding fragments (main fragment and camera fragment)
    private boolean change = false;
    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager supportFragmentManager) {

            super(supportFragmentManager);
        }

        @Override
        public void setPrimaryItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            super.setPrimaryItem(container, position, object);

            if (position == 0) {
                if (change && ((MainFragment) object).adaptersReady()) {
                    ((MainFragment) object).refreshAdapters();

                    change = false;
                }
            }

            else {
                change = true;
            }
        }

        @Override
        public Fragment getItem(int i) {

            if (i == 1) {
                CameraFragment fragCam = new CameraFragment();

                return fragCam;
            }

            MainFragment fragMain = new MainFragment();

            return fragMain;
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();              // release the camera immediately on pause event
    }

    private void releaseCamera(){
        if (mCamera != null){
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }

    // google drive api helpers
    public void createPermissions(Context c, Activity a){
        String permission = Manifest.permission.READ_SMS;
        if (ContextCompat.checkSelfPermission(c, permission) != PackageManager.PERMISSION_GRANTED){
            if(!ActivityCompat.shouldShowRequestPermissionRationale(a, permission)){
                requestPermissions(new String[]{permission}, 0);
            }
        }
    }

    private void requestPermission() {
        String dirPath = getFilesDir().getAbsolutePath() + File.separator + "PDF";
        storageDir = new File(dirPath);
        if (!storageDir.exists())
            storageDir.mkdirs();
    }


    private void initializeDriveClient(GoogleSignInAccount signInAccount) {
        mDriveClient = Drive.getDriveClient(getApplicationContext(), signInAccount);
        mDriveResourceClient = Drive.getDriveResourceClient(getApplicationContext(), signInAccount);
    }

    private void signIn() {
        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(Drive.SCOPE_FILE)
                .requestScopes(Drive.SCOPE_APPFOLDER)
                .build();
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, signInOptions);

        Task<GoogleSignInAccount> task = googleSignInClient.silentSignIn();

        if (task.isSuccessful()) {

            signInAccount = task.getResult();
        }

        else {

            task.addOnCompleteListener(new OnCompleteListener<GoogleSignInAccount>() {
                @Override
                public void onComplete(@NonNull Task<GoogleSignInAccount> task) {

                    try {

                        signInAccount = task.getResult(ApiException.class);
                        initializeDriveClient(signInAccount);
                        onDriveClientReady();
                    }

                    catch (ApiException ae) {Log.d(TAG, "DEBUG MA 319 " + ae);}
                }
            });
        }

        startActivityForResult(googleSignInClient.getSignInIntent(), REQUEST_CODE_SIGN_IN);
    }

    private void onDriveClientReady() {

        context = this;

        dir = context.getFilesDir();

        mOpenItemTaskSource = new TaskCompletionSource<>();

        try {

            driveService = getDriveService();

            String fileId = "1XyM8UcvCbmubeWVdWa_u05BGieGpjFiJ";
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            driveService.files().get(fileId)
                    .executeMediaAndDownloadTo(outputStream);

            String fileName = "trained_clothing_model.zip";
            FileOutputStream fos = openFileOutput(fileName, Context.MODE_PRIVATE);

            fos.write(outputStream.toByteArray());
            fos.close();

        } catch (IOException e) {Log.d(TAG, "DEBUG MA 301 " + e);}

    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void initialize() {

        requiredScopes = new HashSet<>(2);
        requiredScopes.add(Drive.SCOPE_FILE);
        requiredScopes.add(Drive.SCOPE_APPFOLDER);
    }

    /**
     * Build and return an authorized Drive client service.
     * @return an authorized Drive client service
     * @throws IOException
     */
    public com.google.api.services.drive.Drive getDriveService() throws IOException {
        Credential credential = authorize();
        return new com.google.api.services.drive.Drive.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    /**
     * Creates an authorized Credential object.
     * @return an authorized Credential object.
     * @throws IOException
     */
    public Credential authorize() throws IOException {
        // Load client secrets.


        InputStream in =
                context.getAssets().open("client_secret_600062760833-vk58t72lkr2r4kqqujjse9jcgl8srlj9.apps.googleusercontent.com.json");

        if (in == null) {

            Log.d(TAG, "DEBUG MA 412 Input Stream is null");
        }

        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow =
                new GoogleAuthorizationCodeFlow.Builder(
                        HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                        .setDataStoreFactory(DATA_STORE_FACTORY)
                        .setAccessType("offline")
                        .build();
        Credential credential = new AuthorizationCodeInstalledApp(
                flow, new LocalServerReceiver()).authorize("user");
        System.out.println(
                "Credentials saved to " + DATA_STORE_DIR.getAbsolutePath());
        return credential;
    }
}
