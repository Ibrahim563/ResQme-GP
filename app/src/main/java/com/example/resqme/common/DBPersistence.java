package com.example.resqme.common;

import com.google.firebase.database.FirebaseDatabase;

public class DBPersistence extends android.app.Application{
    @Override
    public void onCreate() {
        super.onCreate();
        /* Enable disk persistence  */
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
