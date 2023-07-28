package com.amit07.real_timelanguagetranslation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {


    //UI views
    private EditText sourceLanguageEt;
    private TextView destinationLanguageTv;
    private AppCompatButton sourceLanguageChooseBtn;

    private AppCompatButton destinationLanguageChooseBtn;

    private Button translateBtn;

    private TranslatorOptions translatorOptions;

    private Translator translator;

    private ProgressDialog progressDialog;

    private ArrayList<ModelLanguage> languageArrayList;
    private static final String TAG = "MAIN_TAG";

    private String sourceLanguageCode = "en";
    private String sourceLanguageTitle = "English";
    private String destinationLanguageCode = "hi";
    private String destinationLanguageTitle = "Hindi";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //init UI views
        sourceLanguageEt = findViewById(R.id.sourceLanguageEt);
        destinationLanguageTv = findViewById(R.id.destinationLanguageTv);
        sourceLanguageChooseBtn = findViewById(R.id.sourceLanguageChooseBtn);
        destinationLanguageChooseBtn = findViewById(R.id.destinationLanguageChooseBtn);
        translateBtn = findViewById(R.id.translateBtn);



        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        loadAvailableLanguages();

// handle SourceLanguageChooseBtn click, choose source language (from list) which you want to translate.
        sourceLanguageChooseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sourceLanguageChoose();
            }
        });

        // handle destinationLanguageChooseBtn click, choose destination language (from list) to which you want to translate
        destinationLanguageChooseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                destinationLanguageChoose();
            }
        });

        // handle translateBtn click, translate text to desired language
        translateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateDate();
            }
        });
    }

    private String sourceLanguageText = "";
    private void validateDate() {

        sourceLanguageText = sourceLanguageEt.getText().toString().trim();

        Log.d(TAG, "validateDate: sourceLanguageText: "+ sourceLanguageText);

        //validate date if empty show error message, otherwise start translation
        if(sourceLanguageText.isEmpty()){
            Toast.makeText(this, "Enter text to translate...",Toast.LENGTH_SHORT).show();
        }
        else{
            startTranslation();
        }
    }

    private void startTranslation() {

        progressDialog.setMessage("Processing language model...");
        progressDialog.show();

        //init TranslatorOption with source and target language e.g. en and hi
        translatorOptions = new TranslatorOptions.Builder()
        .setSourceLanguage(sourceLanguageCode)
                .setTargetLanguage(destinationLanguageCode)
                .build();
        translator = Translation.getClient(translatorOptions);

        //init DownloadConditions with option to requireWIFI (Optional)
        DownloadConditions downloadConditions = new DownloadConditions.Builder()
                .requireWifi()
                .build();

        //start downloading translation model if required (will download 1st time)
        translator.downloadModelIfNeeded(downloadConditions)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        // translation model ready to be translated, lets translate

                        Log.d(TAG,"onSuccess: model ready, starting translate...");

                        progressDialog.setMessage("Translating...");

                        translator.translate(sourceLanguageText)
                                .addOnSuccessListener(new OnSuccessListener<String>() {
                                    @Override
                                    public void onSuccess(String translatedText) {
                                        //successfully translated

                                        Log.d(TAG, "onSuccess: translatedText: "+translatedText);
                                        progressDialog.dismiss();

                                        destinationLanguageTv.setText(translatedText);
                                    }
                                })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                //failed to translated
                                progressDialog.dismiss();
                                Log.d(TAG, "onFailure: ",e);
                                Toast.makeText(MainActivity.this, "Failed to translate due to "+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed to ready translation model, can't proceed to translation
                        progressDialog.dismiss();
                        Log.d(TAG, "onFailure: ",e);
                        Toast.makeText(MainActivity.this, "Failed to ready model due to "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void sourceLanguageChoose(){

        PopupMenu popupMenu = new PopupMenu(this, sourceLanguageChooseBtn);

        // from languageArrayList we will display language titles
        for(int i=0; i<languageArrayList.size(); i++){

            popupMenu.getMenu().add(Menu.NONE, i, i, languageArrayList.get(i).languageTitle);
        }

        // show popup menu
        popupMenu.show();

        // handle popup menu item click
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int position = item.getItemId();


                // get code and title of the language selected
                sourceLanguageCode = languageArrayList.get(position).languageCode;
                sourceLanguageTitle = languageArrayList.get(position).languageTitle;

                // set the selected language to sourceLanguageChooseBtn as text and sourceLanguageEt as hint
                sourceLanguageChooseBtn.setText(sourceLanguageTitle);
                sourceLanguageEt.setHint("Enter "+sourceLanguageTitle);

                //show in logs
                Log.d(TAG, "onMenuItemClick: sourceLanguageCode: "+sourceLanguageCode);
                Log.d(TAG, "onMenuItemClick: sourceLanguageTitle: "+sourceLanguageTitle);

                return false;
            }
        });
    }

    private void destinationLanguageChoose(){

        PopupMenu popupMenu = new PopupMenu(this, destinationLanguageChooseBtn);

        for(int i=0; i<languageArrayList.size(); i++){

            popupMenu.getMenu().add(Menu.NONE, i, i, languageArrayList.get(i).getLanguageTitle());
        }

        popupMenu.show();

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                int position = item.getItemId();

                destinationLanguageCode = languageArrayList.get(position).languageCode;
                destinationLanguageTitle = languageArrayList.get(position).languageTitle;

                destinationLanguageChooseBtn.setText(destinationLanguageTitle);

                Log.d(TAG,"onMenuItemClick: destinationLanguageCode: "+destinationLanguageCode);
                Log.d(TAG,"onMenuItemClick: destinationLanguageTitle: "+destinationLanguageTitle);
                return false;
            }
        });

    }


    private void loadAvailableLanguages() {

        languageArrayList = new ArrayList<>();

        List<String> languageCodeList = TranslateLanguage.getAllLanguages();

        for(String languageCode: languageCodeList){
            String languageTitle = new Locale(languageCode).getDisplayLanguage();  // e.g. en -> English
            Log.d(TAG,"loadAvailableLanguage: languageCode: "+languageCode);
            Log.d(TAG,"loadAvailableLanguage: languageTitle: "+languageTitle);

            ModelLanguage modelLanguage = new ModelLanguage(languageCode, languageTitle);
            languageArrayList.add(modelLanguage);
        }
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
        alertDialog.setTitle("Exit App");
        alertDialog.setMessage("Are you sure you want to exit app?");
        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finishAffinity();
            }
        });
        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog.show();
    }
}