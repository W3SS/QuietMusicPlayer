package android.cruciblecrab.quietmusicplayer;

import android.content.Context;
import android.content.Intent;
import android.database.CursorWrapper;
import android.media.MediaPlayer;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Toast;

import java.io.IOError;
import java.io.IOException;
import java.util.ArrayList;

public class ListActivity extends AppCompatActivity {
    public static final String TYPE_KEY = "typeKey1001";
    public static final String EXTRA_INFO = "typeAlbumName1001";
    public static final String FILTER_MODE = "typeFilterMode1001";
    public static final String NO_INFO = MediaSearcher.NO_INFO;
    public static final int KEY_SONGS = MediaSearcher.MODE_SONG;
    public static final int KEY_ALBUMS = MediaSearcher.MODE_ALBUM;
    public static final int KEY_ARTISTS = MediaSearcher.MODE_ARTIST;
    ListView list;
    MediaSearcher searcher;
    MediaLogic.LocalBinder binder;
    MediaControls mediaControls;
    private int mode;
    private String filter;
    private int filterMode;
    SeekBar seekBar;
    private Handler handler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        Intent infoIntent = getIntent();
        mode = infoIntent.getIntExtra(TYPE_KEY, 0);
        filter = infoIntent.getStringExtra(EXTRA_INFO);
        filterMode = infoIntent.getIntExtra(FILTER_MODE, 0);
        list = (ListView)findViewById(R.id.listactivitylist);
        list.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> myAdapter, View myView, int myItemInt, long mylng){
                    CursorWrapper selected = (CursorWrapper)(list.getItemAtPosition(myItemInt));
                    switch(mode){
                        case KEY_SONGS:
                            try {
                                songClickListener(selected);
                                setSongList(myItemInt);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            break;
                        case KEY_ALBUMS:
                            albumClickListener(selected); break;
                        case KEY_ARTISTS:
                            artistClickListener(selected); break;
                        default:
                            Log.d("XXX_L.A. onClick","No list clicker chosen, bad mode!");
                    }
                }
        });

        binder = MediaLogicConnection.getBinder();
        if(mode == KEY_SONGS){
            if(searcher==null){
                searcher = new MediaSearcher(this, list, MediaSearcher.MODE_SONG, filter, filterMode);
                getLoaderManager().initLoader(0, null, searcher);
            }
        }else if(mode == KEY_ALBUMS){
            if(searcher==null){
                searcher = new MediaSearcher(this, list, MediaSearcher.MODE_ALBUM, filter, filterMode);
                getLoaderManager().initLoader(0, null, searcher);
            }
        }else if(mode == KEY_ARTISTS){
            if(searcher==null){
                searcher = new MediaSearcher(this, list, MediaSearcher.MODE_ARTIST, filter, filterMode);
                getLoaderManager().initLoader(0, null, searcher);
            }
        }


        mediaControls = new MediaControls();
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        handler = new android.os.Handler();
        //Make sure you update Seekbar on UI thread
        ListActivity.this.runOnUiThread(new SeekbarRunnable(handler, seekBar));
        seekBar.setOnSeekBarChangeListener(mediaControls.seekBarChangeListener());

        Button playButton = (Button) findViewById(R.id.playbutton);
        playButton.setOnClickListener(mediaControls.playButtonListener());
        mediaControls.preparePlayButton(playButton);
        Button prevButton = (Button) findViewById(R.id.prevbutton);
        prevButton.setOnClickListener(mediaControls.prevButtonListener());
        Button nextButton = (Button) findViewById(R.id.nextbutton);
        nextButton.setOnClickListener(mediaControls.nextButtonListener());
    }

    private void songClickListener(CursorWrapper selected) throws IOException{
        String text = selected.getString(2);
        int id = selected.getInt(0);
        if(binder != null){
            Log.d("LIST_ACTIVITY", "Playing a song "+id);
            binder.playSong(id);
        }else{
            Log.d("LIST_ACTIVITY", "No binder on click " + id);
        }
        Toast toast=Toast.makeText(getApplicationContext(), text/*+" "+myItemInt*/, Toast.LENGTH_SHORT);
        toast.show();
    }

    private void albumClickListener(CursorWrapper selected){
        String name = selected.getString(1);
        Intent intent = new Intent(this, ListActivity.class);
        intent.putExtra(ListActivity.TYPE_KEY, ListActivity.KEY_SONGS);
        intent.putExtra(ListActivity.EXTRA_INFO, name);
        intent.putExtra(ListActivity.FILTER_MODE, MediaSearcher.MODE_ALBUM);
        startActivity(intent);
    }

    private void artistClickListener(CursorWrapper selected){
        String name = selected.getString(1);
        Intent intent = new Intent(this, ListActivity.class);
        intent.putExtra(ListActivity.TYPE_KEY, ListActivity.KEY_SONGS);
        intent.putExtra(ListActivity.EXTRA_INFO, name);
        intent.putExtra(ListActivity.FILTER_MODE, MediaSearcher.MODE_ARTIST);
        startActivity(intent);
    }

    private void setSongList(int startPoint) throws IOException{
        int length = list.getAdapter().getCount();
        ArrayList<Song> songs = new ArrayList<Song>();
        for(int i = 0; i < length; i++){
            CursorWrapper item = (CursorWrapper)list.getItemAtPosition(i);
            songs.add( new Song(item.getString(2), item.getInt(0)));
        }
        binder.setSongList(songs, startPoint);
        binder.startPlaying();
    }

}
