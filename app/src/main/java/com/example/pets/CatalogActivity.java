package com.example.pets;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Loader;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.example.pets.data.PetContract.PetEntry;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int PET_LOADER = 0;
    private static int ORDER_BY = 0;

    public static String orderBy = null;

    PetCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        FloatingActionButton fab = (FloatingActionButton)findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent =new Intent(CatalogActivity.this,EditorActivity.class);
                startActivity(intent);
            }
        });
        ListView petListView = (ListView)findViewById(R.id.petsList);
        View emptyView = findViewById(R.id.empty_view);
        petListView.setEmptyView(emptyView);

        mCursorAdapter = new PetCursorAdapter(this, null);
        petListView.setAdapter(mCursorAdapter);

        petListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(CatalogActivity.this,EditorActivity.class);
                Uri currentPetUri = ContentUris.withAppendedId(PetEntry.CONTENT_URI,id);
                intent.setData(currentPetUri);
                startActivity(intent);
            }
        });
        getLoaderManager().initLoader(PET_LOADER,null,this);
    }

    private void insertDummyData() {

        String presentDate = new SimpleDateFormat("yyyy/MM/dd\nHH:mm", Locale.getDefault()).format(new Date());

        ContentValues values = new ContentValues();

        values.put(PetEntry.COLUMN_PET_IMAGE,"");
        values.put(PetEntry.COLUMN_PET_NAME,"Toto");
        values.put(PetEntry.COLUMN_PET_BREED,"Terrier");
        values.put(PetEntry.COLUMN_PET_GENDER,PetEntry.GENDER_MALE);
        values.put(PetEntry.COLUMN_PET_WEIGHT,7);
        values.put(PetEntry.COLUMN_PET_DATE,presentDate);
    }

    private void deleteAllPets(){
        int rowsDeleted = getContentResolver().delete(PetEntry.CONTENT_URI,null,null);
        if(rowsDeleted>0){
            toast(1,"All Pets Deleted");
        } else{
            toast(-1,"Error!");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_catalog,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_insert_dummy_data:
                insertDummyData();
                return true;
            case R.id.action_delete_all_entries:
                showDeleteConfirmationDialog();
                return true;
            case R.id.action_sort_by:
                sort();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void sort(){
        String[] orderArray = new String[]{"Oldest","Latest","Name"};
        AlertDialog.Builder order = new AlertDialog.Builder(this);
        order.setTitle(R.string.action_sort);
        order.setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        order.setSingleChoiceItems(orderArray, ORDER_BY, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String[] projection = {
                        PetEntry._ID,
                        PetEntry.COLUMN_PET_NAME,
                        PetEntry.COLUMN_PET_BREED,
                        PetEntry.COLUMN_PET_IMAGE,
                        PetEntry.COLUMN_PET_DATE,
                };
                switch (which) {
                    case 0:
                        ORDER_BY =0 ;
                        orderBy = null;
                        mCursorAdapter.swapCursor(getContentResolver()
                                .query(PetEntry.CONTENT_URI,projection,null,null,orderBy));
                        break;
                    case 1:
                        ORDER_BY = 1;
                        orderBy = PetEntry.COLUMN_PET_DATE + " DESC ";
                        mCursorAdapter.swapCursor(getContentResolver()
                                .query(PetEntry.CONTENT_URI,projection,null,null,orderBy));
                        break;
                    case 2:
                        ORDER_BY = 2;
                        orderBy = PetEntry.COLUMN_PET_NAME + " ASC ";
                        mCursorAdapter.swapCursor(getContentResolver()
                                .query(PetEntry.CONTENT_URI,projection,null,null,orderBy));
                        break;
                }
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog = order.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog() {
        if(mCursorAdapter.getCount()==0){
            toast(0,"No Pets to Delete");
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_all_pets_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteAllPets();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String[] projection = {
                PetEntry._ID,
                PetEntry.COLUMN_PET_NAME,
                PetEntry.COLUMN_PET_BREED,
                PetEntry.COLUMN_PET_IMAGE,
                PetEntry.COLUMN_PET_DATE,
        };

        return new CursorLoader(this,PetEntry.CONTENT_URI,projection,null,null,orderBy);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader,Cursor data) {
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }

    public void toast(int value,String message){
        Toast toast = new Toast(this);
        View view = getLayoutInflater().inflate(R.layout.custom_toast,null);
        ImageView imageView = (ImageView) view.findViewById(R.id.toastImage);
        TextView textView = (TextView) view.findViewById(R.id.toastText);
        if(value==-1){
            view.setBackgroundResource(R.drawable.toast_error);
            imageView.setImageResource(R.drawable.ic_error);
        } else if(value==1){
            view.setBackgroundResource(R.drawable.toast_success);
            imageView.setImageResource(R.drawable.ic_success);
        } else {
            view.setBackgroundResource(R.drawable.toast_warning);
            imageView.setImageResource(R.drawable.ic_warning);
        }
        textView.setText(message);
        toast.setView(view);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.show();
    }

}