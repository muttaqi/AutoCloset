package com.autocloset.mobile;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.fragment.app.Fragment;

import com.autocloset.mobile.Data.Useful;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.datavec.api.io.labels.ParentPathLabelGenerator;
import org.datavec.api.split.FileSplit;
import org.datavec.image.loader.NativeImageLoader;
import org.datavec.image.recordreader.ImageRecordReader;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.nd4j.linalg.dataset.api.preprocessor.ImagePreProcessingScaler;
import org.nd4j.linalg.factory.Nd4j;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import at.markushi.ui.CircleButton;

import static android.content.ContentValues.TAG;
import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;

public class CameraFragment extends Fragment {
    
    static Camera mCamera = null;
    private CameraPreview mPreview;
    private static boolean deviceHasCameraHardware;

    CircleButton captureButton;

    private static Context context;
    int width, height;

    public String DATA_PATH = "src/main/res/";
    public File dir;
    public MultiLayerNetwork model;

    private StorageReference mStorageRef;
    private int userID;

    public static CameraFragment newInstance() {
        return new CameraFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.camera_fragment, container, false);

        mStorageRef = FirebaseStorage.getInstance().getReference("images/" + ((MainActivity) this.getActivity()).userID);

        if (getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // this device has a camera
            deviceHasCameraHardware = true;
        } else {
            // no camera on this device
            deviceHasCameraHardware = false;
        }

        context = this.getActivity();

        dir = context.getFilesDir();

        File locationToSave = new File(dir, "trained_clothing_model.zip");

        if (locationToSave.exists()) {
            Log.d(TAG, "CF 287 Saved Model Found! " + locationToSave);
        } else {
            Log.d(TAG, "CF 289 File not found! " + locationToSave);
        }

