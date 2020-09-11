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

    final Bitmap data;
    private WeakReference<Context> cref;
    private String key;

    public writeCacheFileTask(Context c, Bitmap data, String key) {
        this.data = data;
        this.cref = new WeakReference<>(c);
        this.key=key;
    }

    @Override
    protected Void doInBackground(File... files) {
        try {
            preferencesManager pm=new preferencesManager(cref.get());
            ByteArrayOutputStream byteOutput=new ByteArrayOutputStream();
            data.compress(Bitmap.CompressFormat.PNG,100,byteOutput);
            byte[] bytes=byteOutput.toByteArray();
            byteOutput.close();

            byte[] salt = new byte[32];
            if(pm.getString("enc_salt","").contentEquals("")) {
                SecureRandom random = new SecureRandom();
                random.nextBytes(salt);
                String saltString = Base64.encodeToString(salt, Base64.NO_WRAP);
                pm.saveString("enc_salt", saltString);
            }
            else{
                salt= Base64.decode(pm.getString("enc_salt",""),Base64.NO_WRAP);
            }

            SecretKeyFactory secretKeyFactory=SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            KeySpec keySpec=new PBEKeySpec(key.toCharArray(),salt,65536,256);
            SecretKey secretKey=new SecretKeySpec(secretKeyFactory.generateSecret(keySpec).getEncoded(),"AES");

            Cipher cipher=Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE,secretKey);
            AlgorithmParameters algorithmParameters=cipher.getParameters();

            byte[] iv=algorithmParameters.getParameterSpec(IvParameterSpec.class).getIV();
            pm.saveInt("enc_iv_length",iv.length);

            byte[] encrypted=cipher.doFinal(bytes);
            byte[] output=new byte[encrypted.length+iv.length];
            System.arraycopy(iv,0,output,0,iv.length);
            System.arraycopy(encrypted,0,output,iv.length,encrypted.length);

            FileOutputStream o = new FileOutputStream(files[0]);
            o.write(output);
            o.flush();
            o.close();

            /*FileOutputStream o = new FileOutputStream(files[0]);
            data.compress(Bitmap.CompressFormat.PNG, 100, o);
            o.flush();
            o.close();
            Log.i("fwg", "written");*/
        } catch (Exception e) {
            Intent i = new Intent(cref.get(), activityErrorHandling.class);
            i.putExtra(activityErrorHandling.errorNameIntentExtra, activityErrorHandling.stackTraceToString(e));
            cref.get().startActivity(i);
        }
        return null;
    }
}
