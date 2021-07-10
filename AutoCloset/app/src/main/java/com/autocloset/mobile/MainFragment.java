package com.autocloset.mobile;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MainFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MainFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MainFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    private Context context;

    private ClothingAdapter hatAdapter, topAdapter, bottomAdapter, shoeAdapter;
    private RecyclerView rvHat, rvTop, rvBottom, rvShoe;
    private TextView tvHatNull, tvTopNull, tvBottomNull, tvShoeNull;

    public MainFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MainFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MainFragment newInstance(String param1, String param2) {
        MainFragment fragment = new MainFragment();
        Bundle args = new Bundle();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.main_fragment, container, false);

        context = getActivity();

        rvHat = v.findViewById(R.id.rv_hats);
        rvTop = v.findViewById(R.id.rv_tops);
        rvBottom = v.findViewById(R.id.rv_bottoms);
        rvShoe = v.findViewById(R.id.rv_shoes);

        tvHatNull = v.findViewById(R.id.tv_hats_null);
        tvTopNull = v.findViewById(R.id.tv_tops_null);
        tvBottomNull = v.findViewById(R.id.tv_bottoms_null);
        tvShoeNull = v.findViewById(R.id.tv_shoes_null);

        StaggeredGridLayoutManager sglm0 = new StaggeredGridLayoutManager(5, StaggeredGridLayoutManager.VERTICAL);
        StaggeredGridLayoutManager sglm1 = new StaggeredGridLayoutManager(5, StaggeredGridLayoutManager.VERTICAL);
        StaggeredGridLayoutManager sglm2 = new StaggeredGridLayoutManager(5, StaggeredGridLayoutManager.VERTICAL);
        StaggeredGridLayoutManager sglm3 = new StaggeredGridLayoutManager(5, StaggeredGridLayoutManager.VERTICAL);

        rvHat.setLayoutManager(sglm0);
        rvTop.setLayoutManager(sglm1);
        rvBottom.setLayoutManager(sglm2);
        rvShoe.setLayoutManager(sglm3);

        final Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;

        long userID = ((MainActivity) getActivity()).userID;

        // adapters for all categories
        hatAdapter = new ClothingAdapter(context, 0, new ClothingAdapter.ClothingAdapterOnClickHandler() {
            @Override
            public void onClickEvent(ClothingAdapter.Clothing c) {

                displayClothingDetails(c.getImgData(), 0, c.getName(), c.getsR());
            }
        }, width, height, userID, this);
        topAdapter = new ClothingAdapter(context, 1, new ClothingAdapter.ClothingAdapterOnClickHandler() {
            @Override
            public void onClickEvent(ClothingAdapter.Clothing c) {

                displayClothingDetails(c.getImgData(), 1, c.getName(), c.getsR());
            }
        }, width, height, userID, this);
        bottomAdapter = new ClothingAdapter(context, 2, new ClothingAdapter.ClothingAdapterOnClickHandler() {
            @Override
            public void onClickEvent(ClothingAdapter.Clothing c) {

                displayClothingDetails(c.getImgData(), 2, c.getName(), c.getsR());
            }
        }, width, height, userID, this);
        shoeAdapter = new ClothingAdapter(context, 3, new ClothingAdapter.ClothingAdapterOnClickHandler() {
            @Override
            public void onClickEvent(ClothingAdapter.Clothing c) {

                displayClothingDetails(c.getImgData(), 3, c.getName(), c.getsR());
            }
        }, width, height, userID, this);

        // attach recycler views to adapters
        rvHat.setAdapter(hatAdapter);
        rvTop.setAdapter(topAdapter);
        rvBottom.setAdapter(bottomAdapter);
        rvShoe.setAdapter(shoeAdapter);

        updateNullViews();

        return v;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    public void refreshAdapters() {
        hatAdapter.refresh();
        topAdapter.refresh();
        bottomAdapter.refresh();
        shoeAdapter.refresh();
    }

    public void updateNullViews() {

        if (hatAdapter.getItemCount() == 0) {
            tvHatNull.setVisibility(View.VISIBLE);
        }

        else {
            tvHatNull.setVisibility(View.INVISIBLE);
        }

        if (topAdapter.getItemCount() == 0) {
            tvTopNull.setVisibility(View.VISIBLE);
        }

        else {
            tvTopNull.setVisibility(View.INVISIBLE);
        }

        if (bottomAdapter.getItemCount() == 0) {
            tvBottomNull.setVisibility(View.VISIBLE);
        }

        else {
            tvBottomNull.setVisibility(View.INVISIBLE);
        }

        if (shoeAdapter.getItemCount() == 0) {
            tvShoeNull.setVisibility(View.VISIBLE);
        }

        else {
            tvShoeNull.setVisibility(View.INVISIBLE);
        }
    }

    // display clothing details (name, category, preview)
    public void displayClothingDetails(final byte[] image, final int selection, String text, final StorageReference mStorageRef) {
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
        imgClothing.setBackground(new BitmapDrawable(context.getResources(), BitmapFactory.decodeByteArray(image, 0, image.length)));

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context, R.array.types_array, R.layout.spinner_item);

        mySpinner.setAdapter(adapter);

        if (selection == 0) {
            mySpinner.setSelection(0);
            // hats
        }

        else if (selection == 1) {
            mySpinner.setSelection(1);
            // bottoms
        }

        else if (selection == 2) {
            mySpinner.setSelection(2);
            // tops
        }

        else {
            mySpinner.setSelection(3);
            // shoes
        }

        etName.setText(text);

        final List<Integer> dim = new ArrayList<>();
        dim.add(-1); dim.add(-2);

        adb.setView(clothingTypeSelector);

        adb
                .setNeutralButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                })
                // update name and category
                .setPositiveButton("SAVE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        if (etName.getText().toString().trim().equals("")) {

                            Toast.makeText(context, "Please enter a name", Toast.LENGTH_LONG).show();
                            displayClothingDetails(image, selection, "", mStorageRef);
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

                            final String fChild = child;

                            // save changes to firebase
                            mStorageRef.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if (task.isSuccessful()) {

                                        StorageReference fileReference = mStorageRef.getParent().getParent().child(fChild + etName.getText().toString() + " " + System.currentTimeMillis() + ".jpg");

                                        fileReference.putBytes(image).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                                                if (task.isSuccessful()) {
                                                    Toast.makeText(context, "Success", Toast.LENGTH_LONG).show();
                                                    refreshAdapters();
                                                } else {

                                                    Toast.makeText(context, "Error Occured " + task.getException(), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    }

                                    else {

                                        Toast.makeText(context, "Error Occured " + task.getException(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    }
                })
                // delete the item and update firebase
                .setNegativeButton("DELETE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        mStorageRef.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                if (task.isSuccessful()) {

                                    Toast.makeText(context, "Success", Toast.LENGTH_SHORT).show();
                                    refreshAdapters();
                                    updateNullViews();
                                }

                                else {

                                    Toast.makeText(context, "Error Occured " + task.getException(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                })
                .setTitle("Clothing Details");

        final AlertDialog ad = adb.create();

        ad.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
            }
        });

        ad.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {

                Button btnPos = ad.getButton(DialogInterface.BUTTON_POSITIVE);

                btnPos.setTextColor(getResources().getColor(R.color.colorPrimary));
                ad.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorPrimary));
                ad.getButton(DialogInterface.BUTTON_NEUTRAL).setTextColor(getResources().getColor(R.color.colorPrimary));
            }
        });

        ad.show();

        final List<Integer> runningCount = new ArrayList<>();
        runningCount.add(0);

        mySpinner.setOnItemSelectedEvenIfUnchangedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                ad.cancel();
                displayClothingDetails(image, i, etName.getText().toString(), mStorageRef);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                ad.cancel();
                displayClothingDetails(image, selection, etName.getText().toString(), mStorageRef);
            }
        });
    }

    // custom spinner with a listener for setting selection
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

    public boolean adaptersReady() {
        return hatAdapter != null &&
                topAdapter != null &&
                bottomAdapter != null &&
                shoeAdapter != null;
    }
}
