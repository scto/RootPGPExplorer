package com.cryptopaths.cryptofm.filemanager;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.cryptopaths.cryptofm.R;
import com.cryptopaths.cryptofm.utils.FileUtils;

import java.util.ArrayList;

/**
 * Created by tripleheader on 1/21/17.
 * manage the selection of files
 * opening the files etc
 */

class FileSelectionManagement {
    private DataModelFiles      mDataModel;
    private FileListAdapter     mFileListAdapter;
    private Context             mContext;
    private Drawable            mSelectedFileIcon;
    private Drawable            mFileIcon;
    private Drawable            mFolderIcon;
    private AdapterCallbacks clickCallBack;

    private static final String TAG="FileSelectionManagement";


    private ArrayList<Integer>      mSelectedPosition   = new ArrayList<>();
    private ArrayList<String>       mSelectedFilePaths  = new ArrayList<>();

    FileSelectionManagement(Context context){
        this.mContext     = context;
        mSelectedFileIcon = mContext.getDrawable(R.drawable.ic_check_circle_white_48dp);
        mFileIcon         = mContext.getDrawable(R.drawable.ic_insert_drive_file_white_48dp);
        mFolderIcon       = mContext.getDrawable(R.drawable.ic_folder_white_48dp);
        clickCallBack = (AdapterCallbacks)mContext;

        mFileListAdapter=SharedData.getInstance().getFileListAdapter(mContext);
    }

    void selectAllFiles() {
        for (int i = 0; i < FileFillerWrapper.getTotalFilesCount(); i++) {
            mDataModel = FileFillerWrapper.getFileAtPosition(i);
            selectFile(i);
        }
        mFileListAdapter.notifyDataSetChanged();
    }


    void selectionOperation(int position){
        mDataModel  = FileFillerWrapper.getFileAtPosition(position);

        if(mDataModel.getSelected()){
            Log.d(TAG, "selectionOperation: fixing a bug in files selection");
            mSelectedFilePaths.remove(mDataModel.getFilePath());
            mDataModel.setSelected(false);
            clickCallBack.decrementSelectionCount();
            if(mDataModel.getFile()){
                mDataModel.setFileIcon(mFileIcon);
            }else{
                mDataModel.setFileIcon(mFolderIcon);
            }
        }else{
            selectFile(position);
        }

        mFileListAdapter.notifyItemChanged(position);

    }
    private void selectFile(int position){
        if(!mDataModel.getSelected()) {
            mSelectedPosition.add(position);
            mSelectedFilePaths.add(mDataModel.getFilePath());
            mDataModel.setFileIcon(mSelectedFileIcon);
            mDataModel.setSelected(true);
            clickCallBack.incrementSelectionCount();
        }
    }
    void selectFileInSelectionMode(String filename){
        clickCallBack.setResult(filename);
    }


    void setmSelectionMode(Boolean value){
        if(value){
            return;
        }
        //first check if there are select files
        if(mSelectedPosition.size()>0) {
            for (Integer pos : mSelectedPosition) {
                mDataModel =  FileFillerWrapper.getFileAtPosition(pos);
                mDataModel.setSelected(false);
            }
            mSelectedPosition.clear();
            mSelectedFilePaths.clear();
        }
    }

    void resetFileIcons(){
        for (Integer pos:
                mSelectedPosition) {
            mDataModel = FileFillerWrapper.getFileAtPosition(pos);
            if(mDataModel.getFile()){
                mDataModel.setFileIcon(mFileIcon);
            }else{
                mDataModel.setFileIcon(mFolderIcon);
            }
            mFileListAdapter.notifyItemChanged(pos);

        }
    }

    ArrayList<String> getmSelectedFilePaths() {
        return mSelectedFilePaths;
    }


    void openFile(String filename){
        if(FileUtils.getExtension(filename).equals("pgp")){
            //TODO, gonna do my assignment for now
        }
        //open file
        String mimeType=
                MimeTypeMap.getSingleton().
                        getMimeTypeFromExtension(
                                FileUtils.getExtension(filename
                                )
                        );

        Intent intent=new Intent();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri uri = FileProvider.getUriForFile(
                    mContext,
                    mContext.getApplicationContext().getPackageName()+".provider",
                    FileUtils.getFile(filename)
            );
            intent.setDataAndType(uri,mimeType);
        }else {
            intent.setDataAndType(Uri.fromFile(FileUtils.getFile(filename)),mimeType);
        }
        intent.setAction(Intent.ACTION_VIEW);
        Intent x=Intent.createChooser(intent,"Open with: ");
        mContext.startActivity(x);
    }

    void openFolder(String filename) {
        String folderPath = FileFillerWrapper.getCurrentPath() + filename + "/";
        clickCallBack.changeTitle(folderPath);
        FileFillerWrapper.fillData(folderPath, mContext);
        mFileListAdapter.notifyDataSetChanged();
        if (FileFillerWrapper.getTotalFilesCount() < 1) {
            ((FileBrowserActivity) mContext).showNoFilesFragment();
        }
    }
    void startSelectionMode(){
        SharedData.SELECTION_MODE = true;
        clickCallBack.onLongClick();
    }
}
