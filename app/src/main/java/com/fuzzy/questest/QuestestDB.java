package com.fuzzy.questest;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

public class QuestestDB {
        public static final String KEY_ROWID = "_id";
        public static final String ID = "id";
        public static final String EMAIL = "email";
        public static final String SIGNATURE = "signature";
        public static final String NAME = "name";
        public static final String AGE = "age";
        public static final String SEX = "sex";

        private static final String DATABASE_NAME = "Questest";
        public static final String USER_TABLE = "User";
        private static final int DATABASE_VERSION = 1;

        private DbHelper ourHelper;
        private final Context ourContext;
        SQLiteDatabase ourDatabase;

        private static class DbHelper extends SQLiteOpenHelper {

            public DbHelper(Context context) {
                super(context, DATABASE_NAME, null, DATABASE_VERSION);
            }

            @Override
            public void onCreate(SQLiteDatabase db) {
                db.execSQL("CREATE TABLE " + USER_TABLE + " ("
                        + KEY_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + ID + " TEXT NOT NULL, "
                        + EMAIL + " INTEGER NOT NULL, "
                        + SIGNATURE + " INTEGER NOT NULL, "
                        + NAME + " INTEGER NOT NULL, "
                        + AGE + " TEXT NOT NULL, "
                        + SEX + " TEXT NOT NULL, "
                        + "UNIQUE (" + ID + ") ON CONFLICT IGNORE);");
            }

            @Override
            public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
                db.execSQL("DROP TABLE IF EXISTS " + USER_TABLE);
                onCreate(db);
            }
        }

        public QuestestDB(Context c) {
            ourContext = c;
        }

        public QuestestDB open() throws SQLException {
            ourHelper = new DbHelper(ourContext);
            ourDatabase = ourHelper.getWritableDatabase();
            return this;
        }

        public void close() {
            ourHelper.close();
        }

        public long createEntry(User user) {
            ContentValues cv = new ContentValues();
            cv.put(ID, user.getId());
            cv.put(EMAIL, user.getEmail());
            cv.put(SIGNATURE, user.getSignature());
            cv.put(NAME, TextUtils.isEmpty(user.getName())?"":user.getName());
            cv.put(AGE,TextUtils.isEmpty(user.getAge())?"":user.getAge());
            cv.put(SEX, TextUtils.isEmpty(user.getSex())?"":user.getSex());
            return ourDatabase.insert(USER_TABLE, null, cv);
        }

        public int getCount() {
            int count = 0;
            Cursor mCount = ourDatabase.rawQuery(
                    "select count(*) from "+USER_TABLE, null);
            mCount.moveToFirst();
            count = mCount.getInt(0);
            mCount.close();
            return count;
        }

        public User getUser() {
            String[] columns = new String[] { KEY_ROWID, ID, EMAIL, SIGNATURE, NAME, AGE, SEX };
            Cursor c = ourDatabase.query(USER_TABLE, columns, null, null, null,
                    null, null);
            c.moveToFirst();
            User user = new User();
            user.setId(c.getString(1));
            user.setEmail(c.getString(2));
            user.setSignature(c.getString(3));
            user.setName(c.getString(4));
            user.setAge(c.getString(5));
            user.setSex(c.getString(6));
            return user;
        }
}
