package br.com.mobi.redemicro.adapter;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import br.com.mobi.redemicro.R;
import br.com.mobi.redemicro.bean.TopicosForum;

public class TopicosForumAdapter extends BaseAdapter {

    private List<TopicosForum> lista;
    private LayoutInflater layoutInflater;
    private Context context;

    public TopicosForumAdapter(List<TopicosForum> lista, Context context) {

        this.lista = lista;
        this.context = context;
        this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    @Override
    public Object getItem(int position) {
        return lista.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = layoutInflater.inflate(R.layout.layout_topicos_forum_adapter, null);

        TopicosForum topicosForum = lista.get(position);

        Picasso.with(context).load(topicosForum.getFotoUsuario()).placeholder(android.R.drawable.ic_menu_camera).into((ImageView)view.findViewById(R.id.img_foto));;

        TextView titulo=(TextView)view.findViewById(R.id.txt_Titulo);
        titulo.setText(topicosForum.getTitulo());
        TextView usuario=(TextView)view.findViewById(R.id.txt_usuario);
        usuario.setText(topicosForum.getNomeUsuario());
        TextView status=(TextView)view.findViewById(R.id.txt_status);
        if(topicosForum.isAtivo()!=true){
        status.setText(context.getString(R.string.ativo));
        }else{
            status.setText(context.getString(R.string.fechado));
        }
        TextView data=(TextView)view.findViewById(R.id.txt_data);
        if(topicosForum.getDataAbertura()!=null){
            data.setText(new SimpleDateFormat("dd/MM/yyyy").format(topicosForum.getDataAbertura()));
        }


        return view;
    }

    @Override
    public int getCount() {
        return lista == null ? 0 : lista.size();
    }

    public void add(TopicosForum topicosForum) {
        lista.add(topicosForum);
    }

    public boolean contains(TopicosForum topicosForum) {
        if (lista == null) return false;

        for (TopicosForum entity : lista) {
            if (entity.getIdTopico() == topicosForum.getIdTopico()) {
                return true;
            }
        }
        return false;
    }

}
