package de.fwg.qr.scanner.tools.cache;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.lang.ref.WeakReference;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import de.fwg.qr.scanner.activityErrorHandling;
import de.fwg.qr.scanner.tools.asyncTask;
import de.fwg.qr.scanner.tools.networkCallbackInterface;
import de.fwg.qr.scanner.tools.preferencesManager;

class readCacheFileCustomAsyncTask extends asyncTask {

    private final String key;
    private networkCallbackInterface ref;
    private addToMemCacheWhileReadInterface cm;
    private WeakReference<Context> cref;
    private String operation;
    private File f;
    private String encKey;

    public readCacheFileCustomAsyncTask(Context c, networkCallbackInterface ref, addToMemCacheWhileReadInterface cm, String key, String operation, File f, String encKey) {
        this.ref = ref;
        this.cm = cm;
        this.key = key;
        this.cref = new WeakReference<>(c);
        this.operation = operation;
        this.f=f;
        this.encKey=encKey;
    }

    @Override
    public void run() {
        Bitmap b;
        try {
            //TODO @me simplify
            SharedPreferences pref = preferencesManager.getInstance(cref.get()).getPreferences();

            FileInputStream inputStream = new FileInputStream(f);
            byte[] input = new byte[(int) f.length()];
            inputStream.read(input);
            inputStream.close();

            byte[] salt;
            String ivBs64 = pref.getString("enc_salt", "");
            salt = Base64.decode(ivBs64, Base64.NO_WRAP);

            int ivLength = pref.getInt("enc_iv_length", -1);
            byte[] iv = new byte[ivLength];
            System.arraycopy(input, 0, iv, 0, ivLength);

            SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            KeySpec keySpec = new PBEKeySpec(encKey.toCharArray(), salt, 65536, 256);
            SecretKey secretKey = new SecretKeySpec(secretKeyFactory.generateSecret(keySpec).getEncoded(), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));

            byte[] data = new byte[input.length - ivLength];
            System.arraycopy(input, ivLength, data, 0, input.length - ivLength);

            ByteArrayInputStream byteInputStream = new ByteArrayInputStream(cipher.doFinal(data));
            b=BitmapFactory.decodeStream(byteInputStream);
        }
        catch (Exception e){
            Intent i = new Intent(cref.get(), activityErrorHandling.class);
            i.putExtra(activityErrorHandling.errorNameIntentExtra, activityErrorHandling.stackTraceToString(e));
            cref.get().startActivity(i);
            b=null;
        }

        if (b== null) {
            ref.onImageCallback("errorCache",null);
        } else {
            ref.onImageCallback(operation,b);
            cm.addToCache(key, b);//add image to memory cache
        }
        stop();
    }
}
