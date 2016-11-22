package br.com.mobi.redemicro;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.net.MalformedURLException;
import java.util.List;

import br.com.mobi.redemicro.bean.Comentar;
import br.com.mobi.redemicro.bean.Noticia;
import br.com.mobi.redemicro.bean.Usuario;
import br.com.mobi.redemicro.bo.NoticiaBo;
import br.com.mobi.redemicro.bo.UsuarioBo;
import br.com.mobi.redemicro.util.Constants;
import br.com.mobi.redemicro.util.HttpAsyncTask;

public class DetalheNoticiaActivity extends AppCompatActivity {

    private Noticia noticia;
    private ImageView imagemDetalhesNoticia;
    private Button like, curtidas;
    private TextView conteudoDetalhes, tituloDetalhes, dataPublicacaoDetalhes;
    private ListView comertarListView;
    private NoticiaBo noticiaBO;
    private boolean opc = false;
    private UsuarioBo usuarioBO;
    private Usuario usuario;
    //private List<Curtir> listaDeCurtir;
    //private CurtirBO curtirBO;
    private String curtirDiscurtir;
    private Integer tamanho;
    private String comentarioUsuario = "";
    //private ComentarBO comentarBO;
    private List<Comentar> listar;
    //private ComentarAdapater adpater;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhe_noticia);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //listaDeCurtir = new ArrayList<>();
        //curtirBO = new CurtirBO(this);
        noticiaBO = new NoticiaBo(this);
        //comentarBO = new ComentarBO(this);

        //galeriaNoticiaBO = new GaleriaNoticiaBO(this);
        imagemDetalhesNoticia = (ImageView) findViewById(R.id.imagemDetalhesNoticia);
        conteudoDetalhes = (TextView) findViewById(R.id.conteudoDetalhes);
        tituloDetalhes = (TextView) findViewById(R.id.tituloDetalhes);
        dataPublicacaoDetalhes = (TextView) findViewById(R.id.dataPublicacaoDetalhes);
        curtidas = (Button) findViewById(R.id.curtidas);
        like = (Button) findViewById(R.id.like);

        comertarListView = (ListView) findViewById(R.id.comertarListView);


        Intent it = getIntent();
        //listaDeCurtir = curtirBO.list();

        final DetalhesTask task = new DetalhesTask();
        task.execute(null, null, null);

        //ListarDeComentariosTask listarDeComentariosTask = new ListarDeComentariosTask();
        //listarDeComentariosTask.execute(null, null, null);

        usuarioBO = new UsuarioBo(this);
        usuario = usuarioBO.get(null, null);

        like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*if (!opc) {
                    if (usuario != null) {
                        curtir();

                    } else {
                        FazerLogin();
                    }
                } else {
                    discurtir();
                }*/
            }
        });

        if ((usuario != null)) {
            registerForContextMenu(this.comertarListView);
        }
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
                String url=getString(R.string.url_rest)+"noticia/exibir/"+noticiaId;

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
                                //noticia.setChamada(json.getString("chamada"));
                                noticia.setData(json.getString("data"));
                                noticia.setFonte(json.getString("fonte"));
                                noticia.setNoticia(json.getString("noticia"));


                                noticiaBO.clean();
                                noticiaBO.insert(noticia);
                                opc=true;
                            } catch (Exception e) {
                                e.printStackTrace();
                                opc=false;
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

}