        // load in the saved model
        try {
            model = ModelSerializer.restoreMultiLayerNetwork(context.getAssets().open("trained_clothing_model.zip"));
        }
        catch (Exception e) {Log.d(TAG, "DEBUG CF 97 " + e);}

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (deviceHasCameraHardware) {

            // Create an instance of Camera
            mCamera = getCameraInstance();

            // Create our Preview view and set it as the content of our activity.

            if (mCamera != null) {

                mPreview = new CameraPreview(this.getActivity(), mCamera);
                FrameLayout preview = (FrameLayout) getView().findViewById(R.id.camera_preview);
                preview.addView(mPreview);

                Display display = getActivity().getWindowManager().getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);
                width = size.x;
                height = size.y;

                // use transparent dark boxes to darken areas of the preview that won't be used in evaluation
                View topBox = getView().findViewById(R.id.rectangle_at_the_top);
                View bottomBox = getView().findViewById(R.id.rectangle_at_the_bottom);
                ImageView cameraBox = getView().findViewById(R.id.camera_box);

                topBox.setX(0);
                topBox.setY(0);

                ViewGroup.LayoutParams layoutParams = topBox.getLayoutParams();
                layoutParams.height = (height - width)/2;
                layoutParams.width = width;
                topBox.setLayoutParams(layoutParams);

                bottomBox.setX(0);
                bottomBox.setY(((height - width)/2) + width);

                layoutParams = bottomBox.getLayoutParams();
                layoutParams.height = (height - width)/2;
                layoutParams.width = width;
                bottomBox.setLayoutParams(layoutParams);

                cameraBox.setMinimumWidth(width);
                cameraBox.setMinimumHeight(cameraBox.getMinimumWidth());
                cameraBox.setX(0);
                cameraBox.setY((height - width)/2);

                // display the capture button
                captureButton = (CircleButton) this.getActivity().findViewById(R.id.button_capture);
                captureButton.setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // on click, get an image from the camera
                                mCamera.takePicture(null, null, mPicture);
                            }
                        }
                );
            }
        }
    }

    /** A safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){

            Log.d(TAG, "CF 61: Camera not available " + e);
        }
        return c; // returns null if camera is unavailable
    }

    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            width = mPreview.getSize().width;
            height = mPreview.getSize().height;

            Matrix matrix = new Matrix();
            matrix.postRotate(90);

            // get the picture as a bitmap
            Bitmap image = BitmapFactory.decodeByteArray(data, 0, data.length);

            // crop image to square
            int newWidth, newHeight;
            double ratio = (double) width/height;

            if ( (double) image.getWidth() / image.getHeight()  > ratio) {

                newWidth = (int) (ratio * image.getHeight());
                newHeight = image.getHeight();
            }

            else {
                newHeight = (int) (image.getWidth() / ratio);
                newWidth = image.getWidth();
            }

            int startX = (image.getWidth() - newWidth) / 2;
            int startY = (image.getHeight() - newHeight) / 2;

            // rotate by 90 degrees
            Bitmap rotatedImage = Bitmap.createBitmap(image, startX, startY, newWidth, newHeight, matrix, true);
            
            Bitmap croppedImage = rotatedImage;

            // write new image locally
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            rotatedImage.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] newData = baos.toByteArray();

            File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
            if (pictureFile == null){
                Log.d(TAG, "CF 111 Error creating media file, check storage permissions");

                return;
            }

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(newData);
                fos.close();
            } catch (FileNotFoundException e) {
                Log.d(TAG, "CF 120 File not found: " + e.getMessage());
            } catch (Exception e) {
                Log.d(TAG, "CF 122 Error accessing file: " + e.getMessage());
            }

            mCamera.startPreview();

            // crop our image
            rotatedImage = Useful.cropImage(rotatedImage);

            croppedImage = Useful.cropImage(rotatedImage);

            rotatedImage = Useful.monoChromeImage(rotatedImage);
            rotatedImage = Useful.blackWhiteImage(rotatedImage);

            baos = new ByteArrayOutputStream();
            rotatedImage.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            newData = baos.toByteArray();

            File picDir = getOutputMediaFile(MEDIA_TYPE_IMAGE);

            if (picDir == null){
                Log.d(TAG, "CF 111 Error creating media file, check storage permissions");

                return;
            }

            try {
                FileOutputStream fos = new FileOutputStream(picDir);
                fos.write(newData);
                fos.close();
            } catch (FileNotFoundException e) {
                Log.d(TAG, "CF 120 File not found: " + e.getMessage());
            } catch (Exception e) {
                Log.d(TAG, "CF 122 Error accessing file: " + e.getMessage());
            }

            try {
                // evaluate the new image
                FileSplit analyze = new FileSplit(picDir, NativeImageLoader.ALLOWED_FORMATS);

                ParentPathLabelGenerator analyzeLabelMaker = new ParentPathLabelGenerator();

                ImageRecordReader analyzeRecordReader = new ImageRecordReader(100, 100, 1, analyzeLabelMaker);


                analyzeRecordReader.initialize(analyze);

                DataSetIterator analyzeDataIter = new RecordReaderDataSetIterator(analyzeRecordReader, 1, 1, 4);
                DataNormalization analyzeScaler = new ImagePreProcessingScaler(0, 1);

                analyzeScaler.fit(analyzeDataIter);
                analyzeDataIter.setPreProcessor(analyzeScaler);

                model.getLabels();

                // Create Eval object with 4 possible classes
                org.deeplearning4j.eval.Evaluation eval = new org.deeplearning4j.eval.Evaluation(4);

                INDArray analyseInputs = Nd4j.zeros(1, 10000);
                INDArray analyseOutputs = Nd4j.zeros(1, 4);

                rotatedImage.setConfig(Bitmap.Config.ARGB_8888);

                // input grayscale version of the image
                for (int i = 0; i < 10000; i ++) {

                    int p = rotatedImage.getPixel((i - (i % 100)) / 100, i % 100);

                    int r = (short) ((p >> 16) & 0xFF);
                    int g = (short) ((p >> 8) & 0xFF);
                    int b = (short) (p & 0xFF);

                    analyseInputs.putScalar(new int[]{0, i}, r + g + b);
                }

                DataSet next = new DataSet(analyseInputs, analyseOutputs);

                INDArray output = model.output(next.getFeatures());

                eval.eval(next.getLabels(), output);

                // output evaluation stats
                Log.d(TAG, "DEBUG CF 309 " + eval.stats());

                // match model output to XML spinner enumeration
                int evaluation;
                if (eval.getConfusionMatrix().getPredictedTotal(0) == 1) {
                    evaluation = 0;
                    // hats
                }

                else if (eval.getConfusionMatrix().getPredictedTotal(1) == 1) {

                    evaluation = 2;
                    // bottoms
                }

                else if (eval.getConfusionMatrix().getPredictedTotal(2) == 1) {

                    evaluation = 1;
                    // tops
                }

                else {

                    evaluation = 3;
                    // shoes
                }

                //display clothing type selector with the evaluation
                displayClothingDetails(croppedImage, evaluation, "");
            } catch (Exception e) {Log.d(TAG, "DEBUG CF 294 " + e);}
        }
    };

    private static File getOutputMediaFile(int type){

        File mediaStorageDir = new File(context.getExternalFilesDir(
                Environment.DIRECTORY_PICTURES), "AutoCloset");

        // create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("AutoCloset", "CF 139 failed to create directory");
                return null;
            }
        }

        // create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else {
            return null;
        }

        return mediaFile;
    }

    // display the image, spinner and the image name input text
    public void displayClothingDetails(final Bitmap croppedImage, final int selection, String text) {
        AlertDialog.Builder adb = new AlertDialog.Builder(context);

        final View clothingTypeSelector = getLayoutInflater().inflate(R.layout.clothing_type_selector, null);

        final AppCompatSpinner spinner = clothingTypeSelector.findViewById(R.id.sp_type);
        final MySpinner mySpinner  = new MySpinner(spinner.getContext());
        mySpinner.setLayoutParams(spinner.getLayoutParams());

        ViewGroup cTSasGroup = (ViewGroup) spinner.getParent();

        cTSasGroup.removeView(spinner);
        cTSasGroup.addView(mySpinner);

        final ImageView imgClothing = clothingTypeSelector.findViewById(R.id.iv_clothing);
        final EditText etName = clothingTypeSelector.findViewById(R.id.et_name);
        imgClothing.setBackground(new BitmapDrawable(getResources(), croppedImage));

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context, R.array.types_array, R.layout.spinner_item);

        mySpinner.setAdapter(adapter);

        // update the spinner
        if (selection == 0) {
            // hats
            mySpinner.setSelection(0);
        }

        else if (selection == 1) {
            // bottoms
            mySpinner.setSelection(1);
        }

        else if (selection == 2) {
            // tops
            mySpinner.setSelection(2);
        }

        else {
            // shoes
            mySpinner.setSelection(3);
        }

        // update EditText with the default text
        etName.setText(text);

        final List<Integer> dim = new ArrayList<>();
        dim.add(-1); dim.add(-2);

        adb.setView(clothingTypeSelector);

        adb
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                })
                .setPositiveButton("SAVE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        if (etName.getText().toString().trim().equals("")) {
                            Toast.makeText(context, "Please enter a name", Toast.LENGTH_LONG).show();
                            displayClothingDetails(croppedImage, selection, "");
                        }

                        else {

                            String child;

                            switch(mySpinner.getSelectedItemPosition()) {

                                case 0:
                                    child = "hats/";
                                    break;

                                case 1:
                                    child = "tops/";
                                    break;

                                case 2:
                                    child = "bottoms/";
                                    break;

                                default:
                                    child = "shoes/";
                                    break;
                            }

                            // save file to firebase based on spinner category
                            StorageReference fileReference = mStorageRef.child(child + etName.getText().toString() + " " + System.currentTimeMillis() + ".jpg");

                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            croppedImage.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                            fileReference.putBytes(baos.toByteArray()).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                                    if (task.isSuccessful()) {

                                        Toast.makeText(context, "Success", Toast.LENGTH_LONG).show();
                                    } else {

                                        Toast.makeText(context, "Error Occured " + task.getException(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    }
                })
                .setTitle("Clothing Details");

        final AlertDialog ad = adb.create();

        // limit the image display size
        ad.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                ad.getWindow().getAttributes().height = (int) Math.floor(0.7 * height);
                ad.getWindow().getAttributes().width = (int) Math.floor(0.8 * width);

                imgClothing.getLayoutParams().height = (int) Math.floor(0.6 * height);
                imgClothing.getLayoutParams().width = (int) Math.floor(0.4 * height);
            }
        });

        // button design
        ad.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {

                Button btnPos = ad.getButton(DialogInterface.BUTTON_POSITIVE);

                btnPos.setTextColor(getResources().getColor(R.color.colorPrimary));
                ad.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorPrimary));
            }
        });

        ad.show();

        // update spinner and re-load the adapter view when a button is clicked
        mySpinner.setOnItemSelectedEvenIfUnchangedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                ad.cancel();
                displayClothingDetails(croppedImage, i, etName.getText().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                ad.cancel();
                displayClothingDetails(croppedImage, selection, etName.getText().toString());
            }
        });
    }

    // custom spinner class with a listener and selection method
    public class MySpinner extends androidx.appcompat.widget.AppCompatSpinner {
        OnItemSelectedListener listener;

        public MySpinner(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public MySpinner(Context context) {

            super(context);
        }

        @Override
        public void setSelection(int position) {
            super.setSelection(position);
            if (listener != null)
                listener.onItemSelected(null, null, position, 0);
        }

        public void setOnItemSelectedEvenIfUnchangedListener(
                OnItemSelectedListener listener) {
            this.listener = listener;
        }
    }
}