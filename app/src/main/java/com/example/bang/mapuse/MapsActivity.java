package com.example.bang.mapuse;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();

    private DatabaseReference mReference = firebaseDatabase.getReference("marks");
    private ChildEventListener mChild;

    private ListView namesv;
    private ArrayAdapter<String> adapter;
    List<Object> Array = new ArrayList<Object>();

    private String selname;
    private int flag;
    private ArrayList<String> namelist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        namelist = new ArrayList<String>();

        initDatabase();

        namesv = (ListView) findViewById(R.id.namesv);
        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1);
        namesv.setAdapter(adapter);
        namesv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View clickedView, int position, long id)
            {
                EditText editText;
                Button makemark;
                ImageView prevmark;
                ListView namesv;

                //
                namesv = (ListView)findViewById(R.id.namesv);
                namesv.setVisibility(View.INVISIBLE);

                //만약 + 라면
                if(adapter.getItem(position) == "+")
                {
                    flag = 0;

                    editText = (EditText)findViewById(R.id.Markname);
                    editText.setVisibility(View.VISIBLE);
                    editText.requestFocus();

                    prevmark = (ImageView)findViewById(R.id.prevmark);
                    prevmark.setVisibility(View.VISIBLE);

                    makemark = (Button)findViewById(R.id.markmake);
                    makemark.setVisibility(View.VISIBLE);
                    showKeyboard();
                }
                //만약 있는 걸 선택했다면
                else{
                    flag = 1;
                    selname = adapter.getItem(position);

                    prevmark = (ImageView)findViewById(R.id.prevmark);
                    prevmark.setVisibility(View.VISIBLE);

                    makemark = (Button)findViewById(R.id.markmake);
                    makemark.setVisibility(View.VISIBLE);
                    showKeyboard();
                }

            }
        });
        mReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                BitmapDrawable bitmapdraw=(BitmapDrawable)getResources().getDrawable(R.drawable.basic_mark);
                Bitmap b=bitmapdraw.getBitmap();
                Bitmap smallMarker = Bitmap.createScaledBitmap(b, 200, 200, false);

                for (DataSnapshot marks : dataSnapshot.getChildren())
                {
                    String name = marks.child("name").getValue().toString();
                    if(name != "+")
                    {
                        String pos = marks.child("pos").getValue().toString();
                        String sp1[] = pos.split("[\\(\\)]");
                        String sp2[] = sp1[1].split(",");

                        double posx = Double.parseDouble(sp2[0]);
                        double posy = Double.parseDouble(sp2[1]);
                        LatLng latLng = new LatLng(posx,posy);

                        String date = marks.child("date").getValue().toString();

                        MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions.title(name)
                                .snippet(date)
                                .icon(BitmapDescriptorFactory.fromBitmap(smallMarker))
                                .position(latLng);
                        mMap.addMarker(markerOptions);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mReference.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                adapter.clear();
                namelist.clear();
                Boolean exist;
                for (DataSnapshot messageData : dataSnapshot.getChildren())
                {
                    // child 내에 있는 데이터만큼 반복합니다.
                    String name = messageData.child("name").getValue().toString();
                    exist = false;

                    //중복검사가 들어가야 할 자리
                    for(int i = 0; i<namelist.size(); i++)
                    {
                        if (namelist.get(i).equals(name)) {
                            exist = true;
                        }
                    }
                    if(!exist) {
                        namelist.add(name);
                        Array.add(name);
                        adapter.add(name);
                    }
                }
                //추가용 한 줄
                Array.add("+");
                adapter.add("+");

                adapter.notifyDataSetChanged();
                namesv.setSelection(adapter.getCount() - 1);
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // 국캠으로 세에팅
        LatLng khukhu = new LatLng(37.2431209, 127.0782051);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(khukhu));
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(16);
        mMap.animateCamera(zoom);

    }

    public void mOnclick(View v)
    {
        BitmapDrawable bitmapdraw=(BitmapDrawable)getResources().getDrawable(R.drawable.basic_mark);
        Bitmap b=bitmapdraw.getBitmap();
        Bitmap smallMarker = Bitmap.createScaledBitmap(b, 200, 200, false);

        EditText editText;
        Button makemark;
        ImageView prevmark;
        ListView namesv;
        ListView namesv2;
        namesv2 = (ListView)findViewById(R.id.namesv2);
        namesv2.setAdapter(adapter);

        switch (v.getId())
        {
            case R.id.button :
                mMap.clear();
                DatabaseReference ref = firebaseDatabase.getReference("marks");
                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        BitmapDrawable bitmapdraw=(BitmapDrawable)getResources().getDrawable(R.drawable.basic_mark);
                        Bitmap b=bitmapdraw.getBitmap();
                        Bitmap smallMarker = Bitmap.createScaledBitmap(b, 200, 200, false);

                        for (DataSnapshot marks : dataSnapshot.getChildren())
                        {
                            String name = marks.child("name").getValue().toString();
                            if(name != "+")
                            {
                                String pos = marks.child("pos").getValue().toString();
                                String sp1[] = pos.split("[\\(\\)]");
                                String sp2[] = sp1[1].split(",");

                                double posx = Double.parseDouble(sp2[0]);
                                double posy = Double.parseDouble(sp2[1]);
                                LatLng latLng = new LatLng(posx,posy);

                                String date = marks.child("date").getValue().toString();

                                MarkerOptions markerOptions = new MarkerOptions();
                                markerOptions.title(name)
                                        .snippet(date)
                                        .icon(BitmapDescriptorFactory.fromBitmap(smallMarker))
                                        .position(latLng);
                                mMap.addMarker(markerOptions);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                namesv = (ListView)findViewById(R.id.namesv);
                namesv.setVisibility(View.VISIBLE);
                break;

            case R.id.markmake :
                hideKeyboard();
                editText = (EditText)findViewById(R.id.Markname);
                editText.setVisibility(View.INVISIBLE);
                makemark = (Button)findViewById(R.id.markmake);
                makemark.setVisibility(View.INVISIBLE);
                prevmark = (ImageView)findViewById(R.id.prevmark);
                prevmark.setVisibility(View.INVISIBLE);

                String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
                //(추가) 좌표 계속 띄워주기
                String pkey = databaseReference.child("marks").push().getKey();

                LatLng nowloc = mMap.getCameraPosition().target;
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions
                        .position(nowloc)
                        .icon(BitmapDescriptorFactory.fromBitmap(smallMarker))
                        .snippet(currentDateTimeString);
                if(flag == 0)
                {
                    markerOptions.title(editText.getText().toString());
                    databaseReference.child("marks").child(pkey).setValue(toMap(editText.getText().toString(),nowloc.toString(),currentDateTimeString));
                }
                else if(flag ==1)
                {
                    markerOptions.title(selname);
                    databaseReference.child("marks").child(pkey).setValue(toMap(selname,nowloc.toString(),currentDateTimeString));
                }
                mMap.addMarker(markerOptions);
                break;

            case R.id.delbtn:
                mMap.clear();
                databaseReference.child("marks").removeValue();
                break;

            case R.id.selbtn:
                mMap.clear();
                namesv2.setVisibility(View.VISIBLE);
                namesv2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View clickedView, int position, long id)
                    {
                        EditText editText;
                        Button makemark;
                        ImageView prevmark;
                        ListView namesv2;
                        final String selname;
                        //
                        namesv2 = (ListView)findViewById(R.id.namesv2);
                        namesv2.setVisibility(View.INVISIBLE);
                        selname = adapter.getItem(position);

                        DatabaseReference tempref = firebaseDatabase.getReference("marks");
                        tempref.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot)
                            {
                                BitmapDrawable bitmapdraw=(BitmapDrawable)getResources().getDrawable(R.drawable.basic_mark);
                                Bitmap b=bitmapdraw.getBitmap();
                                Bitmap smallMarker = Bitmap.createScaledBitmap(b, 200, 200, false);

                                ListView namesv2;
                                namesv2 = (ListView)findViewById(R.id.namesv2);

                                for (DataSnapshot messageData : dataSnapshot.getChildren())
                                {
                                    // child 내에 있는 데이터만큼 반복합니다.
                                    String name = messageData.child("name").getValue().toString();


                                    //같은가?
                                    if(selname.equals(name))
                                    {
                                        String pos = messageData.child("pos").getValue().toString();
                                        String sp1[] = pos.split("[\\(\\)]");
                                        String sp2[] = sp1[1].split(",");

                                        double posx = Double.parseDouble(sp2[0]);
                                        double posy = Double.parseDouble(sp2[1]);
                                        LatLng latLng = new LatLng(posx,posy);

                                        String date = messageData.child("date").getValue().toString();

                                        MarkerOptions markerOptions = new MarkerOptions();
                                        markerOptions.title(name)
                                                .snippet(date)
                                                .icon(BitmapDescriptorFactory.fromBitmap(smallMarker))
                                                .position(latLng);
                                        mMap.addMarker(markerOptions);
                                    }
                                }
                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                    }
                });
                break;
        }

    }
    public Map<String, Object> toMap(String name ,String pos,String date) {
        HashMap<String, Object> result = new HashMap<>();
        result.put("name",name);
        result.put("pos", pos);
        result.put("date", date);
        return result;
    }

    public void showKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        View v = getCurrentFocus();
        if (v != null)
            imm.showSoftInput(v, 0);
    }
    public void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        View v = getCurrentFocus();
        if (v != null)
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    private void initDatabase() {

        mChild = new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        databaseReference.addChildEventListener(mChild);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        databaseReference.removeEventListener(mChild);
    }
}
