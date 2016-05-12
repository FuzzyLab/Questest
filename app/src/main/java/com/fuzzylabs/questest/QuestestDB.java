package com.fuzzylabs.questest;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

public class QuestestDB {
    public static final String KEY_ROWID = "_id";
    public static final String ID = "id";
    public static final String EMAIL = "email";
    public static final String SIGNATURE = "signature";
    public static final String NAME = "name";
    public static final String AGE = "age";
    public static final String SEX = "sex";

    public static final String SUBJECT = "subject";
    public static final String QUESTION = "question";
    public static final String ANSWER = "answer";
    public static final String OPTIONA = "optionA";
    public static final String OPTIONB = "optionB";
    public static final String OPTIONC = "optionC";
    public static final String SOLUTION = "solution";
    public static final String MARKED = "marked";

    private static final String DATABASE_NAME = "Questest";
    public static final String USER_TABLE = "User";
    public static final String QUESTION_TABLE = "Question";
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
                    + EMAIL + " TEXT NOT NULL, "
                    + SIGNATURE + " TEXT NOT NULL, "
                    + NAME + " TEXT NOT NULL, "
                    + AGE + " TEXT NOT NULL, "
                    + SEX + " TEXT NOT NULL, "
                    + "UNIQUE (" + ID + ") ON CONFLICT REPLACE);");

            db.execSQL("CREATE TABLE " + QUESTION_TABLE + " ("
                    + KEY_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + ID + " TEXT NOT NULL, "
                    + SUBJECT + " TEXT NOT NULL, "
                    + QUESTION + " TEXT NOT NULL, "
                    + ANSWER + " TEXT NOT NULL, "
                    + OPTIONA + " TEXT NOT NULL, "
                    + OPTIONB + " TEXT NOT NULL, "
                    + OPTIONC + " TEXT NOT NULL, "
                    + SOLUTION + " TEXT NOT NULL, "
                    + MARKED + " TEXT NOT NULL, "
                    + "UNIQUE (" + ID + ") ON CONFLICT IGNORE);");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + USER_TABLE);
            db.execSQL("DROP TABLE IF EXISTS " + QUESTION_TABLE);
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

    public long saveUser(User user) {
        ContentValues cv = new ContentValues();
        cv.put(ID, user.getId());
        cv.put(EMAIL, user.getEmail());
        cv.put(SIGNATURE, user.getSignature());
        cv.put(NAME, TextUtils.isEmpty(user.getName())?"":user.getName());
        cv.put(AGE,TextUtils.isEmpty(user.getAge())?"":user.getAge());
        cv.put(SEX, TextUtils.isEmpty(user.getSex())?"":user.getSex());
        return ourDatabase.insert(USER_TABLE, null, cv);
    }

    public int getUserCount() {
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

    public long saveQuestion(Question question) {
        ContentValues cv = new ContentValues();
        cv.put(ID, question.getId());
        cv.put(SUBJECT, question.getSubject());
        cv.put(QUESTION, question.getQuestion());
        cv.put(ANSWER, question.getAnswer());
        cv.put(OPTIONA, question.getOptionA());
        cv.put(OPTIONB, question.getOptionB());
        cv.put(OPTIONC, question.getOptionC());
        cv.put(SOLUTION, question.getSolution());
        cv.put(MARKED, question.getMarked());
        return ourDatabase.insert(QUESTION_TABLE, null, cv);
    }

    public List<Question> getQuestions(String subject) {
        String[] columns = new String[] { KEY_ROWID,ID,SUBJECT,QUESTION,ANSWER,OPTIONA,OPTIONB,OPTIONC,SOLUTION,MARKED };
        Cursor c = ourDatabase.query(QUESTION_TABLE, columns, SUBJECT+" like ?", new String[]{subject}, null,
                null, null);
        List<Question> questions = new ArrayList<Question>();
        c.moveToNext();
        while(!c.isAfterLast()) {
            Question question = new Question();
            question.setId(c.getString(1));
            question.setSubject(c.getString(2));
            question.setQuestion(c.getString(3));
            question.setAnswer(c.getString(4));
            question.setOptionA(c.getString(5));
            question.setOptionB(c.getString(6));
            question.setOptionC(c.getString(7));
            question.setSolution(c.getString(8));
            question.setMarked(c.getString(9));
            questions.add(question);
            c.moveToNext();
        }
        return questions;
    }
}
