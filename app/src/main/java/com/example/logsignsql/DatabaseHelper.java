package com.example.logsignsql;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import static com.mongodb.client.model.Filters.eq;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.bson.Document;
public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String databaseName = "SignLog.db";

    public DatabaseHelper(@Nullable Context context) {
        super(context, "SignLog.db", null, 1);
    }
   public String uri = "mongodb+srv://DuuuKieee:<899767147>@loginserver.hqnkiia.mongodb.net/?retryWrites=true&w=majority&appName=LoginServer";
    @Override
    public void onCreate(SQLiteDatabase MyDatabase) {
        MyDatabase.execSQL("create Table users(email TEXT primary key,username TEXT, password TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase MyDB, int i, int i1) {
        MyDB.execSQL("drop Table if exists users");
    }

    public Boolean insertData(String email, String username, String password){
        SQLiteDatabase MyDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("email", email);
        contentValues.put("username", username);
        contentValues.put("password", password);
        long result = MyDatabase.insert("users", null, contentValues);

        if (result == -1) {
            return false;
        } else {
            return true;
        }
    }

    public Boolean checkEmail(String email){
        SQLiteDatabase MyDatabase = this.getWritableDatabase();
        Cursor cursor = MyDatabase.rawQuery("Select * from users where email = ?", new String[]{email});

        if(cursor.getCount() > 0) {
            return true;
        }else {
            return false;
        }
    }
    public Boolean checkUsernamePassword(String username, String password){
        SQLiteDatabase MyDatabase = this.getWritableDatabase();
        Cursor cursor = MyDatabase.rawQuery("Select * from users where username = ? and password = ?", new String[]{username, password});

        if (cursor.getCount() > 0) {
            return true;
        }else {
            return false;
        }
    }
    public String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
    public boolean signInMongoDB(String username, String password) {
        try (MongoClient mongoClient = MongoClients.create(uri)) {
            MongoDatabase database = mongoClient.getDatabase("AndroidDB");
            MongoCollection<Document> collection = database.getCollection("User");
            Document userDoc = collection.find(eq("username", username)).first();
            if (userDoc != null) {
                String hashedPassword = userDoc.getString("password");
                if (hashPassword(password).equals(hashedPassword)) {
                    return true;
                }
            }
            return false;
        }
    }

    public boolean signUpMongoDB(String email, String username, String password) {
        try (MongoClient mongoClient = MongoClients.create(uri)) {
            MongoDatabase database = mongoClient.getDatabase("AndroidDB");
            MongoCollection<Document> collection = database.getCollection("User");
            Document existingUser = collection.find(eq("username", username)).first();
            if (existingUser != null) {
                return false;
            }
            String hashedPassword = hashPassword(password);
            Document newUser = new Document("email", email)
                    .append("username", username)
                    .append("password", hashedPassword);
            collection.insertOne(newUser);

            return true;
        }
    }
}