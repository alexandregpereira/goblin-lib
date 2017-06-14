package com.bano.goblin.database;

import android.os.Parcel;

import java.util.UUID;

/**
 *
 * Created by Alexandre on 09/05/2017.
 */

public abstract class Model {

    public final String id;

    protected Model(){
        id= UUID.randomUUID().toString();
    }

    protected Model(String id){
        this.id = id;
    }

    protected Model(Parcel in) {
        id = in.readString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Model model = (Model) o;

        return id.equals(model.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
