package com.hearthstonegeek.webview;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebSettings.RenderPriority;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.SendMessageToWX;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.mm.sdk.openapi.WXMediaMessage;
import com.tencent.mm.sdk.openapi.WXTextObject;

public class MainActivity extends Activity implements View.OnClickListener {

	protected WebView webView;
	private Context context;
	private SoundPlay sp;

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.replay:
			webView.loadUrl("javascript:function()");
			break;
		case R.id.ranking:
			break;
		case R.id.share:
			Toast.makeText(context, "share", Toast.LENGTH_SHORT).show();
			share();
			break;
		case R.id.guide:
			if (guideDialog == null) {
				StringBuffer sb = new StringBuffer();
				try {
					InputStream is = getAssets().open("help.txt");
					BufferedReader br = new BufferedReader(new InputStreamReader(is), 4096);
					String line = null;
					while ((line = br.readLine()) != null) {
						sb.append(line).append("\r\n");
					}
					is.close();
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				guideDialog = new AlertDialog.Builder(this).setTitle("极限2048高分技巧").setMessage(sb.toString()).setNegativeButton("确定", null).create();
			}
			guideDialog.show();
			break;
		}
	}

	private static final int THUMB_SIZE = 150;

	private void share() {
		String text = "分享测试";

		// 初始化一个WXTextObject对象
		WXTextObject textObj = new WXTextObject();
		textObj.text = text;

		// 用WXTextObject对象初始化一个WXMediaMessage对象
		WXMediaMessage msg = new WXMediaMessage();
		msg.mediaObject = textObj;
		// 发送文本类型的消息时，title字段不起作用
		// msg.title = "Will be ignored";
		msg.description = text;

		// 构造一个Req
		SendMessageToWX.Req req = new SendMessageToWX.Req();
		req.transaction = buildTransaction("text"); // transaction字段用于唯一标识一个请求
		req.message = msg;
		req.scene = SendMessageToWX.Req.WXSceneTimeline;

		// 调用api接口发送数据到微信
		api.sendReq(req);

		// webView.buildDrawingCache();
		// Bitmap bmp = webView.getDrawingCache();
		//
		// WXImageObject imgObj = new WXImageObject(bmp);
		//
		// WXMediaMessage msg = new WXMediaMessage();
		// msg.mediaObject = imgObj;
		//
		// Bitmap thumbBmp = Bitmap.createScaledBitmap(bmp, THUMB_SIZE,
		// THUMB_SIZE, true);
		// bmp.recycle();
		// msg.thumbData = Util.bmpToByteArray(thumbBmp, true); // 设置缩略图
		//
		// SendMessageToWX.Req req = new SendMessageToWX.Req();
		// req.transaction = buildTransaction("img");
		// req.message = msg;
		// req.scene = SendMessageToWX.Req.WXSceneTimeline;
		// api.sendReq(req);

	}

	private String buildTransaction(final String type) {
		return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
	}

	AlertDialog guideDialog;

	public static Bitmap refBitmap(Context context, String name) {
		int resID = context.getResources().getIdentifier(name, "drawable", context.getPackageName());
		return BitmapFactory.decodeResource(context.getResources(), resID);
	}

	public static int refLayout(Context context, String name) {
		int layout = context.getResources().getIdentifier(name, "layout", context.getPackageName());
		return layout;
	}

	public static String refString(Context context, String name) {
		int id = context.getResources().getIdentifier(name, "string", context.getPackageName());
		return context.getString(id);
	}

	public static int refId(Context context, String name) {
		int id = context.getResources().getIdentifier(name, "id", context.getPackageName());
		return id;
	}

	public View findViewById(View root, String id) {
		return root.findViewById(refId(this, id));
	}

	public View findViewById(String id) {
		return findViewById(refId(this, id));
	}

	private IWXAPI api;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(refLayout(this, "activity_main"));

		// 通过WXAPIFactory工厂，获取IWXAPI的实例
		api = WXAPIFactory.createWXAPI(this, "wx7bb8d98ff739e698", false);
		api.registerApp("wx7bb8d98ff739e698");

		context = this;
		sp = new SoundPlay(this);
		// sp.loadSound("scoreUp.wav");
		webView = (WebView) findViewById("webView");
		webView.setWebViewClient(new MyWebViewClient());
		webView.setWebChromeClient(new MyWebChromeClient());

		WebSettings settings = webView.getSettings();
		settings.setBlockNetworkImage(true);
		settings.setRenderPriority(RenderPriority.HIGH);
		settings.setJavaScriptEnabled(true);
		settings.setPluginsEnabled(true);
		settings.setAllowFileAccess(true);
		settings.setPluginState(PluginState.ON);
		settings.setDomStorageEnabled(true);
		settings.setUseWideViewPort(true);//
		settings.setLoadWithOverviewMode(true);
		webView.loadUrl("file:///android_asset/index.html");
		webView.addJavascriptInterface(new JsToJava(), "jsToJava");
	}

	class JsToJava {
		public void gameOver() {
			Toast.makeText(context, "GameOver", Toast.LENGTH_SHORT).show();
		}

		public void playSound(String sound) {
			sp.palySound(sound);
		}

		public void loadSound(String sound) {
			sp.loadSound(sound);
		}
	}

	public boolean installApkFromPath(Context context, String path) {
		try {
			Intent intent = new Intent();
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.setAction(android.content.Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.fromFile(new File(path)), "application/vnd.android.package-archive");
			context.startActivity(intent);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (webView.canGoBack()) {
				webView.goBack();
				return true;
			} else {
				finish();
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	private class MyWebViewClient extends WebViewClient {

	}

	private class MyWebChromeClient extends WebChromeClient {

		public void onProgressChanged(WebView view, int newProgress) {
			super.onProgressChanged(view, newProgress);
		}

		public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
			result.confirm();
			return true;
		}
	}

}
