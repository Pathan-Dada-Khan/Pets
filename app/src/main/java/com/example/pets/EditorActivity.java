package com.example.pets;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Loader;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pets.data.PetContract.PetEntry;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EXISTING_PET_LOADER = 0;

    private Uri mCurrentPetUri=null;

    private EditText mNameEditText;
    private EditText mBreedEditText;
    private EditText mWeightEditText;
    private TextInputLayout textInputLayout;
    private Uri imageUri=null;
    private ImageView image;
    private String petImage;
    private FloatingActionButton camera;

    private EditText mGenderSpinner;
    private String imageString="";

    private int mGender = PetEntry.GENDER_UNKNOWN;

    private boolean mPetHasChanged = false;

    private View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mPetHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Intent intent = getIntent();
        mCurrentPetUri = intent.getData();

        if(mCurrentPetUri == null){
            setTitle(getString(R.string.editor_activity_title_new_pet));
            invalidateOptionsMenu();
        } else {
            setTitle(getString(R.string.editor_activity_title_edit_pet));
            getLoaderManager().initLoader(EXISTING_PET_LOADER,null,this);
        }

        textInputLayout = (TextInputLayout)findViewById(R.id.nameInput);
        mNameEditText = (EditText)findViewById(R.id.edit_pet_name);
        mBreedEditText = (EditText)findViewById(R.id.edit_pet_breed);
        mWeightEditText = (EditText)findViewById(R.id.edit_pet_weight);
        mGenderSpinner = (EditText) findViewById(R.id.gender_spinner);
        mNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                textInputLayout.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        image = (ImageView)findViewById(R.id.image);
        camera = (FloatingActionButton)findViewById(R.id.camera);

        mNameEditText.setOnTouchListener(onTouchListener);
        mBreedEditText.setOnTouchListener(onTouchListener);
        mWeightEditText.setOnTouchListener(onTouchListener);
        camera.setOnTouchListener(onTouchListener);
        mGenderSpinner.setOnTouchListener(onTouchListener);

        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImagePicker.with(EditorActivity.this)
                        .cropSquare()
                        .start();
            }
        });
        mGenderSpinner.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus)setSpinner();
            }
        });
        mGenderSpinner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSpinner();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK){
            imageUri = data.getData();
            image.setImageURI(imageUri);
        } else {
            Toast(false,"Operation Cancelled");
        }
    }

    private void setSpinner(){
        AlertDialog.Builder builder = new AlertDialog.Builder(EditorActivity.this);
        builder.setTitle(R.string.category_gender);
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setSingleChoiceItems(R.array.array_gender_options, mGender, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mGender=which;
                switch (mGender){
                    case 0:
                        mGenderSpinner.setText(R.string.gender_unknown);
                        break;
                    case 1:
                        mGenderSpinner.setText(R.string.gender_male);
                        break;
                    case 2:
                        mGenderSpinner.setText(R.string.gender_female);
                        break;
                }
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void savePet(){

        String nameString = mNameEditText.getText().toString().trim();
        String breedString = mBreedEditText.getText().toString().trim();
        String weightString = mWeightEditText.getText().toString().trim();
        if(imageUri!=null){
            imageString = imageUri.toString();
        }

        if(mCurrentPetUri == null && TextUtils.isEmpty(nameString) && TextUtils.isEmpty(breedString) &&
                TextUtils.isEmpty(weightString) && mGender == PetEntry.GENDER_UNKNOWN && imageUri==null){
            finish();
            return;
        }

        ContentValues values = new ContentValues();
        values.put(PetEntry.COLUMN_PET_NAME,nameString);
        values.put(PetEntry.COLUMN_PET_BREED,breedString);
        values.put(PetEntry.COLUMN_PET_GENDER,mGender);
        values.put(PetEntry.COLUMN_PET_IMAGE,imageString);

        int weight = 0;
        if(!TextUtils.isEmpty(weightString)){
            weight = Integer.parseInt(weightString);
        }
        values.put(PetEntry.COLUMN_PET_WEIGHT,weight);

        if(nameString == null || nameString.isEmpty()){
            textInputLayout.setError("Enter pet name");
            return;
        }

        if(mCurrentPetUri == null){

            String presentDate = new SimpleDateFormat("yyyy/MM/dd\nHH:mm", Locale.getDefault()).format(new Date());

            values.put(PetEntry.COLUMN_PET_DATE,presentDate);

            Uri newUri = getContentResolver().insert(PetEntry.CONTENT_URI,values);
            if(newUri == null){
                Toast(false,getString(R.string.editor_inset_pet_failed));
            }else{
                Toast(true,getString(R.string.editor_inset_pet_successful));
            }
        } else {
            int rowsAffected = getContentResolver().update(mCurrentPetUri,values,null,null);
            if(rowsAffected == 0){
                Toast(false,getString(R.string.editor_update_pet_failed));
            } else {
                Toast(true,getString(R.string.editor_update_pet_successful));
            }
        }
        finish();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor,menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if(mCurrentPetUri == null){
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_save:
                savePet();
                return true;
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            case android.R.id.home:
                if(!mPetHasChanged){
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }
                DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    }
                };
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if(!mPetHasChanged) {
            super.onBackPressed();
            return;
        }

        DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        };
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                PetEntry._ID,
                PetEntry.COLUMN_PET_NAME,
                PetEntry.COLUMN_PET_BREED,
                PetEntry.COLUMN_PET_GENDER,
                PetEntry.COLUMN_PET_IMAGE,
                PetEntry.COLUMN_PET_WEIGHT
        };

        return new CursorLoader(this,mCurrentPetUri,projection,null,null,null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(data == null || data.getCount()<1){
            return;
        }
        if(data.moveToFirst()){
            int nameColumnIndex = data.getColumnIndex(PetEntry.COLUMN_PET_NAME);
            int breedColumnIndex = data.getColumnIndex(PetEntry.COLUMN_PET_BREED);
            int genderColumnIndex = data.getColumnIndex(PetEntry.COLUMN_PET_GENDER);
            int weightColumnIndex = data.getColumnIndex(PetEntry.COLUMN_PET_WEIGHT);
            int imageColumnIndex = data.getColumnIndex(PetEntry.COLUMN_PET_IMAGE);

            String name = data.getString(nameColumnIndex);
            String breed = data.getString(breedColumnIndex);
            petImage = data.getString(imageColumnIndex);
            int gender = data.getInt(genderColumnIndex);
            int weight = data.getInt(weightColumnIndex);

            mNameEditText.setText(name);
            mBreedEditText.setText(breed);
            mWeightEditText.setText(Integer.toString(weight));

            if(!petImage.isEmpty() && image!=null){
                image.setImageURI(Uri.parse(petImage));
                imageString = petImage;
            }
            switch(gender){
                case PetEntry.GENDER_MALE:
                    mGenderSpinner.setText(R.string.gender_male);
                    break;
                case PetEntry.GENDER_FEMALE:
                    mGenderSpinner.setText(R.string.gender_female);
                    break;
                default:
                    mGenderSpinner.setText(R.string.gender_unknown);
                    break;
            }
            mGender=gender;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNameEditText.setText("");
        mBreedEditText.setText("");
        mWeightEditText.setText("");
        mGenderSpinner.setSelection(0);
    }

    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard,discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(dialog!=null){
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deletePet();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(dialog!=null){
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deletePet(){
        if(mCurrentPetUri!=null){
            int rowsDeleted = getContentResolver().delete(mCurrentPetUri,null,null);
            if(rowsDeleted == 0){
                Toast(false,getString(R.string.editor_delete_pet_failed));
            }
            else{
                Toast(true,getString(R.string.editor_delete_pet_successful));
            }
        }
        finish();
    }

    public void Toast(boolean success,String text){
        Toast toast = new Toast(this);
        View view = getLayoutInflater().inflate(R.layout.custom_toast,null);
        ImageView imageView = (ImageView) view.findViewById(R.id.toastImage);
        if(success){
            view.setBackgroundResource(R.drawable.toast_success);
            imageView.setImageResource(R.drawable.ic_success);
        } else {
            view.setBackgroundResource(R.drawable.toast_error);
            imageView.setImageResource(R.drawable.ic_error);
        }
        TextView textView = (TextView) view.findViewById(R.id.toastText);
        textView.setText(text);
        toast.setView(view);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.show();
    }

}