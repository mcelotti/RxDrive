package com.francescocervone.rxdrive.sample;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.francescocervone.rxdrive.RxDrive;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.Metadata;

import java.util.ArrayList;
import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

public class DriveFileAdapter extends RecyclerView.Adapter<DriveFileAdapter.DriveFileViewHolder> {
    private static final String TAG = DriveFileAdapter.class.getName();
    private RxDrive mRxDrive;

    private List<DriveId> mResources = new ArrayList<>();

    private OnDriveIdClickListener mListener;

    public static class DriveFileViewHolder extends RecyclerView.ViewHolder {

        private TextView mTextView;
        private Button mRemoveButton;

        public DriveFileViewHolder(View itemView) {
            super(itemView);
            mTextView = (TextView) itemView.findViewById(R.id.percentage);
            mRemoveButton = (Button) itemView.findViewById(R.id.remove);
        }
    }

    public DriveFileAdapter(@NonNull RxDrive rxDrive) {
        mRxDrive = rxDrive;
    }

    public DriveFileAdapter(@NonNull RxDrive rxDrive, @NonNull List<DriveId> resources) {
        this(rxDrive);
        mResources = resources;
    }

    public void setDriveIdClickListener(OnDriveIdClickListener listener) {
        mListener = listener;
    }

    public void setResources(List<DriveId> resources) {
        mResources = resources;
        notifyDataSetChanged();
    }

    public void addResource(DriveId resource) {
        mResources.add(resource);
        notifyItemInserted(mResources.size() - 1);
    }

    @Override
    public DriveFileViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_drive_file, parent, false);
        return new DriveFileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final DriveFileViewHolder holder, int position) {
        final DriveId driveId = mResources.get(position);
        mRxDrive.getMetadata(driveId.asDriveResource())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Metadata>() {
                    @Override
                    public void call(Metadata metadata) {
                        holder.mTextView.setText(metadata.getTitle());
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        log(throwable);
                    }
                });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onDriveIdClick(driveId);
            }
        });
        holder.mRemoveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRxDrive.delete(driveId.asDriveResource())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<Boolean>() {
                            @Override
                            public void call(Boolean removed) {
                                if (removed) {
                                    remove(holder.getAdapterPosition());
                                }
                            }
                        }, new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                log(throwable);
                            }
                        });
            }
        });
    }

    private void remove(int position) {
        mResources.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public int getItemCount() {
        return mResources.size();
    }

    private void log(Object object) {
        Log.d(TAG, "log: " + object);
        if (object instanceof Throwable) {
            ((Throwable) object).printStackTrace();
        }
    }

    interface OnDriveIdClickListener {
        void onDriveIdClick(DriveId driveId);
    }
}
