package com.example.pets.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.widget.Toast;

import com.example.pets.data.PetContract.PetEntry;


public class PetProvider extends ContentProvider {

    private static final int PETS = 100;
    private static final int PETS_ID = 101;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY,PetContract.PATH_PETS,PETS);
        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY,PetContract.PATH_PETS+"/#",PETS_ID);
    }

    private PetDbHelper mDbHelper;

    @Override
    public boolean onCreate() {

        mDbHelper = new PetDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query( Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        Cursor cursor = null;

        int match = sUriMatcher.match(uri);

        switch (match){
            case PETS:
                cursor = db.query(PetEntry.TABLE_NAME,projection,selection,selectionArgs,null,null,sortOrder);
            break;
            case PETS_ID:
                selection = PetEntry._ID+"=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = db.query(PetEntry.TABLE_NAME,projection,selection,selectionArgs,null,null,sortOrder);
            break;
        }

        cursor.setNotificationUri(getContext().getContentResolver(),uri);

        return cursor;
    }

    @Override
    public Uri insert( Uri uri, ContentValues values) {
        if(values == null){
            getContext().getContentResolver().notifyChange(uri,null);
            return null;
        }
        return insertPet(uri,values);
    }

    private Uri insertPet(Uri uri , ContentValues values){

        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        long id = db.insert(PetEntry.TABLE_NAME,null,values);

        getContext().getContentResolver().notifyChange(uri,null);

        return ContentUris.withAppendedId(uri,id);
    }


    @Override
    public int update( Uri uri,  ContentValues values,  String selection,  String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch(match){
            case PETS :
                return updatePet(uri,values,selection,selectionArgs);
            case PETS_ID:
                selection = PetEntry._ID+"=?";
                selectionArgs = new String []{String.valueOf(ContentUris.parseId(uri))};
                return updatePet(uri,values,selection,selectionArgs);
        }
        return 0;
    }

    private int updatePet(Uri uri,ContentValues values,String selection ,String[] selectionArgs){

        if(values.size()==0){
            return 0;
        }

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int rowUpdated = db.update(PetEntry.TABLE_NAME,values,selection,selectionArgs);

        if(rowUpdated!=0){
            getContext().getContentResolver().notifyChange(uri,null);
        }
        return rowUpdated;
    }

    @Override
    public int delete( Uri uri, String selection, String[] selectionArgs) {
         SQLiteDatabase db = mDbHelper.getWritableDatabase();
         int rowsDeleted=0;
         final int match = sUriMatcher.match(uri);
         switch (match){
             case PETS:
                 rowsDeleted = db.delete(PetEntry.TABLE_NAME,selection,selectionArgs);
                 break;
             case PETS_ID:
                 selection = PetEntry._ID + "=?";
                 selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                 rowsDeleted = db.delete(PetEntry.TABLE_NAME,selection,selectionArgs);
                 break;
         }

         if(rowsDeleted!=0){
             getContext().getContentResolver().notifyChange(uri,null);
         }
         return rowsDeleted;
    }

    @Override
    public String getType( Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match){
            case PETS:
                return PetEntry.CONTENT_LIST_TYPE;
            case PETS_ID:
                return PetEntry.CONTENT_ITEM_TYPE;
        }
        return null;
    }
}
