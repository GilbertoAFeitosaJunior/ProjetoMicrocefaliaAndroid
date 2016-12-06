package br.com.mobi.redemicro.dao;

import android.content.Context;

import br.com.mobi.redemicro.bean.Pergunta;
import mobi.stos.podataka_lib.repository.AbstractRepository;

/**
 * Created by Gilberto on 05/12/2016.
 */


public class PergutaDao extends AbstractRepository<Pergunta> {
    public PergutaDao (Context context){
        super(context,Pergunta.class);
    }
}
