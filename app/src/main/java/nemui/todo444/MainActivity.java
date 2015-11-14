package nemui.todo444;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends Activity {

    private ArrayList<String> list;
    private ArrayAdapter<String> adapter;
    private ListView view;
    private AdapterView.OnItemClickListener listViewOnItemClickListener;

    //通信に関する設定てか準備？
    private static final String url ="http://133.27.171.234/ToDo444.php";
//    private static final String testurl ="http://133.27.171.234/ToDotest.php";

    private static final HttpClient client =new DefaultHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //起動時にソフトキーボードを表示しない
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        setContentView(R.layout.activity_main);


        view = (ListView) findViewById(R.id.listView);
        list = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_multiple_choice, list);
        view.setAdapter(adapter);

        //保存してあるデータを読み込む
        ListUpdate();

        //リストの行をクリックした時のイベントリスナーを作る。
        view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //ここに処理を書く
            }
        });


        //「+」ボタンについて
        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onEnter();
            }
        });

        //「チェックしたタスクが消えるすごいボタン」について
        Button button2 = (Button) findViewById(R.id.button2);
        button2.setText("チェックしたタスクが消えるすごいボタン");
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDelete();
            }
        });

        //Enterキーを押すとExitTextの入力内容を送信(+ボタンを押すのと同じ)
        EditText editText1=(EditText)findViewById(R.id.editText);
        editText1.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN
                        && keyCode == KeyEvent.KEYCODE_ENTER) {
                    onEnter();
                    return true;
                }
                return false;
            }
        });
    }

    public void onEnter() {

        //入力欄の設定
        EditText editText = (EditText) findViewById(R.id.editText);
        String text = editText.getText().toString();

        // 空白判定。textがnullでないかつ文字列の長さが0より大きい時のみaddする。
        if(StringUtils.isNotBlank(text)){
            adapter.add(text);
            int this_ID = list.size(); //list.sizeは要素の数を参照。list[2]まであるとき３を変えす
            httpPost(this_ID,text,0);
        }

        //ソフトキーボードを隠すための設定とか
        InputMethodManager imm
                = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow
                (editText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);



        System.out.println(text); //入力内容

        //入力後にEditTextの中身をclear
        editText.getText().clear();
    }

    public void onDelete(){
        //チェックボックスが入ってるid番号を探り、その番号をhttpDelete();する。
        httpPost(id, "", 0);
        ListUpdate();
    }

    public void ListUpdate(){
        httpGet();
        //もらったデータの数を数える＋名前を取り出さないと
        for(int i=0;i<10;i++){
            list.set(i,"");
        }

    }

    public void httpGet() {
        Thread thread = new getThread(url);
        thread.start();
    }

    public void httpPost(int id, String str,int checked) {
        String S_id = String.valueOf(id);
        String S_checked = String.valueOf(checked);

        Thread thread = new postThread(url,S_id,str,S_checked);
        thread.start();
    }

        //デリートは名前なし、checkboxがなしのデータをポストする扱いにす



        @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onDestroy(Bundle savedInstanceState){
        client.getConnectionManager().shutdown();

    }
}

class getThread extends Thread{
    private String url;
    private HttpClient client =new DefaultHttpClient();
    private HttpGet httpGet;
    private String jsonData;
    private JSONObject jsonObject;
    private JSONArray jsonArray;

    public JSONObject getJsonObject(){
        return this.jsonObject;
    }

    public JSONArray getJsonArray(){
        return this.jsonArray;
    }

    public getThread(String url){
        this.url=url;
        this.httpGet =new HttpGet(url);
    }
    public void run(){
        try {
            HttpResponse httpResponse = client.execute(httpGet);
        int status = httpResponse.getStatusLine().getStatusCode();
            if(HttpStatus.SC_OK == status){
                Log.d("gettest","getに成功") ;
                this.jsonData= EntityUtils.toString(httpResponse.getEntity(), "UTF-8"); //レスポンスを保存
                Log.d("HTTPGet", jsonData); //デバック用のログ表示
            }
            //JSON解析する
            try{
                this.jsonObject = new JSONObject(this.jsonData);
                this.jsonArray = this.jsonObject.getJSONArray("response");
            }catch(JSONException e){
                Log.e("Errer","Jsonデータがおかしいよ");
            }
    } catch (Exception ex) {
        System.out.println(ex);
    }finally{
        client.getConnectionManager().shutdown();
    }}
}



class postThread extends Thread{
    private String id;
    private String name;
    private String checked;
    private String url;
    private HttpClient client =new DefaultHttpClient();

    public postThread(String url,String id,String name, String checked){
        this.url=url;
        this.id = id;
        this.name = name;
        this.checked = checked;
    }
     @Override
     public void run() {
            List <BasicNameValuePair>  params = new ArrayList<BasicNameValuePair> ();
            BasicNameValuePair param1 =new BasicNameValuePair ("id",id);
            BasicNameValuePair param2 =new BasicNameValuePair ("taskName",name);
            BasicNameValuePair param3 =new BasicNameValuePair ("checked",checked);
            params.add(param1);
            params.add(param2);
            params.add(param3);

            try {
                HttpPost httpPost = new HttpPost(url);
                httpPost.setEntity(new UrlEncodedFormEntity(params, "utf-8"));//コード変更
                HttpResponse httpResponse = client.execute(httpPost);//実行

            } catch (Exception ex) {
                System.out.println(ex);
            }finally{
                client.getConnectionManager().shutdown();
            }
     }

}


