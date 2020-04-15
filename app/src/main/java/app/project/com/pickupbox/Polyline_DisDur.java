package app.project.com.pickupbox;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import app.project.com.pickupbox.Data.DataParser;

public class Polyline_DisDur{


    // url 보내고 받아서 파싱 하는 부분 시작
    public static String getUrl(LatLng origin, LatLng dest) // 위치 두개 받아서 길찾기 URL 형식으로 바꿈  // 키 필요   Google Direction APi 이용
    {
        String url = "";
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
//        if()
        Log.d("로그 시간 :", Long.toString(System.currentTimeMillis()));
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat a = new SimpleDateFormat("hh a, zzzz");
        Log.d("로그 날짜 :", a.format(date));

        //derection
        url = "https://maps.googleapis.com/maps/api/directions/json?" + str_origin + "&" + str_dest + "&mode=transit" + "&alternatives=true" + "&key=AIzaSyCA-UoD4WRsPs_ilJkhgcB3OQVSFZ0wXnQ";

        Log.d("로그 유알엘 :", url);
        return url;
    }


    // 길찾기 할때 패치함
    public static class fetchUrl extends AsyncTask<String, Void, String> // AsyncTsk는 일종의 쓰레드 doInBackground 에서 PostExecute로 return값 넘겨줄수 있고, Post Execute는 ui컨트롤 부분 가능 Google Direction APi 이용
    {
        protected String doInBackground(String... url) {
            String data = "";
            try {
                data = downloadUrl(url[0]); // URL 보내서 정보 받기
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;

        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            ParserTask parserTask = new ParserTask();
            parserTask.execute(result);

        }
    }// fetchUrl


    private static String downloadUrl(String strUrl) throws IOException // 만든 URL 보내서 관련 정보 받아오기
    {
        String data = "";
        InputStream iStream = null;
        HttpsURLConnection urlConnection = null;
        Log.d("Url", strUrl);
        try {
            URL url = new URL(strUrl);

            // url 만들기
            urlConnection = (HttpsURLConnection) url.openConnection();

            // 연결
            urlConnection.connect();

            // 데이터 읽기
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) // 다 읽을 때 까지 버퍼에 계속 넣기
            {
                sb.append(line);
            }

            data = sb.toString(); // 버퍼에 쌓인 내용 저장
            Log.d("downloadUrl", data.toString());
            br.close();

        } catch (Exception e) {
            Log.d("Urlfail", "urldownloadfail");
        } finally {
            Log.d("Urlend", "end");
            iStream.close();
            ;
            urlConnection.disconnect();
        }
        return data;
    }

    private static class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>>
            // 맵에 길찾기 한 루트를 Polyline을 이용해 그려주고 소요시간, 거리 가져오는 함수 DataParser클래스를 이용해 JSON파싱한 내용을 이용한다. Google Direction API 이용

    {
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {
            //루트 관련 정보 저장
            JSONObject jObject_route;
            List<List<HashMap<String, String>>> routes = null;
            //  List<List<String>> DD;

            try {
                jObject_route = new JSONObject(jsonData[0]);


                Log.d("ParserTask", jsonData[0].toString());
                DataParser parser = new DataParser();
                Log.d("ParserTask", parser.toString());

                // Starts parsing data
                routes = parser.parse(jObject_route);

                Log.d("ParserTask", "Executing routes");
                Log.d("ParserTask", routes.toString());

            } catch (Exception e) {
                Log.d("ParserTask", e.toString());
                e.printStackTrace();
            }
            return routes;
        }// doinback

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            Context context = null;
            String Dis, Dur,  DestName; //소요시간 소요거리
            String newDur= null;
            PolylineOptions lineOptions;
            ArrayList<LatLng> polypoints;
            int ccnt = 0; // 길찾기 라인 색 다르게 체크

            lineOptions = null;
            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                polypoints = new ArrayList<>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);


                Log.d("d_parsing", "path size: " + Integer.toString(path.size()));
                // Fetching all the points in i-th route

                for (int j = 0; j < path.size(); j++) { // 패스 수 많금 포문
                    HashMap<String, String> point = path.get(j);

                    if (point.containsKey("Distance") || point.containsKey("Duration")) { // 거리나 소요시간 키를 가지고 있으면
                        Dis = point.get("Distance"); // 그 거리 정보 가져온다.
                        Dur = point.get("Duration"); // 그 소요시간 정보 가져온다.

                        String hourRep, minRep;
                        if (Dur.contains("hour")) {
                            //Log.d("시간 :", "일단 시간으로 분류");
                            newDur = Dur.replace("hours", "시간");
                            Log.d("시간", newDur);
                            newDur = newDur.trim();

                            if (Dur.contains("mins")) {
                                //Log.d("시간 :", "시간+분으로 분류");
                                newDur = Dur.replace("mins", "분");
                                newDur = newDur.trim();

                                Log.d("시간", newDur);

                            }

                        } else if (Dur.contains("mins")) {
                            Log.d("시간 :", "분으로만 분류");
                            newDur = Dur.replace("mins", "분");
                            newDur = newDur.trim();

                            Log.d("시간", newDur);
                        }
                        Intent someIntent = new Intent("poly_result");
                        someIntent.putExtra("duration",newDur);
                        someIntent.putExtra("distance",Dis);


                        LocalBroadcastManager.getInstance(context.getApplicationContext()).sendBroadcast(someIntent);

                        //tvPickResult.append("목적지 까지의 거리 : " + Dis + "\n 예상 소요 시간 : " + Dur + "\n"); // 텍스트 뷰에 그 정보들 뿌려준다.


                    } else {

                        double lat = Double.parseDouble(point.get("lat"));
                        double lng = Double.parseDouble(point.get("lng"));
                        LatLng position = new LatLng(lat, lng);
                        polypoints.add(position);
                    }
                } // for

                if (ccnt == 0) {
                    //lineOptions.color(Color.rgb(33, 142, 233)); //8EC7fF
                    ccnt++;
                } else { //이후 목적지를 변경 하였다면
                    //lineOptions.color(Color.rgb(133, 142, 233));    //8EC7fF
                    ccnt = 0;
                }
                Log.d("onPostExecute", "onPostExecute lineoptions decoded");
            }
            // Drawing polyline in the Google Map for the i-th route                                     //폴리 라인 그려주기
            if (lineOptions != null) {
                /*mMap.addPolyline(lineOptions);
                polyline = mMap.addPolyline(lineOptions);*/
            } else {
                Log.d("onPostExecute", "without Polylines drawn");
            }

        }

    }// ParserTask
}
// url 보내고 받아서 파싱 하는 부분 끝