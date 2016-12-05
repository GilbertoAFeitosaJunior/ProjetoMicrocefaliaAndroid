package br.com.mobi.redemicro.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import br.com.mobi.redemicro.bean.TopicosForum;
import br.com.mobi.redemicro.bean.Usuario;
import br.com.mobi.redemicro.bo.UsuarioBo;
import br.com.mobi.redemicro.util.Constants;
import br.com.mobi.redemicro.util.HttpAsyncTask;


public class PerguntaFragment extends Fragment {

    private String titulo = "", msg = "";
    private UsuarioBo usuarioBO;
    private Usuario usuario;
    private ProgressDialog progressDialog;
    private int forumId;
    TopicosForum forum = new TopicosForum();

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.fragment_pergunta, container, false);

        return view;
    }

    private class ListForumTask extends AsyncTask<Void, Void, Void> {

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
                String url = getString(R.string.url_rest) + "forum/mensagem" + forumId;
                HttpAsyncTask task = new HttpAsyncTask(url, getContext());

                try {
                    task.get(new HttpAsyncTask.FutureCallback() {
                        @Override
                        public void onCallback(Object jsonObject, int responseCode) throws JSONException {
                            JSONObject json = (JSONObject) jsonObject;

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
                            } catch (Exception e) {
                                e.getMessage();
                            }
                            forum.setAtivo(json.getBoolean("ativo"));
                            opc = true;
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    opc = false;

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
        if (!opc) {
            Toast.makeText(getContext(), R.string.erro_internet, Toast.LENGTH_SHORT).show();
        } else {
            TextView titulo = (TextView) view.findViewById(R.id.txt_Titulo);
            TextView msg = (TextView) view.findViewById(R.id.txt_msg);
            TextView usuario = (TextView) view.findViewById(R.id.txt_usuario);
            TextView status = (TextView) view.findViewById(R.id.txt_status);
            TextView dataAbertura=(TextView) view.findViewById(R.id.txt_data);
            TextView dataFecha=(TextView) view.findViewById(R.id.txt_data_fecha);
            TextView moderador=(TextView) view.findViewById(R.id.txt_moderador);
            TextView especialdade=(TextView) view.findViewById(R.id.txt_especialidade);

            Picasso.with(getContext()).load(forum.getFotoUsuario()).placeholder(android.R.drawable.ic_menu_camera).into((ImageView)view.findViewById(R.id.img_foto));
            titulo.setText(forum.getTitulo());
            msg.setText(forum.getMensagem());
            usuario.setText(forum.getNomeUsuario());
            dataAbertura.setText(new SimpleDateFormat("dd/MM/yyyy").format(forum.getDataAbertura()));

            if (forum.isAtivo() != true) {
                status.setText(getContext().getString(R.string.ativo));
                status.setTextColor(Color.parseColor("#00ff00"));
                dataFecha.setText(new SimpleDateFormat("dd/MM/yyyy").format(forum.getDataFechamento()));
                moderador.setText(forum.getNomeModerador());
                especialdade.setText(forum.getEspecialidade());
            } else {
                status.setText(getContext().getString(R.string.fechado));
                status.setTextColor(Color.parseColor("#FF0000"));
            }
        }
    }

}
