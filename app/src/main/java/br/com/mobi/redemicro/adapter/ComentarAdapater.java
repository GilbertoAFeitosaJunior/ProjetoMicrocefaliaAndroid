package br.com.mobi.redemicro.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.List;

import br.com.mobi.redemicro.R;
import br.com.mobi.redemicro.bean.Comentar;

/**
 * Created by Allesson on 28/11/2016.
 */

public class ComentarAdapater extends BaseAdapter {
    private List<Comentar> lista;
    private LayoutInflater layoutInflater;
    private Context context;
    private Comentar comentar;


    public ComentarAdapater(List<Comentar> lista, Context context) {
        this.lista = lista;
        this.context = context;
        this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return lista.size();
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
    public View getView(final int position, View convertView, ViewGroup parent) {
        final View view = layoutInflater.inflate(R.layout.layout_comentar_adapter, null);

        comentar = lista.get(position);


        Picasso.with(context).load(comentar.getFoto()).into((ImageView) view.findViewById(R.id.fotoComentar));

        TextView nomeUsuario = (TextView) view.findViewById(R.id.nomeUsuario);
        nomeUsuario.setText(comentar.getNome());

        TextView comentario = (TextView) view.findViewById(R.id.comentario);
        comentario.setText(comentar.getComentario());
        TextView data=(TextView) view.findViewById(R.id.data);
        data.setText(new SimpleDateFormat("HH:mm").format(comentar.getDate())+"hs "+new SimpleDateFormat(" dd/MM/yyyy").format(comentar.getDate()));
        return view;
    }
}
