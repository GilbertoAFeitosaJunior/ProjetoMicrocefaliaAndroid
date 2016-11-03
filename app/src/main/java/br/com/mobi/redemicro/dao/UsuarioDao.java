package br.com.mobi.redemicro.dao;

import android.content.Context;

import br.com.mobi.redemicro.bean.Usuario;
import mobi.stos.podataka_lib.repository.AbstractRepository;

public class UsuarioDao extends AbstractRepository<Usuario> {
    public UsuarioDao(Context context) {super(context, Usuario.class);}
}
