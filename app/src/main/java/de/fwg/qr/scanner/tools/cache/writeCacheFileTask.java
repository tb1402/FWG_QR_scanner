package de.fwg.qr.scanner.tools.cache;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.ref.WeakReference;
import java.security.AlgorithmParameters;
import java.security.SecureRandom;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import de.fwg.qr.scanner.activityErrorHandling;
import de.fwg.qr.scanner.tools.preferencesManager;

/**
 * asynchronous task to write to storage cache
 */
class writeCacheFileTask extends AsyncTask<File, Void, Void> {

    final Bitmap data;//bitmap data to write
    private WeakReference<Context> cref;//weak reference to context, to prevent memory leaks
    private String key;//encryption key

    public writeCacheFileTask(Context c, Bitmap data, String key) {
        this.data = data;
        this.cref = new WeakReference<>(c);
        this.key=key;
    }

    @Override
    protected Void doInBackground(File... files) {
        try {
            preferencesManager pm=preferencesManager.getInstance(cref.get());

            //compress the bitmap (png is losless) and write it to a byte array
            ByteArrayOutputStream byteOutput=new ByteArrayOutputStream();
            data.compress(Bitmap.CompressFormat.PNG,100,byteOutput);
            byte[] bytes=byteOutput.toByteArray();
            byteOutput.close();

            //generate or get encryption salt
            byte[] salt = new byte[32];
            if(pm.getPreferences().getString("enc_salt","").contentEquals("")) {
                //no (valid) slat in sharedPreferences, create new one
                SecureRandom random = new SecureRandom();
                random.nextBytes(salt);
                String saltString = Base64.encodeToString(salt, Base64.NO_WRAP);
                pm.saveString("enc_salt", saltString);
            }
            else{
                //read from sharedPreferences
                salt= Base64.decode(pm.getPreferences().getString("enc_salt",""),Base64.NO_WRAP);
            }

            //setup keyFactory and keySpec to make the encryption key a secretKey
            SecretKeyFactory secretKeyFactory=SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            KeySpec keySpec=new PBEKeySpec(key.toCharArray(),salt,65536,256);
            SecretKey secretKey=new SecretKeySpec(secretKeyFactory.generateSecret(keySpec).getEncoded(),"AES");

            //setup cipher in encrypt mode
            Cipher cipher=Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE,secretKey);
            AlgorithmParameters algorithmParameters=cipher.getParameters();

            //generate initialization vector and write the length to sharedPreferences
            byte[] iv=algorithmParameters.getParameterSpec(IvParameterSpec.class).getIV();
            pm.saveInt("enc_iv_length",iv.length);

            //encrypt the data
            byte[] encrypted=cipher.doFinal(bytes);

            //append iv to the beginning of the file
            byte[] output=new byte[encrypted.length+iv.length];
            System.arraycopy(iv,0,output,0,iv.length);
            System.arraycopy(encrypted,0,output,iv.length,encrypted.length);

            //write to file
            FileOutputStream o = new FileOutputStream(files[0]);
            o.write(output);
            o.flush();
            o.close();
        } catch (Exception e) {
            Intent i = new Intent(cref.get(), activityErrorHandling.class);
            i.putExtra(activityErrorHandling.errorNameIntentExtra, activityErrorHandling.stackTraceToString(e));
            cref.get().startActivity(i);
        }
        return null;
    }
}
