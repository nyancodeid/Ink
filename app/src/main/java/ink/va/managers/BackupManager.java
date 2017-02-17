package ink.va.managers;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.ink.va.R;

import java.io.File;

import ink.va.callbacks.GeneralCallback;
import ink.va.interfaces.BackUpManagerCallback;
import ink.va.utils.RealmHelper;
import ink.va.utils.SharedHelper;
import lombok.Setter;

import static ink.va.utils.Constants.FIREBASE_STORAGE_BUCKET;
import static ink.va.utils.Constants.REALM_DB_NAME;

/**
 * Created by PC-Comp on 2/17/2017.
 */

public class BackupManager {

    private Context context;
    private SharedHelper mSharedHelper;

    @Setter
    private BackUpManagerCallback onBackUpManagerCallback;
    private FirebaseStorage storage;

    public BackupManager(Context context) {
        this.context = context;
        mSharedHelper = new SharedHelper(context);
        storage = FirebaseStorage.getInstance();
    }

    public void backUpMessages() {

        FirebaseStorage storage = FirebaseStorage.getInstance();
        final StorageReference storageReference = storage.getReferenceFromUrl(FIREBASE_STORAGE_BUCKET);

        RealmHelper.getInstance().backup(context, new GeneralCallback<File>() {
            @Override
            public void onSuccess(File file) {
                Uri fileUr = Uri.fromFile(file);
                StorageReference riversRef = storageReference.child(mSharedHelper.getUserId() + "/" + fileUr.getLastPathSegment());
                UploadTask uploadTask = riversRef.putFile(fileUr);

                uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        if (onBackUpManagerCallback != null) {
                            onBackUpManagerCallback.onBackUpProgress(progress);
                        }
                    }
                });
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        exception.printStackTrace();
                        if (onBackUpManagerCallback != null) {
                            onBackUpManagerCallback.onBackUpError(context.getString(R.string.serverErrorText));
                        }
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        if (onBackUpManagerCallback != null) {
                            onBackUpManagerCallback.onBackUpFinished();
                        }

                    }
                });
            }

            @Override
            public void onFailure(File file) {
                if (onBackUpManagerCallback != null) {
                    onBackUpManagerCallback.onBackUpError(context.getString(R.string.realmFileError));
                }
            }
        });


    }

    public void restoreMessages() {

        StorageReference storageReference = storage.getReferenceFromUrl(FIREBASE_STORAGE_BUCKET);


        StorageReference fileReference = storageReference.child(mSharedHelper.getUserId() + "/" + REALM_DB_NAME);

        File localFile = new File(Environment.getExternalStorageDirectory(), REALM_DB_NAME);

        final File finalLocalFile = localFile;

        fileReference.getFile(localFile)
                .addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    }
                })
                .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        RealmHelper.getInstance().restore(context, finalLocalFile, new GeneralCallback<Object>() {
                            @Override
                            public void onSuccess(Object o) {
                                finalLocalFile.delete();
                                if (onBackUpManagerCallback != null) {
                                    onBackUpManagerCallback.onRestoreFinished();
                                }
                            }

                            @Override
                            public void onFailure(Object o) {
                                if (onBackUpManagerCallback != null) {
                                    onBackUpManagerCallback.onRestoreError(context.getString(R.string.realmRestoreError));
                                }
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                exception.printStackTrace();
                if (exception.toString().contains("not exist")) {
                    if (onBackUpManagerCallback != null) {
                        onBackUpManagerCallback.onRestoreError(context.getString(R.string.noRestoreAvailable));
                    }
                } else {
                    if (onBackUpManagerCallback != null) {
                        onBackUpManagerCallback.onRestoreError(context.getString(R.string.realmRestoreError));
                    }
                }
            }
        });
    }

}
