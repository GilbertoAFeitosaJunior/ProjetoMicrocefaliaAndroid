package br.com.mobi.redemicro.bean;

import java.io.Serializable;

import mobi.stos.podataka_lib.annotations.Column;
import mobi.stos.podataka_lib.annotations.Entity;
import mobi.stos.podataka_lib.annotations.PrimaryKey;

/**
 * Created by Allesson on 18/11/2016.
 */
@Entity
public class Noticia implements Serializable {
    @PrimaryKey (autoIncrement = false)
    private int idNoticia;
    @Column
    private String titulo;
    @Column
    private String chamada;
    @Column
    private String foto;
    @Column
    private int qtdCurtida;
    @Column
    private int qtdViews;
    @Column
    private int qtdComentarios;
    @Column
    private String fonte;
    @Column
    private String Data;
    @Column
    private String noticia;

    public int getQtdComentarios() {
        return qtdComentarios;
    }

    public void setQtdComentarios(int qtdComentarios) {
        this.qtdComentarios = qtdComentarios;
    }

    public int getIdNoticia() {
        return idNoticia;
    }

    public void setIdNoticia(int idNoticia) {
        this.idNoticia = idNoticia;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getChamada() {
        return chamada;
    }

    public void setChamada(String chamada) {
        this.chamada = chamada;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    public int getQtdCurtida() {
        return qtdCurtida;
    }

    public void setQtdCurtida(int qtdCurtida) {
        this.qtdCurtida = qtdCurtida;
    }

    public int getQtdViews() {
        return qtdViews;
    }

    public void setQtdViews(int qtdViews) {
        this.qtdViews = qtdViews;
    }

    public String getFonte() {
        return fonte;
    }

    public void setFonte(String fonte) {
        this.fonte = fonte;
    }

    public String getData() {
        return Data;
    }

    public void setData(String data) {
        Data = data;
    }

    public String getNoticia() {
        return noticia;
    }

    public void setNoticia(String noticia) {
        this.noticia = noticia;
    }
}
