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
	public static final int PHOTOHRAPH = 1<<0;// ����
	public static final int PHOTOALBUM = 1<<2;// �����
	public static final int PHOTOZOOM = 1<<3;// �ü�
	public static final int PHOTORESOULT = 1<<4;// ���
	
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
    	Log.i("TEST", "��ʼ�ü�" + uri);
    	
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, IMAGE_UNSPECIFIED);
        intent.putExtra("crop", "true");
        // aspectX aspectY �ǿ�ߵı���
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // outputX outputY �ǲü�ͼƬ���
        intent.putExtra("outputX", 240);
        intent.putExtra("outputY", 240);
        intent.putExtra("return-data", true);
        this.m_Activity.startActivityForResult(intent, PHOTORESOULT);
    }  
	
	private void SaveBitmap(Bitmap bitmap,int compressRatio) throws IOException {
    	Log.i("TEST", "�����ļ�");
        FileOutputStream fOut = null;
        //ע��
        String path = getProjectPath();
        try {
                fOut = new FileOutputStream(temp_PhotoPath) ;
                Log.i("TEST", "����·��:" + temp_PhotoPath);
                Log.i("TEST", "success");
        } catch (FileNotFoundException e) {
                e.printStackTrace();
        }
        //��Bitmap����д�뱾��·���У�Unity��ȥ��ͬ��·������ȡ����ļ�
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
	
	/* ͼƬѹ��*/
	
	private Bitmap CompressPicQuality(Bitmap bitmap,long quality) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();  
		bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);//����ѹ������������100��ʾ��ѹ������ѹ��������ݴ�ŵ�baos�� 
		 int options = 100;
		while ( baos.toByteArray().length >quality) {  //ѭ���ж����ѹ����ͼƬ�Ƿ����100kb,���ڼ���ѹ�� 
            baos.reset();//����baos�����baos  
            bitmap.compress(Bitmap.CompressFormat.JPEG, options, baos);//����ѹ��options%����ѹ��������ݴ�ŵ�baos��  
            options -= 10;//ÿ�ζ�����10
        }  
		ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//��ѹ���������baos��ŵ�ByteArrayInputStream��  
	    Bitmap newBitmap = BitmapFactory.decodeStream(isBm, null, null);//��ByteArrayInputStream��������ͼƬ  
	    return newBitmap;  
	}
	
	private Bitmap CompressPicSize(Bitmap bitmap,float hh,float ww) {
		ByteArrayOutputStream os = new ByteArrayOutputStream(); 
		bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);  
		ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());    
		BitmapFactory.Options newOpts = new BitmapFactory.Options();    
		//��ʼ����ͼƬ����ʱ��options.inJustDecodeBounds ���true��    
		newOpts.inJustDecodeBounds = true;  
		newOpts.inPreferredConfig = Config.RGB_565;  
		Bitmap newBitmap = BitmapFactory.decodeStream(is, null, newOpts);    
		newOpts.inJustDecodeBounds = false;    
		int w = newOpts.outWidth;    
		int h = newOpts.outHeight;    
//		float hh = 480f;// ���ø߶�Ϊ240fʱ���������Կ���ͼƬ��С��  
//		float ww = 480f;// ���ÿ��Ϊ120f���������Կ���ͼƬ��С��  
		//���űȡ������ǹ̶��������ţ�ֻ�ø߻��߿�����һ�����ݽ��м��㼴��    
		int be = 1;//be=1��ʾ������    
		if (w > h && w > ww) {//�����ȴ�Ļ����ݿ�ȹ̶���С����    
		    be = (int) (newOpts.outWidth / ww);    
		} else if (w < h && h > hh) {//����߶ȸߵĻ����ݿ�ȹ̶���С����    
		    be = (int) (newOpts.outHeight / hh);    
		}    
		if (be <= 0) be = 1;    
		newOpts.inSampleSize = be;//�������ű���    
		//���¶���ͼƬ��ע���ʱ�Ѿ���options.inJustDecodeBounds ���false��    
		is = new ByteArrayInputStream(os.toByteArray());    
		newBitmap = BitmapFactory.decodeStream(is, null, newOpts);  
		return newBitmap;
	}
	
	private Bitmap Compress(Bitmap image,long quality,float hh,float ww)
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();         
		image.compress(Bitmap.CompressFormat.JPEG, 100, baos);  
		if( baos.toByteArray().length / 1024>1024) {//�ж����ͼƬ����1M,����ѹ������������ͼƬ��BitmapFactory.decodeStream��ʱ���    
		    baos.reset();//����baos�����baos  
		    image.compress(Bitmap.CompressFormat.JPEG, 50, baos);//����ѹ��50%����ѹ��������ݴ�ŵ�baos��  
		}  
		ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());  
		BitmapFactory.Options newOpts = new BitmapFactory.Options();  
		//��ʼ����ͼƬ����ʱ��options.inJustDecodeBounds ���true��  
		newOpts.inJustDecodeBounds = true;  
		Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);  
		newOpts.inJustDecodeBounds = false;  
		int w = newOpts.outWidth;  
		int h = newOpts.outHeight;  
		int be = 1;//be=1��ʾ������  
		if (w > h && w > ww) {//�����ȴ�Ļ����ݿ�ȹ̶���С����  
		    be = (int) (newOpts.outWidth / ww);  
		} else if (w < h && h > hh) {//����߶ȸߵĻ����ݿ�ȹ̶���С����  
		    be = (int) (newOpts.outHeight / hh);  
		}  
		if (be <= 0)  
		    be = 1;  
		newOpts.inSampleSize = be;//�������ű���  
		//���¶���ͼƬ��ע���ʱ�Ѿ���options.inJustDecodeBounds ���false��  
		isBm = new ByteArrayInputStream(baos.toByteArray());  
		bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);  
		return CompressPicQuality(bitmap,quality);//ѹ���ñ�����С���ٽ�������ѹ��  
	}
}
