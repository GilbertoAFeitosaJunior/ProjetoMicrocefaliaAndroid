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
import br.com.mobi.redemicro.bean.Curtir;
import br.com.mobi.redemicro.bean.Noticia;

/**
 * Created by Allesson on 26/11/2016.
 */

public class CurtidasUsuarioAdapter extends BaseAdapter {

    private List<Curtir> lista;
    private LayoutInflater layoutInflater;
    private Context context;
    private ImageView foto;

    public CurtidasUsuarioAdapter(List<Curtir> lista, Context context) {

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
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = layoutInflater.inflate(R.layout.layout_curtidias_usuario_adapter, null);

        Curtir curtir = lista.get(position);

        foto = (ImageView) view.findViewById(R.id.fotoUsuarioCurtir);
        Picasso.with(context).load(curtir.getFoto()).placeholder(android.R.drawable.ic_menu_camera).into(foto);

        TextView nomeUsuarioCurtir = (TextView) view.findViewById(R.id.nomeUsuarioCurtir);
        nomeUsuarioCurtir.setText(curtir.getNome());

        return view;
    }

}
