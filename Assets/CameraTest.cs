using UnityEngine;
using System.Collections;
using UnityEngine.UI;
using UnityEngine.Events;
using System.IO;

public class CameraTest : MonoBehaviour {

	// Use this for initialization
	void Start () {

     
        RegisterButtonEvent(btn_Open, OnButton_OpenCamera);
        RegisterButtonEvent(btn_OpenAlbum, OnButton_Openalbum);
        Init();
    }

    public void RegisterButtonEvent(Button btn,UnityAction action)
    {
        btn.onClick.AddListener(action);
    }

	
	// Update is called once per frame
	void Update () {
	
	}

    public Text text_Log = null;
    public Text text_FilePath = null;

    public Button btn_Open = null;
    public Button btn_OpenAlbum = null;

    public Image img_Show = null;
    public RawImage rawImage_Show = null;
    private void OnButton_OpenCamera()
    {
#if UNITY_ANDROID
        text_Log.text = "OnButton_OpenCamera";
        if(null == cameraOpera)
        {
            text_Log.text = "cameraOpera is null";
        }
        cameraOpera.Call("openCamrea",true);
#endif
    }


    private void Init()
    {
#if UNITY_ANDROID
        AndroidJavaClass cls = new AndroidJavaClass("com.unity3d.player.UnityPlayer");
        ajo = cls.GetStatic<AndroidJavaObject>("currentActivity");
        cameraOpera = ajo.Call<AndroidJavaObject>("getCameraHander");
#endif
    }

    public void OnMessage(string str)
    {
        text_Log.text = "OnMessage";
#if UNITY_ANDROID
        string[] paramLs = str.Split(':');
        if(paramLs[0] == "Log")
        {
            text_Log.text = "<color=blue>" + paramLs[1] + "</color>";
        }
        else if(paramLs[0] == "CameraHandlerTake")
        {
            text_Log.text = paramLs[1];
            ShowPic(paramLs[1]);
        }
#endif
    }

    private void OnButton_Openalbum()
    {
        cameraOpera.Call("openAlbum",false);
    }

    public void ShowPic(string path)
    {
        //StartCoroutine(Load(path));
        LoadTexture(path);
    }

    public IEnumerator Load(string path)
    {
        string loadPath = "jar:file://" + path;
        text_FilePath.text = loadPath;
        WWW www = new WWW(loadPath);
        yield return www;
        if(www.isDone && null != www.error)
        {
            Texture2D img = www.texture;
            img_Show.sprite = Sprite.Create(img,new Rect(0, 0, img.width, img.height), new Vector2(0.5f, 0.5f));
            text_Log.text = string.Format( "Load Successed width:{0},height:{1}",img.width,img.height);
            rawImage_Show.texture = img;
        }
        else
        {
            text_Log.text = "Load Failed";
        }
    }
   
    public void LoadTexture(string path)
    {
        text_FilePath.text = path;
        text_Log.text = "LoadTexture Begin:"+path;
        Texture2D texture = ReadTexture(path);
        img_Show.sprite = Sprite.Create(texture, new Rect(0, 0, texture.width, texture.height), new Vector2(0.5f, 0.5f));
        rawImage_Show.texture = texture;

        Vector2 size = new Vector2(texture.width,texture.height);
        if(size.x>400)
        {
            size.y = 400 / size.x * size.y;
            size.x = 400;
        }
        rawImage_Show.rectTransform.sizeDelta = size;
        Debug.Log("Size:"+size);
        text_Log.text = string.Format("Load Successed width:{0},height:{1}", texture.width, texture.height);
    }


   private Texture2D ReadTexture(string path)
    {
        // 创建文件读取流
        FileStream fileStream = new FileStream(path, FileMode.Open, FileAccess.Read);
        fileStream.Seek(0, SeekOrigin.Begin);
        //创建文件长度缓冲区
        byte[] bytes = new byte[fileStream.Length];
        //读取文件
        fileStream.Read(bytes, 0, (int)fileStream.Length);
        //释放文件读取流
        fileStream.Close();
        fileStream.Dispose();
        fileStream = null;

        //创建Texture
        int width = 800;
        int height = 640;
        Texture2D texture = new Texture2D(width, height);
        texture.LoadImage(bytes);
        return texture;
    }

    public AndroidJavaObject ajo = null;
    public AndroidJavaObject cameraOpera = null;
}
