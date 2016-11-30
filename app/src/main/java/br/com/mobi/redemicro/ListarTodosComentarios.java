package br.com.mobi.redemicro;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import br.com.mobi.redemicro.adapter.ComentarAdapater;
import br.com.mobi.redemicro.bean.Comentar;
import br.com.mobi.redemicro.bean.Usuario;
import br.com.mobi.redemicro.bo.UsuarioBo;
import br.com.mobi.redemicro.util.Constants;
import br.com.mobi.redemicro.util.HttpAsyncTask;
import mobi.stos.podataka_lib.exception.NoPrimaryKeyFoundException;
import mobi.stos.podataka_lib.exception.NoPrimaryKeyValueFoundException;

public class ListarTodosComentarios extends AppCompatActivity {

    private List<Comentar> listar;
    private ComentarAdapater adpater;
    private ListView comertarListView;
    private ImageView bntComentar;
    private EditText comentarioUsuario;

    private Usuario usuario;
    int idNoticia;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listar_todos_comentarios);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        idNoticia=(int) getIntent().getSerializableExtra("idNoticia");

        ListarDeComentariosTask listarDeComentariosTask=new ListarDeComentariosTask();
        listarDeComentariosTask.execute(null,null,null);

        comentarioUsuario = (EditText) findViewById(R.id.comentarioUsuario);
        comertarListView = (ListView) findViewById(R.id.listViewTodosComentarios);

        UsuarioBo usuarioBO = new UsuarioBo(this);
        usuario = usuarioBO.get(null, null);

        if (usuario != null) {
            registerForContextMenu(comertarListView);
        }

        bntComentar = (ImageView) findViewById(R.id.bntComentar);
        bntComentar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (usuario == null) {
                    FazerLogin();
                } else {
                    if (!comentarioUsuario.getText().toString().equals("")) {
                        ComentarTask comentarTask = new ComentarTask(comentarioUsuario.getText().toString());
                        comentarTask.execute(null, null, null);
                        comentarioUsuario.setText("");
                    }
                }
            }
        });
    }

    public void FazerLogin() {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setMessage(getString(R.string.deseja_fazer_login));

        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Sim", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(ListarTodosComentarios.this, LoginActivity.class));
            }
        });
        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Não", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        alertDialog.show();
    }

    public void listarTodosComentarios() {
        for(Comentar item: listar){
            System.out.println("@$@$$@$@$@"+item.getComentario());
        }
        adpater = new ComentarAdapater(listar, this);
        comertarListView.setAdapter(adpater);
    }

    private List<Comentar> listaComente(List<Comentar> listar) {
        List<Comentar>lista=new ArrayList<>();
        int i=0;
        for(int tamanho=listar.size()-1;tamanho>= i;tamanho--){
            lista.add(listar.get(tamanho));
        }
        return lista;
    }

    public class ListarDeComentariosTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            String url = getString(R.string.url_rest) + "noticia/comentarios/" + idNoticia;
            try {
                HttpAsyncTask httpAsyncTask = new HttpAsyncTask(url, ListarTodosComentarios.this);
                try {
                    httpAsyncTask.get(new HttpAsyncTask.FutureCallback() {
                        @Override
                        public void onCallback(Object jsonObject, int responseCode) {
                            JSONArray jsonArray = (JSONArray) jsonObject;
                            listar = new ArrayList<>();


                            try {

                                for (int i = 0; i < jsonArray.length(); i++) {

                                    JSONObject json = jsonArray.getJSONObject(i);
                                    Comentar comentar = new Comentar();

                                    comentar.setId(json.getInt("id"));
                                    comentar.setNome(json.getString("nome"));
                                    comentar.setFoto(json.getString("foto"));
                                    comentar.setComentario(json.getString("comentario"));
                                    comentar.setDate(new Date(json.getLong("date")));

                                    listar.add(comentar);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }


            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            listar= listaComente(listar);
            listarTodosComentarios();
        }
    }

    public class ComentarTask extends AsyncTask<Void, Void, Void> {
        String texto;
        public ComentarTask(String texto) {
            this.texto=texto;
        }

        @Override
        protected Void doInBackground(Void... params) {
            String url = getString(R.string.url_rest) + "noticia/comentar";

            try {
                HttpAsyncTask httpAsyncTask = new HttpAsyncTask(url, ListarTodosComentarios.this);
                httpAsyncTask.addParams("idUsuario", usuario.getId());
                httpAsyncTask.addParams("idNoticia", idNoticia);
                httpAsyncTask.addParams("comentario", texto);

                httpAsyncTask.post(new HttpAsyncTask.FutureCallback() {
                    @Override
                    public void onCallback(Object jsonObject, int responseCode) {
                        switch (responseCode) {
                            case 200:
                                ListarDeComentariosTask listarDeComentariosTask=new ListarDeComentariosTask();
                                listarDeComentariosTask.execute();
                                break;
                            case 400:
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
}
