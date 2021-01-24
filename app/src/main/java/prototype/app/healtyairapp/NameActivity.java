package prototype.app.healtyairapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.io.OutputStreamWriter;

public class NameActivity extends AppCompatActivity {
    Button goBlutoothButton;
    Button backToDisSelection;
    TextView disiaseViewText;
    EditText name;
    EditText surname;
    boolean[] tools;
    public String nameSurname = " ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_name);
        goBlutoothButton = findViewById(R.id.next_to_blutooth_activity);
        backToDisSelection = findViewById(R.id.back_to_dis_selection_activity);
        name = findViewById(R.id.name);
        surname = findViewById(R.id.surname);

        try{
            getDataFromDisiaseSelection();
        }catch(Exception e){
            System.out.println(""+e);
        }

        goBlutoothButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    nameSurname = "Mr/Mrs. " + surname.getText().toString() + " " + name.getText().toString() ;
                }
                catch (Exception e){
                    nameSurname = "Mr/Mrs. Sir";
                }
                String text = "" + surname.getText().toString() + "," + name.getText().toString();
                BluetoothActivity.nameSurname = "Mr/Mrs." + surname.getText().toString() + " " + name.getText().toString();
                writeToFile(text,NameActivity.this);
                Intent intentBlutooth = new Intent(NameActivity.this, BluetoothActivity.class);
                intentBlutooth.putExtra("tools",tools);
                intentBlutooth.putExtra("nameSurname", nameSurname);
                startActivity(intentBlutooth);
            }
        });

        backToDisSelection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentBackToDisSelection = new Intent(NameActivity.this,MainActivity.class);
                startActivity(intentBackToDisSelection);
            }
        });
    }

    private void getDataFromDisiaseSelection() {
        Intent intentName = getIntent();
        Bundle bundle = intentName.getExtras();
        tools = bundle.getBooleanArray("tools");
        disiaseViewText  = findViewById(R.id.disiaseView);

        //This text is using for check
        disiaseViewText.setText("Your disiase selections are saved.");
    }

    private void writeToFile(String data, Context context) {

        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("config.txt", Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }


}