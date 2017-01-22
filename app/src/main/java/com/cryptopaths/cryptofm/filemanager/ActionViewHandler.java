package com.cryptopaths.cryptofm.filemanager;

import android.app.Dialog;
import android.content.Context;
import android.view.ActionMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.cryptopaths.cryptofm.R;
import com.cryptopaths.cryptofm.filemanager.listview.FileSelectionManagement;
import com.cryptopaths.cryptofm.filemanager.ui.FileBrowserActivity;


/**
 * Created by tripleheader on 1/13/17.
 * Action mode callbacks
 */

public class ActionViewHandler implements ActionMode.Callback {
    private Context mContext;

    public ActionViewHandler(Context context){
        this.mContext=context;
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        Log.d("actionMode", "onCreateActionMode: created action mode");
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.file_select_options,menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        final FileSelectionManagement manager=SharedData.getInstance().getCurrentFragment().getmManager();

        final TaskHandler mTaskHandler = SharedData.getInstance().getTaskHandler(mContext);
        if (item.getItemId()==R.id.rename_menu_item){
            mTaskHandler.renameFile();
        }
        else if(item.getItemId()==R.id.delete_menu_item){
            mTaskHandler.deleteFile();
        }
        else if(item.getItemId()==R.id.encrypt_menu_item){
            mTaskHandler.encryptTask(manager.getmSelectedFilePaths());
        }
        else if(item.getItemId()==R.id.decrypt_menu_item){
            if(SharedData.KEY_PASSWORD==null) {
                final Dialog dialog = new Dialog(mContext);
                dialog.setCancelable(false);
                dialog.setContentView(R.layout.password_dialog_layout);
                dialog.show();
                dialog.findViewById(R.id.cancel_decrypt_button).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
                final EditText editText = (EditText) dialog.findViewById(R.id.key_password);
                dialog.findViewById(R.id.decrypt_file_button).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (editText.getText().length() < 1) {
                            editText.setError("please give me your encryption password");
                            return;
                        } else {
                            SharedData.KEY_PASSWORD = editText.getText().toString();
                            dialog.dismiss();
                            mTaskHandler.decryptFile(
                                    SharedData.USERNAME,
                                    SharedData.KEY_PASSWORD,
                                    SharedData.DB_PASSWWORD,
                                    manager.getmSelectedFilePaths()
                                    );
                        }
                    }
                });
            }else{
                mTaskHandler.decryptFile(
                        SharedData.USERNAME,
                        SharedData.KEY_PASSWORD,
                        SharedData.DB_PASSWWORD,
                        manager.getmSelectedFilePaths()
                );
            }
        }
        else if(item.getItemId()==R.id.selectall_menu_item){
            manager.selectAllFiles();
        }
        else if(item.getItemId()==R.id.move_menu_item){
            Log.d("move", "onActionItemClicked: moving files");
            ((FileBrowserActivity)mContext).showCopyDialog();
        }
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        final FileSelectionManagement manager=SharedData.getInstance().getCurrentFragment().getmManager();
        Log.d("action","destroying action mode");
        SharedData.SELECT_COUNT=0;
        SharedData.SELECTION_MODE=false;
        manager.resetFileIcons();
        manager.setmSelectionMode(false);
    }

    /**
     * end of action mode section
     */
}
