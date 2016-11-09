package br.com.mobi.redemicro;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;

import android.support.annotation.NonNull;

import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;


import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import br.com.mobi.redemicro.bean.Usuario;
import br.com.mobi.redemicro.bo.UsuarioBo;
import br.com.mobi.redemicro.util.Constants;
import br.com.mobi.redemicro.util.HttpAsyncTask;
import br.com.mobi.redemicro.util.LocationThread;

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

    private GoogleApiClient mGoogleApiClient;
    private static final int SIGN_IN = 10;
    private Usuario usuario = null;
    private UsuarioBo usuarioBO;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.titulo_fazer_login));

        findViewById(R.id.sign_in_button).setOnClickListener(this);
        findViewById(R.id.pular).setOnClickListener(this);


        usuarioBO = new UsuarioBo(this);


        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationThread locationThread = new LocationThread(this);
            locationThread.requestLocation();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 9);
        }
        loginPadrao();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 9) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                LocationThread locationThread = new LocationThread(this);
                locationThread.requestLocation();
            }
        }
    }

    private void loginPadrao() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                signIn();
                break;
            case R.id.pular:
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                LoginActivity.this.finish();
                break;
        }
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, SIGN_IN);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SIGN_IN) {
            GoogleSignInResult resultado = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            Log.v("Login", "resultado: " + resultado.isSuccess());

            if (resultado.isSuccess()) {
                GoogleSignInAccount acct = resultado.getSignInAccount();
                CriarLoginTask loginTask = new CriarLoginTask(acct);
                loginTask.execute();
            } else {
                falhaNoLogin();
            }
        }
    }


    private class CriarLoginTask extends AsyncTask<Void, Void, Boolean> {
        private GoogleSignInAccount acc;
        private boolean opc;

        public CriarLoginTask(GoogleSignInAccount acc) {
            this.acc = acc;
            usuario = new Usuario();
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(LoginActivity.this);
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(true);
            progressDialog.setMessage(getString(R.string.carregando));
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            String url = getString(R.string.url_rest) + "usuario/criar"; //lembrar de pedir a gilberto o link

            try {
                HttpAsyncTask task = new HttpAsyncTask(url, LoginActivity.this);
                task.addParams("nome", acc.getDisplayName());
                task.addParams("email", acc.getEmail());
                task.addParams("idgoogle", acc.getId());
                if (acc.getPhotoUrl() != null) {
                    task.addParams("foto", acc.getPhotoUrl().toString());
                }

                try {
                    task.post(new HttpAsyncTask.FutureCallback() {
                        @Override
                        public void onCallback(Object jsonObject, int responseCode) throws JSONException {
                            JSONObject json = (JSONObject) jsonObject;

                            usuario.setId(json.getInt("id"));
                            usuario.setNome(json.getString("nome"));
                            usuario.setEmail(json.getString("email"));
                            usuario.setFoto(json.getString("foto"));
                            usuario.setIdgoogle(json.getString("idgoogle"));
                            usuario.setDdd(json.getString("ddd"));
                            usuario.setTelefone(json.getString("telefone"));
                            usuario.setLogradouro(json.getString("logradouro"));
                            usuario.setNumero(json.getString("numero"));
                            usuario.setBairro(json.getString("bairro"));
                            usuario.setCidade(json.getString("cidade"));
                            usuario.setPais(json.getString("pais"));
                            if (!json.getString("datanascimento").equals("null")) {
                                try {
                                    usuario.setDatanascimento(new SimpleDateFormat("yyyy-MM-dd").parse(json.getString("datanascimento")));
                               //new Date(json)
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }
                            usuario.setEstado(json.getString("estado"));

                            usuarioBO.clean();
                            usuarioBO.insert(usuario);

                            SharedPreferences.Editor editor = getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE).edit();
                            editor.putString(Constants.ID_LOGIN, "OK");
                            editor.commit();
                            opc = true;

                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();// quando não tiver conexão com internet
                    opc = false;
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            return opc;
        }


        @Override
        protected void onPostExecute(Boolean aVoid) {
            super.onPostExecute(aVoid);
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }

            if (aVoid) {
                if (TextUtils.isEmpty(usuario.getTelefone()) || (usuario.getDatanascimento() == null)) {
                    startActivity(new Intent(LoginActivity.this, CompletarPerfilActivity.class));
                } else {
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                }

            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        falhaNoLogin();
                    }
                });
            }

        }
    }

    public void falhaNoLogin() {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle(getString(R.string.title_login_erro));
        alertDialog.setMessage(getString(R.string.msg_erro_login));
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        alertDialog.show();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e("Login", "Conexão falhou");
    }
}