package learner.sandman.nosetrackingdemo2;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.TextView;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

	int noOfCLicks=0;
	boolean smiling=false;

	//vars related to view
	TextView coOrdTextView,smileTextView;
	CameraBridgeViewBase cameraBridgeViewBase;
	//vars related to file1 opening
	InputStream is;
	File cascadeDir,mCascadeFile;
	FileOutputStream os;
	byte[]buffer;
	int bytesRead;
	CascadeClassifier haarCascade;
	//vars related to file2 opening
	InputStream is2;
	File cascadeDir2,mCascadeFile2;
	FileOutputStream os2;
	byte[]buffer2;
	int bytesRead2;
	CascadeClassifier haarCascade2;




	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		if(OpenCVLoader.initDebug()){
			Log.d("TAG1","OpenCV started successfully");
		}

		//Setting up the text view
		coOrdTextView=findViewById(R.id.co_ords_textView);
		smileTextView=findViewById(R.id.smileTextView);

		//OPENING THE HAAR CASCADE FILES
		bringInTheCascadeFile();
		bringInTheCascadeFile2();



		//BRINGING IN FRAMES

		cameraBridgeViewBase=findViewById(R.id.javaCameraViewID);
		cameraBridgeViewBase.setVisibility(SurfaceView.VISIBLE);
		cameraBridgeViewBase.setCvCameraViewListener(this);
		cameraBridgeViewBase.enableView();




	}


	@Override
	public void onCameraViewStarted(int width, int height) {

	}

	@Override
	public void onCameraViewStopped() {

	}

	@Override
	public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
		//Log.d("TAG1","i am being caled");

		Mat mRgba=inputFrame.rgba();
		Mat mGray=inputFrame.gray();
		Core.flip(mRgba,mRgba,1);
		Core.flip(mGray,mGray,1);
		//This is the code for face detection
		MatOfRect faces=new MatOfRect();

		if(haarCascade != null)
		{
			haarCascade.detectMultiScale(mGray, faces, 1.1, 2,2, new Size(230,230), new Size());
		}

		Rect[]facesArray = faces.toArray();

		for (int i = 0; i < facesArray.length; i++) {
			Imgproc.rectangle(mRgba, facesArray[i].tl(),facesArray[i].br(), new Scalar(100), 3);
			int x=facesArray[i].x;
			int y=facesArray[i].y;
			coOrdTextView.setText("Co-ords:\nx="+x+"\ny="+y);

		}
		//This is the code for face detection


		//this code is for teeth detection

		MatOfRect smile=new MatOfRect();
		if(haarCascade2!=null){
			Log.d("TAG1","Not null");
			haarCascade2.detectMultiScale(mGray, smile, 1.9, 8,2, new Size(120,120), new Size());
		}
		Rect[] smileArray=smile.toArray();
		Log.d("TAG1","Smile array length="+smileArray.length);
		//***************SMILE CLICK MECHANISM************
		if(smiling==false && smileArray.length!=0 && facesArray.length!=0){
			noOfCLicks++;
			smileTextView.setText("Clicking");
			smiling=true;
		}else if(smiling ==true && smileArray.length==0){//if equal to zero
			smileTextView.setText("Not Clicking");
			smiling=false;

		}
		//***************SMILE CLICK MECHANISM************

		for (int i = 0; i < smileArray.length; i++) {
			if(facesArray.length!=0)
				Imgproc.rectangle(mRgba, smileArray[i].tl(),smileArray[i].br(), new Scalar(100), 3);
		}



		//this code is for teeth detection


		Log.d("TAG2","rows="+mRgba.rows()+"cols="+mRgba.cols());


		//testing codes
		/*byte[] d={(byte) 255};
		Mat kernel= new Mat(3,3,CvType.CV_16SC1);
		kernel.put(0, 0	,	 -1, -1, -1,
							 -1, 8, -1,
							 -1, -1, -1);
		Mat kernel2= new Mat(3,3,CvType.CV_16SC1);
		kernel2.put(0, 0	,	 0, -1, 0,
								-1, 5, -1,
								0, -1, 0);


		Imgproc.filter2D(mGray,mGray,mGray.depth(),kernel);
		Imgproc.filter2D(mGray,mGray,mGray.depth(),kernel);
		for(  int r=mGray.rows()*1/4  ;   r<mGray.rows()*3/4   ;r++   ){
			Log.d("TAG2","depth1="+mGray.depth());
			Log.d("TAG2","depth2="+mRgba.depth());
			mGray.put(r,mGray.cols()/2,d);



		}*/

		return mRgba;

		/*Mat mat=inputFrame.rgba();
		Core.flip(mat,mat,1);
		return mat;*/
	}

	void bringInTheCascadeFile2(){
		is2=getResources().openRawResource(R.raw.haarcascade_smile);
		cascadeDir2=getDir("cascade", Context.MODE_PRIVATE);
		mCascadeFile2=new File(cascadeDir2,"cascade1.xml");
		try {
			os2=new FileOutputStream(mCascadeFile2);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		buffer2=new byte[4096];
		while(true){
			try {
				if (!((bytesRead2=is2.read(buffer2))!=-1)) break;
				os2.write(buffer2,0,bytesRead2);

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			is2.close();
			os2.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		haarCascade2=new CascadeClassifier(mCascadeFile2.getAbsolutePath());
		if(haarCascade2.empty()==false){

			Log.d("TAG1","Successfully opened haar cascade2 file");
		}


	}
	void bringInTheCascadeFile(){
		is=getResources().openRawResource(R.raw.haarcascade_frontalface_alt);
		cascadeDir=getDir("cascade", Context.MODE_PRIVATE);
		mCascadeFile=new File(cascadeDir,"cascade1.xml");
		try {
			os=new FileOutputStream(mCascadeFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		buffer=new byte[4096];
		while(true){
			try {
				if (!((bytesRead=is.read(buffer))!=-1)) break;
				os.write(buffer,0,bytesRead);

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			is.close();
			os.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		haarCascade=new CascadeClassifier(mCascadeFile.getAbsolutePath());
		if(haarCascade.empty()==false){
			Log.d("TAG1","Successfully opened haar cascade file");
		}

	}

}
