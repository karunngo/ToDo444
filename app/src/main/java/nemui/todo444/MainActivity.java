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
import org.apache.http.util.EntityUtils;
import org.apache.http.HttpStatus;


public class MainActivity extends Activity {

    private ArrayList<String> list;
    private ArrayAdapter<String> adapter;
    private ListView view;
    private AdapterView.OnItemClickListener listViewOnItemClickListener;

    //通信に関する設定てか準備？
    private static final String url ="http://133.27.171.234/ToDo444.php";
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
//        list.add("んごすぎ");


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

        //Enterキーを押すとExitTextの入力内容を送信(+ボタンを押すのと同じ)
        EditText editText1=(EditText)findViewById(R.id.editText);
        editText1.setOnKeyListener(new View.OnKeyListener(){
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

    //phhGetってメソッドをつくっちゃう。スレッド機能もつけとくよ
    public void phhGet() {
        new Thread(new Runnable() {
            HttpGet httpGet = new HttpGet(url); //getのための準備。listenerの準備みたいなの
            @Override
            public void run() {
                try {
                    HttpResponse httpResponse = client.execute(httpGet);//ここで実行！のはず
                    String str = EntityUtils.toString(httpResponse.getEntity(), "UTF-8"); //レスポンスをstringにする
                    Log.d("HTTPGet", str); //デバック用のログ表示
                } catch (Exception ex) {
                    System.out.println(ex);
                }
            }

        }).start();
    }
/*
    //phhPutってメソッドをつくっちゃう。スレッド機能もつけとくよ
    public void phhPost(☆) {
        new Thread(new Runnable() {
            HttpPost httpPost = new HttpPost(url); //準備。phhGet()参照
            ArrayList<NameValuePair> params = new ArrayList <NameValuePair>();
            ↑送る用のリスト。NameValuePairってのは、名前と要素を一緒に送れるらしい。
            　非推奨？　細けぇこたぁいいんだよ！　phpはこの名前で反応してくれるみたい


            params.add( new BasicNameValuePair("taskName","var"));
            //なんでaddが機能しないの!? んごーーーーー

            @Override
            public void run() {
                try {
                    httpPost.setEntity(new UrlEncodedFormEntity(params, "utf-8"));//コード変更
                    HttpResponse httpResponse = httpPost.execute(httpPost);//実行するはず
                    Log.d("HTTPGet", str); //デバック用のログ表示
                } catch (Exception ex) {
                    System.out.println(ex);
                }
            }

        }).start();
    }
*/




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
}
