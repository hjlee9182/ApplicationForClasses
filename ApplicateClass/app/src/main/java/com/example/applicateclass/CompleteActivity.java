package com.example.applicateclass;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.applicateclass.ChooseSubjects.SubjectSet;
import com.example.applicateclass.CustomView.CustomSelectBtn;
import com.example.applicateclass.TimeTable.CustomScheduleItem;
import com.example.applicateclass.TimeTable.CustomTimeset;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;


public class CompleteActivity extends AppCompatActivity {
    public final String PREFERENCE = "com.example.applicateclass"; //저장, 불러오기 위한
    private long lastTimeBackPressed;
    int Write, Grade, TimeSet, RestDay;
    List<Integer> essential_subjects_credit = new ArrayList<>();
    String Days[] = new String[6];
    private Gson gson;
    private List<CustomScheduleItem> subjects = new ArrayList<CustomScheduleItem>();
    private List<CustomScheduleItem> culturesubjects = new ArrayList<CustomScheduleItem>();
    private List<SubjectSet> essentialsubjects = new ArrayList<>();
    private List<List<CustomScheduleItem>> all_choose_subjects = new ArrayList<>();
    private List<List<CustomScheduleItem>> available_choose_subjects = new ArrayList<>();
    private List<List<CustomScheduleItem>> final_choose_subjects = new ArrayList<>();
    private List<CustomScheduleItem> choose_subjects = new ArrayList<>();
    private List<List<CustomScheduleItem>> every_subjects = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete);

        /**********RestDay 공강 요일 선택(월,화,수,목,금,무관)으로 Int형으로 차례대로 1 2 3 4 5 0 으로 표현하여 정보를 저장한다*******************/
        //계속 가지고 있는 정보
        Intent intent_info = getIntent(); //데이터 수신 (학년+ 학점) //다음 액티비티에도 포함하여 저장
        //final ArrayList<String> subject = (ArrayList<String>) intent_info.getSerializableExtra("subject"); //과목명이 담긴 배열
        Write = intent_info.getExtras().getInt("Write"); //입력한 학점 받아옴
        Grade = intent_info.getExtras().getInt("Grade"); //선택한 grade1, grade2...
        TimeSet = intent_info.getExtras().getInt("TimeSet"); //선택한 시간대(오전 오후 무관 1 2 0 )

        essentialsubjects = takeChooseData();
        MakeChooseSubjects(0, choose_subjects);
        if (available_choose_subjects.size() == 0) {
            Make_AvailableList();
        }
        for (int i = 0; i < 3; i++) {
            Collections.shuffle(available_choose_subjects);
            List<CustomScheduleItem> list = new ArrayList<>();
            list.addAll(available_choose_subjects.get(0));
            final_choose_subjects.add(list);
        }
        Boolean mon_key = intent_info.getExtras().getBoolean("mon_key");
        Boolean tue_key = intent_info.getExtras().getBoolean("tue_key");
        Boolean wed_key = intent_info.getExtras().getBoolean("wed_key");
        Boolean thr_key = intent_info.getExtras().getBoolean("thr_key");
        Boolean fri_key = intent_info.getExtras().getBoolean("fri_key");
        Boolean noting_key = intent_info.getExtras().getBoolean("noting_key");

        Days[0] = String.valueOf(mon_key);
        Days[1] = String.valueOf(tue_key);
        Days[2] = String.valueOf(wed_key);
        Days[3] = String.valueOf(thr_key);
        Days[4] = String.valueOf(fri_key);
        Days[5] = String.valueOf(noting_key);

        for (int i = 0; i < 6; i++) {
            if (Days[i] == "true") {
                if (i == 0) {  //월
                    RestDay = 1;
                } else if (i == 1) { //화
                    RestDay = 2;
                } else if (i == 2) { //수
                    RestDay = 3;
                } else if (i == 3) { //목
                    RestDay = 4;
                } else if (i == 4) { //금
                    RestDay = 5;
                } else if (i == 5) { //무관
                    RestDay = 0;
                }
            }
        }
        essential_subjects_credit = count_essential_credit();
        DatabaseReference myref = FirebaseDatabase.getInstance().getReference();
        myref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.v("들어오나요?", "ㅇㅇ");
                for (DataSnapshot keys : dataSnapshot.getChildren()) {
                    if (keys.getKey().equals(String.valueOf(Grade))) {
                        showData(keys);
                    }
                    if (keys.getKey().equals(String.valueOf(0))) {
                        showCultureData(keys);
                    }
                }
                saveClasses();
                istimeavailable(subjects, 0);
                istimeavailable(culturesubjects, 1);
                make_timetable();
                SharedPreferences sf = getSharedPreferences("check", MODE_PRIVATE);// check -> empty 가 no면 데이터가 이미 존재한다는 거
                String check_subejcts = sf.getString("empty", "");
                Log.v("데이터확인", check_subejcts + "!!!!!!");
                if (check_subejcts.equals("")) {
                    SharedPreferences.Editor editor = sf.edit();
                    editor.putString("empty", "no");
                    editor.commit();
                    onSaveData();

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        /**********************
         *
         * 정보 받아오기 완료
         * Grade: 학년
         * Write: 학점
         * TimeSet: 오전/오후
         * RestDay: 공간 선택
         *
         ***********************/

        //select1 Button
        CustomSelectBtn sel1 = (CustomSelectBtn) findViewById(R.id.complete_select1);
        sel1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(
                        getApplicationContext(),
                        MainActivity.class);
                intent.putExtra("select", "1");
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });
        //select2 Button
        CustomSelectBtn sel2 = (CustomSelectBtn) findViewById(R.id.complete_select2);
        sel2.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(
                        getApplicationContext(),
                        MainActivity.class);
                intent.putExtra("select", "2");
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });

        //select3 Button
        CustomSelectBtn sel3 = (CustomSelectBtn) findViewById(R.id.complete_select3);
        sel3.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(
                        getApplicationContext(),
                        MainActivity.class);
                intent.putExtra("select", "3");
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });

    }

    private void Make_AvailableList() {
        int n = 1;
        while (true) {
            for (int i = 0; i < all_choose_subjects.size(); i++) {
                for (int j = 0; j < all_choose_subjects.get(i).size(); j++) {
                    List<CustomScheduleItem> list = new ArrayList<>();
                    list.addAll(all_choose_subjects.get(i));
                    for (int k = 0; k < n; k++) {
                        Collections.shuffle(list);
                        list.remove(0);
                    }
                    if (isnotconflict_list(list)) {
                        available_choose_subjects.add(list);
                        return;
                    }
                }
            }
            n++;
        }
    }

    private void MakeChooseSubjects(int i, List<CustomScheduleItem> lists) {
        List<CustomScheduleItem> items = essentialsubjects.get(i).getSubjectsArray();
        for (int j = 0; j < items.size(); j++) {
            lists.add(items.get(j));
            if (lists.size() == essentialsubjects.size()) {
                List<CustomScheduleItem> addlist = new ArrayList<>();
                addlist.addAll(lists);
                all_choose_subjects.add(addlist);
                if (isnotconflict_list(addlist)) {
                    available_choose_subjects.add(addlist);
                }
                lists.remove(items.get(j));

                continue;
            }

            MakeChooseSubjects(i + 1, lists);
            lists.remove(items.get(j));
        }

    }

    private void onSaveData() {

        for (int i = 0; i < 3; i++) {
            gson = new GsonBuilder().create();
            Type listType = new TypeToken<ArrayList<CustomScheduleItem>>() {
            }.getType();
            String json = gson.toJson(final_choose_subjects.get(i), listType);
            SharedPreferences sharedPreferences;
            sharedPreferences = getSharedPreferences(String.valueOf(i + 1), MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("contacts", json);
            editor.commit();
        }
    }


    public boolean getPerferenceBoolean(String key) { //데이터 불러오기(확인용)
        SharedPreferences pref = getSharedPreferences(PREFERENCE, MODE_PRIVATE);
        return pref.getBoolean(key, false);
    }

    private void showData(DataSnapshot dataSnapshot) {
        for (DataSnapshot keys : dataSnapshot.getChildren()) {
            CustomScheduleItem customScheduleItem = keys.getValue(CustomScheduleItem.class);
            subjects.add(customScheduleItem);
        }
    }

    private void showCultureData(DataSnapshot dataSnapshot) {
        for (DataSnapshot keys : dataSnapshot.getChildren()) {
            CustomScheduleItem customScheduleItem = keys.getValue(CustomScheduleItem.class);
            culturesubjects.add(customScheduleItem);
        }
    }

    @Override
    public void onBackPressed() { //화면에서 뒤로가기 방지
        //super.onBackPressed();
        if (System.currentTimeMillis() - lastTimeBackPressed < 2000) {
            Intent intent = new Intent(
                    getApplicationContext(),
                    RestDayActivity.class);
            startActivity(intent); //종료시 오류나는 것 고치기
            overridePendingTransition(0, 0);
            if (System.currentTimeMillis() - lastTimeBackPressed < 2000) {
                finish();
                return;
            }
            Toast.makeText(this, "뒤로 버튼을 한번 더 누르시면 앱이 종료됩니다.", Toast.LENGTH_SHORT).show();
            lastTimeBackPressed = System.currentTimeMillis();
        }
    }

    private void istimeavailable(List<CustomScheduleItem> subjects, int k) {

        for (int i = 0; i < 3; i++) {
            List<CustomScheduleItem> subject = new ArrayList<>();
            subject.addAll(subjects);
            Iterator<CustomScheduleItem> item = subject.iterator();
            while (item.hasNext()) {
                CustomScheduleItem scheduleItem = item.next();
                if (!checktimelist(i, scheduleItem, TimeSet, RestDay)) {
                    item.remove();
                }
            }
            if (k == 1) {
                every_subjects.get(i).addAll(subject);
            } else {
                every_subjects.add(subject);
            }
        }
    }

    private boolean checktimelist(int j, CustomScheduleItem subject, int timeSet, int restDay) {
        ArrayList<CustomTimeset> timelist = subject.getTimelist();
        for (int i = 0; i < timelist.size(); i++) {
            CustomTimeset time = timelist.get(i);
            if (timeSet == 1 && (time.getStartTime() >= 1300 || restDay == time.getDay())) {
                return false;
            } else if (timeSet == 2 && (time.getStartTime() <= 1200 || restDay == time.getDay())) {
                return false;
            } else {
                if (timeSet == 0 && restDay == time.getDay()) {
                    return false;
                }
            }
        }
        if (!isaddtosubjects(final_choose_subjects.get(j), subject)) {
            return false;
        }
        return true;

    }

    private boolean isaddtosubjects(List<CustomScheduleItem> essential_subjects, CustomScheduleItem addsubject) {

        for (int i = 0; i < essential_subjects.size(); i++) {
            CustomScheduleItem subject = essential_subjects.get(i);
            ArrayList<CustomTimeset> timesets = subject.getTimelist();
            if (subject.getSubjectnumber().equals(addsubject.getSubjectnumber())) {
                return false;
            }
            for (int j = 0; j < timesets.size(); j++) {
                CustomTimeset timeset = timesets.get(j);
                ArrayList<CustomTimeset> addsubject_list = addsubject.getTimelist();

                for (int k = 0; k < addsubject_list.size(); k++) {
                    CustomTimeset addsubject_timeset = addsubject_list.get(k);
                    if ((timeset.getDay() == addsubject_timeset.getDay()) &&
                            ((CheckConflict(timeset, addsubject_timeset) || CheckConflict(addsubject_timeset, timeset)))) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private List<Integer> count_essential_credit() {
        List<Integer> result = new ArrayList<>();

        for (int j = 0; j < 3; j++) {
            int a = 0;
            List<CustomScheduleItem> lists = final_choose_subjects.get(j);
            for (int i = 0; i < lists.size(); i++) {
                CustomScheduleItem item = lists.get(i);
                a += item.getCredit();
            }
            result.add(Write - a);
        }
        return result;
    }

    private void make_timetable() {
        for (int i = 0; i < 3; i++) {
            int write = essential_subjects_credit.get(i);
            List<CustomScheduleItem> subjects = every_subjects.get(i);
            Collections.shuffle(subjects);
            for (int j = 0; j < subjects.size(); j++) {
                if (write == 0) {
                    break;
                }
                if (write >= subjects.get(j).getCredit() && isaddtosubjects(final_choose_subjects.get(i), subjects.get(j))) {
                    final_choose_subjects.get(i).add(subjects.get(j));
                    write -= subjects.get(j).getCredit();
                }
            }
        }
    }

    private List<SubjectSet> takeChooseData() {
        Gson gson = new GsonBuilder().create();
        SharedPreferences sp = getSharedPreferences("choose", MODE_PRIVATE);
        String strContact = sp.getString("final", "");
        Type listType = new TypeToken<List<SubjectSet>>() {
        }.getType();
        List<SubjectSet> datas = gson.fromJson(strContact, listType);
        return datas;
    }

    private void saveClasses() {

        Gson gson = new Gson();
        SharedPreferences sf = getSharedPreferences("check", MODE_PRIVATE);// check -> empty 가 no면 데이터가 이미 존재한다는 거
        SharedPreferences.Editor editor = sf.edit();

        editor.putString("subject", gson.toJson(subjects));
        editor.putString("culture", gson.toJson(culturesubjects));
        Log.e("asd", gson.toJson(culturesubjects));
        editor.commit();


    }

    private boolean isnotconflict_list(List<CustomScheduleItem> essential_subjects) {
        for (int q = 0; q < essential_subjects.size(); q++) {
            CustomScheduleItem addsubject = essential_subjects.get(q);
            for (int i = 0; i < essential_subjects.size(); i++) {
                if (i == q) {
                    continue;
                }
                CustomScheduleItem subject = essential_subjects.get(i);
                ArrayList<CustomTimeset> timesets = subject.getTimelist();
                for (int j = 0; j < timesets.size(); j++) {
                    CustomTimeset timeset = timesets.get(j);
                    ArrayList<CustomTimeset> addsubject_list = addsubject.getTimelist();

                    for (int k = 0; k < addsubject_list.size(); k++) {
                        CustomTimeset addsubject_timeset = addsubject_list.get(k);
                        if ((timeset.getDay() == addsubject_timeset.getDay()) &&
                                (CheckConflict(timeset, addsubject_timeset) || CheckConflict(addsubject_timeset, timeset))) {
                            return false;
                        }

                    }
                }
            }

        }
        return true;
    }

    private boolean CheckConflict(CustomTimeset start, CustomTimeset end) {
        if ((start.getEndTime() == end.getStartTime()) || (end.getEndTime() == start.getStartTime())) {
            return false;
        }
        if (((start.getStartTime() <= end.getStartTime() && end.getStartTime() <= start.getEndTime())
                || (end.getEndTime() <= start.getEndTime() && start.getStartTime() <= end.getEndTime()))) {
            return true;
        }
        return false;
    }
}
