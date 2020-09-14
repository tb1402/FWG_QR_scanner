package de.fwg.qr.scanner.tools.cache;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import de.fwg.qr.scanner.activityErrorHandling;
import de.fwg.qr.scanner.tools.networkCallbackInterface;
import de.fwg.qr.scanner.tools.preferencesManager;

/**
 * asynchronous task to read files from storage cache
 */
class readCacheFileTask extends AsyncTask<File, Void, Bitmap> {

    private final String key;
    private WeakReference<networkCallbackInterface> ref;
    private addToMemCacheWhileReadInterface cm;
    private WeakReference<Context> cref;
    private String operation;
    private String encKey;

    public readCacheFileTask(Context c, WeakReference<networkCallbackInterface> ref, addToMemCacheWhileReadInterface cm, String key,String operation, String encKey) {
        this.ref = ref;
        this.cm = cm;
        this.key = key;
        this.cref = new WeakReference<>(c);
        this.operation=operation;
        this.encKey=encKey;
    }

    @Override
    protected Bitmap doInBackground(File... files) {
        try {
            SharedPreferences pm=preferencesManager.getInstance(cref.get()).getPreferences();

            FileInputStream inputStream=new FileInputStream(files[0]);
            byte[] input=new byte[(int) files[0].length()];
            inputStream.read(input);
            inputStream.close();
            Log.i("FWG0",files[0].length()+" "+input.length);

            byte[] salt;
            String ivBs64=pm.getString("enc_salt","");
            salt= Base64.decode(ivBs64,Base64.NO_WRAP);

            int ivLength=pm.getInt("enc_iv_length",-1);
            byte[] iv =new byte[ivLength];
            System.arraycopy(input,0,iv,0,ivLength);

            SecretKeyFactory secretKeyFactory=SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            KeySpec keySpec=new PBEKeySpec(encKey.toCharArray(),salt,65536,256);
            SecretKey secretKey=new SecretKeySpec(secretKeyFactory.generateSecret(keySpec).getEncoded(),"AES");

            Cipher cipher=Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE,secretKey,new IvParameterSpec(iv));

            byte[] data=new byte[input.length-ivLength];
            System.arraycopy(input,ivLength,data,0,input.length-ivLength);

            ByteArrayInputStream byteInputStream=new ByteArrayInputStream(cipher.doFinal(data));
            return BitmapFactory.decodeStream(byteInputStream);

            /*FileInputStream i = new FileInputStream(files[0]);
            return BitmapFactory.decodeStream(i);*/
        } catch (Exception e) {
            Intent i = new Intent(cref.get(), activityErrorHandling.class);
            i.putExtra(activityErrorHandling.errorNameIntentExtra, activityErrorHandling.stackTraceToString(e));
            cref.get().startActivity(i);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        if (result == null) {
            ref.get().onImageCallback("errorCache",null);
        } else {
            ref.get().onImageCallback(operation,result);
            cm.addToCache(key, result);//add image to memory cache
        }
    }
}
