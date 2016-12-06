package br.com.mobi.redemicro.adapter;

import android.content.Context;
import android.graphics.Color;
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
import br.com.mobi.redemicro.bean.Mensagem;
import br.com.mobi.redemicro.bean.TopicosForum;

/**
 * Created by Gilberto on 05/12/2016.
 */

public class MensagemAdapter extends BaseAdapter {
    private List<Mensagem> lista;
    private LayoutInflater layoutInflater;
    private Context context;
    public MensagemAdapter(List<Mensagem> lista, Context context) {

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
        View view = layoutInflater.inflate(R.layout.layout_mensagem_adapter, null);

        Mensagem mensagem = lista.get(position);

       Picasso.with(context).load(mensagem.getFoto()).placeholder(android.R.drawable.ic_menu_camera).into((ImageView)view.findViewById(R.id.img_foto_res));

        TextView usuario=(TextView)view.findViewById(R.id.txt_usuario_resposta);
        usuario.setText(mensagem.getNome());
        TextView data=(TextView)view.findViewById(R.id.txt_data_resposta);
        if(mensagem.getData()!=null){
            data.setText(new SimpleDateFormat("dd/MM/yyyy").format(mensagem.getData()));
        }
        TextView msg=(TextView)view.findViewById(R.id.txt_msg_resposta);
        msg.setText(mensagem.getMensagem());

        return view;
    }

    @Override
    public int getCount() {
        return lista.size();
    }


}
