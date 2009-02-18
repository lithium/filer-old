package org.hlidskialf.filer;

import android.app.ListActivity;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.view.View;
import android.view.Menu;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.os.Bundle;
import android.os.Environment;
import android.net.Uri;

import android.widget.AdapterView.AdapterContextMenuInfo;

import java.io.File;
import java.lang.String;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import android.util.Log;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class FileListActivity extends ListActivity 
{
  
    private BroadcastReceiver pReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) { fillData(pCurDir); }
    };
    private File pCurDir;
    private List<String> pCurFiles;
    private File pYanked = null;
    private final File pRootFile = new File(Environment.getExternalStorageDirectory().toString());
    private boolean pIgnoreNextClick = false;
    private boolean pCreatingShortcut = false;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.filer);

      Intent i = getIntent();

      pCreatingShortcut = Intent.ACTION_CREATE_SHORTCUT.equals(i.getAction());

      if (pCreatingShortcut) {
        Toast t = Toast.makeText(this, "Longclick to create a shortcut", Toast.LENGTH_LONG);
        t.show();
      }

      pCurDir = pRootFile;

      Uri uri = i.getData();
      if (uri != null) {
        File f = new File(uri.getPath());
        if (f.isDirectory())
          pCurDir = f;
        else { // file viewer
          Log.v("Filer", "view file");
          finish();
        }
      }

      registerForContextMenu(getListView());  
    }

    @Override
    public void onResume()
    {
      super.onResume();
    
      IntentFilter filt = new IntentFilter();
      filt.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
      filt.addAction(Intent.ACTION_MEDIA_MOUNTED);
      filt.addDataScheme("file");
      registerReceiver(pReceiver, filt);

      fillData(pCurDir);
    }
    @Override
    public void onPause()
    {
      super.onPause();
      unregisterReceiver(pReceiver);
    }


    private void fillData(File new_dir)
    {
      pCurDir = new_dir;
      try {
        setTitle(pCurDir.getCanonicalPath());
      } catch (Exception e) {}

      View empty = findViewById(R.id.empty);

      ArrayList<String> files = new ArrayList<String>();

      String state = Environment.getExternalStorageState();
      if (Environment.MEDIA_MOUNTED.equals(state))  {
        try {
          if (!pCurDir.getCanonicalPath().equals(pRootFile.getCanonicalPath()))
            files.add("..");
        } catch (Exception e) {}
        files.addAll(Arrays.asList(pCurDir.list()));
        empty.setVisibility(View.INVISIBLE);
      }
      else {
        empty.setVisibility(View.VISIBLE);
      }

      Collections.sort(files, new Comparator() {
        public int compare(Object a, Object b) {
          if (((String)a).equals("..")) return -1;
          if (((String)b).equals("..")) return 1;
          File fa = new File(pCurDir, (String)a); 
          File fb = new File(pCurDir, (String)b); 
          if (fa.isDirectory()) {
            if (fb.isDirectory()) 
              return fa.getName().compareTo( fb.getName() );
            return -1;
          }
          if (fb.isDirectory()) 
            return 1;
          return 0;
        }
      });
      pCurFiles = files;

      ArrayAdapter<String> file_adapter = new ArrayAdapter(this, R.layout.file_list_item, pCurFiles) {
        public View getView(int position, View v, ViewGroup parent)
        {
          String name = (String)pCurFiles.get(position);
          LayoutInflater li = getLayoutInflater();
          View ret = li.inflate(R.layout.file_list_item, parent, false);

          TextView txt = (TextView)ret.findViewById(R.id.row_text);
          txt.setText(name);

          File f = new File(pCurDir, name);

          if (f.isDirectory()) {
            ImageView img = (ImageView)ret.findViewById(R.id.row_icon);
            img.setImageResource(android.R.drawable.ic_menu_more);

          }
          else {
            txt = (TextView)ret.findViewById(R.id.row_type);
            txt.setText("text/*");
            txt = (TextView)ret.findViewById(R.id.row_size);
            txt.setText(String.valueOf(f.length()));

            txt = (TextView)ret.findViewById(R.id.row_mtime);
            txt.setText(String.valueOf(f.lastModified()));
          }

          return ret;

        }
      };
  
      setListAdapter(file_adapter);
    }


    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) 
    {
  

      if (pIgnoreNextClick) {
        pIgnoreNextClick = false;
        return;
      }
      super.onListItemClick(l,v,position,id);

      File f = new File(pCurDir, (String)pCurFiles.get(position));

      if (f.isDirectory()) {
        fillData(f);
      }
      else {
        if (pCreatingShortcut) {
          create_shortcut(f);
        }
        else {

          try {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setDataAndType(Uri.fromFile(f), "text/*");
            startActivity(i);
          } catch (android.content.ActivityNotFoundException ex) { }

        }
      }
    }

    private void create_shortcut(File f)
    {
      int draw;
      String type;
      String action = Intent.ACTION_VIEW;

      if (f.isDirectory()) {
        type = "text/directory";
        action = Intent.ACTION_RUN;
        draw = R.drawable.mime_folder;
      }
      else {
        type = "text/*";
        draw = R.drawable.mime_generic;
      }

      Intent.ShortcutIconResource icon = Intent.ShortcutIconResource.fromContext(this, draw);

      Intent i = new Intent(action);
      i.setDataAndType(Uri.fromFile(f), type);

      Intent short_i = new Intent();
      short_i.putExtra(Intent.EXTRA_SHORTCUT_INTENT, i);
      short_i.putExtra(Intent.EXTRA_SHORTCUT_NAME, f.getName());
      short_i.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);
      setResult(RESULT_OK, short_i);
      finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
      super.onCreateOptionsMenu(menu);
      getMenuInflater().inflate(R.menu.options, menu);

      MenuItem mipaste = menu.findItem(R.id.options_menu_paste);
      MenuItem midelete = menu.findItem(R.id.options_menu_delete);

      if (pYanked == null) {
        mipaste.setVisible(false);
        midelete.setVisible(false);
      }
      else {
        mipaste.setTitle("Paste "+pYanked.getName()+" here");
        midelete.setTitle("Delete "+pYanked.getName()+" forever");
      }

      return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
    {
      super.onCreateContextMenu(menu, v, menuInfo);
      AdapterContextMenuInfo info = (AdapterContextMenuInfo)menuInfo;


      if (pCreatingShortcut) {
        File f = new File(pCurDir, pCurFiles.get(info.position));
        create_shortcut(f);
        return;

      } else {

        if (pCurFiles.get(info.position).equals("..")) {
          pIgnoreNextClick = true;
          fillData(pRootFile);
          return;
        }
        getMenuInflater().inflate(R.menu.files_context, menu);

      }
    }


    @Override
    public boolean onContextItemSelected(MenuItem item)
    {
      String name = pCurFiles.get(item.getOrder());

      File f = new File(pCurDir, name);

      switch (item.getItemId()) {
        case R.id.file_context_menu_rename:
          return true;
      }
      return super.onContextItemSelected(item);

    }

}
