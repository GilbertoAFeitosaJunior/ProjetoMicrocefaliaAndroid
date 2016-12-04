package br.com.mobi.redemicro;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.ActionMenuItemView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import br.com.mobi.redemicro.adapter.HospitalAdapter;
import br.com.mobi.redemicro.bean.Hospital;
import br.com.mobi.redemicro.util.Constants;
import br.com.mobi.redemicro.util.HttpAsyncTask;

public class HospitalActivity extends AppCompatActivity {

    private List<Hospital> hospitalList;

    private boolean opc;
    private boolean loading;
    private HospitalAdapter adapter;
    private ListView listViewHopitais;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hospital);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        listViewHopitais = (ListView) findViewById(R.id.listViewHospitais);
        HospitalTask hospitalTask = new HospitalTask();
        hospitalTask.execute();

        /*listViewHopitais.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Hospital hospital = hospitalList.get(position);
                LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                final View viewHospital = layoutInflater.inflate(R.layout.hospital_alert_dialog, null);
                Button ligar = (Button) viewHospital.findViewById(R.id.foneButton);
                Button compartilhar = (Button) viewHospital.findViewById(R.id.shareButton);
                ligar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            Intent intent = new Intent(Intent.ACTION_CALL);
                            intent.setData(Uri.parse("tel:" + hospital.getFone()));
                            if (ActivityCompat.checkSelfPermission(HospitalActivity.this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                                // TODO: Consider calling
                                //    ActivityCompat#requestPermissions
                                // here to request the missing permissions, and then overriding
                                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                //                                          int[] grantResults)
                                // to handle the case where the user grants the permission. See the documentation
                                // for ActivityCompat#requestPermissions for more details.
                                return;
                            }
                            startActivity(intent);
                        }catch (Exception e){
                            e.getMessage();
                        }
                    }
                });
                compartilhar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            String mensagem = "APP: "+getString(R.string.app_name)+ "\n\n"
                                    +hospital.getNome() + "\n\nLocalização: "+hospital.getLocalizacao()+"\nFone: "
                                    +hospital.getFone()+"  \nPúblico: "+hospital.getPublico()+"\n\n"+hospital.getAtendimento() ;
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_SEND);
                            intent.putExtra(Intent.EXTRA_TEXT, mensagem);
                            intent.setType("text/plain");
                            startActivity(Intent.createChooser(intent, mensagem));
                            startActivity(intent);
                        }catch (Exception e){
                            e.getMessage();
                        }
                    }
                });

            }
        });*/

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class HospitalTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            try {
                SharedPreferences preferences = getSharedPreferences(Constants.APP, MODE_PRIVATE);
                int hospitalId = preferences.getInt("ESTADO", 0);
                String url = getString(R.string.url_rest) + "hospital/hospitais/"+hospitalId;
                HttpAsyncTask task = new HttpAsyncTask(url, HospitalActivity.this);

                try {
                    task.get(new HttpAsyncTask.FutureCallback() {
                        @Override
                        public void onCallback(Object jsonObject, int responseCode) {
                            if (responseCode == 200) {
                                try {
                                    hospitalList = new ArrayList<>();
                                    JSONArray jsonArray = (JSONArray) jsonObject;

                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        JSONObject json = jsonArray.getJSONObject(i);
                                        Hospital hospital=new Hospital();

                                        hospital.setIdHospital(json.getInt("id"));
                                        hospital.setNome(json.getString("nome"));
                                        hospital.setEstados(json.getString("estados"));
                                        hospital.setLocalizacao(json.getString("localizacao"));
                                        hospital.setDdd(json.getInt("ddd"));
                                        hospital.setFone(json.getInt("fone"));
                                        hospital.setPublico(json.getString("publico"));
                                        hospital.setAtendimento(json.getString("atendimento"));
                                        hospital.setLatitude(json.getDouble("latitude"));
                                        hospital.setLongitude(json.getDouble("longitude"));

                                        hospitalList.add(hospital);
                                        opc=true;
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
            if (!isCancelled()) {
                createAdapter();
            }

        }
    }
    private void createAdapter() {
        adapter = new HospitalAdapter(hospitalList, HospitalActivity.this);
        if (!opc) {
            Toast.makeText(this,R.string.erro_internet, Toast.LENGTH_SHORT).show();
            HospitalActivity.this.finish();
        }else {
            for(Hospital hospital: hospitalList){
                //synchronized (adapter) {
                if (!adapter.contains(hospital)) {
                    adapter.add(hospital);
                    //  }
                }
            }
            this.listViewHopitais.setAdapter(adapter);
        }

        loading = true;
    }
}
