package com.example.pets;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.pets.data.PetContract.PetEntry;

public class PetCursorAdapter extends CursorAdapter {
    public PetCursorAdapter(Context context, Cursor c) {
        super(context, c,0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item,parent,false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView name = (TextView) view.findViewById(R.id.pet_name);
        TextView breed = (TextView) view.findViewById(R.id.pet_breed);
        ImageView image = (ImageView) view.findViewById(R.id.image);
        TextView date = (TextView) view.findViewById(R.id.date);

        Integer nameColumn = cursor.getColumnIndex(PetEntry.COLUMN_PET_NAME);
        Integer breedColumn = cursor.getColumnIndex(PetEntry.COLUMN_PET_BREED);
        Integer imageColumn = cursor.getColumnIndex(PetEntry.COLUMN_PET_IMAGE);
        Integer dateColumn = cursor.getColumnIndex(PetEntry.COLUMN_PET_DATE);

        String petName = cursor.getString(nameColumn);
        String petBreed = cursor.getString(breedColumn);
        String petDate = cursor.getString(dateColumn);
        String petImage = cursor.getString(imageColumn);


        if(TextUtils.isEmpty(petBreed)){
            petBreed = context.getString(R.string.unknown_breed);
        }

        name.setText(petName);
        breed.setText(petBreed);
        date.setText(petDate);
        if(petImage.isEmpty() || petImage==null){
            image.setImageResource(R.drawable.pet_template);
        }
        else{
            image.setImageURI(Uri.parse(petImage));
        }
    }
}
