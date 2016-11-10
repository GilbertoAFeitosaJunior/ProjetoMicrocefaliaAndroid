package br.com.mobi.redemicro;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.ConnectionResult;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import br.com.mobi.redemicro.bean.Usuario;
import br.com.mobi.redemicro.bo.UsuarioBo;
import br.com.mobi.redemicro.util.Constants;
import br.com.mobi.redemicro.util.HttpAsyncTask;
import br.com.mobi.redemicro.util.Mask;


public class CompletarPerfilActivity extends AppCompatActivity implements View.OnClickListener {

    private Calendar calendario = Calendar.getInstance();
    private DatePickerDialog dataPicker;
    private int ano, mes, dia;
    String dataConvertida;
    private UsuarioBo usuarioBO;
    private Usuario usuario;
    private EditText ddd, telefone, logradouro, numero, bairro, cidade, cep, estado, pais;
    private ProgressDialog progressDialog;
    private Button data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_completar_perfil);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher);
        getSupportActionBar().setTitle(getString(R.string.app_name));
        getSupportActionBar().setSubtitle(R.string.editar);

        usuarioBO = new UsuarioBo(this);
        usuario = usuarioBO.get(null, null);
        SharedPreferences preferences = getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE);

        data=(Button)findViewById(R.id.data);

        ano = calendario.get(Calendar.YEAR);
        mes = calendario.get(Calendar.MONTH);
        dia = calendario.get(Calendar.DAY_OF_MONTH);

        dataConvertida=ano+"-"+(mes+1)+"-"+dia;

        if(usuario.getDatanascimento()!=null){
            data.setText(new SimpleDateFormat("dd/MM/yyyy").format(usuario.getDatanascimento()));
        }

        ddd = (EditText) findViewById(R.id.ddd);
        if (!usuario.getDdd().equals("")) {
            ddd.setText(usuario.getDdd());
        }
        telefone = (EditText) findViewById(R.id.telefone);
        if (!usuario.getTelefone().equals("")) {
            telefone.setText(usuario.getTelefone());
        }
        telefone.addTextChangedListener(Mask.insert("#####-####", telefone));
        logradouro = (EditText) findViewById(R.id.logradouro);
        if (!usuario.getLogradouro().equals("")) {
            logradouro.setText(usuario.getLogradouro());
        } else {
            logradouro.setText(preferences.getString(Constants.ADDRESS, ""));
        }
        numero = (EditText) findViewById(R.id.numero);
        if (!usuario.getNumero().equals("")) {
            numero.setText(usuario.getNumero());
        } else {
            numero.setText(preferences.getString(Constants.NUMBER, ""));
        }
        bairro = (EditText) findViewById(R.id.bairro);
        if (!usuario.getBairro().equals("")) {
            bairro.setText(usuario.getBairro());
        } else {
            bairro.setText(preferences.getString(Constants.NEIGHBORHOOD, ""));
        }
        cidade = (EditText) findViewById(R.id.cidade);
        if (!usuario.getCidade().equals("")) {
            cidade.setText(usuario.getCidade());
        } else {
            cidade.setText(preferences.getString(Constants.CITY, ""));
        }
        cep = (EditText) findViewById(R.id.cep);
        if (!usuario.getCep().equals("")) {
            cep.setText(usuario.getCep());
        } else {
            cep.setText(preferences.getString(Constants.POSTAL_CODE, ""));
        }
        cep.addTextChangedListener(Mask.insert("#####-###", cep));
        estado = (EditText) findViewById(R.id.estado);
        if (!usuario.getEstado().equals("")) {
            estado.setText(usuario.getEstado());
        } else {
            estado.setText(preferences.getString(Constants.STATE, ""));
        }
        pais = (EditText) findViewById(R.id.pais);
        if (!usuario.getPais().equals("")) {
            pais.setText(usuario.getPais());
        } else {
            pais.setText(preferences.getString(Constants.PAIS, ""));
        }
        findViewById(R.id.mais_tarde).setOnClickListener(this);
        findViewById(R.id.enviar).setOnClickListener(this);
        findViewById(R.id.data).setOnClickListener(this);

    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mais_tarde:
                startActivity(new Intent(CompletarPerfilActivity.this, MainActivity.class));
                CompletarPerfilActivity.this.finish();
                break;
            case R.id.enviar:
                salvar();
                break;
            case R.id.data:
                if(usuario.getDatanascimento()!=null) {
                    ano = Integer.parseInt(new SimpleDateFormat("yyyy-MM-dd").format(usuario.getDatanascimento()).substring(0, 4));
                    mes=Integer.parseInt(new SimpleDateFormat("yyyy-MM-dd").format(usuario.getDatanascimento()).substring(5, 7))-1;
                    dia=Integer.parseInt(new SimpleDateFormat("yyyy-MM-dd").format(usuario.getDatanascimento()).substring(8, 10));
                }
                System.out.println("#################################"+ano);
                System.out.println("#################################"+new SimpleDateFormat("yyyy-MM-dd").format(usuario.getDatanascimento()).substring(5, 7));
                System.out.println("#################################"+new SimpleDateFormat("yyyy-MM-dd").format(usuario.getDatanascimento()).substring(8, 10));
                dataPicker = new DatePickerDialog(CompletarPerfilActivity.this, compraDateSetListener, ano, mes, dia);// data
                data.setText(dia + " / " + (mes + 1) + " / " + ano);
                dataPicker.show();
                break;

        }
    }
    DatePickerDialog.OnDateSetListener compraDateSetListener = new DatePickerDialog.OnDateSetListener() {// data
        @Override
        public void onDateSet(DatePicker view, int ano, int mes, int dia) {

            data.setText(dia + " / " + (mes + 1) + " / " + ano);
            dataConvertida = ano + "-" + (mes + 1) + "-" + dia;
        }
    };

    public void salvar() {
        int i = 0;
        try {
            usuario.setDatanascimento(new SimpleDateFormat("yyyy-MM-dd").parse(dataConvertida));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (ddd.getText().toString().length() == 2) {
            usuario.setDdd(ddd.getText().toString());
        } else if (ddd.getText().toString().length() > 0 && ddd.getText().toString().length() < 2 || ddd.getText().toString().equals("00")) {
            i++;
            ddd.requestFocus();
        }
        if (telefone.getText().toString().length() == 10) {
            usuario.setTelefone(telefone.getText().toString());
        } else if (telefone.getText().toString().length() > 0 && telefone.getText().toString().length() < 10 || telefone.getText().toString().equals(Mask.insert("0####-####", telefone))) {
            i++;
            telefone.requestFocus();
        }
        if (!logradouro.getText().toString().equals("")) {
            usuario.setLogradouro(logradouro.getText().toString());
        }
        if (!numero.getText().toString().equals("")) {
            usuario.setNumero(numero.getText().toString());
        }
        if (!bairro.getText().toString().equals("")) {
            usuario.setBairro(bairro.getText().toString());
        }
        if (!cidade.getText().toString().equals("")) {
            usuario.setCidade(cidade.getText().toString());
        }
        if (estado.getText().toString().length() == 2) {
            usuario.setEstado(estado.getText().toString());
        } else if (estado.getText().toString().length() > 0 && estado.getText().toString().length() < 2) {
            i++;
            estado.requestFocus();
        }
        if (cep.getText().toString().length() == 9) {
            usuario.setCep(cep.getText().toString());
        } else if (cep.getText().toString().length() > 0 && cep.getText().toString().length() < 9) {
            i++;
            cep.requestFocus();
        }
        if (pais.getText().toString().length() == 2) {
            usuario.setPais(pais.getText().toString());
        } else if (pais.getText().toString().length() > 0 && pais.getText().toString().length() < 2) {
            i++;
            pais.requestFocus();
        }
        if (i == 0) {
            CompletarPerfilTask completarPerfilTask = new CompletarPerfilTask();
            completarPerfilTask.execute();
        }else {
            Toast.makeText(CompletarPerfilActivity.this,getString(R.string.toast_completar),Toast.LENGTH_SHORT).show();
        }

    }

    private class CompletarPerfilTask extends AsyncTask<Void, Void, Boolean> {
        private boolean opc;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(CompletarPerfilActivity.this);
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(true);
            progressDialog.setMessage(getString(R.string.carregando));
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            String url = getString(R.string.url_rest) + "usuario/editar"; //lembrar de pedir a gilberto o link

            try {
                HttpAsyncTask task = new HttpAsyncTask(url, CompletarPerfilActivity.this);
                task.addParams("id", usuario.getId());
                task.addParams("nome", usuario.getNome());
                task.addParams("email", usuario.getEmail());
                task.addParams("idgoogle", usuario.getIdgoogle());
                if (usuario.getFoto() != null) {
                    task.addParams("foto", usuario.getFoto());
                }
                task.addParams("ddd", usuario.getDdd());
                task.addParams("telefone", usuario.getTelefone());
                task.addParams("logradouro", usuario.getLogradouro());
                task.addParams("numero", usuario.getNumero());
                task.addParams("bairro", usuario.getBairro());
                task.addParams("cidade", usuario.getCidade());
                task.addParams("cep", usuario.getCep());
                task.addParams("estado", usuario.getEstado());
                task.addParams("pais", usuario.getPais());
                task.addParams("datanascimento", new SimpleDateFormat("yyyy-MM-dd").format(usuario.getDatanascimento()));

                try {
                    task.put(new HttpAsyncTask.FutureCallback() {
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
                            usuario.setCep(json.getString("cep"));
                            usuario.setEstado(json.getString("estado"));
                            usuario.setPais(json.getString("pais"));
                            if (!json.getString("datanascimento").equals("null")) {
                                try {
                                    usuario.setDatanascimento(new SimpleDateFormat("yyyy-MM-dd").parse(json.getString("datanascimento")));
                                    //new Date(json)
                                    //Log.i("chave", valor);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }

                            usuarioBO.clean();
                            usuarioBO.insert(usuario);
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
                startActivity(new Intent(CompletarPerfilActivity.this, MainActivity.class));
                CompletarPerfilActivity.this.finish();

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
        alertDialog.setMessage(getString(R.string.msg_erro_enviar));
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        alertDialog.show();
    }

}
