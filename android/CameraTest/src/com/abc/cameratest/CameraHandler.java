package com.abc.cameratest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Logger;

import com.unity3d.player.UnityPlayer;

import android.R.bool;
import android.R.string;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;


public class CameraHandler {
	private static final int NONE = 0;
	public static final int PHOTOHRAPH = 1<<0;// 拍照
	public static final int PHOTOALBUM = 1<<2;// 打开相册
	public static final int PHOTOZOOM = 1<<3;// 裁剪
	public static final int PHOTORESOULT = 1<<4;// 结果
	
	public static final String IMAGE_UNSPECIFIED = "image/*";
	
	private UnityPlayerActivity m_Activity = new UnityPlayerActivity();
	private String temp_PhotoPath = null;
	private long m_CompressQuality = 1024 * 300;
	
	private int m_CompressSize_height = 480;
	private int m_CompressSize_Width = 480;
	
	public String getProjectPath() {
		return m_Activity.getExternalFilesDir();
	}
	
	public CameraHandler(UnityPlayerActivity activity)
	{
		m_Activity = activity;
		temp_PhotoPath = getProjectPath() + "/temp.png";
	}
	
	public void openCamrea(boolean bPhotoZoom)
	{
		UnityMessenger.SendMessage("Log:openCamrea");
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(temp_PhotoPath)));
        this.m_Activity.startActivityForResult(intent,bPhotoZoom ? PHOTOHRAPH|PHOTOZOOM :PHOTOHRAPH);
	}
	
	public void openAlbum(boolean bPhotoZoom) 
	{
		 Intent intent = new Intent(Intent.ACTION_PICK, null);
         intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_UNSPECIFIED);
         this.m_Activity.startActivityForResult(intent, bPhotoZoom ? PHOTOALBUM|PHOTOZOOM :PHOTOALBUM);
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if(resultCode == NONE)
		{
			return;
		}
		if((requestCode & PHOTOHRAPH) >0 )
		{
			boolean bPhotoZoom = (requestCode & PHOTOZOOM)>0;
			
			if(bPhotoZoom)
			{
				File picture = new File(temp_PhotoPath);
				startPhotoZoom(Uri.fromFile(picture));
			}
			else
			{
				UnityMessenger.SendMessage("CameraHandlerTake:"+temp_PhotoPath);
			}
			
		}
		else if ((requestCode & PHOTOALBUM) >0 ) 
		{
			System.out.println("Test");
			boolean bPhotoZoom = (requestCode & PHOTOZOOM)>0;
			if(bPhotoZoom){
				startPhotoZoom(data.getData());
			}else{
				SavePhoto(data.getData());
				UnityMessenger.SendMessage("CameraHandlerTake:"+temp_PhotoPath);//data.getData().getEncodedPath()
			}
		}
		else if (requestCode == PHOTORESOULT)
		{
			 SavePhoto(data);
			
			 UnityMessenger.SendMessage("CameraHandlerTake:"+temp_PhotoPath);
		}
	}

	private void SavePhoto(Uri uri) {
		try {
			Bitmap bitmap = BitmapFactory.decodeStream(m_Activity.getContentResolver().openInputStream(uri));
			Bitmap newbitmap = Compress(bitmap, m_CompressQuality, m_CompressSize_height, m_CompressSize_Width);
			SaveBitmap(newbitmap,100);
			UnityMessenger.SendMessage("CameraHandlerTake:"+temp_PhotoPath);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void SavePhoto(Intent data) 
	{
		if(null == data)
		{
			return;
		}
		   Bundle extras = data.getExtras();
           if (extras != null) 
           {  
	            Bitmap bitmap = extras.getParcelable("data");
	            try {
	            	Bitmap newBitmap = CompressPicQuality(bitmap,m_CompressQuality);
	            	SaveBitmap(newBitmap,100);
	                UnityMessenger.SendMessage("CameraHandlerTake:"+temp_PhotoPath);
	            } 
	            catch (IOException e) 
	            {                   
	            	// TODO Auto-generated catch block               
	            	e.printStackTrace();           
	            }
           }  
	}

	
	private void startPhotoZoom(Uri uri) {
    	Log.i("TEST", "开始裁剪" + uri);
    	
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, IMAGE_UNSPECIFIED);
        intent.putExtra("crop", "true");
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // outputX outputY 是裁剪图片宽高
        intent.putExtra("outputX", 240);
        intent.putExtra("outputY", 240);
        intent.putExtra("return-data", true);
        this.m_Activity.startActivityForResult(intent, PHOTORESOULT);
    }  
	
	private void SaveBitmap(Bitmap bitmap,int compressRatio) throws IOException {
    	Log.i("TEST", "保存文件");
        FileOutputStream fOut = null;
        //注解
        String path = getProjectPath();
        try {
                fOut = new FileOutputStream(temp_PhotoPath) ;
                Log.i("TEST", "保存路径:" + temp_PhotoPath);
                Log.i("TEST", "success");
        } catch (FileNotFoundException e) {
                e.printStackTrace();
        }
        //将Bitmap对象写入本地路径中，Unity在去相同的路径来读取这个文件
        bitmap.compress(Bitmap.CompressFormat.PNG, compressRatio, fOut);
        try {
                fOut.flush();
        } catch (IOException e) {
                e.printStackTrace();
        }
        try {
                fOut.close();
        } catch (IOException e) {
                e.printStackTrace();
        }
	}
	
	/* 图片压缩*/
	
	private Bitmap CompressPicQuality(Bitmap bitmap,long quality) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();  
		bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中 
		 int options = 100;
		while ( baos.toByteArray().length >quality) {  //循环判断如果压缩后图片是否大于100kb,大于继续压缩 
            baos.reset();//重置baos即清空baos  
            bitmap.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options%，把压缩后的数据存放到baos中  
            options -= 10;//每次都减少10
        }  
		ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//把压缩后的数据baos存放到ByteArrayInputStream中  
	    Bitmap newBitmap = BitmapFactory.decodeStream(isBm, null, null);//把ByteArrayInputStream数据生成图片  
	    return newBitmap;  
	}
	
	private Bitmap CompressPicSize(Bitmap bitmap,float hh,float ww) {
		ByteArrayOutputStream os = new ByteArrayOutputStream(); 
		bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);  
		ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());    
		BitmapFactory.Options newOpts = new BitmapFactory.Options();    
		//开始读入图片，此时把options.inJustDecodeBounds 设回true了    
		newOpts.inJustDecodeBounds = true;  
		newOpts.inPreferredConfig = Config.RGB_565;  
		Bitmap newBitmap = BitmapFactory.decodeStream(is, null, newOpts);    
		newOpts.inJustDecodeBounds = false;    
		int w = newOpts.outWidth;    
		int h = newOpts.outHeight;    
