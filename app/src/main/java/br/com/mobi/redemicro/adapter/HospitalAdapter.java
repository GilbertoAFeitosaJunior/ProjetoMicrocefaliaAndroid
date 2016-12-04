package br.com.mobi.redemicro.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.List;

import br.com.mobi.redemicro.R;
import br.com.mobi.redemicro.bean.Hospital;
import br.com.mobi.redemicro.util.Mask;

/**
 * Created by Allesson on 02/12/2016.
 */

public class HospitalAdapter extends BaseAdapter {
    private List<Hospital> lista;
    private LayoutInflater layoutInflater;
    private Button shareButton, ligarButton, rotaButton;

    public HospitalAdapter(List<Hospital> lista, Context context) {

        this.lista = lista;
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
        View view = layoutInflater.inflate(R.layout.layout_hospital_adapter, null);

        final Hospital hospital = lista.get(position);

        TextView nome=(TextView)view.findViewById(R.id.nome);
        nome.setText(hospital.getNome());
        TextView localizacao=(TextView)view.findViewById(R.id.localizacao);
        localizacao.setText(hospital.getLocalizacao());
        TextView fone=(TextView)view.findViewById(R.id.fone);
        fone.setText("("+hospital.getDdd()+")"+ Integer.toString(hospital.getFone()).substring(0,4)+"-"+Integer.toString(hospital.getFone()).substring(4,8));
        TextView publico=(TextView)view.findViewById(R.id.publico);
        publico.setText(hospital.getPublico());
        TextView atendimento=(TextView)view.findViewById(R.id.atendimento);
        atendimento.setText(hospital.getAtendimento());

        ligarButton=(Button)view.findViewById(R.id.foneButton);
        shareButton=(Button)view.findViewById(R.id.shareButton);
        rotaButton=(Button) view.findViewById(R.id.rotaButton);

        ligarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new Intent(Intent.ACTION_CALL);
                    intent.setData(Uri.parse("tel:0" +hospital.getDdd()+""+ hospital.getFone()));
                    if (ActivityCompat.checkSelfPermission(v.getContext(), android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    v.getContext().startActivity(intent);
                }catch (Exception e){
                    e.getMessage();
                }
            }
        });
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String mensagem = "APP: "+v.getContext().getString(R.string.app_name)+ "\n\n"
                            +hospital.getNome() + "\n\nLocalização: "+hospital.getLocalizacao()+"\nFone: "
                            +"("+hospital.getDdd()+")"+hospital.getFone()+"  \nPúblico: "+hospital.getPublico()+"\n\n"+hospital.getAtendimento() ;
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_SEND);
                    intent.putExtra(Intent.EXTRA_TEXT, mensagem);
                    intent.setType("text/plain");
                    v.getContext().startActivity(Intent.createChooser(intent, mensagem));
                    v.getContext().startActivity(intent);
                }catch (Exception e){
                    e.getMessage();
                }
            }
        });

        rotaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                            Uri.parse("geo:0,0?q=" + hospital.getLatitude() + "," + hospital.getLongitude() + "(" + hospital.getNome() + ")"));
                    v.getContext().startActivity(intent);
                }catch (Exception e){
                    e.getMessage();
                }
            }
        });

        return view;
    }

    @Override
    public int getCount() {
        return lista == null ? 0 : lista.size();
    }

    public void add(Hospital hospital) {
        lista.add(hospital);
    }

    public boolean contains(Hospital hospital) {
        if (lista == null) return false;

        for (Hospital entity : lista) {
            if (entity.getIdHospital()==hospital.getIdHospital()) {
                return true;
            }
        }
        return false;
    }
}
