package com.first.birdsclassification;

import static android.content.ContentValues.TAG;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.first.birdsclassification.ml.BirdsModel;

import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.label.Category;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    Button btLoadImage,btCaptureImage;
    TextView tvResult;
    ImageView ivAddImage;
    ActivityResultLauncher<String> mGetContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ivAddImage=findViewById( R.id.iv_add_image );
        tvResult=findViewById( R.id.tv_result );
        btLoadImage=findViewById( R.id.bt_load_image );
        btCaptureImage=findViewById( R.id.bt_capture_image );

        //pick image from gallery and camera
        mGetContent=registerForActivityResult( new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
            @Override
            public void onActivityResult(Uri result) {
                Bitmap imageBitmap=null;
                try {
                    imageBitmap=UriToBitmap(result);
                }catch (IOException e){
                    e.printStackTrace();
                }
                //ivAddImage.setImageURI(result);
                ivAddImage.setImageBitmap(imageBitmap);
                outputGenerator(imageBitmap);

                Log.d( "TAG_URI","" +result );
            }
        } );
        btLoadImage.setOnClickListener( new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mGetContent.launch( "image/*" );
                    }
                } );
       /* btCaptureImage.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGetContent.launch( "image/*" );
            }
        } );*/
        tvResult.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(Intent.ACTION_VIEW,Uri.parse( "https://www.google.com/search?q="+tvResult.getText().toString() ));
                startActivity( intent );
            }
        } );

    }

    private void outputGenerator(Bitmap imageBitmap)
    {
        try {
            BirdsModel model = BirdsModel.newInstance(MainActivity.this);

            // Creates inputs for reference.
            TensorImage image = TensorImage.fromBitmap(imageBitmap);

            // Runs model inference and gets result.
            BirdsModel.Outputs outputs = model.process(image);
            List<Category> probability = outputs.getProbabilityAsCategoryList();

                int index=0;
                float max=probability.get( 0 ).getScore();

                for(int i=0;i<probability.size();i++){
                    if(max<probability.get( i ).getScore()){
                        max=probability.get( i ).getScore();
                        index=i;
                    }
                }

                Category output=probability.get( index );
                tvResult.setText( output.getLabel() );

            // Releases model resources if no longer used.
            model.close();
        } catch (IOException e) {
            // TODO Handle the exception
        }

    }

    private Bitmap UriToBitmap(Uri result) throws IOException {
        return MediaStore.Images.Media.getBitmap( this.getContentResolver(),result);
    }

}