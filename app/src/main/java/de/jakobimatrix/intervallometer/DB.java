package de.jakobimatrix.intervallometer;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Vector;


public class DB extends SQLiteOpenHelper{

    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "intervallometerDB";
    private static final String TABLE_NAME = "functionDescription";
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_Y_UNIT = "yunit";
    private static final String KEY_DESCRIPTION = "description";
    Activity activity;

    public DB(Activity activity) {
        super(activity, DATABASE_NAME, null, DATABASE_VERSION);
        this.activity = activity;
        //3rd argument to be passed is CursorFactory instance
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_NAME + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_NAME + " TEXT," + KEY_Y_UNIT + " TEXT, " + KEY_DESCRIPTION + " TEXT" + ")";
        db.execSQL(CREATE_CONTACTS_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);

        // Create tables again
        onCreate(db);
    }

    public static void Json2Function(Activity activity, ArrayList<Function> fs, ArrayList<Double> start, ArrayList<Double>stop, JSONArray json_array) throws JSONException {
        fs.clear();
        start.clear();
        start.clear();
        for(int i = 0; i < json_array.length(); i++){
            JSONObject json_obj = json_array.getJSONObject(i);
            double x_min = json_obj.getDouble("x_min");
            double x_max = json_obj.getDouble("x_max");
            double x1  = json_obj.getDouble("x1");
            double y1  = json_obj.getDouble("y1");
            double x2  = json_obj.getDouble("x2");
            double y2  = json_obj.getDouble("y2");
            Pos3d left = new Pos3d(x1,y1,0);
            Pos3d right = new Pos3d(x2,y2, 0);
            String function = json_obj.getString("function_type");
            SUPPORTED_FUNCTION sf = Function.FunctionString2Enum(activity, function);
            Function f = Function.create(left, right, sf);
            if(f == null){
                // todo just do a linear function
                throw new IllegalArgumentException( "DB::Json2Function: could not create Function." );
            }
            fs.add(f);
            start.add(x_min);
            stop.add(x_max);
        }
    }

    private JSONObject function2Json(MovableFunction mf) {
        Function f = mf.getFunction();
        SUPPORTED_FUNCTION sp = Function.FunctionClass2Enum(f);
        String function_type = Function.FunctionEnum2String(activity,  sp);
        Pos3d p1 = new Pos3d(mf.getFunctionMinX(), f.f(mf.getFunctionMinX()), 0);
        Pos3d p2 = new Pos3d(mf.getFunctionMaxX(), f.f(mf.getFunctionMaxX()), 0);

        JSONObject json_function = new JSONObject();
        try {
            json_function.put("x_min", mf.getFunctionMinX());
            json_function.put("x_max", mf.getFunctionMaxX());
            json_function.put("function_type", function_type);
            json_function.put("x1", p1.x);
            json_function.put("y1", p1.y);
            json_function.put("x2", p2.x);
            json_function.put("y2", p2.y);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return json_function;
    }

    public void addFunctionDescription(Vector<MovableFunction> movable_functions, String name, Y_UNIT y_unit) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        JSONArray jsonArray = new JSONArray();
        for(MovableFunction mf: movable_functions) {
            jsonArray.put(function2Json(mf));
        }
        String function_description = jsonArray.toString();

        values.put(KEY_DESCRIPTION, function_description);
        values.put(KEY_NAME, name);
        values.put(KEY_Y_UNIT, Utility.Y_UNIT2String(activity, y_unit));

        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    public void copyFunctionDescription(int id){
        JSONArray ja = getFunctionDescription(id);
        if(ja != null){
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();

            String function_description = ja.toString();
            values.put(KEY_DESCRIPTION, function_description);
            values.put(KEY_NAME, getFunctionName(id) + "_c");
            values.put(KEY_Y_UNIT, getY_UNIT_S(id));

            db.insert(TABLE_NAME, null, values);
            db.close();
        }
    }

    public JSONArray getFunctionDescription(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_NAME, new String[] { KEY_ID,
                        KEY_DESCRIPTION}, KEY_ID + "=?",
                new String[] { String.valueOf(id) }, null, null, null, null);
        if (cursor == null){
            return null;
        }
        if(cursor.getCount() == 0){
            return null;
        }

        cursor.moveToFirst();
        try {
            int index = cursor.getColumnIndex(KEY_DESCRIPTION);
            return new JSONArray(cursor.getString(index));
        } catch (JSONException e) {
            e.printStackTrace();
            return  null;
        }
    }

    public String getFunctionName(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_NAME, new String[] { KEY_ID,
                        KEY_NAME}, KEY_ID + "=?",
                new String[] { String.valueOf(id) }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        return cursor.getString(cursor.getColumnIndex(KEY_NAME));
    }

    public String getY_UNIT_S(int id){
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_NAME, new String[] { KEY_ID,
                        KEY_Y_UNIT}, KEY_ID + "=?",
                new String[] { String.valueOf(id) }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        return cursor.getString(cursor.getColumnIndex(KEY_Y_UNIT));
    }

    public Y_UNIT getY_UNIT(int id){
        return Utility.String2Y_UNIT(activity, getY_UNIT_S(id));
    }

    public LinkedHashMap<Integer, JSONArray> getAllFunctionDescriptions() {
        LinkedHashMap<Integer, JSONArray> map = new LinkedHashMap<Integer, JSONArray>();
        String selectQuery = "SELECT  * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                JSONArray ja = null;
                int id = -1;
                try {
                    ja = new JSONArray(cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION)));
                    id = cursor.getInt(cursor.getColumnIndex(KEY_ID));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if(ja != null){
                    map.put(id, ja);
                }
            } while (cursor.moveToNext());
        }
        return map;
    }

    public LinkedHashMap<Integer, String> getAllFunctionNames() {
        LinkedHashMap<Integer, String> map = new LinkedHashMap<Integer, String>();
        String selectQuery = "SELECT  * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                String s = cursor.getString(cursor.getColumnIndex(KEY_NAME));
                int id = cursor.getInt(cursor.getColumnIndex(KEY_ID));
                map.put(id,s);
            } while (cursor.moveToNext());
        }
        return map;
    }

    public int updateFunctionDescription(int id, Vector<MovableFunction> movable_functions, String name, Y_UNIT y_unit) {
        SQLiteDatabase db = this.getWritableDatabase();
        JSONArray jsonArray = new JSONArray();
        for(MovableFunction mf: movable_functions) {
            jsonArray.put(function2Json(mf));
        }
        String function_description = jsonArray.toString();

        ContentValues values = new ContentValues();
        values.put(KEY_DESCRIPTION, function_description);
        values.put(KEY_NAME, name);
        values.put(KEY_Y_UNIT, Utility.Y_UNIT2String(activity, y_unit));

        // updating row
        return db.update(TABLE_NAME, values, KEY_ID + " = ?",
                new String[] { String.valueOf(id) });
    }

    public void deleteFunctionDescription(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, KEY_ID + " = ?",
                new String[] { String.valueOf(id) });
        db.close();
    }

    public int getFunctionDescriptionCount() {
        String countQuery = "SELECT  * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.close();

        // return count
        return cursor.getCount();
    }
}
