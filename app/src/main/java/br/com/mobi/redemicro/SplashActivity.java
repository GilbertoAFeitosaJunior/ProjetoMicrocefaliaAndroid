package br.com.mobi.redemicro;

import br.com.mobi.redemicro.bean.Usuario;
import br.com.mobi.redemicro.util.Constants;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;

import br.com.mobi.redemicro.bo.UsuarioBo;
import xyz.hanks.library.SmallBang;

public class SplashActivity extends Activity {
    private UsuarioBo usuarioBo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        usuarioBo = new UsuarioBo(this);
        SmallBang smallBang = SmallBang.attach2Window(this);
        smallBang.callOnClick();
        InitialTask task = new InitialTask();
        task.execute(null, null, null);

    }

    private class InitialTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            SharedPreferences sharedPreferences = getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE);
            if (sharedPreferences.getString(Constants.ID_LOGIN, "").equals("OK")) {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                SplashActivity.this.finish();
            } else {
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                SplashActivity.this.finish();
            }
        }
    }
}
