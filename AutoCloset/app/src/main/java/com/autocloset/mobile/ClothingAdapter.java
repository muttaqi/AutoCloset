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

    // a piece of clothing, attached to a Firebase storage reference
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

        // firebase call
        mStorageRef = FirebaseStorage.getInstance().getReference("images/" + userID + "/" + child);

        // get all items and add them to adapter's items list
        mStorageRef.listAll()
                .addOnCompleteListener(new OnCompleteListener<ListResult>() {
                    @Override
                    public void onComplete(@NonNull Task<ListResult> task) {

                        if (task.isSuccessful()) {

                            try {

                                for (final StorageReference item : task.getResult().getItems()) {

                                    final String[] nameA = item.getName().split(" ");

                                    item.getBytes(1000000).addOnCompleteListener(new OnCompleteListener<byte[]>() {
                                        @Override
                                        public void onComplete(@NonNull Task<byte[]> task) {

                                            if (task.isSuccessful()) {

                                                String name = "";
                                                for (int i = 0; i < nameA.length - 1; i ++) {

                                                    name  += nameA[i];
                                                }

                                                byte[] image = task.getResult();
                                                mItems.add(new Clothing(name, image, item));

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
                        }

                        else {

                            Log.d(TAG, "DEBUG CA 118 " + task.getException());
                        }
                    }
                });
    }

    public void refresh() {
        // reload firebase items into frontend

        mItems.clear();

        final String child;

        switch(mType) {

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

        // firebase call
        mStorageRef = FirebaseStorage.getInstance().getReference("images/" + mUserID + "/" + child);

        mStorageRef.listAll()
                .addOnCompleteListener(new OnCompleteListener<ListResult>() {
                    @Override
                    public void onComplete(@NonNull Task<ListResult> task) {

                        if (task.isSuccessful()) {

                            try {

                                for (final StorageReference item : task.getResult().getItems()) {

                                    final String[] nameA = item.getName().split(" ");

                                    item.getBytes(1000000).addOnCompleteListener(new OnCompleteListener<byte[]>() {
                                        @Override
                                        public void onComplete(@NonNull Task<byte[]> task) {

                                            if (task.isSuccessful()) {

                                                String name = "";
                                                for (int i = 0; i < nameA.length - 1; i ++) {

                                                    name  += nameA[i];
                                                }

                                                byte[] image = task.getResult();
                                                mItems.add(new Clothing(name, image, item));

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

            ivClothing = (ImageView) itemView.findViewById(R.id.iv_clothing);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position = (int) view.getTag(R.id.clothingID);

            // trigger on click event to display clothing details
            mClickHandler.onClickEvent(mItems.get(position));
        }
    }

    public ClothingViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // display the loaded item

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

        holder.ivClothing.setBackground(new BitmapDrawable(mContext.getResources(), BitmapFactory.decodeByteArray(mItems.get(i).getImgData(), 0, mItems.get(i).getImgData().length)));
        holder.itemView.setTag(R.id.clothingID, i);
    }

    public int getItemCount() {return mItems.size();}
}
