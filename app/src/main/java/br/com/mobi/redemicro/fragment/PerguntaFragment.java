package br.com.mobi.redemicro.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import br.com.mobi.redemicro.ForumActivity;
import br.com.mobi.redemicro.ForumMsgActivity;
import br.com.mobi.redemicro.R;
import br.com.mobi.redemicro.adapter.TopicosForumAdapter;
import br.com.mobi.redemicro.bean.Mensagem;
import br.com.mobi.redemicro.bean.Pergunta;
import br.com.mobi.redemicro.bean.TopicosForum;
import br.com.mobi.redemicro.bean.Usuario;
import br.com.mobi.redemicro.bo.PerguntaBo;
import br.com.mobi.redemicro.bo.UsuarioBo;
import br.com.mobi.redemicro.util.Constants;
import br.com.mobi.redemicro.util.HttpAsyncTask;


public class PerguntaFragment extends Fragment {

    private ProgressDialog progressDialog;
    private int forumId;
    private Pergunta forum;
    private PerguntaBo perguntaBo;
    private Mensagem mensagem;
    private TextView titulo;
    private TextView msg;
    private TextView usuario;
    private TextView status;
    private TextView dataAbertura;
    private TextView dataFecha;
    private TextView moderador;
    private TextView especialdade;

    View view;

    private boolean opc;
    private boolean loading;

    public PerguntaFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences preferences = getContext().getSharedPreferences(Constants.APP, Context.MODE_PRIVATE);
        forumId = preferences.getInt("FORUMTOPICO", 0);



        MensagemTask mensagemTask=new MensagemTask();
        mensagemTask.execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.fragment_pergunta, container, false);
        perguntaBo=new PerguntaBo(getContext());
        forum = perguntaBo.get(null,null);

        titulo = (TextView) view.findViewById(R.id.txt_Titulo);
        msg = (TextView) view.findViewById(R.id.txt_msg);
        usuario = (TextView) view.findViewById(R.id.txt_usuario);
        status = (TextView) view.findViewById(R.id.txt_status);
        dataAbertura=(TextView) view.findViewById(R.id.txt_data);
        dataFecha=(TextView) view.findViewById(R.id.txt_data_fecha);
        dataFecha.setVisibility(View.GONE);
        moderador=(TextView) view.findViewById(R.id.txt_moderador);
        moderador.setVisibility(View.GONE);
        especialdade=(TextView) view.findViewById(R.id.txt_especialidade);
        especialdade.setVisibility(View.GONE);

        ((LinearLayout)view.findViewById(R.id.layout7)).setVisibility(View.GONE);



        Picasso.with(getContext()).load(forum.getFotoUsuario()).placeholder(android.R.drawable.ic_menu_camera).into((ImageView)view.findViewById(R.id.img_foto));
        titulo.setText(forum.getTitulo());
        msg.setText(forum.getMensagem());
        usuario.setText(forum.getNomeUsuario());
        dataAbertura.setText(new SimpleDateFormat("dd/MM/yyyy").format(forum.getDataAbertura()));

        if (forum.isAtivo() != true) {
            status.setText(getContext().getString(R.string.ativo));
            status.setTextColor(Color.parseColor("#00ff00"));
            dataFecha.setText(new SimpleDateFormat("dd/MM/yyyy").format(forum.getDataFechamento()));
            dataAbertura.setVisibility(View.VISIBLE);
            moderador.setText(forum.getNomeModerador());
            moderador.setVisibility(View.VISIBLE);
            especialdade.setText(forum.getEspecialidade());
            especialdade.setVisibility(View.VISIBLE);
        } else {
            status.setText(getContext().getString(R.string.fechado));
            status.setTextColor(Color.parseColor("#ff0000"));
        }



        return view;
    }

    private class MensagemTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(getContext());
            progressDialog.setMessage(getString(R.string.carregando));
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(true);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                String url = getString(R.string.url_rest) + "forum/mensagem/"+forumId;
                HttpAsyncTask task = new HttpAsyncTask(url, getContext());

                try {
                    task.get(new HttpAsyncTask.FutureCallback() {
                        @Override
                        public void onCallback(Object jsonObject, int responseCode) {
                            if (responseCode == 200) {
                                try {
                                    JSONArray jsonArray = (JSONArray) jsonObject;

                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        JSONObject json = jsonArray.getJSONObject(i);
                                      if(json.getBoolean("revisado")==true) {
                                            mensagem = new Mensagem();
                                            mensagem.setNome(json.getString("nome"));
                                            mensagem.setFoto(json.getString("foto"));
                                            mensagem.setMensagem(json.getString("mensagem"));
                                            mensagem.setRevisado(json.getBoolean("revisado"));
                                            try {
                                                mensagem.setData(new Date(json.getLong("data")));
                                            } catch (Exception e) {
                                                e.getMessage();
                                            }
                                            opc=true;
                                            break;
                                        }
                                        opc=false;

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
                createAdapterMsg();
            }

        }
    }
    private void createAdapterMsg() {
        if (opc) {
            ((LinearLayout)view.findViewById(R.id.layout7)).setVisibility(View.VISIBLE);
            Picasso.with(getContext()).load(mensagem.getFoto()).placeholder(android.R.drawable.ic_menu_camera).into((ImageView)view.findViewById(R.id.img_foto_res));

            TextView usuario=(TextView)view.findViewById(R.id.txt_usuario_resposta);
            usuario.setText(mensagem.getNome());
            TextView data=(TextView)view.findViewById(R.id.txt_data_resposta);
            if(mensagem.getData()!=null){
                data.setText(new SimpleDateFormat("dd/MM/yyyy").format(mensagem.getData()));
            }
            TextView msg=(TextView)view.findViewById(R.id.txt_msg_resposta);
            msg.setText(mensagem.getMensagem());
        }else {
            ((LinearLayout)view.findViewById(R.id.layout7)).setVisibility(View.GONE);
        }
        loading = true;
    }

}