//		float hh = 480f;// 设置高度为240f时，可以明显看到图片缩小了  
//		float ww = 480f;// 设置宽度为120f，可以明显看到图片缩小了  
		//缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可    
		int be = 1;//be=1表示不缩放    
		if (w > h && w > ww) {//如果宽度大的话根据宽度固定大小缩放    
		    be = (int) (newOpts.outWidth / ww);    
		} else if (w < h && h > hh) {//如果高度高的话根据宽度固定大小缩放    
		    be = (int) (newOpts.outHeight / hh);    
		}    
		if (be <= 0) be = 1;    
		newOpts.inSampleSize = be;//设置缩放比例    
		//重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了    
		is = new ByteArrayInputStream(os.toByteArray());    
		newBitmap = BitmapFactory.decodeStream(is, null, newOpts);  
		return newBitmap;
	}
	
	private Bitmap Compress(Bitmap image,long quality,float hh,float ww)
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();         
		image.compress(Bitmap.CompressFormat.JPEG, 100, baos);  
		if( baos.toByteArray().length / 1024>1024) {//判断如果图片大于1M,进行压缩避免在生成图片（BitmapFactory.decodeStream）时溢出    
		    baos.reset();//重置baos即清空baos  
		    image.compress(Bitmap.CompressFormat.JPEG, 50, baos);//这里压缩50%，把压缩后的数据存放到baos中  
		}  
		ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());  
		BitmapFactory.Options newOpts = new BitmapFactory.Options();  
		//开始读入图片，此时把options.inJustDecodeBounds 设回true了  
		newOpts.inJustDecodeBounds = true;  
		Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);  
		newOpts.inJustDecodeBounds = false;  
		int w = newOpts.outWidth;  
		int h = newOpts.outHeight;  
		int be = 1;//be=1表示不缩放  
		if (w > h && w > ww) {//如果宽度大的话根据宽度固定大小缩放  
		    be = (int) (newOpts.outWidth / ww);  
		} else if (w < h && h > hh) {//如果高度高的话根据宽度固定大小缩放  
		    be = (int) (newOpts.outHeight / hh);  
		}  
		if (be <= 0)  
		    be = 1;  
		newOpts.inSampleSize = be;//设置缩放比例  
		//重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了  
		isBm = new ByteArrayInputStream(baos.toByteArray());  
		bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);  
		return CompressPicQuality(bitmap,quality);//压缩好比例大小后再进行质量压缩  
	}
}
