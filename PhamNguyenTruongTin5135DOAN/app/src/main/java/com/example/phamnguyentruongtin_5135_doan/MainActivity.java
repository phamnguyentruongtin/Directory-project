package com.example.phamnguyentruongtin_5135_doan;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    private EditText input;
    private Button btnTranslateToEng;
    private Button btnTranslateToVN;
    private ListView listHistory;
    private ArrayAdapter<String> listHistoryApdapter;
    private List<String> listHistoryData;
    private final String DB_PATH ="/databases/";
    private SQLiteDatabase database = null;
    private String nameOfDatabase = "directory.db";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listHistory = findViewById(R.id.history);
        input = findViewById(R.id.input);
        btnTranslateToEng = findViewById(R.id.translate);
        btnTranslateToVN = findViewById(R.id.engToVN);
        processCopy();
        selectDatabase();
        btnTranslateToVN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(input.getText().toString())) {
                    Toast.makeText(MainActivity.this, "Not be empty !!!", Toast.LENGTH_SHORT).show();
                } else {

                    TranslatorOptions options = new TranslatorOptions.Builder()
                            .setTargetLanguage("vi")
                            .setSourceLanguage("en")
                            .build();
                    Translator translator = Translation.getClient(options);
                    String sourceText = input.getText().toString();
                    ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
                    progressDialog.setMessage("Downloading the translation model...");
                    progressDialog.setCancelable(false);
                    progressDialog.show();


                    translator.downloadModelIfNeeded().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            progressDialog.dismiss();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                        }
                    });
                    Task<String> result = translator.translate(sourceText).addOnSuccessListener(new OnSuccessListener<String>() {
                        @Override
                        public void onSuccess(String s) {
                            String first = input.getText().toString();
                            String second = s;
                            insertDatabase(first,second);
                            selectDatabase();
                            Intent intent = new Intent(MainActivity.this, ResultActivity.class);
                            intent.putExtra("result", s.toString());
                            startActivity(intent);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(MainActivity.this, e.getMessage().toString(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }

            }
        });
        btnTranslateToEng.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(input.getText().toString())) {
                    Toast.makeText(MainActivity.this, "Không được để trống !!!", Toast.LENGTH_SHORT).show();
                } else {

                    TranslatorOptions options = new TranslatorOptions.Builder()
                            .setTargetLanguage("en")
                            .setSourceLanguage("vi")
                            .build();
                    Translator translator = Translation.getClient(options);
                    String sourceText = input.getText().toString();
                    ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
                    progressDialog.setMessage("Downloading the translation model...");
                    progressDialog.setCancelable(false);
                    progressDialog.show();


                    translator.downloadModelIfNeeded().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            progressDialog.dismiss();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                        }
                    });
                    Task<String> result = translator.translate(sourceText).addOnSuccessListener(new OnSuccessListener<String>() {
                        @Override
                        public void onSuccess(String s) {
                            String first = input.getText().toString();
                            String second = s;
                            insertDatabase(first,second);
                            selectDatabase();
                            Intent intent = new Intent(MainActivity.this, ResultActivity.class);
                            intent.putExtra("result", s.toString());
                            startActivity(intent);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(MainActivity.this, e.getMessage().toString(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }
    private void insertDatabase(String input,String output){
        ContentValues myRecord = new ContentValues();
        myRecord.put("input",input);
        myRecord.put("output",output);
        String sms = "";
        if ( database.insert("directory",null,myRecord)==-1){
            sms = "EROR";
            Toast.makeText(MainActivity.this, sms, Toast.LENGTH_SHORT).show();
        }
    }

    private void selectDatabase(){
        listHistoryData =  new ArrayList<>();
        database = openOrCreateDatabase("directory.db", MODE_PRIVATE, null);
        Cursor c = database.query("directory",null,null,null,null,null,"id DESC",null);
        String data = "";
        c.moveToFirst();
        while (c.isAfterLast()==false){
            data = c.getString(1)+" - "+c.getString(2);
            listHistoryData.add(data);
            c.moveToNext();
        }
        listHistoryApdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, listHistoryData);
        listHistory.setAdapter(listHistoryApdapter);
        c.close();
        listHistoryApdapter.notifyDataSetChanged();
    }
    private void processCopy() {
        //private app
        File dbFile = getDatabasePath(nameOfDatabase);
        if (!dbFile.exists()) {
            try {
                CopyDataBaseFromAsset();
                Toast.makeText(this, "Copying sucess from Assets folder", Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
            }
        }

    }

    private String getDatabasePath() {
        return getApplicationInfo().dataDir + DB_PATH + nameOfDatabase;
    }

    public void CopyDataBaseFromAsset() {
        // TODO Auto-generated method stub
        try {
            InputStream myInput;
            myInput = getAssets().open(nameOfDatabase);
            String outFileName = getDatabasePath();
            File f = new File(getApplicationInfo().dataDir + DB_PATH);
            if (!f.exists())
                f.mkdir();
            OutputStream myOutput = new FileOutputStream(outFileName);
            int size = myInput.available();
            byte[] buffer = new byte[size];
            myInput.read(buffer);
            myOutput.write(buffer);
            myOutput.flush();
            myOutput.close();
            myInput.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}