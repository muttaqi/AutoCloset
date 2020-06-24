package com.autocloset.mobile;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

;import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class ClothingAdapter extends RecyclerView.Adapter<ClothingAdapter.ClothingViewHolder> {

    private static final String TAG = ClothingAdapter.class.getSimpleName();

    private Context mContext;
    private int mWidth, mHeight;

    private StorageReference mStorageRef;

    private final ClothingAdapterOnClickHandler mClickHandler;
    private MainFragment mParent;
    private int mType;
    private long mUserID;

    public class Clothing {

        private String name;
        private byte[] imgData;
        private StorageReference sR;

        public Clothing (String name, byte[] imgData, StorageReference sR) {

            this.name = name;
            this.imgData = imgData;
            this.sR = sR;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public byte[] getImgData() {
            return imgData;
        }

        public void setImgData(byte[] imgData) {
            this.imgData = imgData;
        }

        public StorageReference getsR() {
            return sR;
        }

        public void setsR(StorageReference sR) {
            this.sR = sR;
        }
    }

    public List<Clothing> mItems = new ArrayList<>();

    public interface ClothingAdapterOnClickHandler {

        void onClickEvent(Clothing c);
    }

    public ClothingAdapter(final Context context, int type, ClothingAdapterOnClickHandler listener, int width, int height, long userID, MainFragment parent) {

        mContext = context;
        mClickHandler = listener;

        mWidth = width;
        mHeight = height;

        final String child;

        mParent = parent;
        mType = type;

        mUserID = userID;

        switch(type) {

            case 0:

                //Toast.makeText(context, "This is a cap", Toast.LENGTH_LONG).show();
                child = "hats/";
                break;

            case 1:

                //Toast.makeText(context, "This is a shirt", Toast.LENGTH_LONG).show();
                child = "tops/";
                break;

            case 2:

                //Toast.makeText(context, "These are pants", Toast.LENGTH_LONG).show();
                child = "bottoms/";
                break;

            default:

                //Toast.makeText(context, "These are shoes", Toast.LENGTH_LONG).show();
                child = "shoes/";
                break;
        }

        // firebase call
        mStorageRef = FirebaseStorage.getInstance().getReference("images/" + userID + "/" + child);

        mStorageRef.listAll()
                .addOnCompleteListener(new OnCompleteListener<ListResult>() {
                    @Override
                    public void onComplete(@NonNull Task<ListResult> task) {

                        if (task.isSuccessful()) {

                            try {

                                Log.d(TAG, "DEBUG CA 89 " + mStorageRef.getPath());

                                for (final StorageReference item : task.getResult().getItems()) {

                                    final String[] nameA = item.getName().split(" ");

                                    item.getBytes(1000000).addOnCompleteListener(new OnCompleteListener<byte[]>() {
                                        @Override
                                        public void onComplete(@NonNull Task<byte[]> task) {

                                            Log.d(TAG, "DEBUG CA 101");

                                            if (task.isSuccessful()) {

                                                String name = "";
                                                for (int i = 0; i < nameA.length - 1; i ++) {

                                                    name  += nameA[i];
                                                }

                                                byte[] image = task.getResult();
                                                mItems.add(new Clothing(name, image, item));

                                                Log.d(TAG, "DEBUG CA 103 " + mItems.size());

                                                notifyDataSetChanged();

                                                mParent.updateNullViews();
                                            }

                                            else {

                                                Log.d(TAG, "DEBUG CA 117 " + task.getException());
                                            }
                                        }
                                    });
                                }
                            } catch (NullPointerException e) {Log.d(TAG, "DEBUG CA 122 " + e);}

                            Log.d(TAG, "DEBUG CA 155 " + mItems.size());
                        }

                        else {

                            Log.d(TAG, "DEBUG CA 118 " + task.getException());
                        }
                    }
                });
    }

    public void refresh() {

        mItems.clear();

        final String child;

        switch(mType) {

            case 0:

                //Toast.makeText(context, "This is a cap", Toast.LENGTH_LONG).show();
                child = "hats/";
                break;

            case 1:

                //Toast.makeText(context, "This is a shirt", Toast.LENGTH_LONG).show();
                child = "tops/";
                break;

            case 2:

                //Toast.makeText(context, "These are pants", Toast.LENGTH_LONG).show();
                child = "bottoms/";
                break;

            default:

                //Toast.makeText(context, "These are shoes", Toast.LENGTH_LONG).show();
                child = "shoes/";
                break;
        }

        // firebase call
        mStorageRef = FirebaseStorage.getInstance().getReference("images/" + mUserID + "/" + child);

        mStorageRef.listAll()
                .addOnCompleteListener(new OnCompleteListener<ListResult>() {
                    @Override
                    public void onComplete(@NonNull Task<ListResult> task) {

                        if (task.isSuccessful()) {

                            try {

                                Log.d(TAG, "DEBUG CA 89 " + mStorageRef.getPath());

                                for (final StorageReference item : task.getResult().getItems()) {

                                    final String[] nameA = item.getName().split(" ");

                                    item.getBytes(1000000).addOnCompleteListener(new OnCompleteListener<byte[]>() {
                                        @Override
                                        public void onComplete(@NonNull Task<byte[]> task) {

                                            Log.d(TAG, "DEBUG CA 101");

                                            if (task.isSuccessful()) {

                                                String name = "";
                                                for (int i = 0; i < nameA.length - 1; i ++) {

                                                    name  += nameA[i];
                                                }

                                                byte[] image = task.getResult();
                                                mItems.add(new Clothing(name, image, item));

                                                Log.d(TAG, "DEBUG CA 103 " + mItems.size());

                                                notifyDataSetChanged();

                                                mParent.updateNullViews();
                                            }

                                            else {

                                                Log.d(TAG, "DEBUG CA 117 " + task.getException());
                                            }
                                        }
                                    });
                                }
                            } catch (NullPointerException e) {Log.d(TAG, "DEBUG CA 122 " + e);}

                            Log.d(TAG, "DEBUG CA 155 " + mItems.size());
                        }

                        else {

                            Log.d(TAG, "DEBUG CA 118 " + task.getException());
                        }
                    }
                });
    }

    class ClothingViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView ivClothing;

        public ClothingViewHolder(View itemView) {

            super(itemView);

            Log.d(TAG, "DEBUG 172 ");

            ivClothing = (ImageView) itemView.findViewById(R.id.iv_clothing);

            Log.d(TAG, "DEBUG CA 174");

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {

            int position = (int) view.getTag(R.id.clothingID);

            Log.d(TAG, "DEBUG CA 184");

            mClickHandler.onClickEvent(mItems.get(position));
        }
    }

    public ClothingViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        Log.d(TAG, "DEBUG 192 ");

        View ivClothing = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.rv_clothing_item, viewGroup, false);


        if (mWidth < mHeight) {
            ivClothing.getLayoutParams().width = (mWidth / 5) - 10;
            ivClothing.getLayoutParams().height = ivClothing.getLayoutParams().width;
        }

        else {

            ivClothing.getLayoutParams().height = (mHeight / 5) - 10;
            ivClothing.getLayoutParams().width = ivClothing.getLayoutParams().height;
        }

        ClothingViewHolder cVH = new ClothingViewHolder(ivClothing);

        return cVH;
    }

    public void onBindViewHolder(@NonNull ClothingViewHolder holder, int i) {

        Log.d(TAG, "DEBUG 214 " + i);

        holder.ivClothing.setBackground(new BitmapDrawable(mContext.getResources(), BitmapFactory.decodeByteArray(mItems.get(i).getImgData(), 0, mItems.get(i).getImgData().length)));
        holder.itemView.setTag(R.id.clothingID, i);
    }

    public int getItemCount() {return mItems.size();}
}
