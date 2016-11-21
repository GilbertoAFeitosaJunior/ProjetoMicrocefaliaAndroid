package br.com.mobi.redemicro.bean;

import java.util.Date;

/**
 * Created by Allesson on 18/11/2016.
 */

public class Comentar {
    int id;
    String nome;
    String foto;
    String comentario;
    Date date;

    public int getId() {
        return id;
    }

    public void setId(int i) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
