package br.com.mobi.redemicro;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import br.com.mobi.redemicro.adapter.ComentarAdapater;
import br.com.mobi.redemicro.adapter.CurtidasUsuarioAdapter;
import br.com.mobi.redemicro.bean.Comentar;
import br.com.mobi.redemicro.bean.Curtir;
import br.com.mobi.redemicro.bean.Noticia;
import br.com.mobi.redemicro.bean.Usuario;
import br.com.mobi.redemicro.bo.NoticiaBo;
import br.com.mobi.redemicro.bo.UsuarioBo;
import br.com.mobi.redemicro.util.Constants;
import br.com.mobi.redemicro.util.HttpAsyncTask;

public class DetalheNoticiaActivity extends AppCompatActivity {

    private Noticia noticia;
    private ImageView imagemDetalhesNoticia;
    private Button like, curtidas,maisComentarios;
    private TextView conteudoDetalhes, tituloDetalhes, dataPublicacaoDetalhes;
    private ListView comertarListView;
    private NoticiaBo noticiaBO;
    private boolean opc = false;
    private UsuarioBo usuarioBO;
    private Usuario usuario;
    private List<Curtir> listaDeCurtir;
    private String comentarioUsuario = "";
    private List<Comentar> listar;
    List<Comentar>listaresun;
    private ComentarAdapater adpater;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhe_noticia);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        noticiaBO = new NoticiaBo(this);

        imagemDetalhesNoticia = (ImageView) findViewById(R.id.imagemDetalhesNoticia);
        conteudoDetalhes = (TextView) findViewById(R.id.conteudoDetalhes);
        tituloDetalhes = (TextView) findViewById(R.id.tituloDetalhes);
        dataPublicacaoDetalhes = (TextView) findViewById(R.id.dataPublicacaoDetalhes);
        curtidas = (Button) findViewById(R.id.curtidas);
        like = (Button) findViewById(R.id.like);
        maisComentarios = (Button) findViewById(R.id.maisComentarios);
        comertarListView = (ListView) findViewById(R.id.comertarListView);

        DetalhesTask task = new DetalhesTask();
        task.execute(null, null, null);

        ListarDeComentariosTask listarDeComentariosTask = new ListarDeComentariosTask();
        listarDeComentariosTask.execute(null, null, null);

        usuarioBO = new UsuarioBo(this);
        usuario = usuarioBO.get(null, null);

        ListarCurtirTask listarCurtirTask = new ListarCurtirTask();
        listarCurtirTask.execute();

        AtualizarTask atualizarTask = new AtualizarTask();
        atualizarTask.execute();

        like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (usuario != null) {
                    if (!opc) {
                        //curtir();
                        CurtirTask curtirTask = new CurtirTask();
                        curtirTask.execute(null, null, null);

                    } else {
                        DiscurtirTask discutirTask = new DiscurtirTask();
                        discutirTask.execute(null, null, null);

                    }

                } else {
                    FazerLogin();
                }

            }
        });
        if ((usuario != null)) {
            registerForContextMenu(this.comertarListView);
        }

    }

    public void FazerLogin() {
        AlertDialog alertDialog = new AlertDialog.Builder(DetalheNoticiaActivity.this).create();
        alertDialog.setMessage(getString(R.string.deseja_fazer_login));

        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Sim", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(DetalheNoticiaActivity.this, LoginActivity.class));
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

    public class DetalhesTask extends AsyncTask<Void, Void, Boolean> {
        private boolean opc;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(DetalheNoticiaActivity.this);
            progressDialog.setMessage(getString(R.string.carregando));
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(true);
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                SharedPreferences preferences = getSharedPreferences(Constants.APP, MODE_PRIVATE);
                int noticiaId = preferences.getInt("NOTICIA", 0);
                String url = getString(R.string.url_rest) + "noticia/exibir/" + noticiaId;

                HttpAsyncTask task = new HttpAsyncTask(url, getApplicationContext());

                try {
                    task.get(new HttpAsyncTask.FutureCallback() {
                        @Override
                        public void onCallback(Object jsonObject, int responseCode) {
                            JSONObject json = (JSONObject) jsonObject;
                            try {
                                noticia = new Noticia();
                                noticia.setIdNoticia(json.getInt("id"));
                                noticia.setFoto(json.getString("foto"));
                                noticia.setTitulo(json.getString("titulo"));
                                noticia.setData(json.getString("data"));
                                noticia.setFonte(json.getString("fonte"));
                                noticia.setNoticia(json.getString("noticia"));

                                opc = true;
                            } catch (Exception e) {
                                e.printStackTrace();
                                opc = false;
                            }


                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            return opc;
        }

        @Override
        protected void onPostExecute(Boolean aVoid) {
            super.onPostExecute(aVoid);

            if (aVoid) {
                Picasso.with(DetalheNoticiaActivity.this).load(noticia.getFoto()).placeholder(android.R.drawable.ic_menu_camera).into(imagemDetalhesNoticia);
                tituloDetalhes.setText(noticia.getTitulo());

                //String dataPublicacao = new SimpleDateFormat(getString(R.string.data_hora_format)).format(noticia.getData());
                dataPublicacaoDetalhes.setText("Publicado em: " + noticia.getData());
                conteudoDetalhes.setText(Html.fromHtml(noticia.getNoticia()).toString());

                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        falhaNaNotícia();
                    }
                });
            }


        }
    }

    public void falhaNaNotícia() {
        android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(this).create();
        alertDialog.setTitle(getString(R.string.title_login_erro));
        alertDialog.setMessage(getString(R.string.title_activity_aqwq));
        alertDialog.setButton(android.app.AlertDialog.BUTTON_POSITIVE, getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                DetalheNoticiaActivity.this.finish();
            }
        });
        alertDialog.show();
    }

    public class CurtirTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            String url = getString(R.string.url_rest) + "noticia/curtir";
            try {

                HttpAsyncTask httpAsyncTask = new HttpAsyncTask(url, DetalheNoticiaActivity.this);
                httpAsyncTask.addParams("idUsuario", usuario.getId());
                httpAsyncTask.addParams("idNoticia", noticia.getIdNoticia());

                httpAsyncTask.post(new HttpAsyncTask.FutureCallback() {
                    @Override
                    public void onCallback(Object jsonObject, int responseCode) {
                        switch (responseCode) {
                            case 200:
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {

                                        opc = true;

                                        AtualizarTask atualizarTask = new AtualizarTask();
                                        atualizarTask.execute();

                                        final DetalhesTask task = new DetalhesTask();
                                        task.execute(null, null, null);
                                    }
                                });
                                break;
                            case 403:
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

    public class DiscurtirTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            String url = getString(R.string.url_rest) + "noticia/discurtir";
            try {

                HttpAsyncTask httpAsyncTask = new HttpAsyncTask(url, DetalheNoticiaActivity.this);
                httpAsyncTask.addParams("idUsuario", usuario.getId());
                httpAsyncTask.addParams("idNoticia", noticia.getIdNoticia());

                httpAsyncTask.post(new HttpAsyncTask.FutureCallback() {
                    @Override
                    public void onCallback(Object jsonObject, int responseCode) {
                        switch (responseCode) {
                            case 200:
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        opc = false;
                                        AtualizarTask atualizarTask = new AtualizarTask();
                                        atualizarTask.execute();
                                        final DetalhesTask task = new DetalhesTask();
                                        task.execute(null, null, null);
                                    }
                                });
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

    public class ListarCurtirTask extends AsyncTask<Void, Void, Void> {
        Curtir listCurtir;

        @Override
        protected Void doInBackground(Void... params) {
            String url = getString(R.string.url_rest) + "noticia/curtidas";
            listaDeCurtir = new ArrayList<>();
            try {

                HttpAsyncTask httpAsyncTask = new HttpAsyncTask(url, DetalheNoticiaActivity.this);
                httpAsyncTask.addParams("idNoticia", noticia.getIdNoticia());

                httpAsyncTask.post(new HttpAsyncTask.FutureCallback() {
                    @Override
                    public void onCallback(Object jsonObject, int responseCode) {
                        JSONArray jsonArray = (JSONArray) jsonObject;


                        try {

                            for (int i = 0; i < jsonArray.length(); i++) {

                                JSONObject json = jsonArray.getJSONObject(i);
                                listCurtir = new Curtir();
                                listCurtir.setId_usuario(json.getInt("id_usuario"));
                                listCurtir.setNome(json.getString("nome"));
                                listCurtir.setFoto(json.getString("foto"));
                                if(usuario!=null) {

                                    if (listCurtir.getId_usuario() == usuario.getId()) {
                                        opc = true;
                                    }
                                }


                                listaDeCurtir.add(listCurtir);

                            }
                            for (Curtir curtir : listaDeCurtir) {
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });


            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public class AtualizarTask extends AsyncTask<Void, Void, Void> {
        Drawable draw;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ListarCurtirTask listarCurtirTask = new ListarCurtirTask();
            listarCurtirTask.execute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            if (usuario != null) {
                if (opc) {
                    draw = ContextCompat.getDrawable(DetalheNoticiaActivity.this, R.drawable.ic_like_ok);


                } else {
                    draw = ContextCompat.getDrawable(DetalheNoticiaActivity.this, R.drawable.ic_like_off);
                }
            }else {
                draw = ContextCompat.getDrawable(DetalheNoticiaActivity.this, R.drawable.ic_like_off);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (opc) {
                like.setText(R.string.curtido);
            } else {
                like.setText(R.string.curtir);
            }
            like.setCompoundDrawablesWithIntrinsicBounds(draw, null, null, null);//left;
            curtidas.setText("" + listaDeCurtir.size());
        }
    }

    public void listarCurtidas(View view) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View viewCurtir = inflater.inflate(R.layout.curtidas_usuarios_alert, null);
        final ListView listViewUsuarioCurtidas = (ListView) viewCurtir.findViewById(R.id.listViewUsuarioCurtidas);

        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setView(viewCurtir);

        CurtidasUsuarioAdapter adpater = new CurtidasUsuarioAdapter(listaDeCurtir, getApplicationContext());
        listViewUsuarioCurtidas.setAdapter(adpater);

        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        alertDialog.show();
    }

    public void comentar(View view) {
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View viewComentar = layoutInflater.inflate(R.layout.comentar_alert_dialog, null);
        final EditText cometarEditText = (EditText) viewComentar.findViewById(R.id.cometarEditText);

        usuario = usuarioBO.get(null, null);
        if (usuario == null) {
            FazerLogin();
        } else {
            AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setView(viewComentar);

            alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Comentar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    comentarioUsuario = cometarEditText.getText().toString();
                    ComentarTask comentarTask = new ComentarTask();
                    comentarTask.execute(null, null, null);
                }
            });
            alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancelar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });

            alertDialog.show();
        }
    }

    public class ComentarTask extends AsyncTask<Void, Void, Void> {

        public ComentarTask(){}
        public ComentarTask(String comentario){
            comentarioUsuario=comentario;
        }


        @Override
        protected Void doInBackground(Void... params) {
            String url = getString(R.string.url_rest) + "noticia/comentar";

            try {
                HttpAsyncTask httpAsyncTask = new HttpAsyncTask(url, DetalheNoticiaActivity.this);
                httpAsyncTask.addParams("idUsuario", usuario.getId());
                httpAsyncTask.addParams("idNoticia", noticia.getIdNoticia());
                httpAsyncTask.addParams("comentario", comentarioUsuario);

                httpAsyncTask.post(new HttpAsyncTask.FutureCallback() {
                    @Override
                    public void onCallback(Object jsonObject, int responseCode) {
                        switch (responseCode) {
                            case 200:
                                ListarDeComentariosTask listarDeComentariosTask = new ListarDeComentariosTask();
                                listarDeComentariosTask.execute(null, null, null);
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

    public class ListarDeComentariosTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            String url = getString(R.string.url_rest) + "noticia/comentarios/"+noticia.getIdNoticia();
            try {
                HttpAsyncTask httpAsyncTask = new HttpAsyncTask(url, DetalheNoticiaActivity.this);
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
                                    comentar.setIdUsuario(json.getInt("idUsuario"));
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
            if(listar!=null){
                listarComentarios();
            }
        }
    }

    public void listarComentarios() {

        adpater = new ComentarAdapater(listaComente(listar), this);
        comertarListView.setAdapter(adpater);
        this.calculeHeightListView(adpater);
    }

    private List<Comentar> listaComente(List<Comentar> listar) {
        listaresun=new ArrayList<>();
        int i=listar.size();
        if(listar.size()>5){
            i=5;
            maisComentarios.setVisibility(View.VISIBLE);

        }else{
            maisComentarios.setVisibility(View.GONE);
        }
        for(int tamanho=0;tamanho<i;tamanho++){
            listaresun.add(listar.get(tamanho));
        }
        return listaresun;
    }

    private void calculeHeightListView(ComentarAdapater adapter) {
        int totalHeight = 0;
        int lenght = adapter.getCount();

        for (int i = 0; i < lenght; i++) {
            View listItem = adapter.getView(i, null, comertarListView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = comertarListView.getLayoutParams();
        params.height = totalHeight + (comertarListView.getDividerHeight() * (adapter.getCount() - 1));
        comertarListView.setLayoutParams(params);
        comertarListView.requestLayout();
    }

    public void listarTodosComentarios(View view) {
        Intent intent=new Intent(this, ListarTodosComentarios.class);
        intent.putExtra("idNoticia",noticia.getIdNoticia());
        startActivity(intent);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        final Comentar comentar = (Comentar) this.comertarListView.getItemAtPosition(info.position);

        if (usuario.getId()==(comentar.getIdUsuario())) {
            getMenuInflater().inflate(R.menu.menu_contexto_excluir_editar, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        final Comentar comentar = (Comentar) this.comertarListView.getItemAtPosition(info.position);

        switch (item.getItemId()) {
            case R.id.menu_item_editar:

                LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View viewComentar = layoutInflater.inflate(R.layout.comentar_alert_dialog, null);
                final EditText cometarEditText = (EditText) viewComentar.findViewById(R.id.cometarEditText);

                AlertDialog alert = new AlertDialog.Builder(this).create();
                alert.setView(viewComentar);
                cometarEditText.setText(comentar.getComentario());

                alert.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        comentar.setComentario(cometarEditText.getText().toString());
                        ComentarTask comentarTask = new ComentarTask(cometarEditText.getText().toString());
                        comentarTask.execute(null, null, null);
                        DeletarTask deletarTask = new DeletarTask(info.position);
                        deletarTask.execute(null, null, null);


                    }
                });
                alert.setButton(DialogInterface.BUTTON_NEGATIVE, "Cencelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                alert.show();
                return true;
            case R.id.menu_item_excluir:
                AlertDialog alertDialog = new AlertDialog.Builder(this).create();
                alertDialog.setMessage(getString(R.string.deseja_exclir));

                alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Sim", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DeletarTask deletarTask = new DeletarTask(info.position);
                        deletarTask.execute(null, null, null);
                    }
                });
                alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Não", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                alertDialog.show();

                return true;
        }
        return super.onContextItemSelected(item);
    }

    public class DeletarTask extends AsyncTask<Void, Void, Void> {
        private Comentar comentar;
        int position;

        public DeletarTask( int position) {
            this.comentar = listar.get(position);
            this.position=position;

        }

        @Override
        protected Void doInBackground(Void... params) {
            String url = getString(R.string.url_rest) + "noticia/descomentar/"+comentar.getId();
            try {

                HttpAsyncTask httpAsyncTask = new HttpAsyncTask(url, DetalheNoticiaActivity.this);

                try {
                    httpAsyncTask.get(new HttpAsyncTask.FutureCallback() {
                        @Override
                        public void onCallback(Object jsonObject, int responseCode) {
                            switch (responseCode) {
                                case 200:
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            listaresun.remove(position);
                                            adpater.notifyDataSetChanged();
                                            calculeHeightListView(adpater);
                                        }
                                    });
                                    break;
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
    }

}

