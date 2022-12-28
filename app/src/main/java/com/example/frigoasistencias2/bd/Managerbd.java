package com.example.frigoasistencias2.bd;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class Managerbd extends SQLiteOpenHelper {
    String t_registro = "Create table t_registro (id_registro integer primary key autoincrement, id_libro integer,cedula varchar(20),fechaingreso varchar(30),estado varchar(50),estadoeliminar varchar(2) default 'A',estadosubido varchar(2) default 'P')";
    String t_libro = "Create table t_libro(id_libro integer primary key autoincrement,id_administrador integer,fechaingreso varchar(50),estadobase varchar(2) default 'P')";

    //estado: si el usuario esta en otra area o no ha marcado
    //estadoeliminar : si se elimina el usuario de la list   'E' = eliminado 'A'= activo
    //estadosubido: si la informacion fue subida al sistema principal 'P' = pendiente 'S' = subido

    public Managerbd(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(t_libro);
        db.execSQL(t_registro);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {    }
}
