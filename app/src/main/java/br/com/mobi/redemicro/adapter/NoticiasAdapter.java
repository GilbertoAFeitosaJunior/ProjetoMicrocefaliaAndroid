package br.com.mobi.redemicro.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import br.com.mobi.redemicro.R;
import br.com.mobi.redemicro.bean.Noticia;

/**
 * Created by Allesson on 18/11/2016.
 */

public class NoticiasAdapter extends BaseAdapter {
    private List<Noticia> lista;
    private LayoutInflater layoutInflater;
    private Context context;

    public NoticiasAdapter(List<Noticia> lista, Context context) {

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
        View view = layoutInflater.inflate(R.layout.layout_noticias_adapter, null);

        Noticia noticia = lista.get(position);

        ImageView fotoNoticias = (ImageView) view.findViewById(R.id.fotoNoticias);

        Picasso.with(context).load(noticia.getFoto()).placeholder(android.R.drawable.ic_menu_camera).into(fotoNoticias);

        TextView titulo = (TextView) view.findViewById(R.id.tituloNoticias);
        titulo.setText(noticia.getTitulo());

        TextView chamada = (TextView) view.findViewById(R.id.chamadaNoticias);
        chamada.setText(noticia.getChamada());

        TextView viewNoticias = (TextView) view.findViewById(R.id.viewNoticias);
        Integer viewsInteger = noticia.getQtdViews();
        viewNoticias.setText(viewsInteger.toString());

        TextView numCurtir = (TextView) view.findViewById(R.id.curtirNoticias);
        Integer qtdCurtidas = noticia.getQtdCurtida();
        numCurtir.setText(qtdCurtidas.toString());

        TextView numComentar = (TextView) view.findViewById(R.id.comentariosNoticias);
        Integer qtdComentar = noticia.getQtdComentarios();
        numComentar.setText(qtdComentar.toString());
        return view;
    }

    @Override
    public int getCount() {
        return lista == null ? 0 : lista.size();
    }

    public void add(Noticia noticia) {
        lista.add(noticia);
    }

    public boolean contains(Noticia noticia) {
        if (lista == null) return false;

        for (Noticia entity : lista) {
            if (entity.getIdNoticia() == noticia.getIdNoticia()) {
                return true;
            }
        }
        return false;
    }
}
