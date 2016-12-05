package br.com.mobi.redemicro;

import android.*;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import br.com.mobi.redemicro.adapter.TopicosForumAdapter;
import br.com.mobi.redemicro.bean.TopicosForum;
import br.com.mobi.redemicro.bean.Usuario;
import br.com.mobi.redemicro.bo.UsuarioBo;
import br.com.mobi.redemicro.util.Constants;
import br.com.mobi.redemicro.util.HttpAsyncTask;

public class ForumActivity extends AppCompatActivity {

    private String titulo = "", msg = "";
    private UsuarioBo usuarioBO;
    private Usuario usuario;
    private ProgressDialog progressDialog;
    private int forumId;
    private List<TopicosForum> forumList;
    ListForumTask listForumTask;

    private boolean opc;
    private boolean loading;
    private TopicosForumAdapter adapter;
    private ListView listViewTopicosForum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forum);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        usuarioBO = new UsuarioBo(this);
        usuario = usuarioBO.get(null, null);
        SharedPreferences preferences = getSharedPreferences(Constants.APP, MODE_PRIVATE);
        forumId = preferences.getInt("FORUM", 0);
        listViewTopicosForum = (ListView)findViewById(R.id.listViewTopicosForum);
        listForumTask=new ListForumTask();
        listForumTask.execute(null,null,null);
        listViewTopicosForum.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TopicosForum forum = forumList.get(position);

                SharedPreferences.Editor preferences = getSharedPreferences(Constants.APP, Context.MODE_PRIVATE).edit();
                preferences.putInt("FORUMTOPICO", forum.getIdTopico());
                preferences.commit();

                startActivity(new Intent(ForumActivity.this, ForumMsgActivity.class));
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.forum_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.ic_forum:
                LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View viewHospital = layoutInflater.inflate(R.layout.forum_alert_dialog, null);
                final EditText edtTitulo = (EditText) viewHospital.findViewById(R.id.edt_titulo);
                final EditText edtMsg = (EditText) viewHospital.findViewById(R.id.edt_msg);

                usuario = usuarioBO.get(null, null);
                if (usuario == null) {
                    FazerLogin();
                } else {
                    AlertDialog alertDialog = new AlertDialog.Builder(ForumActivity.this).create();
                    alertDialog.setView(viewHospital);

                    alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Comentar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            titulo = edtTitulo.getText().toString();
                            msg = edtMsg.getText().toString();
                            if (titulo.equals("") || msg.equals("")) {
                                Toast.makeText(ForumActivity.this, getString(R.string.toast_completar), Toast.LENGTH_SHORT).show();
                            } else {
                                NewForumTask newForumTask = new NewForumTask();
                                newForumTask.execute(null, null, null);
                            }
                        }
                    });
                    alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancelar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    alertDialog.show();
                }
                break;
        }
        return true;
    }
    public void FazerLogin() {
        AlertDialog alertDialog = new AlertDialog.Builder(ForumActivity.this).create();
        alertDialog.setMessage(getString(R.string.deseja_fazer_login));

        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Sim", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(ForumActivity.this, LoginActivity.class));
                finish();
            }
        });
        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Não", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        alertDialog.show();
    }
    public class NewForumTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            String url = getString(R.string.url_rest) + "forum/topico";

            try {
                HttpAsyncTask httpAsyncTask = new HttpAsyncTask(url, ForumActivity.this);
                httpAsyncTask.addParams("idCategoria", forumId);
                httpAsyncTask.addParams("idUsuario", usuario.getId());
                httpAsyncTask.addParams("titulo", titulo);
                httpAsyncTask.addParams("mensagem",msg);

                httpAsyncTask.post(new HttpAsyncTask.FutureCallback() {
                    @Override
                    public void onCallback(Object jsonObject, int responseCode) {
                        switch (responseCode) {
                            case 200:
                                listForumTask=new ListForumTask();
                                listForumTask.execute();
                                Toast.makeText(ForumActivity.this,"Ok",Toast.LENGTH_SHORT).show();
                                break;
                            case 400:
                                Toast.makeText(ForumActivity.this,"erro",Toast.LENGTH_SHORT).show();
                                break;
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
    private class ListForumTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(ForumActivity.this);
            progressDialog.setMessage(getString(R.string.carregando));
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(true);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                String url = getString(R.string.url_rest) + "forum/topicos/"+forumId;
                HttpAsyncTask task = new HttpAsyncTask(url, ForumActivity.this);

                try {
                    task.get(new HttpAsyncTask.FutureCallback() {
                        @Override
                        public void onCallback(Object jsonObject, int responseCode) {
                            if (responseCode == 200) {
                                try {
                                    forumList = new ArrayList<>();
                                    JSONArray jsonArray = (JSONArray) jsonObject;

                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        JSONObject json = jsonArray.getJSONObject(i);
                                        TopicosForum forum=new TopicosForum();

                                        forum.setIdTopico(json.getInt("idTopico"));
                                        forum.setTitulo(json.getString("titulo"));
                                        forum.setMensagem(json.getString("mensagem"));
                                        forum.setDataAbertura(new Date(json.getLong("dataAbertura")));
                                        forum.setNomeUsuario(json.getString("nomeUsuario"));
                                        forum.setFotoUsuario(json.getString("fotoUsuario"));
                                        forum.setNomeModerador(json.getString("nomeModerador"));
                                        forum.setEspecialidade(json.getString("especialidade"));
                                        try {
                                            forum.setDataFechamento(new Date(json.getLong("dataFechamento")));
                                        }catch (Exception e){
                                            e.getMessage();
                                        }
                                        forum.setAtivo(json.getBoolean("ativo"));


                                        forumList.add(forum);
                                        opc=true;
                                    }

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    opc=false;

                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            if (!isCancelled()) {
                createAdapter();
            }

        }
    }
    private void createAdapter() {
        adapter = new TopicosForumAdapter(forumList, ForumActivity.this);
        if (!opc) {
            Toast.makeText(ForumActivity.this,R.string.erro_internet, Toast.LENGTH_SHORT).show();
        }else {
            for(TopicosForum forum: forumList){
                //synchronized (adapter) {
                if (!adapter.contains(forum)) {
                    adapter.add(forum);
                    //  }
                }
            }
            this.listViewTopicosForum.setAdapter(adapter);
        }

        loading = true;
    }
}
